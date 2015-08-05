package org.kevoree.modeling;

import org.kevoree.modeling.memory.chunk.KLongTree;
import org.kevoree.modeling.memory.chunk.impl.OffHeapLongTree;
import org.openjdk.jmh.annotations.Benchmark;

public class OffHeapLongTreeBenchmark {

    public KLongTree createKLongTree() {
        return new OffHeapLongTree(null,-1,-1,-1);
    }

    @Benchmark
    public void test() throws Exception {
        KLongTree tree = createKLongTree();
        tree.init(null,null,-1);

        for (long i = 0; i <= 10000; i++) {
            tree.insert(i);
        }
    }

}
