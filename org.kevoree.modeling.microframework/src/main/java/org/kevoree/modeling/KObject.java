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

    long universe();

    long now();

    long uuid();

    void delete(KCallback cb);

    void select(String query, KCallback<Object[]> cb);

    void visitAttributes(KModelAttributeVisitor visitor);

    void visit(KModelVisitor visitor, KCallback cb);

    KTimeWalker timeWalker();

    KMetaClass metaClass();

    void mutate(KActionType actionType, KMetaReference metaReference, KObject param);

    void ref(KMetaReference metaReference, KCallback<KObject[]> cb);

    KTraversal traversal();

    Object get(KMetaAttribute attribute);

    Object getByName(String atributeName);

    void set(KMetaAttribute attribute, Object payload);

    void setByName(String atributeName, Object payload);

    long[] getRefValuesByName(String refName);

    String toJSON();

    boolean equals(Object other);

    void jump(long time, KCallback<KObject> callback);

    KMetaReference[] referencesWith(KObject o);

    void call(KMetaOperation operation, Object[] params, KCallback<Object> cb);

    KDataManager manager();

    long timeDephasing();

}
