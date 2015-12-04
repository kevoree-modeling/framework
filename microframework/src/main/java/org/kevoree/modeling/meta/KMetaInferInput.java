package org.kevoree.modeling.meta;

import org.kevoree.modeling.traversal.KTraversal;

public interface KMetaInferInput extends KMeta {

    String extractorQuery();

    KTraversal extractor();

}
