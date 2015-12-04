package org.kevoree.modeling;

import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.operation.KOperationStrategy;
import org.kevoree.modeling.traversal.KTraversal;
import org.kevoree.modeling.traversal.visitor.KModelAttributeVisitor;
import org.kevoree.modeling.traversal.visitor.KModelVisitor;

public interface KObject {

    /**
     * KObject identification
     */
    long universe();

    long now();

    long uuid();

    KMetaClass metaClass();

    /**
     * Visitor, KTraversal and Jump strategies
     */
    void visitAttributes(KModelAttributeVisitor visitor);

    void visit(KModelVisitor visitor, KCallback callback);

    KTraversal traversal();

    void jump(long time, KCallback<KObject> callback);

    /**
     * Selector are untyped version of traversal
     */
    void select(String query, KCallback<Object[]> callback);

    /**
     * Detach KObject
     */
    void detach(KCallback cb);

    /**
     * Reflexive References API
     */
    void addByName(String metaRelationName, KObject objToAdd);

    void add(KMetaRelation metaRelation, KObject objToAdd);

    void removeByName(String metaRelationName, KObject objToRemove);

    void remove(KMetaRelation metaRelation, KObject objToRemove);

    void addAllByName(String metaRelationName, KObject[] objsToAdd);

    void addAll(KMetaRelation metaRelation, KObject[] objsToAdd);

    void removeAllByName(String metaRelationName, KCallback callback);

    void removeAll(KMetaRelation metaRelation, KCallback callback);

    void getRelationByName(String metaRelationName, KCallback<KObject[]> callback);

    void getRelation(KMetaRelation metaRelation, KCallback<KObject[]> callback);

    long[] getRelationValuesByName(String metaRelationName);

    long[] getRelationValues(KMetaRelation metaRelation);

    /**
     * Reflexive Attributes API
     */
    Object get(KMetaAttribute metaAttribute);

    Object getByName(String metaAttributeName);

    void set(KMetaAttribute metaAttribute, Object payload);

    void setByName(String metaAttributeName, Object payload);

    /**
     * Time related naviguation
     */
    void enforceTimepoint();

    long timeDephasing();

    void allTimes(KCallback<long[]> cb);

    void timesBefore(long endOfSearch, KCallback<long[]> cb);

    void timesAfter(long beginningOfSearch, KCallback<long[]> cb);

    void timesBetween(long beginningOfSearch, long endOfSearch, KCallback<long[]> cb);

    /**
     * Bulk KObject management
     */
    String toJSON();

    boolean equals(Object other);

    KMetaRelation[] referencesWith(KObject o);

    void invokeOperation(KMetaOperation operation, Object[] params, KOperationStrategy strategy, KCallback cb);

    void invokeOperationByName(String operationName, Object[] params, KOperationStrategy strategy, KCallback cb);

    KDataManager manager();

    KMeta[] compare(KObject target);

}
