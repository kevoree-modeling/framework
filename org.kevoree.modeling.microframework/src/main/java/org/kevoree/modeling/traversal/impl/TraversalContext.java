package org.kevoree.modeling.traversal.impl;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KView;
import org.kevoree.modeling.traversal.KTraversalActionContext;

public class TraversalContext implements KTraversalActionContext {

    private KObject[] _inputs;
    private KView _view;
    private KCallback<Object[]> _finalCallback;

    public TraversalContext(KObject[] p_inputs, KView p_view, KCallback<Object[]> p_finalCallback) {
        this._inputs = p_inputs;
        this._view = p_view;
        this._finalCallback = p_finalCallback;
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
    public KView baseView() {
        return this._view;
    }

    @Override
    public KCallback<Object[]> finalCallback() {
        return _finalCallback;
    }
}
