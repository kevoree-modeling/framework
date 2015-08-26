package org.kevoree.modeling.operation;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.impl.Message;
import org.kevoree.modeling.meta.KLiteral;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.util.PrimitiveHelper;
import org.kevoree.modeling.util.Base64;

import java.util.ArrayList;
import java.util.List;

public class OperationStrategies {

    public static String serialize(int type, Object elem, boolean isArray) {
        if (isArray) {
            Object[] elements = (Object[]) elem;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < elements.length; i++) {
                if (i != 0) {
                    builder.append(KConfig.KEY_SEP);
                }
                builder.append(serialize(type, elements[i], false));
            }
            return builder.toString();
        } else {
            switch (type) {
                case KPrimitiveTypes.BOOL_ID:
                    if ((Boolean) elem) {
                        return "1";
                    } else {
                        return "0";
                    }
                case KPrimitiveTypes.STRING_ID:
                    return JsonString.encode(elem.toString());
                case KPrimitiveTypes.DOUBLE_ID:
                    return Base64.encodeDouble((Double) elem);
                case KPrimitiveTypes.INT_ID:
                    return Base64.encodeInt((Integer) elem);
                case KPrimitiveTypes.LONG_ID:
                    return Base64.encodeLong((Integer) elem);
                default:
                    return Base64.encodeInt(((KLiteral) elem).index());
            }
        }
    }

    public static String[] serializeParam(KMetaOperation metaOperation, Object[] param) {
        int[] paramTypes = metaOperation.paramTypes();
        boolean[] paramIsArray = metaOperation.paramMultiplicities();
        String[] stringParams = new String[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            stringParams[i] = serialize(paramTypes[i], param[i], paramIsArray[i]);
        }
        return stringParams;
    }

    public static String serializeReturn(KMetaOperation metaOperation, Object result) {
        return serialize(metaOperation.returnType(), result, metaOperation.returnTypeIsArray());
    }

    public static Object unserialize(KMetaModel metaModel, int type, String payload, boolean isArray) {
        if (isArray) {
            List<String> params = new ArrayList<String>();
            int i = 0;
            int previous = 0;
            while (i < payload.length()) {
                if (payload.charAt(i) == KConfig.KEY_SEP) {
                    if (i != previous) {
                        params.add(payload.substring(previous, i));
                    }
                    previous = i+1;
                }
                i++;
            }
            if (i != previous) {
                params.add(payload.substring(previous, i));
            }
            Object[] result;
            switch (type) {
                case KPrimitiveTypes.BOOL_ID:
                    result = new Boolean[params.size()];
                    break;
                case KPrimitiveTypes.STRING_ID:
                    result = new String[params.size()];
                    break;
                case KPrimitiveTypes.DOUBLE_ID:
                    result = new Double[params.size()];
                    break;
                case KPrimitiveTypes.INT_ID:
                    result = new Integer[params.size()];
                    break;
                case KPrimitiveTypes.LONG_ID:
                    result = new Long[params.size()];
                    break;
                default:
                    result = new KLiteral[params.size()];
            }
            for (int j = 0; j < params.size(); j++) {
                result[j] = unserialize(metaModel, type, params.get(j), false);
            }
            return result;
        } else {
            switch (type) {
                case KPrimitiveTypes.BOOL_ID:
                    return PrimitiveHelper.equals(payload, "1");
                case KPrimitiveTypes.STRING_ID:
                    return JsonString.unescape(payload);
                case KPrimitiveTypes.DOUBLE_ID:
                    return Base64.decodeToDouble(payload);
                case KPrimitiveTypes.INT_ID:
                    return Base64.decodeToInt(payload);
                case KPrimitiveTypes.LONG_ID:
                    return Base64.decodeToLong(payload);
                default:
                    int literalIndex = Base64.decodeToInt(payload);
                    return metaModel.metaTypes()[type].literal(literalIndex);
            }
        }
    }

    public static Object unserializeReturn(KMetaModel metaModel, KMetaOperation metaOperation, String resultString) {
        return unserialize(metaModel, metaOperation.returnType(), resultString, metaOperation.returnTypeIsArray());
    }

    public static Object[] unserializeParam(KMetaModel metaModel, KMetaOperation metaOperation, String[] param) {
        int[] paramTypes = metaOperation.paramTypes();
        boolean[] paramMultiplicities = metaOperation.paramMultiplicities();
        Object[] objParam = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            objParam[i] = unserialize(metaModel, paramTypes[i], param[i], paramMultiplicities[i]);
        }
        return objParam;
    }


    public static KOperationStrategy ONLY_ONE = new KOperationStrategy() {
        @Override
        public void invoke(final KContentDeliveryDriver cdn, final KMetaOperation metaOperation, final KObject source, final Object[] param, final KOperationManager manager, final KCallback callback) {
            //Prepare the message
            KMessage operationCall = new Message();
            operationCall.setType(Message.OPERATION_CALL_TYPE);
            operationCall.setKeys(new long[]{source.universe(), source.now(), source.uuid()});
            operationCall.setClassName(source.metaClass().metaName());
            operationCall.setOperationName(metaOperation.metaName());
            operationCall.setValues(serializeParam(metaOperation, param));
            cdn.sendToPeer(null, operationCall, new KCallback<KMessage>() {
                @Override
                public void on(KMessage message) {
                    if (message.values() != null) {
                        callback.on(unserializeReturn(source.manager().model().metaModel(), metaOperation, message.values()[0]));
                    } else {
                        callback.on(unserializeReturn(source.manager().model().metaModel(), metaOperation, null));
                    }
                }
            });
        }
    };

    public static KOperationStrategy NAMED_PEER(String peerName) {
        return new KOperationStrategy() {
            @Override
            public void invoke(final KContentDeliveryDriver cdn, final KMetaOperation metaOperation, final KObject source, final Object[] param, final KOperationManager manager, final KCallback callback) {
                //Prepare the message
                KMessage operationCall = new Message();
                operationCall.setType(Message.OPERATION_CALL_TYPE);
                operationCall.setKeys(new long[]{source.universe(), source.now(), source.uuid()});
                operationCall.setClassName(source.metaClass().metaName());
                operationCall.setOperationName(metaOperation.metaName());
                operationCall.setValues(serializeParam(metaOperation, param));
                cdn.sendToPeer(peerName, operationCall, new KCallback<KMessage>() {
                    @Override
                    public void on(KMessage message) {
                        if (message.values() != null) {
                            callback.on(unserializeReturn(source.manager().model().metaModel(), metaOperation, message.values()[0]));
                        } else {
                            callback.on(unserializeReturn(source.manager().model().metaModel(), metaOperation, null));
                        }
                    }
                });
            }
        };
    }

}
