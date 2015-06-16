package org.kevoree.modeling;

import org.kevoree.modeling.event.KEventListener;
import org.kevoree.modeling.memory.manager.KMemoryManager;
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

    void select(String query, KCallback<KObject[]> cb);

    void listen(long groupId, KEventListener listener);

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

    String toJSON();

    boolean equals(Object other);

    void jump(long time, KCallback<KObject> callback);

    KMetaReference[] referencesWith(KObject o);

    void call(KMetaOperation operation, Object[] params, KCallback<Object> cb);

    KMemoryManager manager();

}
