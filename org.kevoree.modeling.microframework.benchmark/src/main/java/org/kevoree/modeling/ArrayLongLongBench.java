package org.kevoree.modeling;

import org.kevoree.modeling.memory.chunk.impl.ArrayLongLongMap;

public class ArrayLongLongBench {

    public static void main(String[] args) {

        int nb = 10000000;

        /*
        HashMap<Long, Long> op_map_ref = new HashMap<Long, Long>();
        ArrayLongLongMap op_map = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        int nb = 10000000;
        Random random = new Random();
        ExecutorService service = Executors.newFixedThreadPool(1000);
        for (long i = 0; i < nb; i++) {
            final long key = random.nextLong();
            final long value = random.nextLong();
            op_map_ref.put(key, value);

            final long finalI = i;
            final ArrayLongLongMap finalOp_map = op_map;
            service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        finalOp_map.put(key, value);
                    } catch (Exception e){
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
        if (op_map_ref.size() != op_map.size()) {
            throw new RuntimeException("WTF SIZE != " + op_map_ref.size() + "-" + op_map.size());
        }
        for (Long key : op_map_ref.keySet()) {
            long resolved = op_map.get(key);
            long resolved2 = op_map_ref.get(key);
            if (resolved != resolved2) {
                throw new RuntimeException("WTF " + resolved2 + "-" + resolved);
            }
        }*/


        System.err.println("Equiality Test passed");

        long time = 0;
        for (int j = 0; j < 30; j++) {
            long before = System.currentTimeMillis();
            // HashMap<Long, Long> op_map2 = new HashMap<Long, Long>();
            ArrayLongLongMap op_map2 = new ArrayLongLongMap(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
            nb = 10000000;
            for (long i = 0; i < nb; i++) {
                op_map2.put(i, i);
            }
            for (long i = 0; i < nb; i++) {
                long resolved = op_map2.get(i);
                if (resolved != i) {
                    throw new RuntimeException("WTF " + i + "-" + resolved);
                }
            }
            if (j >= 10) {
                time += System.currentTimeMillis() - before;
            }
        }
        System.err.println((time / 20) + "ms");
    }

}
