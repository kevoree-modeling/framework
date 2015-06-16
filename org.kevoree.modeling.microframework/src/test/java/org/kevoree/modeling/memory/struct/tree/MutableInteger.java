package org.kevoree.modeling.memory.struct.tree;

/**
 * Created by duke on 26/05/15.
 */
public class MutableInteger {

    private int v = -1;

    public int get() {
        return v;
    }

    public void set(int newV) {
        v = newV;
    }

    public void increment(){
        v++;
    }

}
