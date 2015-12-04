package org.kevoree.modeling.memory;

public class KChunkFlags {

    public static final short DIRTY_BIT_INDEX = 0;

    public static final int DIRTY_BIT = 1 << DIRTY_BIT_INDEX;

    public static final short REMOVED_BIT_INDEX = 1;

    public static final int REMOVED_BIT = 1 << REMOVED_BIT_INDEX;

}
