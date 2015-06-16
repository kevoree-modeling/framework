package org.kevoree.modeling.message;

import org.kevoree.modeling.format.json.JsonObjectReader;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.impl.ContentPutRequest;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.message.impl.*;

public class KMessageLoader {

    public static String TYPE_NAME = "type";
    public static String OPERATION_NAME = "op";
    public static String KEY_NAME = "key";
    public static String KEYS_NAME = "keys";
    public static String ID_NAME = "id";
    public static String VALUE_NAME = "value";
    public static String VALUES_NAME = "values";
    public static String CLASS_IDX_NAME = "class";
    public static String PARAMETERS_NAME = "params";

    public static final int EVENTS_TYPE = 0;

    public static final int GET_REQ_TYPE = 1;

    public static final int GET_RES_TYPE = 2;

    public static final int PUT_REQ_TYPE = 3;

    public static final int PUT_RES_TYPE = 4;

    public static final int OPERATION_CALL_TYPE = 5;

    public static final int OPERATION_RESULT_TYPE = 6;

    public static final int ATOMIC_GET_INC_REQUEST_TYPE = 7;

    public static final int ATOMIC_GET_INC_RESULT_TYPE = 8;

    public static KMessage load(String payload) {
        if (payload == null) {
            return null;
        }
        JsonObjectReader objectReader = new JsonObjectReader();
        objectReader.parseObject(payload);
        try {
            Integer parsedType = Integer.parseInt(objectReader.get(TYPE_NAME).toString());
            if (parsedType == EVENTS_TYPE) {
                Events eventsMessage = null;
                if (objectReader.get(KEYS_NAME) != null) {
                    String[] objIdsRaw = objectReader.getAsStringArray(KEYS_NAME);
                    eventsMessage = new Events(objIdsRaw.length);
                    KContentKey[] keys = new KContentKey[objIdsRaw.length];
                    for (int i = 0; i < objIdsRaw.length; i++) {
                        try {
                            keys[i] = KContentKey.create(objIdsRaw[i]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    eventsMessage._objIds = keys;
                    if (objectReader.get(VALUES_NAME) != null) {
                        String[] metaInt = objectReader.getAsStringArray(VALUES_NAME);
                        int[][] metaIndexes = new int[metaInt.length][];
                        for (int i = 0; i < metaInt.length; i++) {
                            try {
                                if (metaInt[i] != null) {
                                    String[] splitted = metaInt[i].split("%");
                                    int[] newMeta = new int[splitted.length];
                                    for (int h = 0; h < splitted.length; h++) {
                                        if (splitted[h] != null && !splitted[h].isEmpty()) {
                                            newMeta[h] = Integer.parseInt(splitted[h]);
                                        }
                                    }
                                    metaIndexes[i] = newMeta;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        eventsMessage._metaindexes = metaIndexes;
                    }
                }
                return eventsMessage;
            } else if (parsedType == GET_REQ_TYPE) {
                GetRequest getKeysRequest = new GetRequest();
                if (objectReader.get(ID_NAME) != null) {
                    getKeysRequest.id = Long.parseLong(objectReader.get(ID_NAME).toString());
                }
                if (objectReader.get(KEYS_NAME) != null) {
                    String[] metaInt = objectReader.getAsStringArray(KEYS_NAME);
                    KContentKey[] keys = new KContentKey[metaInt.length];
                    for (int i = 0; i < metaInt.length; i++) {
                        keys[i] = KContentKey.create(metaInt[i]);
                    }
                    getKeysRequest.keys = keys;
                }
                return getKeysRequest;
            } else if (parsedType == GET_RES_TYPE) {
                GetResult getResult = new GetResult();
                if (objectReader.get(ID_NAME) != null) {
                    getResult.id = Long.parseLong(objectReader.get(ID_NAME).toString());
                }
                if (objectReader.get(VALUES_NAME) != null) {
                    String[] metaInt = objectReader.getAsStringArray(VALUES_NAME);
                    String[] values = new String[metaInt.length];
                    for (int i = 0; i < metaInt.length; i++) {
                        values[i] = JsonString.unescape(metaInt[i]);
                    }
                    getResult.values = values;
                }
                return getResult;
            } else if (parsedType == PUT_REQ_TYPE) {
                PutRequest putRequest = new PutRequest();
                if (objectReader.get(ID_NAME) != null) {
                    putRequest.id = Long.parseLong(objectReader.get(ID_NAME).toString());
                }
                String[] toFlatKeys = null;
                String[] toFlatValues = null;
                if (objectReader.get(KEYS_NAME) != null) {
                    toFlatKeys = objectReader.getAsStringArray(KEYS_NAME);
                }
                if (objectReader.get(VALUES_NAME) != null) {
                    toFlatValues = objectReader.getAsStringArray(VALUES_NAME);
                }
                if (toFlatKeys != null && toFlatValues != null && toFlatKeys.length == toFlatValues.length) {
                    if (putRequest.request == null) {
                        putRequest.request = new ContentPutRequest(toFlatKeys.length);
                    }
                    for (int i = 0; i < toFlatKeys.length; i++) {
                        putRequest.request.put(KContentKey.create(toFlatKeys[i]), JsonString.unescape(toFlatValues[i]));
                    }
                }
                return putRequest;
            } else if (parsedType == PUT_RES_TYPE) {
                PutResult putResult = new PutResult();
                if (objectReader.get(ID_NAME) != null) {
                    putResult.id = Long.parseLong(objectReader.get(ID_NAME).toString());
                }
                return putResult;
            } else if (parsedType == OPERATION_CALL_TYPE) {
                OperationCallMessage callMessage = new OperationCallMessage();
                if (objectReader.get(ID_NAME) != null) {
                    callMessage.id = Long.parseLong(objectReader.get(ID_NAME).toString());
                }
                if (objectReader.get(KEY_NAME) != null) {
                    callMessage.key = KContentKey.create(objectReader.get(KEY_NAME).toString());
                }
                if (objectReader.get(CLASS_IDX_NAME) != null) {
                    callMessage.classIndex = Integer.parseInt(objectReader.get(CLASS_IDX_NAME).toString());
                }
                if (objectReader.get(OPERATION_NAME) != null) {
                    callMessage.opIndex = Integer.parseInt(objectReader.get(OPERATION_NAME).toString());
                }
                if (objectReader.get(PARAMETERS_NAME) != null) {
                    String[] params = objectReader.getAsStringArray(PARAMETERS_NAME);
                    String[] toFlat = new String[params.length];
                    for (int i = 0; i < params.length; i++) {
                        toFlat[i] = JsonString.unescape(params[i]);
                    }
                    callMessage.params = toFlat;
                }
                return callMessage;
            } else if (parsedType == OPERATION_RESULT_TYPE) {
                OperationResultMessage resultMessage = new OperationResultMessage();
                if (objectReader.get(ID_NAME) != null) {
                    resultMessage.id = Long.parseLong(objectReader.get(ID_NAME).toString());
                }
                if (objectReader.get(KEY_NAME) != null) {
                    resultMessage.key = KContentKey.create(objectReader.get(KEY_NAME).toString());
                }
                if (objectReader.get(VALUE_NAME) != null) {
                    resultMessage.value = objectReader.get(VALUE_NAME).toString();
                }
                return resultMessage;
            } else if (parsedType == ATOMIC_GET_INC_REQUEST_TYPE) {
                AtomicGetIncrementRequest atomicGetMessage = new AtomicGetIncrementRequest();
                if (objectReader.get(ID_NAME) != null) {
                    atomicGetMessage.id = Long.parseLong(objectReader.get(ID_NAME).toString());
                }
                if (objectReader.get(KEY_NAME) != null) {
                    atomicGetMessage.key = KContentKey.create(objectReader.get(KEY_NAME).toString());
                }
                return atomicGetMessage;
            } else if (parsedType == ATOMIC_GET_INC_RESULT_TYPE) {
                AtomicGetIncrementResult atomicGetResultMessage = new AtomicGetIncrementResult();
                if (objectReader.get(ID_NAME) != null) {
                    atomicGetResultMessage.id = Long.parseLong(objectReader.get(ID_NAME).toString());
                }
                if (objectReader.get(VALUE_NAME) != null) {
                    try {
                        atomicGetResultMessage.value = Short.parseShort(objectReader.get(VALUE_NAME).toString());
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                return atomicGetResultMessage;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
