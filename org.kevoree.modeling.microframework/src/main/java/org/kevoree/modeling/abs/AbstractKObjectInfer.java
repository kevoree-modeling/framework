package org.kevoree.modeling.abs;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KObjectInfer;
import org.kevoree.modeling.KView;
import org.kevoree.modeling.defer.KDefer;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.meta.impl.MetaLiteral;
import org.kevoree.modeling.util.PrimitiveHelper;
import org.kevoree.modeling.util.maths.structure.KArray2D;
import org.kevoree.modeling.util.maths.structure.impl.NativeArray2D;

public class AbstractKObjectInfer extends AbstractKObject implements KObjectInfer {

    public AbstractKObjectInfer(long p_universe, long p_time, long p_uuid, KMetaClass p_metaClass, KInternalDataManager p_manager, long currentUniverse, long currentTime) {
        super(p_universe, p_time, p_uuid, p_metaClass, p_manager, currentUniverse, currentTime);
    }

    @Override
    public void genericTrain(KObject[] dependencies, Object[] expectedOutputs, KCallback callback) {
        //wrap input
        KObject[][] all_dependencies = new KObject[1][dependencies.length];
        all_dependencies[0] = dependencies;
        //wrap output
        Object[][] all_expectedOutputs = null;
        if (expectedOutputs != null) {
            all_expectedOutputs = new Object[1][expectedOutputs.length];
            all_expectedOutputs[0] = expectedOutputs;
        }
        //call the genericTrainAll method
        genericTrainAll(all_dependencies, all_expectedOutputs, callback);
    }

    @Override
    public void genericTrainAll(KObject[][] p_dependencies, Object[][] p_outputs, KCallback callback) {
        if (p_dependencies == null) {
            throw new RuntimeException("Dependencies are mandatory for KObjectInfer");
        }
        final KObjectInfer selfObject = this;
        final KView selfView = selfObject.manager().model().universe(_universe).time(_time);
        KDefer waiter = this.manager().model().defer();
        for (int i = 0; i < p_dependencies.length; i++) {
            if (p_dependencies[i].length != _metaClass.dependencies().allDependencies().length) {
                throw new RuntimeException("Bad number of arguments for allDependencies");
            }
            KObject[] loopDependencies = p_dependencies[i];
            for (int j = 0; j < _metaClass.inputs().length; j++) {
                KMetaInferInput loopInput = _metaClass.inputs()[j];
                if (PrimitiveHelper.equals(loopInput.metaName(), "this") || PrimitiveHelper.equals(loopInput.metaName(), "self")) {
                    loopInput.extractor().exec(new KObject[]{selfObject}, selfView, waiter.waitResult());
                } else {
                    KMetaDependency dependency = _metaClass.dependencies().dependencyByName(loopInput.metaName());
                    if (dependency != null) {
                        loopInput.extractor().exec(new KObject[]{loopDependencies[dependency.index()]}, selfView, waiter.waitResult());
                    } else {
                        throw new RuntimeException("Bad API definition, " + loopInput.metaName() + " isn't defined as a dependency");
                    }
                }
            }
        }
        waiter.then(new KCallback<Object[]>() {
            @Override
            public void on(Object[] results) {
                //collect output
                NativeArray2D extractedInputs = new NativeArray2D(p_dependencies.length, _metaClass.inputs().length);
                int k = 0;
                for (int i = 0; i < p_dependencies.length; i++) {
                    for (int j = 0; j < _metaClass.inputs().length; j++) {
                        Object[] extracted = (Object[]) results[k];
                        if (extracted != null && extracted.length > 0) {
                            extractedInputs.set(i, j, (double) extracted[0]);
                        }
                        k++;
                    }
                }
                NativeArray2D extractedOutputs = new NativeArray2D(1, _metaClass.outputs().length);
                for (int i = 0; i < p_dependencies.length; i++) {
                    for (int j = 0; j < _metaClass.outputs().length; j++) {
                        KMetaInferOutput metaInferOutput = _metaClass.outputs()[j];
                        Object currentOutputObject = null;
                        if (p_outputs != null) {
                            currentOutputObject = p_outputs[i][j];
                        }
                        extractedOutputs.set(i, j, internalConvertOutput(currentOutputObject, metaInferOutput));
                    }
                }
                _metaClass.inferAlg().train(extractedInputs, extractedOutputs, selfObject, _manager);
                if (callback != null) {
                    callback.on(null);
                }
            }
        });
    }

    @Override
    public void genericInfer(KObject[] dependencies, KCallback<Object[]> callback) {
        //wrap input
        KObject[][] all_dependencies = new KObject[1][dependencies.length];
        all_dependencies[0] = dependencies;
        //call the genericTrainAll method
        genericInferAll(all_dependencies, new KCallback<Object[][]>() {
            @Override
            public void on(Object[][] objects) {
                if (objects != null && objects.length > 0) {
                    callback.on(objects[0]);
                } else {
                    callback.on(null);
                }
            }
        });
    }

    @Override
    public void genericInferAll(KObject[][] p_dependencies, KCallback<Object[][]> callback) {
        if (p_dependencies == null) {
            throw new RuntimeException("Bad number of arguments for allDependencies");
        }
        final KObjectInfer selfObject = this;
        final KView selfView = selfObject.manager().model().universe(_universe).time(_time);
        KDefer waiter = this.manager().model().defer();
        for (int i = 0; i < p_dependencies.length; i++) {
            if (p_dependencies[i].length != _metaClass.dependencies().allDependencies().length) {
                throw new RuntimeException("Bad number of arguments for allDependencies");
            }
            KObject[] loopDependencies = p_dependencies[i];
            for (int j = 0; j < _metaClass.inputs().length; j++) {
                KMetaInferInput loopInput = _metaClass.inputs()[j];
                if (PrimitiveHelper.equals(loopInput.metaName(), "this") || PrimitiveHelper.equals(loopInput.metaName(), "self")) {
                    loopInput.extractor().exec(new KObject[]{selfObject}, selfView, waiter.waitResult());
                } else {
                    KMetaDependency dependency = _metaClass.dependencies().dependencyByName(loopInput.metaName());
                    if (dependency != null) {
                        loopInput.extractor().exec(new KObject[]{loopDependencies[dependency.index()]}, selfView, waiter.waitResult());
                    } else {
                        throw new RuntimeException("Bad API definition, " + loopInput.metaName() + " isn't defined as a dependency");
                    }
                }
            }
        }
        waiter.then(new KCallback<Object[]>() {
            @Override
            public void on(Object[] results) {
                //collect output
                NativeArray2D extractedInputs = new NativeArray2D(p_dependencies.length, _metaClass.inputs().length);
                int k = 0;
                for (int i = 0; i < p_dependencies.length; i++) {
                    for (int j = 0; j < _metaClass.inputs().length; j++) {
                        Object[] extracted = (Object[]) results[k];
                        if (extracted != null && extracted.length > 0) {
                            extractedInputs.set(i, j, (double) extracted[0]);
                        }
                        k++;
                    }
                }
                KArray2D extractedOutputs = _metaClass.inferAlg().infer(extractedInputs, selfObject, _manager);
                Object[][] result = new Object[extractedOutputs.rows()][extractedOutputs.columns()];
                for (int i = 0; i < extractedOutputs.rows(); i++) {
                    result[i] = new Object[extractedOutputs.columns()];
                    for (int j = 0; j < extractedOutputs.columns(); j++) {
                        result[i][j] = internalReverseOutput(extractedOutputs.get(i, j), _metaClass.outputs()[j]);
                    }
                }
                callback.on(result);
            }
        });
    }

    @Override
    public void resetLearning() {
        //TODO
        throw new RuntimeException("Not Implemented Yet!");
    }

    private double internalConvertOutput(Object output, KMetaInferOutput metaOutput) {
        if (output == null) {
            return 0;
        }
        int typeId = metaOutput.attributeTypeId();
        switch (typeId) {
            case KPrimitiveTypes.BOOL_ID:
                if ((boolean) output) {
                    return 1.0;
                } else {
                    return 0.0;
                }
            case KPrimitiveTypes.DOUBLE_ID:
                return (double) output;
            case KPrimitiveTypes.INT_ID:
                return (double) output;
            case KPrimitiveTypes.CONTINUOUS_ID:
                return (double) output;
            case KPrimitiveTypes.LONG_ID:
                return (double) output;
            case KPrimitiveTypes.STRING_ID:
                throw new RuntimeException("String are not managed yet");
            default:
                if (KPrimitiveTypes.isEnum(metaOutput.attributeTypeId())) {
                    KMetaEnum metaEnum = _manager.model().metaModel().metaTypes()[metaOutput.attributeTypeId()];
                    if (output instanceof MetaLiteral) {
                        return (double) ((MetaLiteral) output).index();
                    } else {
                        KMeta literal = metaEnum.literalByName(output.toString());
                        if (literal != null) {
                            return (double) literal.index();
                        }
                    }
                }
                return 0;
        }
    }

    private Object internalReverseOutput(double inferred, KMetaInferOutput metaOutput) {
        int typeId = metaOutput.attributeTypeId();
        switch (typeId) {
            case KPrimitiveTypes.BOOL_ID:
                if (inferred >= 0.5) {
                    return true;
                } else {
                    return false;
                }
            case KPrimitiveTypes.DOUBLE_ID:
                return inferred;
            case KPrimitiveTypes.INT_ID:
                return (int) inferred;
            case KPrimitiveTypes.CONTINUOUS_ID:
                return inferred;
            case KPrimitiveTypes.LONG_ID:
                return inferred;
            case KPrimitiveTypes.STRING_ID:
                throw new RuntimeException("String are not managed yet");
            default:
                if (KPrimitiveTypes.isEnum(metaOutput.attributeTypeId())) {
                    int ceiledInferred = math_ceil(inferred);
                    KMetaEnum metaEnum = _manager.model().metaModel().metaTypes()[metaOutput.attributeTypeId()];
                    return metaEnum.literal(ceiledInferred);
                }
                return null;

        }
    }

    /**
     * @native ts
     * return Math.round(toCeilValue);
     */
    private int math_ceil(double toCeilValue) {
        return (int) Math.round(toCeilValue);
    }


}
