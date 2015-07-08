package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.traversal.KTraversalAction;

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
    public void execute(KObject[] inputs) {
        //TODO enhance this to general index usages
        if (_indexName.equals("root")) {
            if (inputs.length > 0) {
                inputs[0].manager().getRoot(inputs[0].universe(), inputs[0].now(), new KCallback<KObject>() {
                    @Override
                    public void on(KObject root) {
                        KObject[] selectedElems = new KObject[1];
                        selectedElems[0] = root;
                        _next.execute(selectedElems);
                    }
                });
            }
        }
    }
}
