package org.kevoree.modeling.abs;

import org.kevoree.modeling.*;
import org.kevoree.modeling.defer.KDefer;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.KLongLongMap;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.operation.KOperationStrategy;
import org.kevoree.modeling.traversal.query.impl.QueryEngine;
import org.kevoree.modeling.traversal.visitor.KModelAttributeVisitor;
import org.kevoree.modeling.traversal.visitor.KModelVisitor;
import org.kevoree.modeling.traversal.visitor.KVisitResult;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.chunk.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.chunk.KLongLongMapCallBack;
import org.kevoree.modeling.traversal.impl.Traversal;
import org.kevoree.modeling.traversal.KTraversal;
import org.kevoree.modeling.util.Checker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractKObject implements KObject {

    final protected long _uuid;
    final protected long _time;
    final protected long _universe;
    final protected KMetaClass _metaClass;
    final public KInternalDataManager _manager;
    final private static String OUT_OF_CACHE_MSG = "Out of cache Error";

    private final AtomicReference<long[]> _previousResolveds;
    public static final int UNIVERSE_PREVIOUS_INDEX = 0;
    public static final int TIME_PREVIOUS_INDEX = 1;

    public AbstractKObject(long p_universe, long p_time, long p_uuid, KMetaClass p_metaClass, KInternalDataManager p_manager, long p_actualUniverse, long p_actualTime) {
        this._universe = p_universe;
        this._time = p_time;
        this._uuid = p_uuid;
        this._metaClass = p_metaClass;
        this._manager = p_manager;
        this._previousResolveds = new AtomicReference<long[]>();
        long[] initResolved = new long[]{p_actualUniverse, p_actualTime};
        this._previousResolveds.set(initResolved);
    }

    public AtomicReference<long[]> previousResolved() {
        return this._previousResolveds;
    }

    @Override
    public long timeDephasing() {
        return _time - this._previousResolveds.get()[TIME_PREVIOUS_INDEX];
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
    public void detach(KCallback callback) {
        final KObject selfPointer = this;
        KObjectChunk rawPayload = _manager.preciseChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
        if (rawPayload == null) {
            if (callback != null) {
                callback.on(new Exception(OUT_OF_CACHE_MSG));
            }
        } else {
            ArrayLongLongMap collector = new ArrayLongLongMap(-1, -1, -1, null);
            KMeta[] metaElements = _metaClass.metaElements();
            for (int i = 0; i < metaElements.length; i++) {
                if (metaElements[i] != null && metaElements[i].metaType() == MetaType.REFERENCE) {
                    long[] inboundsKeys = rawPayload.getLongArray(metaElements[i].index(), _metaClass);
                    for (int j = 0; j < inboundsKeys.length; j++) {
                        collector.put(inboundsKeys[j], inboundsKeys[j]);
                    }
                    rawPayload.clearLongArray(metaElements[i].index(), _metaClass);
                }
            }
            long[] flatCollected = new long[collector.size()];
            int[] indexI = new int[1];
            indexI[0] = 0;
            collector.each(new KLongLongMapCallBack() {
                @Override
                public void on(long key, long value) {
                    flatCollected[indexI[0]] = value;
                    indexI[0]++;
                }
            });
            _manager.lookupAllObjects(_universe, _time, flatCollected, new KCallback<KObject[]>() {
                @Override
                public void on(KObject[] resolved) {
                    for (int i = 0; i < resolved.length; i++) {
                        if (resolved[i] != null) {
                            KMetaRelation[] linkedReferences = resolved[i].referencesWith(selfPointer);
                            for (int j = 0; j < linkedReferences.length; j++) {
                                ((AbstractKObject) resolved[i]).internal_remove(linkedReferences[j], selfPointer, false);
                            }
                        }
                    }
                    if (callback != null) {
                        callback.on(null);
                    }
                }
            });
        }
    }

    @Override
    public void select(String query, KCallback<Object[]> cb) {
        if (!Checker.isDefined(query)) {
            cb.on(new KObject[0]);
        } else {
            KObject[] singleRoot = new KObject[1];
            singleRoot[0] = this;
            QueryEngine.getINSTANCE().eval(query, singleRoot, cb);
        }
    }

    @Override
    public Object get(KMetaAttribute p_attribute) {
        KMetaAttribute transposed = internal_transpose_att(p_attribute);
        if (transposed == null) {
            throw new RuntimeException("Bad KMF usage, the attribute named " + p_attribute.metaName() + " is not part of " + metaClass().metaName());
        } else {
            return transposed.strategy().extrapolate(this, transposed, _manager);
        }
    }

    @Override
    public Object getByName(String attributeName) {
        KMetaAttribute transposed = _metaClass.attribute(attributeName);
        if (transposed != null) {
            return transposed.strategy().extrapolate(this, transposed, _manager);
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
            transposed.strategy().mutate(this, transposed, payload, _manager);
        }
    }

    @Override
    public void setByName(String attributeName, Object payload) {
        KMetaAttribute transposed = _metaClass.attribute(attributeName);
        if (transposed != null) {
            transposed.strategy().mutate(this, transposed, payload, _manager);
        }
    }

    @Override
    public void addByName(String relationName, KObject objToAdd) {
        KMetaRelation metaReference = _metaClass.reference(relationName);
        if (metaReference == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + relationName + " is not part of " + metaClass().metaName());
        }
        internal_add(metaReference, objToAdd, true);
    }

    @Override
    public void add(KMetaRelation p_metaReference, KObject objToAdd) {
        final KMetaRelation metaReference = internal_transpose_ref(p_metaReference);
        if (metaReference == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + p_metaReference.metaName() + " is not part of " + metaClass().metaName());
        }
        internal_add(metaReference, objToAdd, true);
    }

    private void internal_add(final KMetaRelation p_metaReference, KObject p_param, final boolean p_setOpposite) {
        if (p_param == null) {
            throw new RuntimeException("Bad KMF usage, the objToAdd param should not be null in the add method of reference named " + p_metaReference.metaName() + " part of " + metaClass().metaName());
        }
        KObjectChunk raw = _manager.preciseChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
        if (raw != null) {
            if (p_metaReference.maxBound() < 0 || (p_metaReference.maxBound() <= raw.getLongArraySize(p_metaReference.index(), _metaClass) +1)) {
                if (raw.addLongToArray(p_metaReference.index(), p_param.uuid(), _metaClass)) {
                    if (p_setOpposite) {
                        ((AbstractKObject) p_param).internal_add(p_param.metaClass().reference(p_metaReference.oppositeName()), this, false);
                    }
                }
            } else {
                throw new RuntimeException("MaxBound constraint violated on relation " + p_metaReference.metaName() + " from metaClass " + _metaClass.metaName());
            }
        }
    }

    @Override
    public void removeByName(String relationName, KObject objToAdd) {
        KMetaRelation metaReference = _metaClass.reference(relationName);
        if (metaReference == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + relationName + " is not part of " + metaClass().metaName());
        }
        internal_remove(metaReference, objToAdd, true);
    }

    @Override
    public void remove(KMetaRelation p_metaReference, KObject objToRemove) {
        final KMetaRelation metaReference = internal_transpose_ref(p_metaReference);
        if (metaReference == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + p_metaReference.metaName() + " is not part of " + metaClass().metaName());
        }
        internal_remove(metaReference, objToRemove, true);
    }

    private void internal_remove(final KMetaRelation p_metaReference, KObject objToRemove, final boolean p_setOpposite) {
        KObjectChunk payload = _manager.preciseChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
        if (payload != null) {
            if (payload.removeLongToArray(p_metaReference.index(), objToRemove.uuid(), _metaClass)) {
                if (p_setOpposite) {
                    ((AbstractKObject) objToRemove).internal_remove(objToRemove.metaClass().reference(p_metaReference.oppositeName()), this, false);
                }
            }
        }
    }

    @Override
    public void addAllByName(String p_metaRelationName, KObject[] objsToAdd) {
        final KMetaRelation metaReference = _metaClass.reference(p_metaRelationName);
        if (metaReference == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + p_metaRelationName + " is not part of " + metaClass().metaName());
        }
        for (int i = 0; i < objsToAdd.length; i++) {
            internal_add(metaReference, objsToAdd[i], true);
        }
    }

    @Override
    public void addAll(KMetaRelation p_metaRelation, KObject[] objsToAdd) {
        final KMetaRelation transposedRelation = internal_transpose_ref(p_metaRelation);
        if (transposedRelation == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + p_metaRelation.metaName() + " is not part of " + metaClass().metaName());
        }
        for (int i = 0; i < objsToAdd.length; i++) {
            internal_add(transposedRelation, objsToAdd[i], true);
        }
    }

    @Override
    public void removeAllByName(final String p_metaRelationName, final KCallback callback) {
        final KMetaRelation metaReference = _metaClass.reference(p_metaRelationName);
        if (metaReference == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + p_metaRelationName + " is not part of " + metaClass().metaName());
        }
        this.internal_removeAll(metaReference, callback);
    }

    @Override
    public void removeAll(final KMetaRelation p_metaRelation, final KCallback callback) {
        final KMetaRelation transposedRelation = internal_transpose_ref(p_metaRelation);
        if (transposedRelation == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + p_metaRelation.metaName() + " is not part of " + metaClass().metaName());
        }
        this.internal_removeAll(transposedRelation, callback);
    }

    private void internal_removeAll(final KMetaRelation p_metaRelation, final KCallback callback) {
        final KObject selfPointer = this;
        KObjectChunk currentChunk = _manager.preciseChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
        this._manager.lookupAllObjects(_universe, _time, currentChunk.getLongArray(p_metaRelation.index(), _metaClass), new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] previousObjs) {
                for (int i = 0; i < previousObjs.length; i++) {
                    if (previousObjs[i] != null) {
                        KMetaRelation oppositeRelation = previousObjs[i].metaClass().reference(p_metaRelation.oppositeName());
                        ((AbstractKObject) previousObjs[i]).internal_remove(oppositeRelation, selfPointer, false);
                    }
                }
                currentChunk.clearLongArray(p_metaRelation.index(), _metaClass);
                if (callback != null) {
                    callback.on(null);
                }
            }
        });
    }

    public int size(KMetaRelation p_metaReference) {
        KMetaRelation transposed = internal_transpose_ref(p_metaReference);
        if (transposed == null) {
            throw new RuntimeException("Bad KMF usage, the attribute named " + p_metaReference.metaName() + " is not part of " + metaClass().metaName());
        } else {
            KObjectChunk raw = _manager.closestChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
            if (raw != null) {
                Object ref = raw.getPrimitiveType(transposed.index(), _metaClass);
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
    public void getRelationByName(String p_metaRelationName, KCallback<KObject[]> cb) {
        KMetaRelation transposed = internal_transpose_ref(_metaClass.reference(p_metaRelationName));
        if (transposed == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + p_metaRelationName + " is not part of " + metaClass().metaName());
        } else {
            internal_getRelation(transposed, cb);
        }
    }

    @Override
    public void getRelation(KMetaRelation p_metaReference, KCallback<KObject[]> cb) {
        KMetaRelation transposed = internal_transpose_ref(p_metaReference);
        if (transposed == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + p_metaReference.metaName() + " is not part of " + metaClass().metaName());
        } else {
            internal_getRelation(transposed, cb);
        }
    }

    private void internal_getRelation(KMetaRelation p_transposedRelation, KCallback<KObject[]> cb) {
        KObjectChunk raw = _manager.closestChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
        if (raw == null) {
            cb.on(new KObject[0]);
        } else {
            long[] o = raw.getLongArray(p_transposedRelation.index(), _metaClass);
            if (o == null) {
                cb.on(new KObject[0]);
            } else {
                _manager.lookupAllObjects(_universe, _time, o, cb);
            }
        }
    }

    @Override
    public long[] getRelationValuesByName(String p_refName) {
        KMetaRelation transposed = internal_transpose_ref(metaClass().reference(p_refName));
        if (transposed == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + p_refName + " is not part of " + metaClass().metaName());
        } else {
            return internal_getRefValues(transposed);
        }
    }

    @Override
    public long[] getRelationValues(KMetaRelation metaRelation) {
        KMetaRelation transposed = internal_transpose_ref(metaRelation);
        if (transposed == null) {
            throw new RuntimeException("Bad KMF usage, the reference named " + metaRelation + " is not part of " + metaClass().metaName());
        } else {
            return internal_getRefValues(transposed);
        }
    }

    private long[] internal_getRefValues(KMetaRelation transposedReference) {
        KObjectChunk raw = _manager.closestChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
        if (raw == null) {
            return new long[0];
        } else {
            long[] o = raw.getLongArray(transposedReference.index(), _metaClass);
            if (o == null) {
                return new long[0];
            } else {
                return o;
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
            if (metaElements[i] != null && metaElements[i].metaType() == MetaType.ATTRIBUTE) {
                KMetaAttribute metaAttribute = (KMetaAttribute) metaElements[i];
                visitor.visit(metaAttribute, get(metaAttribute));
            }
        }
    }

    @Override
    public void visit(KModelVisitor p_visitor, KCallback cb) {
        internal_visit(p_visitor, cb, new ArrayLongLongMap(-1, -1, -1, null), new ArrayLongLongMap(-1, -1, -1, null));
    }

    private void internal_visit(final KModelVisitor visitor, final KCallback end, final KLongLongMap visited, final KLongLongMap traversed) {
        if (!Checker.isDefined(visitor)) {
            return;
        }
        if (traversed != null) {
            traversed.put(_uuid, _uuid);
        }
        final ArrayLongLongMap toResolveIds = new ArrayLongLongMap(-1, -1, -1, null);
        KMeta[] metaElements = metaClass().metaElements();
        for (int i = 0; i < metaElements.length; i++) {
            if (metaElements[i] != null && metaElements[i].metaType() == MetaType.REFERENCE) {
                final KMetaRelation reference = (KMetaRelation) metaElements[i];
                KObjectChunk raw = _manager.closestChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
                if (raw != null) {
                    long[] idArr = raw.getLongArray(reference.index(), _metaClass);
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
            _manager.lookupAllObjects(_universe, _time, trimmed, new KCallback<KObject[]>() {
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
                            if (!Checker.isDefined(result)) {
                                result = KVisitResult.STOP;
                            }
                            if (resolved != null && result.equals(KVisitResult.CONTINUE)) {
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
        StringBuilder builder = new StringBuilder();
        builder.append("{\"universe\":");
        builder.append(_universe);
        builder.append(",\"time\":");
        builder.append(_time);
        builder.append(",\"uuid\":");
        builder.append(_uuid);
        KObjectChunk raw = _manager.closestChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
        if (raw != null) {
            builder.append(",\"data\":");
            builder.append(raw.toJSON(_manager.model().metaModel()));
        }
        builder.append("}");
        return builder.toString();
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
        _manager.lookup(_universe, p_time, _uuid, p_callback);
    }

    public KMetaRelation internal_transpose_ref(KMetaRelation p) {
        if (!Checker.isDefined(p)) {
            return null;
        } else {
            return (KMetaRelation) this._metaClass.metaByName(p.metaName());
        }
    }

    public KMetaAttribute internal_transpose_att(KMetaAttribute p) {
        if (!Checker.isDefined(p)) {
            return null;
        } else {
            return (KMetaAttribute) this._metaClass.metaByName(p.metaName());
        }
    }

    public KMetaOperation internal_transpose_op(KMetaOperation p) {
        if (!Checker.isDefined(p)) {
            return null;
        } else {
            return (KMetaOperation) this._metaClass.metaByName(p.metaName());
        }
    }

    @Override
    public KTraversal traversal() {
        KObject[] singleRoot = new KObject[1];
        singleRoot[0] = this;
        return new Traversal(singleRoot);
    }

    @Override
    public KMetaRelation[] referencesWith(KObject o) {
        if (Checker.isDefined(o)) {
            KObjectChunk raw = _manager.closestChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
            if (raw != null) {
                KMeta[] metaElements = metaClass().metaElements();
                List<KMetaRelation> selected = new ArrayList<KMetaRelation>();
                for (int i = 0; i < metaElements.length; i++) {
                    if (metaElements[i] != null && metaElements[i].metaType() == MetaType.REFERENCE) {
                        long[] rawI = raw.getLongArray((metaElements[i].index()), _metaClass);
                        if (rawI != null) {
                            long oUUID = o.uuid();
                            for (int h = 0; h < rawI.length; h++) {
                                if (rawI[h] == oUUID) {
                                    selected.add((KMetaRelation) metaElements[i]);
                                    break;
                                }
                            }
                        }
                    }
                }
                return selected.toArray(new KMetaRelation[selected.size()]);
            } else {
                return new KMetaRelation[0];
            }
        } else {
            return new KMetaRelation[0];
        }
    }

    @Override
    public void invokeOperation(KMetaOperation p_operation, Object[] p_params, KOperationStrategy strategy, KCallback cb) {
        _manager.operationManager().invoke(this, p_operation, p_params, strategy, cb);
    }

    @Override
    public void invokeOperationByName(String operationName, Object[] p_params, KOperationStrategy strategy, KCallback cb) {
        KMetaOperation metaOp = _metaClass.operation(operationName);
        if (metaOp == null) {
            throw new RuntimeException("Operation not founded with name " + operationName + " in the metaClass " + _metaClass.metaName());
        }
        _manager.operationManager().invoke(this, metaOp, p_params, strategy, cb);
    }

    @Override
    public KDataManager manager() {
        return _manager;
    }

    private void internal_times(final long start, final long end, KCallback<long[]> cb) {
        _manager.resolveTimes(_universe, _uuid, start, end, cb);
    }

    @Override
    public void allTimes(KCallback<long[]> cb) {
        internal_times(KConfig.BEGINNING_OF_TIME, KConfig.END_OF_TIME, cb);
    }

    @Override
    public void timesBefore(long endOfSearch, KCallback<long[]> cb) {
        internal_times(KConfig.BEGINNING_OF_TIME, endOfSearch, cb);
    }

    @Override
    public void timesAfter(long beginningOfSearch, KCallback<long[]> cb) {
        internal_times(beginningOfSearch, KConfig.END_OF_TIME, cb);
    }

    @Override
    public void timesBetween(long beginningOfSearch, long endOfSearch, KCallback<long[]> cb) {
        internal_times(beginningOfSearch, endOfSearch, cb);
    }

}
