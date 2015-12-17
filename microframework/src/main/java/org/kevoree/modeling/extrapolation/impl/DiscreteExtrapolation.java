package org.kevoree.modeling.extrapolation.impl;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KObjectIndex;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.extrapolation.Extrapolation;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.meta.impl.MetaLiteral;
import org.kevoree.modeling.util.PrimitiveHelper;

public class DiscreteExtrapolation implements Extrapolation {

    private static DiscreteExtrapolation INSTANCE;

    public static Extrapolation instance() {
        if (INSTANCE == null) {
            INSTANCE = new DiscreteExtrapolation();
        }
        return INSTANCE;
    }

    @Override
    public Object extrapolate(KObject current, KMetaAttribute attribute, KInternalDataManager dataManager) {
        KObjectChunk payload = dataManager.closestChunk(current.universe(), current.now(), current.uuid(), current.metaClass(), ((AbstractKObject) current).previousResolved());
        if (payload != null) {
            if (KPrimitiveTypes.isEnum(attribute.attributeTypeId())) {
                KMetaEnum metaEnum = ((AbstractKObject) current)._manager.model().metaModel().metaTypes()[attribute.attributeTypeId()];
                return metaEnum.literal((int) payload.getPrimitiveType(attribute.index(), current.metaClass()));
            } else {
                return payload.getPrimitiveType(attribute.index(), current.metaClass());
            }
        } else {
            return null;
        }
    }

    @Override
    public void mutate(KObject current, KMetaAttribute attribute, Object payload, KInternalDataManager dataManager) {
        KObjectChunk internalPreviousPayload = dataManager.closestChunk(current.universe(), current.now(), current.uuid(), current.metaClass(), ((AbstractKObject) current).previousResolved());
        if (internalPreviousPayload != null) {
            Object toSetValue;
            if (KPrimitiveTypes.isEnum(attribute.attributeTypeId())) {
                if (payload instanceof MetaLiteral) {
                    toSetValue = ((KLiteral) payload).index();
                } else {
                    KMetaEnum metaEnum = ((AbstractKObject) current)._manager.model().metaModel().metaTypes()[attribute.attributeTypeId()];
                    KLiteral foundLiteral = metaEnum.literalByName(payload.toString());
                    if (foundLiteral != null) {
                        toSetValue = foundLiteral.index();
                    } else {
                        toSetValue = null;
                    }
                }
            } else {
                if (payload == null) {
                    toSetValue = null;
                } else {
                    toSetValue = convert(attribute, payload);
                }
            }
            Object previousValue = internalPreviousPayload.getPrimitiveType(attribute.index(), current.metaClass());
            //if both value are null then we go out
            if (previousValue == null && toSetValue == null) {
                return;
            }
            //if both are null then check potential equality
            if (previousValue != null && toSetValue != null) {
                switch (attribute.attributeTypeId()) {
                    case KPrimitiveTypes.BOOL_ID:
                        boolean previousBoolOrdinal = (boolean) previousValue;
                        boolean nextBoolOrdinal = (boolean) toSetValue;
                        if (previousBoolOrdinal == nextBoolOrdinal) {
                            return;
                        }
                        break;
                    case KPrimitiveTypes.CONTINUOUS_ID:
                        double previousContinuousOrdinal = (double) previousValue;
                        double nextContinuousOrdinal = (double) toSetValue;
                        if (previousContinuousOrdinal == nextContinuousOrdinal) {
                            return;
                        }
                        break;
                    case KPrimitiveTypes.DOUBLE_ID:
                        double previousDoubleOrdinal = (double) previousValue;
                        double nextDoubleOrdinal = (double) toSetValue;
                        if (previousDoubleOrdinal == nextDoubleOrdinal) {
                            return;
                        }
                        break;
                    case KPrimitiveTypes.INT_ID:
                        int previousIntOrdinal = (int) previousValue;
                        int nextIntOrdinal = (int) toSetValue;
                        if (previousIntOrdinal == nextIntOrdinal) {
                            return;
                        }
                        break;
                    case KPrimitiveTypes.LONG_ID:
                        long previousLongOrdinal = (long) previousValue;
                        long nextLongOrdinal = (long) toSetValue;
                        if (previousLongOrdinal == nextLongOrdinal) {
                            return;
                        }
                        break;
                    case KPrimitiveTypes.STRING_ID:
                        String previousString = (String) previousValue;
                        String nextString = (String) toSetValue;
                        if (PrimitiveHelper.equals(previousString, nextString)) {
                            return;
                        }
                        break;
                    default:
                        if (KPrimitiveTypes.isEnum(attribute.attributeTypeId())) {
                            int previousEnumOrdinal = (int) previousValue;
                            int nextEnumOrdinal = (int) toSetValue;
                            if (previousEnumOrdinal == nextEnumOrdinal) {
                                return;
                            }
                        }
                        break;
                }
            }
            //no equality detected, let's insert the value
            String previousHash = null;
            if (attribute.key()) {
                //the attribute if part of the key, let's compute the previous hash
                KMeta[] metas = current.metaClass().metaElements();
                for (int i = 0; i < metas.length; i++) {
                    if (metas[i].metaType().equals(MetaType.ATTRIBUTE) && ((KMetaAttribute) metas[i]).key()) {
                        Object loopElem = internalPreviousPayload.getPrimitiveType(metas[i].index(), current.metaClass());
                        if (loopElem != null) {
                            if (previousHash == null) {
                                previousHash = loopElem.toString();
                            } else {
                                previousHash += loopElem.toString();
                            }
                        }
                    }
                }
            }
            //By requiring a raw on the current object, we automatically create and copy the previous object
            KObjectChunk internalPayload = dataManager.preciseChunk(current.universe(), current.now(), current.uuid(), current.metaClass(), ((AbstractKObject) current).previousResolved());
            //The object is also automatically cset to Dirty
            if (internalPayload != null) {
                internalPayload.setPrimitiveType(attribute.index(), toSetValue, current.metaClass());
                String newHash = null;
                if (attribute.key()) {
                    KMeta[] metas = current.metaClass().metaElements();
                    for (int i = 0; i < metas.length; i++) {
                        if (metas[i].metaType().equals(MetaType.ATTRIBUTE) && ((KMetaAttribute) metas[i]).key()) {
                            Object loopElem = internalPayload.getPrimitiveType(metas[i].index(), current.metaClass());
                            if (loopElem != null) {
                                if (newHash == null) {
                                    newHash = loopElem.toString();
                                } else {
                                    newHash += loopElem.toString();
                                }
                            }
                        }
                    }
                    //update index
                    final String finalPreviousHash = previousHash;
                    final String finalNewHash = newHash;
                    dataManager.index(current.universe(), current.now(), current.metaClass().metaName(), true, new KCallback<KObjectIndex>() {
                        @Override
                        public void on(KObjectIndex classIndex) {
                            if (finalPreviousHash != null) {
                                classIndex.setIndex(finalPreviousHash, KConfig.NULL_LONG);
                            }
                            if (finalNewHash != null) {
                                classIndex.setIndex(finalNewHash, current.uuid());
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * @native ts
     * return payload;
     */
    private final Object convert(KMetaAttribute attribute, Object payload) {
        int attTypeId = attribute.attributeTypeId();
        switch (attTypeId) {
            case KPrimitiveTypes.INT_ID:
                if (payload instanceof Integer) {
                    return payload;
                } else {
                    try {
                        return PrimitiveHelper.parseInt(payload.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            case KPrimitiveTypes.DOUBLE_ID:
                if (payload instanceof Double) {
                    return payload;
                } else {
                    try {
                        return PrimitiveHelper.parseDouble(payload.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            case KPrimitiveTypes.LONG_ID:
                if (payload instanceof Long) {
                    return payload;
                } else {
                    try {
                        return PrimitiveHelper.parseLong(payload.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            case KPrimitiveTypes.STRING_ID:
                if (payload instanceof String) {
                    return payload;
                } else {
                    try {
                        return payload.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            case KPrimitiveTypes.BOOL_ID:
                if (payload instanceof Boolean) {
                    return payload;
                } else {
                    try {
                        return PrimitiveHelper.parseBoolean(payload.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            default:
                return payload;
        }
    }

}
