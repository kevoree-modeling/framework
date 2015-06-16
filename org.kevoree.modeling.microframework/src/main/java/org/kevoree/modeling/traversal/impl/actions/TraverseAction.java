package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.manager.AccessMode;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.meta.impl.MetaReference;
import org.kevoree.modeling.traversal.KTraversalAction;

public class TraverseAction implements KTraversalAction {

    private KTraversalAction _next;

    private KMetaReference _reference;

    public TraverseAction(KMetaReference p_reference) {
        this._reference = p_reference;
    }

    @Override
    public void chain(KTraversalAction p_next) {
        _next = p_next;
    }

    @Override
    public void execute(KObject[] p_inputs) {
        if (p_inputs == null || p_inputs.length == 0) {
            _next.execute(p_inputs);
            return;
        } else {
            final AbstractKObject currentObject = (AbstractKObject) p_inputs[0];
            KLongLongMap nextIds = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            for (int i = 0; i < p_inputs.length; i++) {
                try {
                    AbstractKObject loopObj = (AbstractKObject) p_inputs[i];
                    KMemorySegment raw = currentObject._manager.segment(loopObj.universe(), loopObj.now(), loopObj.uuid(), AccessMode.RESOLVE, loopObj.metaClass(), null);
                    if (raw != null) {
                        if (_reference == null) {
                            KMeta[] metaElements = loopObj.metaClass().metaElements();
                            for (int j = 0; j < metaElements.length; j++) {
                                if (metaElements[j] instanceof MetaReference) {
                                    KMetaReference ref = (KMetaReference) metaElements[j];
                                    long[] resolved = raw.getRef(ref.index(), currentObject.metaClass());
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
                                long[] resolved = raw.getRef(translatedRef.index(), currentObject.metaClass());
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
                    _next.execute(kObjects);
                }
            });
        }
    }

}
