package org.kevoree.modeling.traversal.impl.selector;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.traversal.KTraversal;

import java.util.List;

public class Selector {

    public static void select(final KObject root, String query, final KCallback<KObject[]> callback) {
        if (callback == null) {
            return;
        }
        KTraversal current = null;
        List<Query> extracted = Query.buildChain(query);
        if(extracted != null){
            for (int i = 0; i < extracted.size(); i++) {
                if (current == null) {
                    current = root.traversal().traverseQuery(extracted.get(i).relationName);
                } else {
                    current = current.traverseQuery(extracted.get(i).relationName);
                }
                current = current.attributeQuery(extracted.get(i).params);
            }
        }
        if (current != null) {
            current.then(callback);
        } else {
            callback.on(new KObject[0]);
        }
    }

}
