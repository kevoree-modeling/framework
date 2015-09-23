package org.kevoree.modeling.util.maths.expression.impl;

public class MathDoubleToken implements MathToken {
    @Override
    public int type() {
        return 2;
    }

    private double _content;

    public MathDoubleToken(double _content) {
        this._content = _content;
    }

    public double content() {
        return this._content;
    }

    @Override
    public int type2() {
        return 0;
    }

}
