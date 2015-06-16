package org.kevoree.modeling.cloudmodel.impl;

import org.kevoree.modeling.abs.AbstractKView;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Element;
import org.kevoree.modeling.cloudmodel.Node;
import org.kevoree.modeling.cloudmodel.meta.MetaElement;
import org.kevoree.modeling.cloudmodel.meta.MetaNode;

/**
 * Created by duke on 10/9/14.
 */
public class CloudViewImpl extends AbstractKView implements CloudView {

    public CloudViewImpl(long p_universe, long _time, KMemoryManager p_manager) {
        super(p_universe, _time, p_manager);
    }

    @Override
    public Node createNode() {
        return (Node) this.create(MetaNode.getInstance());
    }

    @Override
    public Element createElement() {
        return (Element) this.create(MetaElement.getInstance());
    }

}
