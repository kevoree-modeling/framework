package org.kevoree.modeling.traversal;

import org.kevoree.modeling.KObject;

public interface KTraversalIndexResolver {

    KObject[] resolve(String indexName);

}
