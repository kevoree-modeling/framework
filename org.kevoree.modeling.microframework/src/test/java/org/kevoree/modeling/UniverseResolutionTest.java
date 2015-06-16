package org.kevoree.modeling;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.memory.manager.impl.ResolutionHelper;
import org.kevoree.modeling.memory.struct.map.KLongLongMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;

/**
 * Created by duke on 03/03/15.
 */
public class UniverseResolutionTest {

    @Test
    public void test() {

        //create a universeTree
        KLongLongMap globalUniverse = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        //root
        globalUniverse.put(0, 0);
        //branch 0 -> 1 -> 3
        globalUniverse.put(1, 0);
        globalUniverse.put(3, 1);
        //branch 0 -> 2 -> 4
        globalUniverse.put(2, 0);
        globalUniverse.put(4, 2);

        KLongLongMap objectUniverse = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        objectUniverse.put(0, 0);
        objectUniverse.put(3, 10);
        objectUniverse.put(2, 8);

        //test branch 0 -> 1 -> 3
        Assert.assertEquals(0, ResolutionHelper.resolve_universe(globalUniverse, objectUniverse, 0, 0));
        Assert.assertEquals(3, ResolutionHelper.resolve_universe(globalUniverse, objectUniverse, 10, 3));
        Assert.assertEquals(3, ResolutionHelper.resolve_universe(globalUniverse, objectUniverse, 50, 3));
        //1 has no modification
        Assert.assertEquals(0, ResolutionHelper.resolve_universe(globalUniverse, objectUniverse, 10, 1));

        //test branch 0 -> 2 -> 4
        Assert.assertEquals(2, ResolutionHelper.resolve_universe(globalUniverse, objectUniverse, 10, 2));
        Assert.assertEquals(0, ResolutionHelper.resolve_universe(globalUniverse, objectUniverse, 1, 2));
        Assert.assertEquals(2, ResolutionHelper.resolve_universe(globalUniverse, objectUniverse, 50, 4));
        Assert.assertEquals(0, ResolutionHelper.resolve_universe(globalUniverse, objectUniverse, 5, 4));

        //Now simulate a hidden evolution

    }

    @Test
    public void testRange() {

        //create a universeTree
        KLongLongMap globalUniverse = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        //root
        globalUniverse.put(0, 0);
        //branch 0 -> 1 -> 3
        globalUniverse.put(1, 0);
        globalUniverse.put(3, 1);
        //branch 0 -> 2 -> 4
        globalUniverse.put(2, 0);
        globalUniverse.put(4, 2);

        KLongLongMap objectUniverse = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        objectUniverse.put(0, 0);
        objectUniverse.put(3, 10);
        objectUniverse.put(2, 8);

        //full resolution branch 0->2->4
        long[] collected = ResolutionHelper.universeSelectByRange(globalUniverse,objectUniverse,KConfig.BEGINNING_OF_TIME,KConfig.END_OF_TIME,4);
        Assert.assertEquals(collected[0],2);
        Assert.assertEquals(collected[1],0);

        //full resolution branch 0->1->3
        collected = ResolutionHelper.universeSelectByRange(globalUniverse,objectUniverse,KConfig.BEGINNING_OF_TIME,KConfig.END_OF_TIME,3);
        Assert.assertEquals(collected[0],3);
        Assert.assertEquals(collected[1],0);

        //mid resolution branch 0->2->4
        collected = ResolutionHelper.universeSelectByRange(globalUniverse,objectUniverse,8,KConfig.END_OF_TIME,4);
        Assert.assertEquals(collected[0],2);

        collected = ResolutionHelper.universeSelectByRange(globalUniverse,objectUniverse,7,KConfig.END_OF_TIME,4);
        Assert.assertEquals(collected[0],2);
        Assert.assertEquals(collected[1],0);

        collected = ResolutionHelper.universeSelectByRange(globalUniverse,objectUniverse,8,9,4);
        Assert.assertEquals(collected[0],2);

        collected = ResolutionHelper.universeSelectByRange(globalUniverse,objectUniverse,8,9,3);
        Assert.assertEquals(collected[0],0);

        collected = ResolutionHelper.universeSelectByRange(globalUniverse,objectUniverse,-3,11,3);
        Assert.assertEquals(collected[0],3);
        Assert.assertEquals(collected[1],0);

    }

}
