package org.kevoree.modeling.memory.storage;

public class KMemoryElementTypes {

    public static final short CHUNK = 0;

    public static final short LONG_TREE = 1;

    public static final short LONG_LONG_TREE = 2;

    public static final short LONG_LONG_MAP = 3;

    public static final short DIRTY_BIT_INDEX = 0;

    public static final int DIRTY_BIT = 1 << DIRTY_BIT_INDEX;
}
