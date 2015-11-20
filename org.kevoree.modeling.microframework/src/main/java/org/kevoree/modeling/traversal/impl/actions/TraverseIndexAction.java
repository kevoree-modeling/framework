package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;
import org.kevoree.modeling.traversal.KTraversalIndexResolver;
import org.kevoree.modeling.util.PrimitiveHelper;

public class TraverseIndexAction implements KTraversalAction {

    private KTraversalAction _next;

    private String _indexName;


    public TraverseIndexAction(String p_indexName) {
        this._indexName = p_indexName;
    }

    @Override
    public void chain(KTraversalAction p_next) {
        _next = p_next;
    }

    @Override
    public void execute(KTraversalActionContext context) {
        //TODO enhance this to general index usages


        /*
        if (PrimitiveHelper.equals(_indexName, "root")) {
            if (context.inputObjects().length > 0) {
                context.inputObjects()[0].manager().getRoot(context.inputObjects()[0].universe(), context.inputObjects()[0].now(), new KCallback<KObject>() {
                    @Override
                    public void on(KObject root) {
                        KObject[] selectedElems = new KObject[1];
                        selectedElems[0] = root;
                        if (_next == null) {
                            context.finalCallback().on(selectedElems);
                        } else {
                            context.setInputObjects(selectedElems);
                            _next.execute(context);
                        }
                    }
                });
            }
        } else {
            KTraversalIndexResolver resolver = context.indexResolver();
            if (resolver != null) {
                KObject[] resolved = resolver.resolve(this._indexName);
                if (resolved != null) {
                    if (_next == null) {
                        context.finalCallback().on(resolved);
                    } else {
                        context.setInputObjects(resolved);
                        _next.execute(context);
                    }
                } else {
                    context.setInputObjects(new KObject[0]);
                    _next.execute(context);
                }
            } else {
                if (_next == null) {
                    context.finalCallback().on(context.inputObjects());
                } else {
                    _next.execute(context);
                }
            }
        }*/
    }
}
