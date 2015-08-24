package org.kevoree.modeling.operation;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.impl.Message;
import org.kevoree.modeling.meta.KMetaOperation;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.util.PrimitiveHelper;
import org.kevoree.modeling.util.maths.Base64;

public class OperationStrategies {

    public static String serializeReturn(KMetaOperation metaOperation, Object result) {
        switch (metaOperation.returnType()) {
            case KPrimitiveTypes.BOOL_ID:
                if ((Boolean) result) {
                    return "1";
                } else {
                    return "0";
                }
            case KPrimitiveTypes.STRING_ID:
                return JsonString.encode(result.toString());
            case KPrimitiveTypes.DOUBLE_ID:
                return Base64.encodeDouble((Double) result);
            case KPrimitiveTypes.INT_ID:
                return Base64.encodeInt((Integer) result);
            case KPrimitiveTypes.LONG_ID:
                return Base64.encodeLong((Integer) result);
            default:
                return null;
        }
    }

    public static Object unserializeReturn(KMetaOperation metaOperation, String resultString) {
        switch (metaOperation.returnType()) {
            case KPrimitiveTypes.BOOL_ID:
                return PrimitiveHelper.equals(resultString, "1");
            case KPrimitiveTypes.STRING_ID:
                return JsonString.unescape(resultString);
            case KPrimitiveTypes.DOUBLE_ID:
                return Base64.decodeToDouble(resultString);
            case KPrimitiveTypes.INT_ID:
                return Base64.decodeToInt(resultString);
            case KPrimitiveTypes.LONG_ID:
                return Base64.decodeToLong(resultString);
            default:
                return null;
        }
    }

    public static Object[] unserializeParam(KMetaOperation metaOperation, String[] param) {
        int[] paramTypes = metaOperation.paramTypes();
        Object[] objParam = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            switch (paramTypes[i]) {
                case KPrimitiveTypes.BOOL_ID:
                    objParam[i] = PrimitiveHelper.equals(param[i], "1");
                    break;
                case KPrimitiveTypes.STRING_ID:
                    objParam[i] = JsonString.unescape(param[i]);
                    break;
                case KPrimitiveTypes.DOUBLE_ID:
                    objParam[i] = Base64.decodeToDouble(param[i]);
                    break;
                case KPrimitiveTypes.INT_ID:
                    objParam[i] = Base64.decodeToInt(param[i]);
                    break;
                case KPrimitiveTypes.LONG_ID:
                    objParam[i] = Base64.decodeToLong(param[i]);
                    break;
                default:
                    objParam[i] = null;
                    break;
            }
        }
        return objParam;
    }

    public static String[] serializeParam(KMetaOperation metaOperation, Object[] param) {
        int[] paramTypes = metaOperation.paramTypes();
        String[] stringParams = new String[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            switch (paramTypes[i]) {
                case KPrimitiveTypes.BOOL_ID:
                    if ((Boolean) param[i]) {
                        stringParams[i] = "1";
                    } else {
                        stringParams[i] = "0";
                    }
                    break;
                case KPrimitiveTypes.STRING_ID:
                    stringParams[i] = JsonString.encode(param[i].toString());
                    break;
                case KPrimitiveTypes.DOUBLE_ID:
                    stringParams[i] = Base64.encodeDouble((Double) param[i]);
                    break;
                case KPrimitiveTypes.INT_ID:
                    stringParams[i] = Base64.encodeInt((Integer) param[i]);
                    break;
                case KPrimitiveTypes.LONG_ID:
                    stringParams[i] = Base64.encodeLong((Integer) param[i]);
                    break;
                default:
                    stringParams[i] = null;
                    break;
            }
        }
        return stringParams;
    }

    public static KOperationStrategy ONLY_ONE = new KOperationStrategy() {
        @Override
        public void invoke(final KContentDeliveryDriver cdn, final KMetaOperation metaOperation, final KObject source, final Object[] param, final KOperationManager manager, final KCallback callback) {
            //Prepare the message
            KMessage operationCall = new Message();
            operationCall.setKeys(new long[]{source.universe(), source.now(), source.uuid()});
            operationCall.setClassName(source.metaClass().metaName());
            operationCall.setOperationName(metaOperation.metaName());
            operationCall.setValues(serializeParam(metaOperation, param));
            //now try to send sequentially...
            String[] peers = cdn.peers();
            final int[] i = {0};
            final KCallback<KMessage>[] internals = new KCallback[1];
            internals[0] = new KCallback<KMessage>() {
                @Override
                public void on(KMessage o) {
                    if (o == null || o.values() == null || o.values().length == 0) {
                        if (i[0] < peers.length) {
                            String selectedPeer = peers[i[0]];
                            i[0]++;
                            manager.send(selectedPeer, operationCall, internals[0]);
                        } else {
                            callback.on(null);
                        }
                    } else {
                        callback.on(unserializeReturn(metaOperation, o.values()[0]));
                    }
                }
            };
            internals[0].on(null);//initiate
        }
    };


}
