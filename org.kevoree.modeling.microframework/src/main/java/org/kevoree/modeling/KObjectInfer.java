package org.kevoree.modeling;

public interface KObjectInfer extends KObject {

    void train(KObject[] dependencies, Object[] expectedOutputs, KCallback callback);

    void trainAll(KObject[][] trainingSet, Object[][] expectedResultSet, KCallback callback);

    void infer(KObject[] features, KCallback<Object[]> callback);

    void inferAll(KObject[][] features, KCallback<Object[][]> callback);

    void resetLearning();

}
