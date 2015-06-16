package org.kevoree.modeling.extrapolation.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.extrapolation.Extrapolation;
import org.kevoree.modeling.extrapolation.impl.maths.PolynomialFitEjml;
import org.kevoree.modeling.memory.manager.AccessMode;
import org.kevoree.modeling.memory.manager.KMemorySegmentResolutionTrace;
import org.kevoree.modeling.memory.manager.impl.MemorySegmentResolutionTrace;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KPrimitiveTypes;

public class PolynomialExtrapolation implements Extrapolation {

    private static int _maxDegree = 20;

    @Override
    public Object extrapolate(KObject current, KMetaAttribute attribute) {
        KMemorySegmentResolutionTrace trace = new MemorySegmentResolutionTrace();
        KMemorySegment raw = ((AbstractKObject) current)._manager.segment(current.universe(), current.now(), current.uuid(), AccessMode.RESOLVE, current.metaClass(), trace);
        if (raw != null) {
            Double extrapolatedValue = extrapolateValue(raw, current.metaClass(), attribute.index(), current.now(), trace.getTime());
            if (attribute.attributeType() == KPrimitiveTypes.DOUBLE) {
                return extrapolatedValue;
            } else if (attribute.attributeType() == KPrimitiveTypes.LONG) {
                return extrapolatedValue.longValue();
            } else if (attribute.attributeType() == KPrimitiveTypes.FLOAT) {
                return extrapolatedValue.floatValue();
            } else if (attribute.attributeType() == KPrimitiveTypes.INT) {
                return extrapolatedValue.intValue();
            } else if (attribute.attributeType() == KPrimitiveTypes.SHORT) {
                return extrapolatedValue.shortValue();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    //Encoded polynomial: Degree, Number of samples, step, last time, and list of weights
    private final static int DEGREE = 0;
    private final static int NUMSAMPLES = 1;
    private final static int STEP = 2;
    private final static int LASTTIME = 3;
    private final static int WEIGHTS = 4;

    private Double extrapolateValue(KMemorySegment segment, KMetaClass meta, int index, long time, long timeOrigin) {
        if (segment.getInferSize(index, meta) == 0) {
            return 0.0;
        }
        double result = 0;
        double power = 1;
        double inferSTEP = segment.getInferElem(index, STEP, meta);
        if (inferSTEP == 0) {
            return segment.getInferElem(index, WEIGHTS, meta);
        }
        double t = (time - timeOrigin) / inferSTEP;
        for (int j = 0; j <= segment.getInferElem(index, DEGREE, meta); j++) {
            result += segment.getInferElem(index, (j + WEIGHTS), meta) * power;
            power = power * t;
        }
        return result;
    }

    private double maxErr(double precision, int degree) {
        double tol = precision;
    /*    if (_prioritization == Prioritization.HIGHDEGREES) {
            tol = precision / Math.pow(2, _maxDegree - degree);
        } else if (_prioritization == Prioritization.LOWDEGREES) {*/
        tol = precision / Math.pow(2, degree + 0.5);
       /* } else if (_prioritization == Prioritization.SAMEPRIORITY) {
            tol = precision * degree * 2 / (2 * _maxDegree);
        }*/
        return tol;
    }


    public boolean insert(long time, double value, long timeOrigin, KMemorySegment raw, int index, double precision, KMetaClass metaClass) {
        if (raw.getInferSize(index, metaClass) == 0) {
            initial_feed(time, value, raw, index, metaClass);
            return true;
        }
        //Set the step
        if (raw.getInferElem(index, NUMSAMPLES, metaClass) == 1) {
            raw.setInferElem(index, STEP, (time - timeOrigin), metaClass);
        }
        int deg = (int) raw.getInferElem(index,DEGREE,metaClass);
        int num = (int) raw.getInferElem(index, NUMSAMPLES, metaClass);
        double maxError = maxErr(precision, deg);
        //If the current model fits well the new value, return
        if (Math.abs(extrapolateValue(raw, metaClass, index, time, timeOrigin) - value) <= maxError) {
            double nexNumSamples = raw.getInferElem(index, NUMSAMPLES, metaClass) + 1;
            raw.setInferElem(index, NUMSAMPLES, nexNumSamples, metaClass);
            raw.setInferElem(index, LASTTIME, time - timeOrigin, metaClass);
            return true;
        }
        //If not, first check if we can increase the degree
        int newMaxDegree = Math.min(num, _maxDegree);
        if (deg < newMaxDegree) {
            deg++;
            int ss = Math.min(deg * 2, num);
            double[] times = new double[ss + 1];
            double[] values = new double[ss + 1];
            for (int i = 0; i < ss; i++) {
                times[i] = ((double) i * num * (raw.getInferElem(index, LASTTIME, metaClass)) / (ss * raw.getInferElem(index, STEP, metaClass)));
                values[i] = internal_extrapolate(times[i], raw, index, metaClass);
            }
            times[ss] = (time - timeOrigin) / raw.getInferElem(index, STEP, metaClass);
            values[ss] = value;
            PolynomialFitEjml pf = new PolynomialFitEjml(deg);
            pf.fit(times, values);
            if (tempError(pf.getCoef(), times, values) <= maxError) {
                raw.extendInfer(index, (raw.getInferSize(index, metaClass) + 1), metaClass);
                for (int i = 0; i < pf.getCoef().length; i++) {
                    raw.setInferElem(index, i + WEIGHTS, pf.getCoef()[i], metaClass);
                }
                raw.setInferElem(index, DEGREE, deg, metaClass);
                raw.setInferElem(index, NUMSAMPLES, num + 1, metaClass);
                raw.setInferElem(index, LASTTIME, time - timeOrigin, metaClass);
                return true;
            }
        }
        return false;

    }

    private double tempError(double[] computedWeights, double[] times, double[] values) {
        double maxErr = 0;
        double temp;
        double ds;
        for (int i = 0; i < times.length; i++) {
            temp = Math.abs(values[i] - test_extrapolate(times[i], computedWeights));
            if (temp > maxErr) {
                maxErr = temp;
            }
        }
        return maxErr;
    }

    private double test_extrapolate(double time, double[] weights) {
        double result = 0;
        double power = 1;
        for (int j = 0; j < weights.length; j++) {
            result += weights[j] * power;
            power = power * time;
        }
        return result;
    }

    private double internal_extrapolate(double t, KMemorySegment raw, int index, KMetaClass metaClass) {
        double result = 0;
        double power = 1;
        if (raw.getInferElem(index, STEP, metaClass) == 0) {
            return raw.getInferElem(index, WEIGHTS, metaClass);
        }
        for (int j = 0; j <= raw.getInferElem(index, DEGREE, metaClass); j++) {
            result += raw.getInferElem(index, (j + WEIGHTS), metaClass) * power;
            power = power * t;
        }
        return result;
    }

    private void initial_feed(long time, double value, KMemorySegment raw, int index, KMetaClass metaClass) {
        //Create initial array of the constant elements + 1 for weights.

        raw.extendInfer(index, WEIGHTS + 1, metaClass); //Create N constants and 1 for the weights
        raw.setInferElem(index, DEGREE, 0, metaClass); //polynomial degree of 0
        raw.setInferElem(index, NUMSAMPLES, 1, metaClass); //contains 1 sample
        raw.setInferElem(index, LASTTIME, 0, metaClass); //the last point in time is 0 = time origin
        raw.setInferElem(index, STEP, 0, metaClass); //Number of step
        raw.setInferElem(index, WEIGHTS, value, metaClass);
    }


    @Override
    public void mutate(KObject current, KMetaAttribute attribute, Object payload) {
        KMemorySegmentResolutionTrace trace = new MemorySegmentResolutionTrace();
        KMemorySegment raw = current.manager().segment(current.universe(), current.now(), current.uuid(), AccessMode.RESOLVE, current.metaClass(), trace);
        if (raw.getInferSize(attribute.index(), current.metaClass()) == 0) {
            raw = current.manager().segment(current.universe(), current.now(), current.uuid(), AccessMode.NEW, current.metaClass(), null);
        }
        if (!insert(current.now(), castNumber(payload), trace.getTime(), raw, attribute.index(), attribute.precision(), current.metaClass())) {
            long prevTime = (long) raw.getInferElem(attribute.index(), LASTTIME, current.metaClass()) + trace.getTime();
            double val = extrapolateValue(raw, current.metaClass(), attribute.index(), prevTime, trace.getTime());
            KMemorySegment newSegment = current.manager().segment(current.universe(), prevTime, current.uuid(), AccessMode.NEW, current.metaClass(), null);
            insert(prevTime, val, prevTime, newSegment, attribute.index(), attribute.precision(), current.metaClass());
            insert(current.now(), castNumber(payload), prevTime, newSegment, attribute.index(), attribute.precision(), current.metaClass());
        }
    }

    /**
     * @native ts
     * return +payload;
     */
    private Double castNumber(Object payload) {
        if (payload instanceof Double) {
            return (Double) payload;
        } else {
            return Double.parseDouble(payload.toString());
        }
    }

    private static PolynomialExtrapolation INSTANCE;

    public static Extrapolation instance() {
        if (INSTANCE == null) {
            INSTANCE = new PolynomialExtrapolation();
        }
        return INSTANCE;
    }


}
