package org.kevoree.modeling.abs;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KActionType;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.event.KEventListener;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.meta.impl.MetaAttribute;
import org.kevoree.modeling.meta.impl.MetaReference;
import org.kevoree.modeling.traversal.visitor.KModelAttributeVisitor;
import org.kevoree.modeling.traversal.visitor.KModelVisitor;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KTimeWalker;
import org.kevoree.modeling.traversal.visitor.KVisitResult;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment;
import org.kevoree.modeling.memory.manager.AccessMode;
import org.kevoree.modeling.format.json.JsonRaw;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.struct.map.KLongLongMapCallBack;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import org.kevoree.modeling.traversal.impl.Traversal;
import org.kevoree.modeling.traversal.KTraversal;
import org.kevoree.modeling.traversal.impl.selector.Selector;
import org.kevoree.modeling.util.Checker;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractKObject implements KObject {

    final protected long _uuid;
    final protected long _time;
    final protected long _universe;
    final private KMetaClass _metaClass;
    final public KMemoryManager _manager;
    final private static String OUT_OF_CACHE_MSG = "Out of cache Error";

    public AbstractKObject(long p_universe, long p_time, long p_uuid, KMetaClass p_metaClass, KMemoryManager p_manager) {
        this._universe = p_universe;
        this._time = p_time;
        this._uuid = p_uuid;
        this._metaClass = p_metaClass;
        this._manager = p_manager;
        this._manager.cache().monitor(this);
    }

    @Override
    public long uuid() {
        return _uuid;
    }

    @Override
    public KMetaClass metaClass() {
        return _metaClass;
    }

    @Override
    public long now() {
        return _time;
    }

    @Override
    public long universe() {
        return _universe;
    }

    @Override
    public KTimeWalker timeWalker() {
        return new AbstractTimeWalker(this);
    }

    @Override
    public void delete(KCallback cb) {
        final KObject selfPointer = this;
        KMemorySegment rawPayload = _manager.segment(_universe, _time, _uuid, AccessMode.DELETE, _metaClass, null);
        if (rawPayload == null) {
            cb.on(new Exception(OUT_OF_CACHE_MSG));
        } else {
            ArrayLongLongMap collector = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            KMeta[] metaElements = _metaClass.metaElements();
            for (int i = 0; i < metaElements.length; i++) {
                if (metaElements[i] instanceof MetaReference) {
                    long[] inboundsKeys = rawPayload.getRef(metaElements[i].index(), _metaClass);
                    for (int j = 0; j < inboundsKeys.length; j++) {
                        collector.put(inboundsKeys[j], inboundsKeys[j]);
                    }
                }
            }
            long[] flatCollected = new long[collector.size()];
            int[] indexI = new int[1];
            indexI[0] = 0;
            collector.each(new KLongLongMapCallBack() {
                @Override
                public void on(long key, long value) {
                    flatCollected[indexI[0]] = key;
                    indexI[0]++;
                }
            });
            _manager.lookupAllobjects(_universe, _time, flatCollected, new KCallback<KObject[]>() {
                @Override
                public void on(KObject[] resolved) {
                    for (int i = 0; i < resolved.length; i++) {
                        if (resolved[i] != null) {
                            //TODO optimize
                            KMetaReference[] linkedReferences = resolved[i].referencesWith(selfPointer);
                            for (int j = 0; j < linkedReferences.length; j++) {
                                ((AbstractKObject) resolved[i]).internal_mutate(KActionType.REMOVE, linkedReferences[j], selfPointer, false);
                            }
                        }
                    }
                    if (cb != null) {
                        cb.on(null);
                    }
                }
            });
        }
    }

    @Override
    public void select(String query, KCallback<KObject[]> cb) {
        if (!Checker.isDefined(query)) {
            cb.on(new KObject[0]);
        } else {
            String cleanedQuery = query;
            if (cleanedQuery.startsWith("/")) {
                cleanedQuery = cleanedQuery.substring(1);
            }
            if (query.startsWith("/")) {
                final String finalCleanedQuery = cleanedQuery;
                _manager.getRoot(_universe, _time, new KCallback<KObject>() {
                    @Override
                    public void on(KObject rootObj) {
                        if (rootObj == null) {
                            cb.on(new KObject[0]);
                        } else {
                            Selector.select(rootObj, finalCleanedQuery, cb);
                        }
                    }
                });
            } else {
                Selector.select(this, query, cb);
            }
        }
    }

    @Override
    public void listen(long groupId, KEventListener listener) {
        _manager.cdn().registerListener(groupId, this, listener);
    }

    @Override
    public Object get(KMetaAttribute p_attribute) {
        KMetaAttribute transposed = internal_transpose_att(p_attribute);
        if (transposed == null) {
            throw new RuntimeException("Bad KMF usage, the attribute named " + p_attribute.metaName() + " is not part of " + metaClass().metaName());
        } else {
            return transposed.strategy().extrapolate(this, transposed);
        }
    }

    @Override
    public Object getByName(String atributeName) {
        KMetaAttribute transposed = _metaClass.attribute(atributeName);
        if (transposed != null) {
            return transposed.strategy().extrapolate(this, transposed);
        } else {
            return null;
        }
    }

    @Override
    public void set(KMetaAttribute p_attribute, Object payload) {
        KMetaAttribute transposed = internal_transpose_att(p_attribute);
        if (transposed == null) {
            throw new RuntimeException("Bad KMF usage, the attribute named " + p_attribute.metaName() + " is not part of " + metaClass().metaName());
        } else {
            transposed.strategy().mutate(this, transposed, payload);
        }
    }

    @Override
    public void setByName(String atributeName, Object payload) {
        KMetaAttribute transposed = _metaClass.attribute(atributeName);
        if (transposed != null) {
            transposed.strategy().mutate(this, transposed, payload);
        }
    }

    @Override
    public void mutate(KActionType actionType, final KMetaReference metaReference, KObject param) {
        internal_mutate(actionType, metaReference, param, true);
    }

    public void internal_mutate(KActionType actionType, final KMetaReference metaReferenceP, KObject param, final boolean setOpposite) {
        final KMetaReference metaReference = internal_transpose_ref(metaReferenceP);
        if (metaReference == null) {
            if (metaReferenceP == null) {
                throw new RuntimeException("Bad KMF usage, the reference " + " is null in metaClass named " + metaClass().metaName());
            } else {
                throw new RuntimeException("Bad KMF usage, the reference named " + metaReferenceP.metaName() + " is not part of " + metaClass().metaName());
            }
        }
        if (actionType.equals(KActionType.ADD)) {
            if (metaReference.single()) {
                internal_mutate(KActionType.SET, metaReference, param, setOpposite);
            } else {
                KMemorySegment raw = _manager.segment(_universe, _time, _uuid, AccessMode.NEW, _metaClass, null);
                if (raw != null) {
                    if (raw.addRef(metaReference.index(), param.uuid(), _metaClass)) {
                        if (setOpposite) {
                            ((AbstractKObject) param).internal_mutate(KActionType.ADD, metaReference.opposite(), this, false);
                        }
                    }
                }
            }
        } else if (actionType.equals(KActionType.SET)) {
            if (!metaReference.single()) {
                internal_mutate(KActionType.ADD, metaReference, param, setOpposite);
            } else {
                if (param == null) {
                    internal_mutate(KActionType.REMOVE, metaReference, null, setOpposite);
                } else {
                    KMemorySegment payload = _manager.segment(_universe, _time, _uuid, AccessMode.NEW, _metaClass, null);
                    long[] previous = payload.getRef(metaReference.index(), _metaClass);
                    //override
                    long[] singleValue = new long[1];
                    singleValue[0] = param.uuid();
                    payload.set(metaReference.index(), singleValue, _metaClass);
                    if (setOpposite) {
                        if (previous != null) {
                            KObject self = this;
                            _manager.lookupAllobjects(_universe, _time, previous, new KCallback<KObject[]>() {
                                @Override
                                public void on(KObject[] kObjects) {
                                    for (int i = 0; i < kObjects.length; i++) {
                                        ((AbstractKObject) kObjects[i]).internal_mutate(KActionType.REMOVE, metaReference.opposite(), self, false);
                                    }
                                    ((AbstractKObject) param).internal_mutate(KActionType.ADD, metaReference.opposite(), self, false);
                                }
                            });
                        } else {
                            ((AbstractKObject) param).internal_mutate(KActionType.ADD, metaReference.opposite(), this, false);
                        }
                    }
                }
            }
        } else if (actionType.equals(KActionType.REMOVE)) {
            if (metaReference.single()) {
                KMemorySegment raw = _manager.segment(_universe, _time, _uuid, AccessMode.NEW, _metaClass, null);
                long[] previousKid = raw.getRef(metaReference.index(), _metaClass);
                raw.set(metaReference.index(), null, _metaClass);
                if (setOpposite) {
                    if (previousKid != null) {
                        final KObject self = this;
                        _manager.lookupAllobjects(_universe, _time, previousKid, new KCallback<KObject[]>() {
                            @Override
                            public void on(KObject[] resolvedParams) {
                                if (resolvedParams != null) {
                                    for (int dd = 0; dd < resolvedParams.length; dd++) {
                                        if (resolvedParams[dd] != null) {
                                            ((AbstractKObject) resolvedParams[dd]).internal_mutate(KActionType.REMOVE, metaReference.opposite(), self, false);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            } else {
                KMemorySegment payload = _manager.segment(_universe, _time, _uuid, AccessMode.NEW, _metaClass, null);
                if (payload != null) {
                    if (payload.removeRef(metaReference.index(), param.uuid(), _metaClass)) {
                        if (setOpposite) {
                            ((AbstractKObject) param).internal_mutate(KActionType.REMOVE, metaReference.opposite(), this, false);
                        }
                    }
                }
            }
        }
    }

    public int size(KMetaReference p_metaReference) {
        KMetaReference transposed = internal_transpose_ref(p_metaReference);
        if (transposed == null) {
            throw new RuntimeException("Bad KMF usage, the attribute named " + p_metaReference.metaName() + " is not part of " + metaClass().metaName());
        } else {
            KMemorySegment raw = _manager.segment(_universe, _time, _uuid, AccessMode.RESOLVE, _metaClass, null);
            if (raw != null) {
                Object ref = raw.get(transposed.index(), _metaClass);
                if (ref == null) {
                    return 0;
                } else {
                    try {
                        long[] castedRefArray = (long[]) ref;
                        return castedRefArray.length;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
            } else {
                return 0;
            }
        }
    }

    @Override
    public void ref(KMetaReference p_metaReference, KCallback<KObject[]> cb) {
        KMetaReference transposed = internal_transpose_ref(p_metaReference);
        if (transposed == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + p_metaReference.metaName() + " is not part of " + metaClass().metaName());
        } else {
            KMemorySegment raw = _manager.segment(_universe, _time, _uuid, AccessMode.RESOLVE, _metaClass, null);
            if (raw == null) {
                cb.on(new KObject[0]);
            } else {
                long[] o = raw.getRef(transposed.index(), _metaClass);
                if (o == null) {
                    cb.on(new KObject[0]);
                } else {
                    _manager.lookupAllobjects(_universe, _time, o, cb);
                }
            }
        }
    }

    @Override
    public void visitAttributes(KModelAttributeVisitor visitor) {
        if (!Checker.isDefined(visitor)) {
            return;
        }
        KMeta[] metaElements = metaClass().metaElements();
        for (int i = 0; i < metaElements.length; i++) {
            if (metaElements[i] instanceof MetaAttribute) {
                KMetaAttribute metaAttribute = (KMetaAttribute) metaElements[i];
                visitor.visit(metaAttribute, get(metaAttribute));
            }
        }
    }

    @Override
    public void visit(KModelVisitor p_visitor, KCallback cb) {
        internal_visit(p_visitor, cb, new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR), new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR));
    }

    private void internal_visit(final KModelVisitor visitor, final KCallback end, final KLongLongMap visited, final KLongLongMap traversed) {
        if (!Checker.isDefined(visitor)) {
            return;
        }
        if (traversed != null) {
            traversed.put(_uuid, _uuid);
        }
        final ArrayLongLongMap toResolveIds = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        KMeta[] metaElements = metaClass().metaElements();
        for (int i = 0; i < metaElements.length; i++) {
            if (metaElements[i] instanceof MetaReference) {
                final KMetaReference reference = (KMetaReference) metaElements[i];
                KMemorySegment raw = _manager.segment(_universe, _time, _uuid, AccessMode.RESOLVE, _metaClass, null);
                if (raw != null) {
                    long[] idArr = raw.getRef(reference.index(), _metaClass);
                    if (idArr != null) {
                        try {
                            for (int k = 0; k < idArr.length; k++) {
                                if (traversed == null || !traversed.contains(idArr[k])) { // this is for optimization
                                    toResolveIds.put(idArr[k], idArr[k]);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (toResolveIds.size() == 0) {
            if (Checker.isDefined(end)) {
                end.on(null);
            }
        } else {
            final long[] trimmed = new long[toResolveIds.size()];
            final int[] inserted = {0};
            toResolveIds.each(new KLongLongMapCallBack() {
                @Override
                public void on(long key, long value) {
                    trimmed[inserted[0]] = value;
                    inserted[0]++;
                }
            });
            _manager.lookupAllobjects(_universe, _time, trimmed, new KCallback<KObject[]>() {
                @Override
                public void on(KObject[] resolvedArr) {
                    final List<KObject> nextDeep = new ArrayList<KObject>();

                    for (int i = 0; i < resolvedArr.length; i++) {
                        final KObject resolved = resolvedArr[i];
                        KVisitResult result = KVisitResult.CONTINUE;
                        if (resolved != null) {
                            if (visitor != null && (visited == null || !visited.contains(resolved.uuid()))) {
                                result = visitor.visit(resolved);
                            }
                            if (visited != null) {
                                visited.put(resolved.uuid(), resolved.uuid());
                            }
                        }
                        if (result != null && result.equals(KVisitResult.STOP)) {
                            if (Checker.isDefined(end)) {
                                end.on(null);
                            }
                        } else {
                            if (result.equals(KVisitResult.CONTINUE)) {
                                if (traversed == null || !traversed.contains(resolved.uuid())) {
                                    nextDeep.add(resolved);
                                }
                            }
                        }
                    }
                    if (!nextDeep.isEmpty()) {
                        final int[] index = new int[1];
                        index[0] = 0;
                        final List<KCallback<Throwable>> next = new ArrayList<KCallback<Throwable>>();
                        next.add(new KCallback<Throwable>() {
                            @Override
                            public void on(Throwable throwable) {
                                index[0] = index[0] + 1;
                                if (index[0] == nextDeep.size()) {
                                    if (Checker.isDefined(end)) {
                                        end.on(null);
                                    }
                                } else {
                                    final AbstractKObject abstractKObject = (AbstractKObject) nextDeep.get(index[0]);
                                    abstractKObject.internal_visit(visitor, next.get(0), visited, traversed);
                                }
                            }
                        });
                        final AbstractKObject abstractKObject = (AbstractKObject) nextDeep.get(index[0]);
                        abstractKObject.internal_visit(visitor, next.get(0), visited, traversed);
                    } else {
                        if (Checker.isDefined(end)) {
                            end.on(null);
                        }
                    }
                }
            });
        }
    }

    public String toJSON() {
        KMemorySegment raw = _manager.segment(_universe, _time, _uuid, AccessMode.RESOLVE, _metaClass, null);
        if (raw != null) {
            return JsonRaw.encode(raw, _uuid, _metaClass, false);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return toJSON();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractKObject)) {
            return false;
        } else {
            AbstractKObject casted = (AbstractKObject) obj;
            return casted._uuid == _uuid && casted._time == _time && casted._universe == _universe;
        }
    }

    @Override
    public int hashCode() {
        return (int) (_universe ^ _time ^ _uuid);
    }

    @Override
    public void jump(long p_time, KCallback<KObject> p_callback) {
        HeapMemorySegment resolve_entry = (HeapMemorySegment) _manager.cache().get(_universe, p_time, _uuid);
        if (resolve_entry != null) {
            KLongTree timeTree = (KLongTree) _manager.cache().get(_universe, KConfig.NULL_LONG, _uuid);
            timeTree.inc();
            KUniverseOrderMap universeTree = (KUniverseOrderMap) _manager.cache().get(KConfig.NULL_LONG, KConfig.NULL_LONG, _uuid);
            universeTree.inc();
            resolve_entry.inc();
            p_callback.on(((AbstractKModel) _manager.model()).createProxy(_universe, p_time, _uuid, _metaClass));
        } else {
            KLongTree timeTree = (KLongTree) _manager.cache().get(_universe, KConfig.NULL_LONG, _uuid);
            if (timeTree != null) {
                final long resolvedTime = timeTree.previousOrEqual(p_time);
                if (resolvedTime != KConfig.NULL_LONG) {
                    HeapMemorySegment entry = (HeapMemorySegment) _manager.cache().get(_universe, resolvedTime, _uuid);
                    if (entry != null) {
                        KUniverseOrderMap universeTree = (KUniverseOrderMap) _manager.cache().get(KConfig.NULL_LONG, KConfig.NULL_LONG, _uuid);
                        universeTree.inc();
                        timeTree.inc();
                        entry.inc();
                        p_callback.on(((AbstractKModel) _manager.model()).createProxy(_universe, p_time, _uuid, _metaClass));
                    } else {
                        //TODO optimize
                        _manager.lookup(_universe, p_time, _uuid, p_callback);
                    }
                }
            } else {
                _manager.lookup(_universe, p_time, _uuid, p_callback);
            }
        }
    }

    public KMetaReference internal_transpose_ref(KMetaReference p) {
        if (!Checker.isDefined(p)) {
            return null;
        } else {
            return (KMetaReference) metaClass().metaByName(p.metaName());
        }
    }

    public KMetaAttribute internal_transpose_att(KMetaAttribute p) {
        if (!Checker.isDefined(p)) {
            return null;
        } else {
            return (KMetaAttribute) metaClass().metaByName(p.metaName());
        }
    }

    public KMetaOperation internal_transpose_op(KMetaOperation p) {
        if (!Checker.isDefined(p)) {
            return null;
        } else {
            return (KMetaOperation) metaClass().metaByName(p.metaName());
        }
    }

    @Override
    public KTraversal traversal() {
        return new Traversal(this);
    }

    @Override
    public KMetaReference[] referencesWith(KObject o) {
        if (Checker.isDefined(o)) {
            KMemorySegment raw = _manager.segment(_universe, _time, _uuid, AccessMode.RESOLVE, _metaClass, null);
            if (raw != null) {
                KMeta[] metaElements = metaClass().metaElements();
                List<KMetaReference> selected = new ArrayList<KMetaReference>();
                for (int i = 0; i < metaElements.length; i++) {
                    if (metaElements[i] instanceof MetaReference) {
                        long[] rawI = raw.getRef((metaElements[i].index()), _metaClass);
                        if (rawI != null) {
                            long oUUID = o.uuid();
                            for (int h = 0; h < rawI.length; h++) {
                                if (rawI[h] == oUUID) {
                                    selected.add((KMetaReference) metaElements[i]);
                                    break;
                                }
                            }
                        }
                    }
                }
                return selected.toArray(new KMetaReference[selected.size()]);
            } else {
                return new KMetaReference[0];
            }
        } else {
            return new KMetaReference[0];
        }
    }

    @Override
    public void call(KMetaOperation p_operation, Object[] p_params, KCallback<Object> cb) {
        _manager.operationManager().call(this, p_operation, p_params, cb);
    }

    @Override
    public KMemoryManager manager() {
        return _manager;
    }

}
