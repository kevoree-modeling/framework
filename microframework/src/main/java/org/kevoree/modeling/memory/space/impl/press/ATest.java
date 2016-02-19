package org.kevoree.modeling.memory.space.impl.press;

import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.chunk.impl.UnsafeUtil;
import org.kevoree.modeling.memory.space.KChunkTypes;
import sun.misc.Unsafe;

/**
 * Created by thomas on 18/02/16.
 */
public class ATest {
    private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    public static void main(String[] args) {

        System.out.println("before");

        int size = 10;
        PressOffHeapChunkSpace space = new PressOffHeapChunkSpace(size);

        //Fill the entire cache
        for (int i = 0; i < size; i++) {
            KChunk chunk = space.create(i, 0, 10, KChunkTypes.OBJECT_CHUNK, null);
            chunk.setFlags(KChunkFlags.DIRTY_BIT, 0);
        }

        for (int i = 0; i < size; i++) {
            KChunk chunk = space.get(i, 0, 10);
            System.out.println(chunk);

            int c = chunk.inc();
            System.out.println("counter: " + c);
        }

        for (int i = 0; i < size; i++) {
            space.create(i, 1, 1, KChunkTypes.OBJECT_CHUNK, null);
        }

        System.out.println("after");
    }
}
