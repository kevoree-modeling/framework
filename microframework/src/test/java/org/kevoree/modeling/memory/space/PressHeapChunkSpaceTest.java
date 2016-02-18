package org.kevoree.modeling.memory.space;

import org.junit.Test;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.space.impl.press.FixedSizeLinkedList;
import org.kevoree.modeling.memory.space.impl.press.PressHeapChunkSpace;

public class PressHeapChunkSpaceTest {

    /*
    @Test
    public void monoThreadTest() {
        PressHeapChunkSpace space = new PressHeapChunkSpace(100);
        space.create(0, 0, 0, KChunkTypes.OBJECT_CHUNK);
        space.create(0, 0, 1, KChunkTypes.OBJECT_CHUNK);
        Assert.assertEquals(space.toString(), "0#:0,0,0=>0(count:0,flag:0)==>no model\n1#:0,0,1=>0(count:0,flag:0)==>no model\n");
        Assert.assertEquals(space.size(), 2);
        space.remove(0, 0, 0, null);
        Assert.assertEquals(space.size(), 1);
        space.create(0, 0, 42, KChunkTypes.OBJECT_CHUNK);
        Assert.assertEquals(space.size(), 2);
        Assert.assertEquals(space.toString(), "0#:0,0,42=>0(count:0,flag:0)==>no model\n1#:0,0,1=>0(count:0,flag:0)==>no model\n");
        space.create(0, 0, 12, KChunkTypes.OBJECT_CHUNK);
        space.create(0, 0, 32, KChunkTypes.OBJECT_CHUNK);
        Assert.assertEquals(space.toString(), "0#:0,0,42=>0(count:0,flag:0)==>no model\n" +
                "1#:0,0,1=>0(count:0,flag:0)==>no model\n" +
                "2#:0,0,12=>0(count:0,flag:0)==>no model\n" +
                "3#:0,0,32=>0(count:0,flag:0)==>no model\n");
    }*/

    //@Test
    public void mini() {
        FixedSizeLinkedList list = new FixedSizeLinkedList(3);
        list.enqueue(0);
        list.enqueue(1);
        list.enqueue(2);

        System.err.println(list.dequeue());
        System.err.println(list.dequeue());
    }

    //@Test
    public void monoThreadFullTest() {

        PressHeapChunkSpace space = new PressHeapChunkSpace(10);
        //Fill the entire cache
        for (int i = 0; i < 10; i++) {
            KChunk chunk = space.create(i, 0, 10, KChunkTypes.OBJECT_CHUNK, null);
            chunk.setFlags(KChunkFlags.DIRTY_BIT, 0);
        }
        //space.create(1, 1, 1, KChunkTypes.OBJECT_CHUNK);
        //space.create(2, 1, 1, KChunkTypes.OBJECT_CHUNK);
        //space.create(3, 1, 1, KChunkTypes.OBJECT_CHUNK);
        //space.create(4, 1, 1, KChunkTypes.OBJECT_CHUNK);

        for (int i = 0; i < 3; i++) {
            space.get(i, 0, 10).inc();
        }

        for (int i = 0; i < 8; i++) {
            space.create(i, 1, 1, KChunkTypes.OBJECT_CHUNK, null);
        }

        System.out.println(space);


    }

}
