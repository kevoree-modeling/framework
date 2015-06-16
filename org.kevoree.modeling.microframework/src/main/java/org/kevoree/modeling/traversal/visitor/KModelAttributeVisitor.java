package org.kevoree.modeling.traversal.visitor;

import org.kevoree.modeling.meta.KMetaAttribute;

public interface KModelAttributeVisitor {

    void visit(KMetaAttribute metaAttribute, Object value);

}