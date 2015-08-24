package org.kevoree.modeling.operation.impl;

import org.kevoree.modeling.*;
import org.kevoree.modeling.memory.chunk.KIntMap;
import org.kevoree.modeling.memory.chunk.KLongMap;
import org.kevoree.modeling.memory.chunk.impl.ArrayIntMap;
import org.kevoree.modeling.memory.chunk.impl.ArrayLongMap;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.impl.Message;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.operation.KOperationManager;
import org.kevoree.modeling.operation.KOperationStrategy;
import org.kevoree.modeling.operation.OperationStrategies;

public class HashOperationManager implements KOperationManager {

    /* TODO enhance it */
    private KIntMap<KIntMap<KOperation>> staticOperations;

    private KLongMap<KIntMap<KOperation>> instanceOperations;

    private KLongMap<KCallback<KMessage>> remoteCallCallbacks = new ArrayLongMap<KCallback<KMessage>>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);

    private KInternalDataManager _manager;

    private int _callbackId = 0;

    public HashOperationManager(KInternalDataManager p_manager) {
        this.staticOperations = new ArrayIntMap<KIntMap<KOperation>>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        this.instanceOperations = new ArrayLongMap<KIntMap<KOperation>>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        this._manager = p_manager;
    }

    @Override
    public synchronized void register(KMetaOperation operation, KOperation callback, KObject target) {
        if (target == null) {
            KIntMap<KOperation> clazzOperations = staticOperations.get(operation.originMetaClassIndex());
            if (clazzOperations == null) {
                clazzOperations = new ArrayIntMap<KOperation>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                staticOperations.put(operation.originMetaClassIndex(), clazzOperations);
            }
            clazzOperations.put(operation.index(), callback);
        } else {
            KIntMap<KOperation> objectOperations = instanceOperations.get(target.uuid());
            if (objectOperations == null) {
                objectOperations = new ArrayIntMap<KOperation>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                instanceOperations.put(target.uuid(), objectOperations);
            }
            objectOperations.put(operation.index(), callback);
        }
    }

    private KOperation searchOperation(long source, int clazz, int operation) {
        KIntMap<KOperation> objectOperations = instanceOperations.get(source);
        if (objectOperations != null) {
            return objectOperations.get(operation);
        }
        KIntMap<KOperation> clazzOperations = staticOperations.get(clazz);
        if (clazzOperations != null) {
            return clazzOperations.get(operation);
        }
        return null;
    }

    @Override
    public void invoke(KObject source, KMetaOperation operation, Object[] param, KOperationStrategy strategy, KCallback callback) {
        int[] paramTypes = operation.paramTypes();
        if (paramTypes.length != 0) {
            if (paramTypes.length != param.length) {
                throw new RuntimeException("Bad Number of arguments for method " + operation.metaName());
            }
        }
        KOperation operationCore = searchOperation(source.uuid(), operation.originMetaClassIndex(), operation.index());
        if (operationCore != null) {
            operationCore.on(source, param, callback);
        } else {
            strategy.invoke(_manager.cdn(), operation, source, param, this, callback);
        }
    }

    public synchronized int nextKey() {
        if (_callbackId == KConfig.CALLBACK_HISTORY) {
            _callbackId = 0;
        } else {
            _callbackId++;
        }
        return _callbackId;
    }

    @Override
    public void dispatch(String fromPeer, KMessage message) {
        if (message.type() == Message.OPERATION_RESULT_TYPE) {
            KCallback<KMessage> cb = remoteCallCallbacks.get(message.id());
            if (cb != null) {
                cb.on(message);
            }
        } else if (message.type() == Message.OPERATION_CALL_TYPE) {
            long[] sourceKey = message.keys();
            KMetaClass mc = _manager.model().metaModel().metaClassByName(message.className());
            KMetaOperation mo = mc.operation(message.operationName());
            final KOperation operationCore = searchOperation(sourceKey[2], mc.index(), mo.index());
            if (operationCore != null) {
                KView view = _manager.model().universe(sourceKey[0]).time(sourceKey[1]);
                view.lookup(sourceKey[2], new KCallback<KObject>() {
                    public void on(KObject kObject) {
                        if (kObject != null) {
                            operationCore.on(kObject, OperationStrategies.unserializeParam(mo, message.values()), new KCallback<Object>() {
                                public void on(Object operationResult) {
                                    KMessage operationResultMessage = new Message();
                                    operationResultMessage.setID(message.id());
                                    operationResultMessage.setType(Message.OPERATION_RESULT_TYPE);
                                    operationResultMessage.setValues(new String[]{OperationStrategies.serializeReturn(mo, operationResult)});
                                    _manager.cdn().sendToPeer(fromPeer, operationResultMessage);
                                }
                            });
                        } else {
                            KMessage operationResultMessage = new Message();
                            operationResultMessage.setID(message.id());
                            operationResultMessage.setType(Message.OPERATION_RESULT_TYPE);
                            operationResultMessage.setValues(null);
                            _manager.cdn().sendToPeer(fromPeer, operationResultMessage);
                        }
                    }
                });
            }
        } else {
            System.err.println("BAD ROUTING !");
            //Wrong routing.
        }
    }

    @Override
    public void send(String peer, KMessage message, KCallback<KMessage> callback) {
        message.setID(nextKey());
        remoteCallCallbacks.put(message.id(), callback);
        _manager.cdn().sendToPeer(peer, message);
    }

}
