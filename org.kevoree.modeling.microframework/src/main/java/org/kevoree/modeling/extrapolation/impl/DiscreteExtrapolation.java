package org.kevoree.modeling.extrapolation.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.extrapolation.Extrapolation;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.meta.KLiteral;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaEnum;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaLiteral;

public class DiscreteExtrapolation implements Extrapolation {

    private static DiscreteExtrapolation INSTANCE;

    public static Extrapolation instance() {
        if (INSTANCE == null) {
            INSTANCE = new DiscreteExtrapolation();
        }
        return INSTANCE;
    }

    @Override
    public Object extrapolate(KObject current, KMetaAttribute attribute) {
        KMemorySegment payload = ((AbstractKObject) current)._manager.segment(current.universe(), current.now(), current.uuid(), true, current.metaClass(), null);
        if (payload != null) {
            if (attribute.attributeType().isEnum()) {
                return ((KMetaEnum) attribute.attributeType()).literal((int) payload.get(attribute.index(), current.metaClass()));
            } else {
                return payload.get(attribute.index(), current.metaClass());
            }
        } else {
            return null;
        }
    }

    @Override
    public void mutate(KObject current, KMetaAttribute attribute, Object payload) {
        //By requiring a raw on the current object, we automatically create and copy the previous object
        KMemorySegment internalPayload = ((AbstractKObject) current)._manager.segment(current.universe(), current.now(), current.uuid(), false, current.metaClass(), null);
        //The object is also automatically cset to Dirty
        if (internalPayload != null) {
            if (attribute.attributeType().isEnum()) {
                if (payload instanceof MetaLiteral) {
                    internalPayload.set(attribute.index(), ((KLiteral) payload).index(), current.metaClass());
                } else {
                    KMetaEnum metaEnum = (KMetaEnum) attribute.attributeType();
                    KLiteral foundLiteral = metaEnum.literalByName(payload.toString());
                    if (foundLiteral != null) {
                        internalPayload.set(attribute.index(), foundLiteral.index(), current.metaClass());
                    }
                }
            } else {
                if (payload == null) {
                    internalPayload.set(attribute.index(), null, current.metaClass());
                } else {
                    internalPayload.set(attribute.index(), convert(attribute, payload), current.metaClass());
                }
            }
        }
    }

    /**
     * @native ts
     * return payload;
     */
    private final Object convert(KMetaAttribute attribute, Object payload) {
        if (attribute.attributeType() == KPrimitiveTypes.INT) {
            if (payload instanceof Integer) {
                return payload;
            } else {
                try {
                    return Integer.parseInt(payload.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (attribute.attributeType() == KPrimitiveTypes.FLOAT) {
            if (payload instanceof Float) {
                return payload;
            } else {
                try {
                    return Float.parseFloat(payload.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (attribute.attributeType() == KPrimitiveTypes.DOUBLE) {
            if (payload instanceof Double) {
                return payload;
            } else {
                try {
                    return Double.parseDouble(payload.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (attribute.attributeType() == KPrimitiveTypes.LONG) {
            if (payload instanceof Long) {
                return payload;
            } else {
                try {
                    return Long.parseLong(payload.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (attribute.attributeType() == KPrimitiveTypes.SHORT) {
            if (payload instanceof Short) {
                return payload;
            } else {
                try {
                    return Short.parseShort(payload.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (attribute.attributeType() == KPrimitiveTypes.STRING) {
            if (payload instanceof String) {
                return payload;
            } else {
                try {
                    return payload.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (attribute.attributeType() == KPrimitiveTypes.BOOL) {
            if (payload instanceof Boolean) {
                return payload;
            } else {
                try {
                    return Boolean.parseBoolean(payload.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return payload;
    }

}
