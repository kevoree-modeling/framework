package org.kevoree.modeling.message.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.util.Base64;

public class Message implements KMessage {

    private static final int TYPE_INDEX = 0;
    private static final int ID_INDEX = 1;
    private static final int OPERATION_INDEX = 2;
    private static final int CLASS_INDEX = 3;
    private static final int PEER_INDEX = 4;
    private static final int KEYS_INDEX = 5;
    private static final int VALUES_INDEX = 6;
    private static final int VALUES2_INDEX = 7;

    private static final char[] KEYS_NAME = new char[]{'T', 'I', 'O', 'C', 'P', 'K', 'V', 'W'}; //type, ids, operation, class, peer, keys, values, values2

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
    public String save() {
        StringBuilder buffer = new StringBuilder();
        boolean isFirst = true;
        for (int i = 0; i < NB_ELEM; i++) {
            if (internal[i] != null) {
                if (!isFirst) {
                    buffer.append(KConfig.ELEM_SEP);
                }
                isFirst = false;
                buffer.append(KEYS_NAME[i]);
                buffer.append(KConfig.VAL_SEP);
                switch (i) {
                    case 0:
                        Base64.encodeIntToBuffer((Integer) internal[i], buffer);
                        break;
                    case 1:
                        Base64.encodeIntToBuffer((Integer) internal[i], buffer);
                        break;
                    case 2:
                        Base64.encodeStringToBuffer((String) internal[i], buffer);
                        break;
                    case 3:
                        Base64.encodeStringToBuffer((String) internal[i], buffer);
                        break;
                    case 4:
                        Base64.encodeStringToBuffer((String) internal[i], buffer);
                        break;
                    case 5:
                        long[] lkeys = (long[]) internal[i];
                        Base64.encodeIntToBuffer(lkeys.length, buffer);
                        for (int j = 0; j < lkeys.length; j++) {
                            buffer.append(KConfig.VAL_SEP);
                            Base64.encodeLongToBuffer(lkeys[j], buffer);
                        }
                        break;
                    case 6:
                        String[] lvalues = (String[]) internal[i];
                        Base64.encodeIntToBuffer(lvalues.length, buffer);
                        for (int j = 0; j < lvalues.length; j++) {
                            buffer.append(KConfig.VAL_SEP);
                            Base64.encodeStringToBuffer(lvalues[j], buffer);
                        }
                        break;
                    case 7:
                        String[] lvalues2 = (String[]) internal[i];
                        Base64.encodeIntToBuffer(lvalues2.length, buffer);
                        for (int j = 0; j < lvalues2.length; j++) {
                            buffer.append(KConfig.VAL_SEP);
                            Base64.encodeStringToBuffer(lvalues2[j], buffer);
                        }
                        break;
                }
            }
        }
        return buffer.toString();
    }

    public static KMessage load(String payload) {
        Message msg = new Message();
        if (payload == null) {
            return null;
        }
        int i = 0;
        int readElemIndex = -1;
        int previousValStart = -1;
        long[] longArray = null;
        String[] stringArray = null;
        int currentArrayIndex = -1;
        final int payloadSize = payload.length();
        while (i < payloadSize) {
            if (payload.charAt(i) == KConfig.ELEM_SEP) {
                if (readElemIndex != -1) {
                    //VAL DETECTED, CONVERT AND INSERT IT
                    if (readElemIndex < 2) {
                        //int val
                        msg.internal[readElemIndex] = Base64.decodeToIntWithBounds(payload, previousValStart, i);
                    } else if (readElemIndex < 5) {
                        //String val
                        msg.internal[readElemIndex] = Base64.decodeToStringWithBounds(payload, previousValStart, i);
                    } else {
                        if (readElemIndex == 5 && longArray != null) {
                            //long[] val
                            longArray[currentArrayIndex] = Base64.decodeToLongWithBounds(payload, previousValStart, i);
                            msg.internal[readElemIndex] = longArray;
                            longArray = null;
                        } else if (stringArray != null) {
                            //String[] val
                            stringArray[currentArrayIndex] = Base64.decodeToStringWithBounds(payload, previousValStart, i);
                            msg.internal[readElemIndex] = stringArray;
                            stringArray = null;
                        }
                    }
                }
                previousValStart = -1;
                readElemIndex = -1;
            } else if (payload.charAt(i) == KConfig.VAL_SEP) {
                if (readElemIndex == -1) {
                    char pastType = payload.charAt(i - 1);
                    for (int h = 0; h < NB_ELEM; h++) {
                        if (pastType == KEYS_NAME[h]) {
                            readElemIndex = h;
                        }
                    }
                } else {
                    if (readElemIndex > 5) {
                        //first value is size;
                        if (stringArray == null) {
                            stringArray = new String[Base64.decodeToIntWithBounds(payload, previousValStart, i)];
                            currentArrayIndex = 0;
                        } else {
                            stringArray[currentArrayIndex] = Base64.decodeToStringWithBounds(payload, previousValStart, i);
                            currentArrayIndex++;
                        }
                    } else {
                        if (longArray == null) {
                            longArray = new long[Base64.decodeToIntWithBounds(payload, previousValStart, i)];
                            currentArrayIndex = 0;
                        } else {
                            longArray[currentArrayIndex] = Base64.decodeToLongWithBounds(payload, previousValStart, i);
                            currentArrayIndex++;
                        }
                    }
                }
                previousValStart = i + 1;
            }
            //iterate for next round
            i++;
        }
        if (readElemIndex != -1) {
            //VAL DETECTED, CONVERT AND INSERT IT
            if (readElemIndex < 2) {
                //int val
                msg.internal[readElemIndex] = Base64.decodeToInt(payload.substring(previousValStart, i));
            } else if (readElemIndex < 5) {
                //String val
                msg.internal[readElemIndex] = Base64.decodeToStringWithBounds(payload, previousValStart, i);
            } else {
                if (readElemIndex == 5 && longArray != null) {
                    //long[] val
                    longArray[currentArrayIndex] = Base64.decodeToLong(payload.substring(previousValStart, i));
                    msg.internal[readElemIndex] = longArray;
                } else if (stringArray != null) {
                    //String[] val
                    stringArray[currentArrayIndex] = Base64.decodeToStringWithBounds(payload, previousValStart, i);
                    msg.internal[readElemIndex] = stringArray;
                }
            }
        }
        return msg;
    }

}
