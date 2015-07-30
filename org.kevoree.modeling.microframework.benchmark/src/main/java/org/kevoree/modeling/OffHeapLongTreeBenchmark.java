package org.kevoree.modeling;

import org.kevoree.modeling.memory.tree.KLongTree;
import org.kevoree.modeling.memory.tree.impl.OffHeapLongTree;
import org.openjdk.jmh.annotations.Benchmark;

public class OffHeapLongTreeBenchmark {

    public KLongTree createKLongTree() {
        return new OffHeapLongTree();
    }

    @Benchmark
    public void test() throws Exception {
        KLongTree tree = createKLongTree();
        tree.init(null, null);

        for (long i = 0; i <= 10000; i++) {
            tree.insert(i);
        }
    }

}
