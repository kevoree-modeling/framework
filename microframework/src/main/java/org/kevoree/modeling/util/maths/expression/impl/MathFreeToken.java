package org.kevoree.modeling.util.maths.expression.impl;

public class MathFreeToken implements MathToken {

    private String _content;

    public MathFreeToken(String content) {
        this._content = content;
    }

    public String content() {
        return this._content;
    }

    @Override
    public int type() {
        return 3;
    }

}
