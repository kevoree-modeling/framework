package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.manager.AccessMode;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap;
import org.kevoree.modeling.memory.struct.map.KLongMapCallBack;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.meta.impl.MetaReference;
import org.kevoree.modeling.traversal.KTraversalAction;
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
    public void execute(KObject[] p_inputs) {
        if (p_inputs == null || p_inputs.length == 0) {
            _next.execute(p_inputs);
            return;
        } else {
            _alreadyPassed = new ArrayLongMap<KObject>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            _finalElements = new ArrayLongMap<KObject>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            KObject[] filtered_inputs = new KObject[p_inputs.length];
            for (int i = 0; i < p_inputs.length; i++) {
                if (_continueCondition == null || _continueCondition.filter(p_inputs[i])) {
                    filtered_inputs[i] = p_inputs[i];
                    _alreadyPassed.put(p_inputs[i].uuid(), p_inputs[i]);
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
                        _next.execute(trimmed);
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
                    KMemorySegment raw = loopObj._manager.segment(loopObj.universe(), loopObj.now(), loopObj.uuid(), AccessMode.RESOLVE, loopObj.metaClass(), null);
                    if (raw != null) {
                        if (_reference == null) {
                            KMeta[] metaElements = loopObj.metaClass().metaElements();
                            for (int j = 0; j < metaElements.length; j++) {
                                if (metaElements[j] instanceof MetaReference) {
                                    long[] resolved = raw.getRef(metaElements[j].index(), loopObj.metaClass());
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
                                long[] resolved = raw.getRef(translatedRef.index(), loopObj.metaClass());
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
        currentObject._manager.lookupAllobjects(currentObject.universe(), currentObject.now(), trimmed, new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] kObjects) {
                private_callback.on(kObjects);
            }
        });
    }

}
