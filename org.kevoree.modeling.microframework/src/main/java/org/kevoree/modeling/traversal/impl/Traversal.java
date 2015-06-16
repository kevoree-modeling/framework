package org.kevoree.modeling.traversal.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.traversal.KTraversal;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalFilter;
import org.kevoree.modeling.traversal.impl.actions.*;

public class Traversal implements KTraversal {

    private static final String TERMINATED_MESSAGE = "Promise is terminated by the call of done method, please create another promise";

    private KObject[] _initObjs;

    private KTraversalAction _initAction;

    private KTraversalAction _lastAction;

    private boolean _terminated = false;

    public Traversal(KObject p_root) {
        this._initObjs = new KObject[1];
        this._initObjs[0] = p_root;
    }

    private KTraversal internal_chain_action(KTraversalAction p_action) {
        if (_terminated) {
            throw new RuntimeException(TERMINATED_MESSAGE);
        }
        if (_initAction == null) {
            _initAction = p_action;
        }
        if (_lastAction != null) {
            _lastAction.chain(p_action);
        }
        _lastAction = p_action;
        return this;
    }


    @Override
    public KTraversal traverse(KMetaReference p_metaReference) {
        return internal_chain_action(new TraverseAction(p_metaReference));
    }

    @Override
    public KTraversal traverseQuery(String p_metaReferenceQuery) {
        return internal_chain_action(new TraverseQueryAction(p_metaReferenceQuery));
    }

    @Override
    public KTraversal withAttribute(KMetaAttribute p_attribute, Object p_expectedValue) {
        return internal_chain_action(new FilterAttributeAction(p_attribute, p_expectedValue));
    }

    @Override
    public KTraversal withoutAttribute(KMetaAttribute p_attribute, Object p_expectedValue) {
        return internal_chain_action(new FilterNotAttributeAction(p_attribute, p_expectedValue));
    }

    @Override
    public KTraversal attributeQuery(String p_attributeQuery) {
        return internal_chain_action(new FilterAttributeQueryAction(p_attributeQuery));
    }

    @Override
    public KTraversal filter(KTraversalFilter p_filter) {
        return internal_chain_action(new FilterAction(p_filter));
    }

    @Override
    public KTraversal collect(KMetaReference metaReference, KTraversalFilter continueCondition) {
        return internal_chain_action(new DeepCollectAction(metaReference, continueCondition));
    }

    @Override
    public void then(KCallback<KObject[]> cb) {
        //set the terminal leaf action
        internal_chain_action(new FinalAction(cb));
        _terminated = true;
        //execute the first element of the chain of actions
        _initAction.execute(_initObjs);
    }

    @Override
    public void map(KMetaAttribute attribute, KCallback<Object[]> cb) {
        //set the terminal leaf action
        internal_chain_action(new MapAction(attribute, cb));
        _terminated = true;
        //execute the first element of the chain of actions
        _initAction.execute(_initObjs);
    }


}
