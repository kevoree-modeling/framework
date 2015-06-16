package org.kevoree.modeling.meta;

import org.kevoree.modeling.KType;
import org.kevoree.modeling.extrapolation.Extrapolation;

public interface KMetaAttribute extends KMeta {

    boolean key();

    KType attributeType();

    Extrapolation strategy();

    double precision();

    void setExtrapolation(Extrapolation extrapolation);

    void setPrecision(double precision);

}
