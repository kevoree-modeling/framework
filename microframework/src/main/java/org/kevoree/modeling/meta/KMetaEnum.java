package org.kevoree.modeling.meta;

import org.kevoree.modeling.KType;

public interface KMetaEnum extends KType, KMeta {

    KLiteral[] literals();

    KLiteral literalByName(String name);

    KLiteral literal(int index);

    KLiteral addLiteral(String name);

}
