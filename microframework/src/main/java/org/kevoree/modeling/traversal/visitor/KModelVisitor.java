package org.kevoree.modeling.traversal.visitor;

import org.kevoree.modeling.KObject;

public interface KModelVisitor {

    KVisitResult visit(KObject elem);

}