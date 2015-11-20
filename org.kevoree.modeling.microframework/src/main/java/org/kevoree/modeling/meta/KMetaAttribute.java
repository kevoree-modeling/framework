package org.kevoree.modeling.meta;

import org.kevoree.modeling.extrapolation.Extrapolation;

public interface KMetaAttribute extends KMeta {

    boolean key();

    int attributeTypeId();

    Extrapolation strategy();

    double precision();

    void setExtrapolation(Extrapolation extrapolation);

    void setPrecision(double precision);

    void setKey(boolean key);

}
