package org.kevoree.modeling;

import org.kevoree.modeling.memory.chunk.impl.ArrayLongLongTree;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ArrayLongLongTreeBenchmark {

    public static void main(String[] args) {
        ArrayLongLongTree tree = new ArrayLongLongTree();
        HashMap<Long, Long> referee = new HashMap<Long, Long>();
        Random random = new Random();
        int nb = 10000000;
        ExecutorService service = Executors.newFixedThreadPool(1000);
        for (long i = 0; i < nb; i++) {
            final long key = random.nextLong();
            final long value = random.nextLong();
            referee.put(key, value);
            service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        tree.insert(key, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
        service.shutdown();
        try {
            service.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (tree.size() != referee.size()) {
            throw new RuntimeException("Size Are not equivalent " + tree.size() + "!=" + referee.size());
        }
        for (Long key : referee.keySet()) {
            long resolvedReferee = referee.get(key);
            long resolvedTree = tree.lookupValue(key);
            if (resolvedReferee != resolvedTree) {
                throw new RuntimeException("Value Are different for key " + key);
            }
        }

    }

}
