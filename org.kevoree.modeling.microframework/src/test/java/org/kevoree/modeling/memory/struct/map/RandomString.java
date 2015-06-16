package org.kevoree.modeling.memory.struct.map;

import java.util.Random;

/**
 * Created by duke on 09/04/15.
 */
public class RandomString {

    private String symbols = "0123456789abcdefghijklmnopqrstuvwxyz";

    private Random random = new Random();

    private int _length;

    public RandomString(int p_length) {
        this._length = p_length;
    }

    public String nextString() {
        StringBuilder buffer = new StringBuilder();
        for (int idx = 0; idx < _length; ++idx){
            buffer.append(symbols.charAt(random.nextInt(symbols.length())));
        }
        return buffer.toString();
    }
}