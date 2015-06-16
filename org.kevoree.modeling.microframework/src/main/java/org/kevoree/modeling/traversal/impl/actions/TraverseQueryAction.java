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

public class TraverseQueryAction implements KTraversalAction {

    private final String SEP = ",";

    private KTraversalAction _next;

    private String _referenceQuery;

    public TraverseQueryAction(String p_referenceQuery) {
        this._referenceQuery = p_referenceQuery;
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
            AbstractKObject currentFirstObject = (AbstractKObject) p_inputs[0];
            KLongLongMap nextIds = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            for (int i = 0; i < p_inputs.length; i++) {
                try {
                    AbstractKObject loopObj = (AbstractKObject) p_inputs[i];
                    KMemorySegment raw = loopObj._manager.segment(loopObj.universe(), loopObj.now(), loopObj.uuid(), AccessMode.RESOLVE, loopObj.metaClass(), null);
                    KMeta[] metaElements = loopObj.metaClass().metaElements();
                    if (raw != null) {
                        if (_referenceQuery == null) {
                            for (int j = 0; j < metaElements.length; j++) {
                                if (metaElements[j] instanceof MetaReference) {
                                    long[] resolved = raw.getRef(metaElements[j].index(), loopObj.metaClass());
                                    if (resolved != null) {
                                        for (int k = 0; k < resolved.length; k++) {
                                            Long idResolved = resolved[k];
                                            nextIds.put(idResolved, idResolved);
                                        }
                                    }
                                }
                            }
                        } else {
                            String[] queries = _referenceQuery.split(SEP);
                            for (int k = 0; k < queries.length; k++) {
                                queries[k] = queries[k].replace("*", ".*");
                            }
                            for (int h = 0; h < metaElements.length; h++) {
                                if (metaElements[h] instanceof MetaReference) {
                                    KMetaReference metaReference = (KMetaReference) metaElements[h];
                                    boolean selected = false;
                                    for (int k = 0; k < queries.length; k++) {
                                        if (queries[k] != null && queries[k].startsWith("#")) {
                                            if (metaReference.opposite().metaName().matches(queries[k].substring(1))) {
                                                selected = true;
                                                break;
                                            }
                                        } else {
                                            if (metaReference.metaName().matches("^"+queries[k]+"$")) {
                                                selected = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (selected) {
                                        long[] resolved = raw.getRef(metaElements[h].index(), loopObj.metaClass());
                                        if (resolved != null) {
                                            for (int j = 0; j < resolved.length; j++) {
                                                nextIds.put(resolved[j], resolved[j]);
                                            }
                                        }
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
            currentFirstObject._manager.lookupAllobjects(currentFirstObject.universe(), currentFirstObject.now(), trimmed, new KCallback<KObject[]>() {
                @Override
                public void on(KObject[] kObjects) {
                    _next.execute(kObjects);
                }
            });
        }
    }

}
