package org.kevoree.modeling.memory.chunk.impl;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/** @ignore ts */
public class UnsafeUtil {

    @SuppressWarnings("restriction")
    public static Unsafe getUnsafe() {
        try {

            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);

        } catch (Exception e) {
            throw new RuntimeException("ERROR: unsafe operations are not available");
        }
    }

}
