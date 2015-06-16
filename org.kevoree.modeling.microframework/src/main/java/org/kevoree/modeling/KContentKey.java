package org.kevoree.modeling;

public class KContentKey {

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
        return new KContentKey(KConfig.END_OF_TIME, KConfig.NULL_LONG, Long.parseLong(prefix.toString()));
    }

    public static KContentKey createLastUniverseIndexFromPrefix(Short prefix) {
        return new KContentKey(KConfig.END_OF_TIME, KConfig.NULL_LONG, Long.parseLong(prefix.toString()));
    }

    public static KContentKey create(String payload) {
        if (payload == null || payload.length() == 0) {
            return null;
        } else {
            long[] temp = new long[KConfig.KEY_SIZE];
            for (int i = 0; i < KConfig.KEY_SIZE; i++) {
                temp[i] = KConfig.NULL_LONG;
            }
            int maxRead = payload.length();
            int indexStartElem = -1;
            int indexElem = 0;
            for (int i = 0; i < maxRead; i++) {
                if (payload.charAt(i) == KConfig.KEY_SEP) {
                    if (indexStartElem != -1) {
                        try {
                            temp[indexElem] = Long.parseLong(payload.substring(indexStartElem, i));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    indexStartElem = -1;
                    indexElem = indexElem + 1;
                } else {
                    if (indexStartElem == -1) {
                        indexStartElem = i;
                    }
                }
            }
            if (indexStartElem != -1) {
                try {
                    temp[indexElem] = Long.parseLong(payload.substring(indexStartElem, maxRead));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return new KContentKey(temp[0], temp[1], temp[2]);
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (universe != KConfig.NULL_LONG) {
            buffer.append(universe);
        }
        buffer.append(KConfig.KEY_SEP);
        if (time != KConfig.NULL_LONG) {
            buffer.append(time);
        }
        buffer.append(KConfig.KEY_SEP);
        if (obj != KConfig.NULL_LONG) {
            buffer.append(obj);
        }
        return buffer.toString();
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
