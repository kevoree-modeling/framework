package org.kevoree.modeling.memory.chunk.impl;

import java.util.Random;

/**
 * Created by thomas on 17/02/16.
 */
public class RandomUtil {

    private static final Random random = new Random();

    public static int nextInt() {
        return random.nextInt();
    }
}
