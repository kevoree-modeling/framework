package org.kevoree.modeling;

public class KConfig {

    public static final int TREE_CACHE_SIZE = 3;

    public static final int CALLBACK_HISTORY = 1000;

    // Limit long lengths to 53 bits because of JS limitation

    public static final int LONG_SIZE = 53;

    public static final int PREFIX_SIZE = 16;

    public static final long BEGINNING_OF_TIME = -0x001FFFFFFFFFFFFEl;

    public static final long END_OF_TIME = 0x001FFFFFFFFFFFFEl;

    public static final long NULL_LONG = 0x001FFFFFFFFFFFFFl;

    // Limit limit local index to LONG limit - prefix size
    public static final long KEY_PREFIX_MASK = 0x0000001FFFFFFFFFl;

    public static final char KEY_SEP = '/';

    public static final int KEY_SIZE = 3;

    public static final int CACHE_INIT_SIZE = 16;

    public static final float CACHE_LOAD_FACTOR = ((float) 75 / (float) 100);

}
