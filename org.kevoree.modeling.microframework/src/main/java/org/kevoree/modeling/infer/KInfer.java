package org.kevoree.modeling.infer;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;

/**
 * TODO consider some strategy to avoid the boxing
 */
public interface KInfer extends KObject {

    /**
     * @param expectedResultSet can be null in case of unsuperviseLearning
     * @param trainingSet       represent the training set, the first array represent the samples, the second array represent the features
     */
    void train(Object[][] trainingSet, Object[] expectedResultSet, KCallback<Throwable> callback);

    Object infer(Object[] features);

    Object accuracy(Object[][] testSet, Object[] expectedResultSet);

    void clear();

}
