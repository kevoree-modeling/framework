package org.kevoree.modeling.traversal.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaRelation;
import org.kevoree.modeling.traversal.KTraversalIndexResolver;
import org.kevoree.modeling.traversal.KTraversal;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalFilter;
import org.kevoree.modeling.traversal.impl.actions.*;

public class Traversal implements KTraversal {

    private static final String TERMINATED_MESSAGE = "Traversal is terminated by the call of done method, please create another promise";

    private KObject[] _initObjs;

    private KTraversalAction _initAction;

    private KTraversalAction _lastAction;

    private boolean _terminated = false;

    public Traversal(KObject[] p_roots) {
        this._initObjs = p_roots;
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
    public KTraversal traverse(KMetaRelation p_metaReference) {
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
    public KTraversal collect(KMetaRelation metaReference, KTraversalFilter continueCondition) {
        return internal_chain_action(new DeepCollectAction(metaReference, continueCondition));
    }

    @Override
    public KTraversal traverseIndex(String p_indexName) {
        return internal_chain_action(new TraverseIndexAction(p_indexName));
    }

    @Override
    public KTraversal traverseTime(long timeOffset, long steps, KTraversalFilter continueCondition) {
        throw new RuntimeException("Not Implemented Yet!");
        //return null;
    }

    @Override
    public KTraversal traverseUniverse(long universeOffset, KTraversalFilter continueCondition) {
        throw new RuntimeException("Not Implemented Yet!");
        //return null;
    }

    @Override
    public void then(KCallback<KObject[]> cb) {
        //execute the first element of the chain of actions
        if (_initObjs != null) {
            _initAction.execute(new TraversalContext(_initObjs, null, new KCallback<Object[]>() {
                @Override
                public void on(Object[] objects) {
                    cb.on((KObject[]) objects);
                }
            }));
        }
    }

    @Override
    public void eval(String p_expression, KCallback<Object[]> callback) {
        //setPrimitiveType the terminal leaf action
        internal_chain_action(new MathExpressionAction(p_expression));
        _terminated = true;
        //execute the first element of the chain of actions
        if (_initObjs != null) {
            _initAction.execute(new TraversalContext(_initObjs, null, callback));
        }
    }

    @Override
    public void map(KMetaAttribute attribute, KCallback<Object[]> cb) {
        //setPrimitiveType the terminal leaf action
        internal_chain_action(new MapAction(attribute));
        _terminated = true;
        //execute the first element of the chain of actions
        if (_initObjs != null) {
            _initAction.execute(new TraversalContext(_initObjs, null, cb));
        }
    }

    @Override
    public void exec(KObject[] origins, KTraversalIndexResolver resolver, KCallback<Object[]> callback) {
        if (this._initObjs == null) {
            _initAction.execute(new TraversalContext(origins, resolver, callback));
        }
    }


}
