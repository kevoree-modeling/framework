package org.kevoree.modeling.format.json;

import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.MetaType;
import org.kevoree.modeling.meta.KPrimitiveTypes;

public class JsonRaw {

    /**
     * @native ts
     * var builder = {};
     * builder["@class"] = p_metaClass.metaName();
     * builder["@uuid"] = +uuid;
     * if(isRoot){ builder["@root"] = true; }
     * var metaElements = p_metaClass.metaElements();
     * for(var i=0;i<metaElements.length;i++){
     * var subElem;
     * if (metaElements[i] != null && metaElements[i].metaType() === org.kevoree.modeling.meta.MetaType.ATTRIBUTE) {
     * var metaAttribute = <org.kevoree.modeling.meta.KMetaAttribute>metaElements[i];
     * if(metaAttribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.CONTINUOUS){
     * subElem = raw.getDoubleArray(metaAttribute.index(),p_metaClass);
     * } else {
     * subElem = raw.getPrimitiveType(metaAttribute.index(),p_metaClass);
     * }
     * } else {
     * subElem = raw.getLongArray(metaElements[i].index(),p_metaClass);
     * }
     * if(subElem != null && subElem != undefined){ builder[metaElements[i].metaName()] = subElem; }
     * }
     * return JSON.stringify(builder);
     */
    public static String encode(KMemoryChunk raw, long uuid, KMetaClass p_metaClass, boolean isRoot) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"@class\":\"");
        builder.append(p_metaClass.metaName());
        builder.append("\",\"@uuid\":");
        builder.append(uuid);
        if (isRoot) {
            builder.append(",\"" + JsonFormat.KEY_ROOT + "\":true");
        }
        KMeta[] metaElements = p_metaClass.metaElements();
        for (int i = 0; i < metaElements.length; i++) {
            KMeta loopMeta = metaElements[i];
            if (loopMeta != null && loopMeta.metaType().equals(MetaType.ATTRIBUTE)) {
                KMetaAttribute metaAttribute = (KMetaAttribute) loopMeta;
                int metaAttId = metaAttribute.attributeType().id();
                if (metaAttId == KPrimitiveTypes.CONTINUOUS_ID) {
                    double[] inferAtt = raw.getDoubleArray(loopMeta.index(), p_metaClass);
                    if (inferAtt != null) {
                        builder.append(",\"");
                        builder.append(loopMeta.metaName());
                        builder.append("\":[");
                        for (int j = 0; j < inferAtt.length; j++) {
                            if (j != 0) {
                                builder.append(",");
                            }
                            builder.append(inferAtt[j]);
                        }
                        builder.append("]");
                    }
                } else {
                    Object payload_res = raw.getPrimitiveType(loopMeta.index(), p_metaClass);
                    if (payload_res != null) {
                        builder.append(",\"");
                        builder.append(loopMeta.metaName());
                        builder.append("\":\"");
                        if (metaAttId == KPrimitiveTypes.STRING_ID) {
                            builder.append(JsonString.encode(payload_res.toString()));
                        } else {
                            builder.append(payload_res.toString());
                        }
                        builder.append("\"");
                    }
                }
            } else if (loopMeta != null && loopMeta.metaType().equals(MetaType.REFERENCE)) {
                long[] refPayload = raw.getLongArray(loopMeta.index(), p_metaClass);
                if (refPayload != null) {
                    builder.append(",\"");
                    builder.append(loopMeta.metaName());
                    builder.append("\":[");
                    for (int j = 0; j < refPayload.length; j++) {
                        if (j != 0) {
                            builder.append(",");
                        }
                        builder.append(refPayload[j]);
                    }
                    builder.append("]");
                }
            }
        }
        builder.append("}");
        return builder.toString();
    }


}
