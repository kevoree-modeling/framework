package org.kevoree.modeling;

public interface KObjectInfer extends KObject {

    void genericTrain(KObject[] dependencies, Object[] expectedOutputs, KCallback callback);

    void genericTrainAll(KObject[][] trainingSet, Object[][] expectedResultSet, KCallback callback);

    void genericInfer(KObject[] features, KCallback<Object[]> callback);

    void genericInferAll(KObject[][] features, KCallback<Object[][]> callback);

    void resetLearning();

}
