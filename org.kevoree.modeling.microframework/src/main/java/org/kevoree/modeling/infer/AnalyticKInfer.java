package org.kevoree.modeling.infer;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.infer.states.AnalyticKInferState;
import org.kevoree.modeling.meta.KMetaClass;

/**
 * This class is a basic live learning of the average of a field.
 * Training this class does not take any features.
 * it takes only results (the field to be learned).
 * The state is an accumulator (Sum of values) and a counter (number of valeus).
 * The prediction is a a fixed result of the average value learned.
 * It is calculated by dividing the accumulator by the counter;
 * Created by assaad on 10/02/15.
 */
public class AnalyticKInfer {

    /*
    @Override
    public void train(Object[][] trainingSet, Object[] expectedResultSet, KCallback<Throwable> callback) {
        AnalyticKInferState currentState = (AnalyticKInferState) modifyState();

        for (int i = 0; i < expectedResultSet.length; i++) {
            double value=Double.parseDouble(expectedResultSet[i].toString());
            currentState.train(value);
        }
    }

    @Override
    public Object infer(Object[] features) {
        AnalyticKInferState currentState = (AnalyticKInferState) readOnlyState();
        return currentState.getAverage();
    }

    @Override
    public Object accuracy(Object[][] testSet, Object[] expectedResultSet) {
        return null;
    }

    @Override
    public void clear() {
        AnalyticKInferState currentState = (AnalyticKInferState) modifyState();
        currentState.clear();
    }

    @Override
    public KInferState createEmptyState() {
        return new AnalyticKInferState();
    }*/

}
