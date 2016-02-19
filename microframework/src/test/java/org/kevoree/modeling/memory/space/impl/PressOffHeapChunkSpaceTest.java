package org.kevoree.modeling.memory.space.impl;

import org.junit.Test;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.memory.space.impl.press.PressOffHeapChunkSpace;

/** @ignore ts */
public class PressOffHeapChunkSpaceTest {

    @Test
    public void monoThreadFullTest() {

        int size = 100000;

        PressOffHeapChunkSpace space = new PressOffHeapChunkSpace(200000);
        //Fill the entire cache
        for (int i = 0; i < size; i++) {
            KChunk chunk = space.create(i, 0, 10, KChunkTypes.OBJECT_CHUNK,null);
            chunk.setFlags(KChunkFlags.DIRTY_BIT, 0);
        }
        //space.create(1, 1, 1, KChunkTypes.OBJECT_CHUNK);
        //space.create(2, 1, 1, KChunkTypes.OBJECT_CHUNK);
        //space.create(3, 1, 1, KChunkTypes.OBJECT_CHUNK);
        //space.create(4, 1, 1, KChunkTypes.OBJECT_CHUNK);

//        for (int i = 0; i < 3; i++) {
//            space.get(i, 0, 10).inc();
//        }
//
//        for (int i = 0; i < 8; i++) {
//            space.create(i, 1, 1, KChunkTypes.OBJECT_CHUNK,null);
//        }

//        System.out.println(space);


    }

}
