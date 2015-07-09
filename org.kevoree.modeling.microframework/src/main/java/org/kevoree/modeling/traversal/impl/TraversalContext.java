package org.kevoree.modeling.traversal.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.traversal.KTraversalActionContext;
import org.kevoree.modeling.traversal.KTraversalIndexResolver;

public class TraversalContext implements KTraversalActionContext {

    private KObject[] _inputs;
    private KTraversalIndexResolver _resolver;

    public TraversalContext(KObject[] _inputs, KTraversalIndexResolver _resolver) {
        this._inputs = _inputs;
        this._resolver = _resolver;
    }

    @Override
    public KObject[] inputObjects() {
        return this._inputs;
    }

    @Override
    public void setInputObjects(KObject[] p_newSet) {
        this._inputs = p_newSet;
    }

    @Override
    public KTraversalIndexResolver indexResolver() {
        return this._resolver;
    }
}
