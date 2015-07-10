package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.meta.impl.MetaReference;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;

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
    public void execute(KTraversalActionContext context) {
        if (context.inputObjects() == null || context.inputObjects().length == 0) {
            if(_next != null){
                _next.execute(context);
            } else {
                context.finalCallback().on(context.inputObjects());
            }
        } else {
            final AbstractKObject currentObject = (AbstractKObject) context.inputObjects()[0];
            KLongLongMap nextIds = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            for (int i = 0; i < context.inputObjects().length; i++) {
                try {
                    AbstractKObject loopObj = (AbstractKObject) context.inputObjects()[i];
                    KMemorySegment raw = currentObject._manager.segment(loopObj.universe(), loopObj.now(), loopObj.uuid(), true, loopObj.metaClass(), null);
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
            currentObject._manager.lookupAllObjects(currentObject.universe(), currentObject.now(), trimmed, new KCallback<KObject[]>() {
                @Override
                public void on(KObject[] kObjects) {
                    if (_next == null) {
                        context.finalCallback().on(kObjects);
                    } else {
                        context.setInputObjects(kObjects);
                        _next.execute(context);
                    }
                }
            });
        }
    }

}
