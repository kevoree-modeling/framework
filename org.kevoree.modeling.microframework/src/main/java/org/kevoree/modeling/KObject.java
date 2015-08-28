package org.kevoree.modeling;

import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.meta.KMetaReference;
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

    void visit(KModelVisitor visitor, KCallback cb);

    KTraversal traversal();

    void jump(long time, KCallback<KObject> callback);

    /**
     * Selector are untyped version of traversal
     */
    void select(String query, KCallback<Object[]> cb);

    /**
     * Delete KObject
     */
    void removeFromAll(KCallback cb);

    /**
     * Reflexive References API
     */
    void addByName(String relationName, KObject objToAdd, KCallback callback);

    void add(KMetaReference metaReference, KObject objToAdd, KCallback callback);

    void removeByName(String relationName, KObject objToRemove, KCallback callback);

    void remove(KMetaReference metaReference, KObject objToRemove, KCallback callback);

    void ref(KMetaReference metaReference, KCallback<KObject[]> cb);

    long[] getRefValuesByName(String refName);

    long[] getRefValues(KMetaReference metaReference);

    void setRef(KMetaReference metaReference, KObject objToset, KCallback callback);

    /**
     * Reflexive Attributes API
     */
    Object get(KMetaAttribute attribute);

    Object getByName(String atributeName);

    void set(KMetaAttribute attribute, Object payload);

    void setByName(String atributeName, Object payload);

    /**
     * Time related naviguation
     */
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

    KMetaReference[] referencesWith(KObject o);

    void invokeOperation(KMetaOperation operation, Object[] params, KOperationStrategy strategy, KCallback cb);

    void invokeOperationByName(String operationName, Object[] params, KOperationStrategy strategy, KCallback cb);

    KDataManager manager();

}
