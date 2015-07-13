package org.kevoree.modeling.meta;

import org.kevoree.modeling.KType;

public interface KMetaEnum extends KType, KMeta {

    KMeta[] literals();

    KMeta literalByName(String name);

    KMeta literal(int index);

    KMeta addLiteral(String name);

}
