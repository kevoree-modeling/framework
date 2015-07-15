package org.kevoree.modeling;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.util.maths.Base64;
import org.openjdk.jmh.annotations.Benchmark;

public class Base64BenchmarkTest {

    @Benchmark
    public void parseLongTest() {
        String hello = "prefix/" + KConfig.END_OF_TIME;
        long sum = 0;
        for (int i = 0; i < 1000; i++) {
            long parsed = Long.parseLong(hello.substring(7, hello.length()));
            sum += parsed;
        }
    }

    @Benchmark
    public void base64longTest() {
        String hello = "prefix/" + Base64.encodeLong(KConfig.END_OF_TIME);
        long sum = 0;
        for (int i = 0; i < 1000; i++) {
            long parsed = Base64.decodeToLongWithBounds(hello, 7, hello.length());
            sum += parsed;
        }
    }

}
