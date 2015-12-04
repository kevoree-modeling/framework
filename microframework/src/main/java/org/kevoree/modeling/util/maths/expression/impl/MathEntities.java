package org.kevoree.modeling.util.maths.expression.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.chunk.KStringMap;
import org.kevoree.modeling.memory.chunk.impl.ArrayStringMap;

public class MathEntities {

    private static MathEntities INSTANCE = null;

    public static MathEntities getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new MathEntities();
        }
        return INSTANCE;
    }

    public KStringMap<MathOperation> operators;
    public KStringMap<MathFunction> functions;

    private MathEntities() {
        operators = new ArrayStringMap<MathOperation>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        operators.put("+", new MathOperation("+", 20, true));
        operators.put("-", new MathOperation("-", 20, true));
        operators.put("*", new MathOperation("*", 30, true));
        operators.put("/", new MathOperation("/", 30, true));
        operators.put("%", new MathOperation("%", 30, true));
        operators.put("^", new MathOperation("^", 40, false));

        operators.put("&&", new MathOperation("&&", 4, false));
        operators.put("||", new MathOperation("||", 2, false));
        operators.put(">", new MathOperation(">", 10, false));
        operators.put(">=", new MathOperation(">=", 10, false));
        operators.put("<", new MathOperation("<", 10, false));
        operators.put("<=", new MathOperation("<=", 10, false));
        operators.put("==", new MathOperation("==", 7, false));
        operators.put("!=", new MathOperation("!=", 7, false));

        functions = new ArrayStringMap<MathFunction>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        functions.put("NOT", new MathFunction("NOT", 1));
        functions.put("IF", new MathFunction("IF", 3));
        functions.put("RAND", new MathFunction("RAND", 0));
        functions.put("SIN", new MathFunction("SIN", 1));
        functions.put("COS", new MathFunction("COS", 1));
        functions.put("TAN", new MathFunction("TAN", 1));
        functions.put("ASIN", new MathFunction("ASIN", 1));
        functions.put("ACOS", new MathFunction("ACOS", 1));
        functions.put("ATAN", new MathFunction("ATAN", 1));
        functions.put("MAX", new MathFunction("MAX", 2));
        functions.put("MIN", new MathFunction("MIN", 2));
        functions.put("ABS", new MathFunction("ABS", 1));
        functions.put("LOG", new MathFunction("LOG", 1));
        functions.put("ROUND", new MathFunction("ROUND", 2));
        functions.put("FLOOR", new MathFunction("FLOOR", 1));
        functions.put("CEILING", new MathFunction("CEILING", 1));
        functions.put("SQRT", new MathFunction("SQRT", 1));
        functions.put("SECONDS", new MathFunction("SECONDS", 1));
        functions.put("MINUTES", new MathFunction("MINUTES", 1));
        functions.put("HOURS", new MathFunction("HOURS", 1));
        functions.put("DAY", new MathFunction("DAY", 1));
        functions.put("MONTH", new MathFunction("MONTH", 1));
        functions.put("YEAR", new MathFunction("YEAR", 1));
        functions.put("DAYOFWEEK", new MathFunction("DAYOFWEEK", 1));

    }

}
