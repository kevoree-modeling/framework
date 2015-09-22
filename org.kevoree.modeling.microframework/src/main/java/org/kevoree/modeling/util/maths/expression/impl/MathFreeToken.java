package org.kevoree.modeling.util.maths.expression.impl;

public class MathFreeToken implements MathToken {
    @Override
    public int type() {
        return 3;
    }

    private String _content;

    public MathFreeToken(String _content) {
        this._content = _content;
    }

    public String content() {
        return this._content;
    }
}
