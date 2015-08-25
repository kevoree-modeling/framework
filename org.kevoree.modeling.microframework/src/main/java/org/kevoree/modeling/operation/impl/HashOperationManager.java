package org.kevoree.modeling.operation.impl;

import org.kevoree.modeling.*;
import org.kevoree.modeling.memory.chunk.KIntMap;
import org.kevoree.modeling.memory.chunk.KIntMapCallBack;
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

import java.util.ArrayList;

public class HashOperationManager implements KOperationManager {

    /* TODO enhance it */
    private KIntMap<KIntMap<KOperation>> staticOperations;

    private KInternalDataManager _manager;

    public HashOperationManager(KInternalDataManager p_manager) {
        this.staticOperations = new ArrayIntMap<KIntMap<KOperation>>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        this._manager = p_manager;
    }

    @Override
    public synchronized void register(KMetaOperation operation, KOperation callback) {
        KIntMap<KOperation> clazzOperations = staticOperations.get(operation.originMetaClassIndex());
        if (clazzOperations == null) {
            clazzOperations = new ArrayIntMap<KOperation>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            staticOperations.put(operation.originMetaClassIndex(), clazzOperations);
        }
        clazzOperations.put(operation.index(), callback);
    }

    @Override
    public void invoke(KObject source, KMetaOperation operation, Object[] param, KOperationStrategy strategy, KCallback callback) {
        if (operation == null) {
            throw new RuntimeException("Operation must be defined to invoke an operation");
        }
        int[] paramTypes = operation.paramTypes();
        if (paramTypes.length != 0) {
            if (paramTypes.length != param.length) {
                throw new RuntimeException("Bad Number of arguments for method " + operation.metaName());
            }
        }
        KIntMap<KOperation> clazzOperations = staticOperations.get(operation.originMetaClassIndex());
        KOperation resolved = null;
        if (clazzOperations != null) {
            resolved = clazzOperations.get(operation.index());
        }
        if (resolved != null) {
            resolved.on(source, param, callback);
        } else {
            strategy.invoke(_manager.cdn(), operation, source, param, this, callback);
        }
    }

    @Override
    public void dispatch(KMessage message) {
        if (message.type() == Message.OPERATION_CALL_TYPE) {
            long[] sourceKey = message.keys();
            KMetaClass mc = _manager.model().metaModel().metaClassByName(message.className());
            KMetaOperation mo = mc.operation(message.operationName());
            KIntMap<KOperation> clazzOperations = staticOperations.get(mc.index());
            KOperation resolved = null;
            if (clazzOperations != null) {
                resolved = clazzOperations.get(mo.index());
            }
            if (resolved != null) {
                final KOperation finalResolved = resolved;
                _manager.lookup(sourceKey[0], sourceKey[1], sourceKey[2], new KCallback<KObject>() {
                    public void on(KObject kObject) {
                        if (kObject != null) {
                            finalResolved.on(kObject, OperationStrategies.unserializeParam(_manager.model().metaModel(), mo, message.values()), new KCallback<Object>() {
                                public void on(Object operationResult) {
                                    if (message.id() != null) {
                                        KMessage operationResultMessage = new Message();
                                        operationResultMessage.setPeer(message.peer());
                                        operationResultMessage.setID(message.id());
                                        operationResultMessage.setType(Message.OPERATION_RESULT_TYPE);
                                        operationResultMessage.setValues(new String[]{OperationStrategies.serializeReturn(mo, operationResult)});
                                        _manager.cdn().sendToPeer(message.peer(), operationResultMessage, null);
                                    }
                                }
                            });
                        } else {
                            if (message.id() != null) {
                                KMessage operationResultMessage = new Message();
                                operationResultMessage.setID(message.id());
                                operationResultMessage.setPeer(message.peer());
                                operationResultMessage.setType(Message.OPERATION_RESULT_TYPE);
                                operationResultMessage.setValues(null);
                                _manager.cdn().sendToPeer(message.peer(), operationResultMessage, null);
                            }
                        }
                    }
                });
            } else {
                if (message.id() != null) {
                    KMessage operationResultMessage = new Message();
                    operationResultMessage.setID(message.id());
                    operationResultMessage.setPeer(message.peer());
                    operationResultMessage.setType(Message.OPERATION_RESULT_TYPE);
                    operationResultMessage.setValues(null);
                    _manager.cdn().sendToPeer(message.peer(), operationResultMessage, null);
                }
            }
        }
    }

    @Override
    public String[] mappings() {
        ArrayList<String> mappings = new ArrayList<String>();
        staticOperations.each(new KIntMapCallBack<KIntMap<KOperation>>() {
            @Override
            public void on(int key, KIntMap<KOperation> value) {
                if (value != null) {
                    KMetaClass metaClass = _manager.model().metaModel().metaClass(key);
                    String metaClassName = metaClass.metaName();
                    value.each(new KIntMapCallBack<KOperation>() {
                        @Override
                        public void on(int key, KOperation value) {
                            KMetaOperation metaOperation = (KMetaOperation) metaClass.meta(key);
                            mappings.add(metaClassName);
                            mappings.add(metaOperation.metaName());
                        }
                    });
                }
            }
        });
        return mappings.toArray(new String[mappings.size()]);
    }

}
