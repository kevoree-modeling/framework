package org.kevoree.modeling.message.impl;

import org.kevoree.modeling.format.json.JsonObjectReader;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.util.Base64;

public class Message implements KMessage {

    private static final int ID_INDEX = 0;
    private static final int TYPE_INDEX = 1;
    private static final int OPERATION_INDEX = 2;
    private static final int CLASS_INDEX = 3;
    private static final int PEER_INDEX = 4;
    private static final int KEYS_INDEX = 5;
    private static final int VALUES_INDEX = 6;
    private static final int VALUES2_INDEX = 7;

    private static final String[] KEYS_NAME = new String[]{"id", "type", "op", "class", "peer", "keys", "values", "values2"};

    public static final int EVENTS_TYPE = 0;
    public static final int GET_REQ_TYPE = 1;
    public static final int GET_RES_TYPE = 2;
    public static final int PUT_REQ_TYPE = 3;
    public static final int PUT_RES_TYPE = 4;
    public static final int OPERATION_CALL_TYPE = 5;
    public static final int OPERATION_RESULT_TYPE = 6;
    public static final int ATOMIC_GET_INC_REQUEST_TYPE = 7;
    public static final int ATOMIC_GET_INC_RESULT_TYPE = 8;
    public static final int OPERATION_MAPPING = 9;

    private static final int NB_ELEM = 8;

    private Object[] internal = new Object[NB_ELEM];

    @Override
    public Integer id() {
        return (Integer) internal[ID_INDEX];
    }

    @Override
    public void setID(Integer val) {
        internal[ID_INDEX] = val;
    }

    @Override
    public Integer type() {
        return (Integer) internal[TYPE_INDEX];
    }

    @Override
    public void setType(Integer val) {
        internal[TYPE_INDEX] = val;
    }

    @Override
    public String operationName() {
        return (String) internal[OPERATION_INDEX];
    }

    @Override
    public void setOperationName(String val) {
        internal[OPERATION_INDEX] = val;
    }

    @Override
    public String className() {
        return (String) internal[CLASS_INDEX];
    }

    @Override
    public void setClassName(String val) {
        internal[CLASS_INDEX] = val;
    }

    @Override
    public long[] keys() {
        return (long[]) internal[KEYS_INDEX];
    }

    @Override
    public void setKeys(long[] val) {
        internal[KEYS_INDEX] = val;
    }

    @Override
    public String[] values() {
        return (String[]) internal[VALUES_INDEX];
    }

    @Override
    public void setValues(String[] val) {
        internal[VALUES_INDEX] = val;
    }

    @Override
    public String[] values2() {
        return (String[]) internal[VALUES2_INDEX];
    }

    @Override
    public void setValues2(String[] val) {
        internal[VALUES2_INDEX] = val;
    }

    @Override
    public String peer() {
        return (String) internal[PEER_INDEX];
    }

    @Override
    public void setPeer(String val) {
        internal[PEER_INDEX] = val;
    }

    @Override
    public String json() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("{");
        boolean isFirst = true;
        for (int i = 0; i < NB_ELEM; i++) {
            if (internal[i] != null) {
                if (!isFirst) {
                    buffer.append(",");
                }
                isFirst = false;
                buffer.append("\"");
                buffer.append(KEYS_NAME[i]);
                if (i < 5) {
                    buffer.append("\":\"");
                } else {
                    buffer.append("\":");
                }
                switch (i) {
                    case 0:
                        Base64.encodeIntToBuffer((Integer) internal[i], buffer);
                        break;
                    case 1:
                        Base64.encodeIntToBuffer((Integer) internal[i], buffer);
                        break;
                    case 2:
                        JsonString.encodeBuffer(buffer, (String) internal[i]);
                        break;
                    case 3:
                        JsonString.encodeBuffer(buffer, (String) internal[i]);
                        break;
                    case 4:
                        JsonString.encodeBuffer(buffer, (String) internal[i]);
                        break;
                    case 5:
                        long[] keys = (long[]) internal[i];
                        buffer.append("[");
                        for (int j = 0; j < keys.length; j++) {
                            if (j != 0) {
                                buffer.append(",");
                            }
                            buffer.append("\"");
                            Base64.encodeLongToBuffer(keys[j], buffer);
                            buffer.append("\"");
                        }
                        buffer.append("]");
                        break;
                    case 6:
                        String[] values = (String[]) internal[i];
                        buffer.append("[");
                        for (int j = 0; j < values.length; j++) {
                            if (j != 0) {
                                buffer.append(",");
                            }
                            buffer.append("\"");
                            JsonString.encodeBuffer(buffer, values[j]);
                            buffer.append("\"");
                        }
                        buffer.append("]");
                        break;
                    case 7:
                        String[] values2 = (String[]) internal[i];
                        buffer.append("[");
                        for (int j = 0; j < values2.length; j++) {
                            if (j != 0) {
                                buffer.append(",");
                            }
                            buffer.append("\"");
                            JsonString.encodeBuffer(buffer, values2[j]);
                            buffer.append("\"");
                        }
                        buffer.append("]");
                        break;
                }
                if (i < 5) {
                    buffer.append("\"");
                }

            }
        }
        buffer.append("}");
        return buffer.toString();
    }

    public static KMessage load(String payload) {
        Message msg = new Message();
        if (payload == null) {
            return null;
        }
        JsonObjectReader objectReader = new JsonObjectReader();
        objectReader.parseObject(payload);
        try {
            if (objectReader.get(KEYS_NAME[TYPE_INDEX]) != null) {
                msg.setType(Base64.decodeToInt(objectReader.get(KEYS_NAME[TYPE_INDEX]).toString()));
            }
            if (objectReader.get(KEYS_NAME[ID_INDEX]) != null) {
                msg.setID(Base64.decodeToInt(objectReader.get(KEYS_NAME[ID_INDEX]).toString()));
            }
            if (objectReader.get(KEYS_NAME[CLASS_INDEX]) != null) {
                msg.setClassName(JsonString.unescape(objectReader.get(KEYS_NAME[CLASS_INDEX]).toString()));
            }
            if (objectReader.get(KEYS_NAME[OPERATION_INDEX]) != null) {
                msg.setOperationName(JsonString.unescape(objectReader.get(KEYS_NAME[OPERATION_INDEX]).toString()));
            }
            if (objectReader.get(KEYS_NAME[PEER_INDEX]) != null) {
                msg.setPeer(JsonString.unescape(objectReader.get(KEYS_NAME[PEER_INDEX]).toString()));
            }
            if (objectReader.get(KEYS_NAME[KEYS_INDEX]) != null) {
                String[] objIdsRaw = objectReader.getAsStringArray(KEYS_NAME[KEYS_INDEX]);
                long[] p_keys = new long[objIdsRaw.length];
                for (int i = 0; i < objIdsRaw.length; i++) {
                    try {
                        p_keys[i] = Base64.decodeToLong(objIdsRaw[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                msg.setKeys(p_keys);
            }
            if (objectReader.get(KEYS_NAME[VALUES_INDEX]) != null) {
                String[] objIdsRaw = objectReader.getAsStringArray(KEYS_NAME[VALUES_INDEX]);
                String[] p_values = new String[objIdsRaw.length];
                for (int i = 0; i < objIdsRaw.length; i++) {
                    try {
                        p_values[i] = JsonString.unescape(objIdsRaw[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                msg.setValues(p_values);
            }
            if (objectReader.get(KEYS_NAME[VALUES2_INDEX]) != null) {
                String[] objIdsRaw = objectReader.getAsStringArray(KEYS_NAME[VALUES2_INDEX]);
                String[] p_values = new String[objIdsRaw.length];
                for (int i = 0; i < objIdsRaw.length; i++) {
                    try {
                        p_values[i] = JsonString.unescape(objIdsRaw[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                msg.setValues2(p_values);
            }
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
