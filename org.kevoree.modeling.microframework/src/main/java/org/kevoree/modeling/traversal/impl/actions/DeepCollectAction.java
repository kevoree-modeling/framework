package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.map.KLongLongMap;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.map.impl.ArrayLongMap;
import org.kevoree.modeling.memory.map.KLongMapCallBack;
import org.kevoree.modeling.memory.map.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.map.KLongLongMapCallBack;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.meta.MetaType;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;
import org.kevoree.modeling.traversal.KTraversalFilter;

public class DeepCollectAction implements KTraversalAction {

    private KTraversalAction _next;

    private KMetaReference _reference;

    private KTraversalFilter _continueCondition;

    public DeepCollectAction(KMetaReference p_reference, KTraversalFilter p_continueCondition) {
        this._reference = p_reference;
        this._continueCondition = p_continueCondition;
    }

    @Override
    public void chain(KTraversalAction p_next) {
        _next = p_next;
    }

    private ArrayLongMap<KObject> _alreadyPassed = null;

    private ArrayLongMap<KObject> _finalElements = null;

    @Override
    public void execute(KTraversalActionContext context) {
        if (context.inputObjects() == null || context.inputObjects().length == 0) {
            if(_next != null){
                _next.execute(context);
            } else {
                context.finalCallback().on(context.inputObjects());
            }
        } else {
            _alreadyPassed = new ArrayLongMap<KObject>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            _finalElements = new ArrayLongMap<KObject>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            KObject[] filtered_inputs = new KObject[context.inputObjects().length];
            for (int i = 0; i < context.inputObjects().length; i++) {
                if (_continueCondition == null || _continueCondition.filter(context.inputObjects()[i])) {
                    filtered_inputs[i] = context.inputObjects()[i];
                    _alreadyPassed.put(context.inputObjects()[i].uuid(), context.inputObjects()[i]);
                }
            }
            final KCallback<KObject[]>[] iterationCallbacks = new KCallback[1];
            iterationCallbacks[0] = new KCallback<KObject[]>() {
                @Override
                public void on(KObject[] traversed) {
                    KObject[] filtered_inputs2 = new KObject[traversed.length];
                    int nbSize = 0;
                    for (int i = 0; i < traversed.length; i++) {
                        if ((_continueCondition == null || _continueCondition.filter(traversed[i])) && !_alreadyPassed.contains(traversed[i].uuid())) {
                            filtered_inputs2[i] = traversed[i];
                            _alreadyPassed.put(traversed[i].uuid(), traversed[i]);
                            _finalElements.put(traversed[i].uuid(), traversed[i]);
                            nbSize++;
                        }
                    }
                    if (nbSize > 0) {
                        executeStep(filtered_inputs2, iterationCallbacks[0]);
                    } else {
                        KObject[] trimmed = new KObject[_finalElements.size()];
                        final int[] nbInserted = {0};
                        _finalElements.each(new KLongMapCallBack<KObject>() {
                            @Override
                            public void on(long key, KObject value) {
                                trimmed[nbInserted[0]] = value;
                                nbInserted[0]++;
                            }
                        });
                        if (_next == null) {
                            context.finalCallback().on(trimmed);
                        } else {
                            context.setInputObjects(trimmed);
                            _next.execute(context);
                        }
                    }
                }
            };
            executeStep(filtered_inputs, iterationCallbacks[0]);
        }
    }

    private void executeStep(KObject[] p_inputStep, KCallback<KObject[]> private_callback) {
        AbstractKObject currentObject = null;
        KLongLongMap nextIds = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        for (int i = 0; i < p_inputStep.length; i++) {
            if (p_inputStep[i] != null) {
                try {
                    AbstractKObject loopObj = (AbstractKObject) p_inputStep[i];
                    currentObject = loopObj;
                    KMemoryChunk raw = loopObj._manager.segment(loopObj.universe(), loopObj.now(), loopObj.uuid(), true, loopObj.metaClass(), null);
                    if (raw != null) {
                        if (_reference == null) {
                            KMeta[] metaElements = loopObj.metaClass().metaElements();
                            for (int j = 0; j < metaElements.length; j++) {
                                if (metaElements[j] != null && metaElements[j].metaType() == MetaType.REFERENCE) {
                                    long[] resolved = raw.getLongArray(metaElements[j].index(), loopObj.metaClass());
                                    if (resolved != null) {
                                        for (int k = 0; k < resolved.length; k++) {
                                            nextIds.put(resolved[k], resolved[k]);
                                        }
                                    }
                                }
                            }
                        } else {
                            KMetaReference translatedRef = loopObj.internal_transpose_ref(_reference);
                            if (translatedRef != null) {
                                long[] resolved = raw.getLongArray(translatedRef.index(), loopObj.metaClass());
                                if (resolved != null) {
                                    for (int j = 0; j < resolved.length; j++) {
                                        nextIds.put(resolved[j], resolved[j]);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        final long[] trimmed = new long[nextIds.size()];
        final int[] inserted = {0};
        nextIds.each(new KLongLongMapCallBack() {
            @Override
            public void on(long key, long value) {
                trimmed[inserted[0]] = key;
                inserted[0]++;
            }
        });
        //call
        currentObject._manager.lookupAllObjects(currentObject.universe(), currentObject.now(), trimmed, new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] kObjects) {
                private_callback.on(kObjects);
            }
        });
    }

}
