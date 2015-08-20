package org.kevoree.modeling.operation.impl;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.chunk.KIntMap;
import org.kevoree.modeling.memory.chunk.KLongMap;
import org.kevoree.modeling.KOperation;
import org.kevoree.modeling.KView;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.chunk.impl.ArrayIntMap;
import org.kevoree.modeling.memory.chunk.impl.ArrayLongMap;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;
import org.kevoree.modeling.message.impl.OperationCallMessage;
import org.kevoree.modeling.message.impl.OperationResultMessage;
import org.kevoree.modeling.operation.KOperationManager;

public class HashOperationManager implements KOperationManager {

    private KIntMap<KIntMap<KOperation>> staticOperations;

    private KLongMap<KIntMap<KOperation>> instanceOperations;

    private KLongMap<KCallback<Object>> remoteCallCallbacks = new ArrayLongMap<KCallback<Object>>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);

    private KDataManager _manager;

    private int _callbackId = 0;

    public HashOperationManager(KDataManager p_manager) {
        this.staticOperations = new ArrayIntMap<KIntMap<KOperation>>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        this.instanceOperations = new ArrayLongMap<KIntMap<KOperation>>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        this._manager = p_manager;
    }

    @Override
    public void registerOperation(KMetaOperation operation, KOperation callback, KObject target) {
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

    private KOperation searchOperation(Long source, int clazz, int operation) {
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
    public void call(KObject source, KMetaOperation operation, Object[] param, KCallback<Object> callback) {
        KOperation operationCore = searchOperation(source.uuid(), operation.originMetaClassIndex(), operation.index());
        if (operationCore != null) {
            operationCore.on(source, param, callback);
        } else {
            sendToRemote(source, operation, param, callback);
        }
    }

    private void sendToRemote(KObject source, KMetaOperation operation, Object[] param, KCallback<Object> callback) {
        String[] stringParams = new String[param.length];
        for (int i = 0; i < param.length; i++) {
            stringParams[i] = param[i].toString();
        }
        OperationCallMessage operationCall = new OperationCallMessage();
        operationCall.id = nextKey();
        operationCall.key = new long[]{source.universe(), source.now(), source.uuid()};
        operationCall.classIndex = source.metaClass().index();
        operationCall.opIndex = operation.index();
        operationCall.params = stringParams;
        remoteCallCallbacks.put(operationCall.id, callback);
        //_manager.cdn().send(operationCall);
    }

    public synchronized long nextKey() {
        if (_callbackId == KConfig.CALLBACK_HISTORY) {
            _callbackId = 0;
        } else {
            _callbackId++;
        }
        return _callbackId;
    }

    public void operationEventReceived(KMessage operationEvent) {
        if (operationEvent.type() == KMessageLoader.OPERATION_RESULT_TYPE) {
            OperationResultMessage operationResult = (OperationResultMessage) operationEvent;
            KCallback<Object> cb = remoteCallCallbacks.get(operationResult.id);
            if (cb != null) {
                cb.on(operationResult.value);
            }
        } else if (operationEvent.type() == KMessageLoader.OPERATION_CALL_TYPE) {
            final OperationCallMessage operationCall = (OperationCallMessage) operationEvent;
            long[] sourceKey = operationCall.key;
            final KOperation operationCore = searchOperation(sourceKey[2], operationCall.classIndex, operationCall.opIndex);
            if (operationCore != null) {
                KView view = _manager.model().universe(sourceKey[0]).time(sourceKey[1]);
                view.lookup(sourceKey[2], new KCallback<KObject>() {
                    public void on(KObject kObject) {
                        if (kObject != null) {
                            operationCore.on(kObject, operationCall.params, new KCallback<Object>() {
                                public void on(Object o) {
                                    /*
                                    OperationResultMessage operationResultMessage = new OperationResultMessage();
                                    operationResultMessage.key = operationCall.key;
                                    operationResultMessage.id = operationCall.id;
                                    operationResultMessage.value = o.toString();
                                    */
                                    //_manager.cdn().send(operationResultMessage);
                                }
                            });
                        }
                    }
                });
            }
        } else {
            System.err.println("BAD ROUTING !");
            //Wrong routing.
        }
    }

}
