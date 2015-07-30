package org.kevoree.modeling;

import org.kevoree.modeling.util.maths.Base64;

public class KContentKey {

    public static String toString(long[] keys, int keyIndex) {
        StringBuilder buffer = new StringBuilder();
        int offset = keyIndex * 3;
        if (keys[offset] != KConfig.NULL_LONG) {
            Base64.encodeLongToBuffer(keys[offset], buffer);
        }
        buffer.append(KConfig.KEY_SEP);
        if (keys[offset + 1] != KConfig.NULL_LONG) {
            Base64.encodeLongToBuffer(keys[offset + 1], buffer);
        }
        buffer.append(KConfig.KEY_SEP);
        if (keys[offset + 2] != KConfig.NULL_LONG) {
            Base64.encodeLongToBuffer(keys[offset + 2], buffer);
        }
        return buffer.toString();
    }


    public long universe;

    public long time;

    public long obj;

    public KContentKey(long p_universeID, long p_timeID, long p_objID) {
        universe = p_universeID;
        time = p_timeID;
        obj = p_objID;
    }

    public static KContentKey createUniverseTree(long p_objectID) {
        return new KContentKey(KConfig.NULL_LONG, KConfig.NULL_LONG, p_objectID);
    }

    public static KContentKey createTimeTree(long p_universeID, long p_objectID) {
        return new KContentKey(p_universeID, KConfig.NULL_LONG, p_objectID);
    }

    public static KContentKey createObject(long p_universeID, long p_quantaID, long p_objectID) {
        return new KContentKey(p_universeID, p_quantaID, p_objectID);
    }

    public static KContentKey createGlobalUniverseTree() {
        return new KContentKey(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
    }

    public static KContentKey createRootUniverseTree() {
        return new KContentKey(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.END_OF_TIME);
    }

    public static KContentKey createRootTimeTree(long universeID) {
        return new KContentKey(universeID, KConfig.NULL_LONG, KConfig.END_OF_TIME);
    }

    public static KContentKey createLastPrefix() {
        return new KContentKey(KConfig.END_OF_TIME, KConfig.NULL_LONG, KConfig.NULL_LONG);
    }

    public static KContentKey createLastObjectIndexFromPrefix(Short prefix) {
        return new KContentKey(KConfig.END_OF_TIME, KConfig.NULL_LONG, prefix);
    }

    public static KContentKey createLastUniverseIndexFromPrefix(Short prefix) {
        return new KContentKey(KConfig.BEGINNING_OF_TIME, KConfig.NULL_LONG, prefix);
    }

    public static KContentKey create(String payload) {
        if (payload == null || payload.length() == 0) {
            return null;
        } else {
            KContentKey key = new KContentKey(KConfig.NULL_LONG, KConfig.NULL_LONG, KConfig.NULL_LONG);
            int maxRead = payload.length();
            int indexStartElem = -1;
            int indexElem = 0;
            int partIndex = 0;
            for (int i = 0; i < maxRead; i++) {
                if (payload.charAt(i) == KConfig.KEY_SEP) {
                    if (indexStartElem != -1) {
                        try {
                            switch (partIndex) {
                                case 0:
                                    key.universe = Base64.decodeToLongWithBounds(payload, indexStartElem, i);
                                    break;
                                case 1:
                                    key.time = Base64.decodeToLongWithBounds(payload, indexStartElem, i);
                                    break;
                                case 2:
                                    key.obj = Base64.decodeToLongWithBounds(payload, indexStartElem, i);
                                    break;
                                default:
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    indexStartElem = -1;
                    indexElem = indexElem + 1;
                    partIndex++;
                } else {
                    if (indexStartElem == -1) {
                        indexStartElem = i;
                    }
                }
            }
            if (indexStartElem != -1) {
                try {
                    switch (partIndex) {
                        case 0:
                            key.universe = Base64.decodeToLongWithBounds(payload, indexStartElem, maxRead);
                            break;
                        case 1:
                            key.time = Base64.decodeToLongWithBounds(payload, indexStartElem, maxRead);
                            break;
                        case 2:
                            key.obj = Base64.decodeToLongWithBounds(payload, indexStartElem, maxRead);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return key;
        }
    }

    @Override
    public boolean equals(Object param) {
        if (param instanceof KContentKey) {
            KContentKey remote = (KContentKey) param;
            return remote.universe == universe && remote.time == time && remote.obj == obj;
        } else {
            return false;
        }
    }

}
