package org.kevoree.modeling;

import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.meta.KMetaReference;
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
    void delete(KCallback cb);

    /**
     * Reflexive API
     */
    void mutate(KActionType actionType, KMetaReference metaReference, KObject param);

    void ref(KMetaReference metaReference, KCallback<KObject[]> cb);

    Object get(KMetaAttribute attribute);

    Object getByName(String atributeName);

    void set(KMetaAttribute attribute, Object payload);

    void setByName(String atributeName, Object payload);

    long[] getRefValuesByName(String refName);

    void addByName(String relationName, KObject objToAdd);

    void removeByName(String relationName, KObject objToAdd);

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

    void call(KMetaOperation operation, Object[] params, KCallback<Object> cb);

    KDataManager manager();


}
