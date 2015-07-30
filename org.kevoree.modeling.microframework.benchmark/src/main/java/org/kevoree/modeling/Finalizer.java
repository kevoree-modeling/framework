package org.kevoree.modeling;

import org.openjdk.jmh.annotations.Benchmark;

public class Finalizer {

    private static class ObjectWithFinalizer
    {
        @Override
        protected void finalize() throws Throwable
        {
            super.finalize();
        }
    }

    @Benchmark
    public Object createNormal()
    {
        return new Object();
    }

    @Benchmark
    public ObjectWithFinalizer createWithFinalizer()
    {
        return new ObjectWithFinalizer();
    }

}
