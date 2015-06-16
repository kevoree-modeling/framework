package org.kevoree.modeling.cloudmodel.impl;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KActionType;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.cloudmodel.Element;
import org.kevoree.modeling.cloudmodel.Node;
import org.kevoree.modeling.cloudmodel.meta.MetaNode;

/**
 * Created by duke on 10/10/14.
 */
public class NodeImpl extends AbstractKObject implements Node {

    public NodeImpl(long p_universe, long p_time, long p_uuid, KMetaClass p_metaClass, KMemoryManager p_manager) {
        super(p_universe, p_time, p_uuid, p_metaClass, p_manager);
    }

    @Override
    public String getName() {
        return (String) this.get(MetaNode.ATT_NAME);
    }

    @Override
    public Node setName(String p_name) {
        this.set(MetaNode.ATT_NAME, p_name);
        return this;
    }

    @Override
    public String getValue() {
        return (String) this.get(MetaNode.ATT_VALUE);
    }

    @Override
    public Node setValue(String p_value) {
        this.set(MetaNode.ATT_VALUE, p_value);
        return this;
    }

    @Override
    public Node addChildren(Node p_obj) {
        this.mutate(KActionType.ADD, MetaNode.REF_CHILDREN, p_obj);
        return this;
    }

    @Override
    public Node removeChildren(Node p_obj) {
        this.mutate(KActionType.REMOVE, MetaNode.REF_CHILDREN, p_obj);
        return this;
    }

    @Override
    public void getChildren(final KCallback<Node[]> p_callback) {
        this.ref(MetaNode.REF_CHILDREN,new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] kObjects) {
                if (p_callback != null) {
                    Node[] casted = new Node[kObjects.length];
                    for (int i = 0; i < kObjects.length; i++) {
                        casted[i] = (Node) kObjects[i];
                    }
                    p_callback.on(casted);
                }
            }
        });
    }

    @Override
    public Node setElement(Element p_obj) {
        this.mutate(KActionType.SET, MetaNode.REF_ELEMENT, p_obj);
        return this;
    }

    @Override
    public void getElement(final KCallback<Element> p_callback) {
        this.ref(MetaNode.REF_ELEMENT,new KCallback<KObject[]>() {
            @Override
            public void on(KObject[] kObject) {
                if (p_callback != null && kObject.length > 0) {
                    p_callback.on((Element) kObject[0]);
                }
            }
        });
    }

    @Override
    public void trigger(String param, final KCallback<String> callback) {
        Object[] internal_params = new Object[1];
        internal_params[0] = param;
        _manager.operationManager().call(this, MetaNode.OP_TRIGGER, internal_params, new KCallback<Object>() {
            @Override
            public void on(Object o) {
                if (callback != null) {
                    callback.on((String) o);
                }
            }
        });
    }

}
