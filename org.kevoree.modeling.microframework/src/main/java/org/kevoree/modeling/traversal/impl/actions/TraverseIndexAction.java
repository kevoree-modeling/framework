package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KObjectIndex;
import org.kevoree.modeling.KView;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;

public class TraverseIndexAction implements KTraversalAction {

    private KTraversalAction _next;

    private String _indexName;

    private String _attributes;

    public TraverseIndexAction(String p_indexName, String p_attributes) {
        this._indexName = p_indexName;
        this._attributes = p_attributes;
    }

    @Override
    public void chain(KTraversalAction p_next) {
        _next = p_next;
    }

    @Override
    public void execute(KTraversalActionContext context) {
        KView originView = context.baseView();
        if (originView != null) {
            if (this._attributes == null && this._indexName != null) {
                originView.model().indexByName(originView.universe(), originView.now(), this._indexName, new KCallback<KObjectIndex>() {
                    @Override
                    public void on(KObjectIndex index) {
                        if (index == null) {
                            if (_next == null) {
                                context.finalCallback().on(new KObject[0]);
                            } else {
                                context.setInputObjects(new KObject[0]);
                                _next.execute(context);
                            }
                        } else {
                            originView.model().lookupAllObjects(originView.universe(), originView.now(), index.values(), new KCallback<KObject[]>() {
                                @Override
                                public void on(KObject[] selectedElems) {
                                    if (_next == null) {
                                        context.finalCallback().on(selectedElems);
                                    } else {
                                        context.setInputObjects(selectedElems);
                                        _next.execute(context);
                                    }
                                }
                            });
                        }
                    }
                });
            } else {
                originView.model().findByName(this._indexName, originView.universe(), originView.now(), this._attributes, new KCallback<KObject>() {
                    @Override
                    public void on(KObject indexedObject) {
                        KObject[] selectedElems;
                        if (indexedObject == null) {
                            selectedElems = new KObject[0];
                        } else {
                            selectedElems = new KObject[1];
                            selectedElems[0] = indexedObject;
                        }
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
            throw new RuntimeException("Not implemented yet!");
        }


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
