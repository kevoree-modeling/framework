package org.kevoree.modeling.extrapolation;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.meta.KMetaAttribute;

public interface Extrapolation {

    Object extrapolate(KObject current, KMetaAttribute attribute);

    void mutate(KObject current, KMetaAttribute attribute, Object payload);

}
