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

    private KTraversalIndexResolver dependenciesResolver(KObject[] dependencies){
        return new KTraversalIndexResolver() {
            @Override
            public KObject[] resolve(String indexName) {
                KMetaDependency dependency = _metaClass.dependencies().dependencyByName(indexName);
                if (dependency != null) {
                    KObject[] single = new KObject[1];
                    single[0] = dependencies[dependency.index()];
                    return single;
                }
                return null;
            }
        };
    }

    @Override
    public void train(KObject[] dependencies, Object[] expectedOutputs, KCallback callback) {
        //wrap input
        KObject[][] all_dependencies = new KObject[1][dependencies.length];
        all_dependencies[0] = dependencies;
        //wrap output
        Object[][] all_expectedOutputs;
        if(expectedOutputs != null){
            all_expectedOutputs = new Object[1][expectedOutputs.length];
            all_expectedOutputs[0] = expectedOutputs;
        }
        //call the trainAll method
        trainAll(all_dependencies, all_dependencies, callback);
    }

    @Override
    public void trainAll(KObject[][] p_dependencies, Object[][] p_outputs, KCallback callback) {
        if(p_dependencies == null){
            throw new RuntimeException("Dependencies are mandatory for KObjectInfer");
        }
        final KObjectInfer selfObject = this;
        KDefer waiter = this.manager().model().defer();
        for(int i=0;i<p_dependencies.length;i++){
            if (p_dependencies[i].length != _metaClass.dependencies().allDependencies().length) {
                throw new RuntimeException("Bad number of arguments for allDependencies");
            }
            KTraversalIndexResolver resolver = dependenciesResolver(p_dependencies[i]);
            for (int j = 0; j < _metaClass.inputs().length; j++) {
                _metaClass.inputs()[j].extractor().exec(null, resolver, waiter.wait(i+","+j));
            }
        }
        waiter.then(new KCallback() {
            @Override
            public void on(Object o) {
                //collect output
                double[][] extractedInputs = new double[p_dependencies.length][_metaClass.inputs().length];
                for (int i = 0; i < p_dependencies.length; i++) {
                    for (int j = 0; j < _metaClass.inputs().length; j++) {
                        try {
                            Object[] extracted = (Object[]) waiter.getResult(i + "," + j);
                            if (extracted != null && extracted.length > 0) {
                                extractedInputs[i][j] = (double) extracted[0];
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                double[][] extractedOutputs = new double[1][_metaClass.outputs().length];
                for (int i = 0; i < p_dependencies.length; i++) {
                    for (int j = 0; j < _metaClass.outputs().length; j++) {
                        KMetaInferOutput metaInferOutput = _metaClass.outputs()[j];
                        Object currentOutputObject = null;
                        if (p_outputs != null) {
                            currentOutputObject = p_outputs[i][j];
                        }
                        extractedOutputs[i][j] = internalConvertOutput(currentOutputObject, metaInferOutput);
                    }
                }
                _metaClass.inferAlg().train(extractedInputs, extractedOutputs, selfObject);
                if (callback != null) {
                    callback.on(null);
                }
            }
        });
    }

    @Override
    public void infer(KObject[] dependencies, KCallback<Object[]> callback) {
        final KObjectInfer selfObject = this;
        if (dependencies == null || dependencies.length != _metaClass.dependencies().allDependencies().length) {
            throw new RuntimeException("Bad number of arguments for allDependencies");
        }
        KTraversalIndexResolver resolver = dependenciesResolver(dependencies);
        KDefer waiter = this.manager().model().defer();
        for (int i = 0; i < _metaClass.inputs().length; i++) {
            _metaClass.inputs()[i].extractor().exec(null, resolver, waiter.wait("" + i));
        }
        waiter.then(new KCallback() {
            @Override
            public void on(Object o) {
                double[][] extractedInputs = new double[1][_metaClass.inputs().length];
                for (int i = 0; i < _metaClass.inputs().length; i++) {
                    try {
                        Object[] extracted = (Object[]) waiter.getResult("" + i);
                        if (extracted != null && extracted.length > 0) {
                            extractedInputs[0][i] = (double) extracted[0];
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                double[][] extractedOutputs = _metaClass.inferAlg().infer(extractedInputs, selfObject);
                if (extractedOutputs[0].length != _metaClass.outputs().length) {
                    callback.on(null);
                } else {
                    Object[] result = new Object[extractedOutputs.length];
                    for (int i = 0; i < extractedOutputs.length; i++) {
                        result[i] = internalReverseOutput(extractedOutputs[0][i], _metaClass.outputs()[i]);
                    }
                    callback.on(result);
                }
            }
        });
    }

    @Override
    public void inferAll(KObject[][] features, KCallback<Object[][]> callback) {

    }

    @Override
    public void resetLearning() {
        //TODO
        throw new RuntimeException("Not Implemented Yet!");
    }

    private double internalConvertOutput(Object output, KMetaInferOutput metaOutput) {
        if (metaOutput.type() == KPrimitiveTypes.BOOL) {
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

    private Object internalReverseOutput(double inferred, KMetaInferOutput metaOutput) {
        return null;
    }

}
