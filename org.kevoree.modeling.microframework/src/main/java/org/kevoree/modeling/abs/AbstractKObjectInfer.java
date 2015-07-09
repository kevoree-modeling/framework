package org.kevoree.modeling.abs;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KObjectInfer;
import org.kevoree.modeling.defer.KDefer;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.traversal.KTraversalIndexResolver;

public class AbstractKObjectInfer extends AbstractKObject implements KObjectInfer {

    public AbstractKObjectInfer(long p_universe, long p_time, long p_uuid, KMetaClass p_metaClass, KMemoryManager p_manager) {
        super(p_universe, p_time, p_uuid, p_metaClass, p_manager);
    }

    @Override
    public void train(KObject[] dependencies, Object[] expectedOutputs, KCallback callback) {
        final KObjectInfer selfObject = this;
        if (dependencies.length != _metaClass.dependencies().dependencies().length || expectedOutputs.length != _metaClass.outputs().length) {
            throw new RuntimeException("Bad number of arguments for dependencies");
        }
        KDefer waiter = this.manager().model().defer();
        KTraversalIndexResolver resolver = new KTraversalIndexResolver() {
            @Override
            public KObject[] resolve(String indexName) {
                KMetaDependency dependency = _metaClass.dependencies().dependencyByName(indexName);
                if(dependency != null){
                    KObject[] single = new KObject[1];
                    single[0] = dependencies[dependency.index()];
                    return single;
                }
                return null;
            }
        };
        for (int i = 0; i < _metaClass.inputs().length; i++) {
            _metaClass.inputs()[i].extractor().exec(null, resolver, waiter.wait(""+i));
        }
        waiter.then(new KCallback() {
            @Override
            public void on(Object o) {
                double[][] extractedInputs = new double[1][_metaClass.inputs().length];
                for (int i = 0; i < _metaClass.inputs().length; i++) {
                    try {
                        extractedInputs[0][i] = (double) waiter.getResult(""+i);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                double[][] extractedOutputs = new double[1][_metaClass.outputs().length];
                for (int i = 0; i < _metaClass.outputs().length; i++) {
                    KMetaInferOutput metaInferOutput = _metaClass.outputs()[i];
                    extractedOutputs[0][i] = internalConvertOutput(expectedOutputs[i], metaInferOutput);
                }
                _metaClass.inferAlg().train(extractedInputs, extractedOutputs, selfObject);
                if (callback != null) {
                    callback.on(null);
                }
            }
        });
    }

    private double internalConvertOutput(Object output, KMetaInferOutput metaOutput) {
        if (metaOutput.type().equals(KPrimitiveTypes.BOOL)) {
            if (output.equals(true)) {
                return 1.0;
            } else {
                return 0.0;
            }
        } else {
            //TODO implement here all the case study
            //default case
            return 0;
        }
    }

    @Override
    public void trainAll(KObject[][] p_trainingSet, Object[][] p_expectedResultSet, KCallback callback) {
        //if(p_trainingSet)
    }

    @Override
    public void infer(KObject[] features, KCallback<Object[]> callback) {
        //TODO
    }

    @Override
    public void inferAll(KObject[][] features, KCallback<Object[][]> callback) {

    }

    @Override
    public void resetLearning() {
        //TODO
        throw new RuntimeException("Not Implemented Yet!");
    }

}
