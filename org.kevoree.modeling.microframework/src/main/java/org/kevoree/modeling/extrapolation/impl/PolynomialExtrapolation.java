package org.kevoree.modeling.extrapolation.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.extrapolation.Extrapolation;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.util.PrimitiveHelper;
import org.kevoree.modeling.util.maths.PolynomialFit;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KPrimitiveTypes;

public class PolynomialExtrapolation implements Extrapolation {

    private static int _maxDegree = 20;

    @Override
    public Object extrapolate(KObject current, KMetaAttribute attribute, KInternalDataManager dataManager) {
        KObjectChunk raw = dataManager.closestChunk(current.universe(), current.now(), current.uuid(), current.metaClass(), ((AbstractKObject) current).previousResolved());
        if (raw != null) {
            Double extrapolatedValue = extrapolateValue(raw, current.metaClass(), attribute.index(), current.now(), raw.time());
            int attTypeId = attribute.attributeTypeId();
            switch (attTypeId) {
                case KPrimitiveTypes.CONTINUOUS_ID:
                    return extrapolatedValue;
                case KPrimitiveTypes.DOUBLE_ID:
                    return extrapolatedValue;
                default:
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

    private Double extrapolateValue(KObjectChunk segment, KMetaClass meta, int index, long time, long timeOrigin) {
        if (segment.getDoubleArraySize(index, meta) == 0) {
            return 0.0;
        }
        double result = 0;
        double power = 1;
        double inferSTEP = segment.getDoubleArrayElem(index, STEP, meta);
        if (inferSTEP == 0) {
            return segment.getDoubleArrayElem(index, WEIGHTS, meta);
        }
        double t = (time - timeOrigin) / inferSTEP;
        double inferArraySize = segment.getDoubleArrayElem(index, DEGREE, meta);
        for (int j = 0; j <= inferArraySize; j++) {
            result += segment.getDoubleArrayElem(index, (j + WEIGHTS), meta) * power;
            power = power * t;
        }
        return result;
    }

    private double maxErr(double precision, int degree) {
        //double tol = precision;
    /*    if (_prioritization == Prioritization.HIGHDEGREES) {
            tol = precision / Math.pow(2, _maxDegree - degree);
        } else if (_prioritization == Prioritization.LOWDEGREES) {*/
        //double tol = precision / Math.pow(2, degree + 0.5);
       /* } else if (_prioritization == Prioritization.SAMEPRIORITY) {
            tol = precision * degree * 2 / (2 * _maxDegree);
        }*/
        return precision / Math.pow(2, degree + 0.5);
    }


    public boolean insert(long time, double value, long timeOrigin, KObjectChunk raw, int index, double precision, KMetaClass metaClass) {
        if (raw.getDoubleArraySize(index, metaClass) == 0) {
            initial_feed(time, value, raw, index, metaClass);
            return true;
        }
        //Set the step
        if (raw.getDoubleArrayElem(index, NUMSAMPLES, metaClass) == 1) {
            raw.setDoubleArrayElem(index, STEP, (time - timeOrigin), metaClass);
        }
        int deg = (int) raw.getDoubleArrayElem(index, DEGREE, metaClass);
        int num = (int) raw.getDoubleArrayElem(index, NUMSAMPLES, metaClass);
        double maxError = maxErr(precision, deg);
        //If the current createModel fits well the new value, return
        if (Math.abs(extrapolateValue(raw, metaClass, index, time, timeOrigin) - value) <= maxError) {
            double nexNumSamples = raw.getDoubleArrayElem(index, NUMSAMPLES, metaClass) + 1;
            raw.setDoubleArrayElem(index, NUMSAMPLES, nexNumSamples, metaClass);
            raw.setDoubleArrayElem(index, LASTTIME, time - timeOrigin, metaClass);
            return true;
        }
        //If not, first check if we can increase the degree
        int newMaxDegree = Math.min(num, _maxDegree);
        if (deg < newMaxDegree) {
            deg++;
            int ss = Math.min(deg * 2, num);
            double[] times = new double[ss + 1];
            double[] values = new double[ss + 1];
            double last = raw.getDoubleArrayElem(index, LASTTIME, metaClass);
            for (int i = 0; i < ss; i++) {
                times[i] = ((double) i * (last + 1)) / ss;
                values[i] = internal_extrapolate(times[i], raw, index, metaClass);
            }
            times[ss] = (time - timeOrigin) / raw.getDoubleArrayElem(index, STEP, metaClass);
            values[ss] = value;
            PolynomialFit pf = new PolynomialFit(deg);
            pf.fit(times, values);
            if (tempError(pf.getCoef(), times, values) <= maxError) {
                raw.extendDoubleArray(index, (raw.getDoubleArraySize(index, metaClass) + 1), metaClass);
                for (int i = 0; i < pf.getCoef().length; i++) {
                    raw.setDoubleArrayElem(index, i + WEIGHTS, pf.getCoef()[i], metaClass);
                }
                raw.setDoubleArrayElem(index, DEGREE, deg, metaClass);
                raw.setDoubleArrayElem(index, NUMSAMPLES, num + 1, metaClass);
                raw.setDoubleArrayElem(index, LASTTIME, time - timeOrigin, metaClass);
                return true;
            }
        }
        return false;

    }

    private double tempError(double[] computedWeights, double[] times, double[] values) {
        double maxErr = 0;
        double temp;
        for (int i = 0; i < times.length; i++) {
            temp = Math.abs(values[i] - PolynomialFit.extrapolate(times[i], computedWeights));
            if (temp > maxErr) {
                maxErr = temp;
            }
        }
        return maxErr;
    }


    private double internal_extrapolate(double t, KObjectChunk raw, int index, KMetaClass metaClass) {
        double result = 0;
        double power = 1;
        if (raw.getDoubleArrayElem(index, STEP, metaClass) == 0) {
            return raw.getDoubleArrayElem(index, WEIGHTS, metaClass);
        }
        for (int j = 0; j <= raw.getDoubleArrayElem(index, DEGREE, metaClass); j++) {
            result += raw.getDoubleArrayElem(index, (j + WEIGHTS), metaClass) * power;
            power = power * t;
        }
        return result;
    }

    private void initial_feed(long time, double value, KObjectChunk raw, int index, KMetaClass metaClass) {
        //Create initial array of the constant elements + 1 for weights.
        raw.extendDoubleArray(index, WEIGHTS + 1, metaClass); //Create N constants and 1 for the weights
        raw.setDoubleArrayElem(index, DEGREE, 0, metaClass); //polynomial degree of 0
        raw.setDoubleArrayElem(index, NUMSAMPLES, 1, metaClass); //contains 1 sample
        raw.setDoubleArrayElem(index, LASTTIME, 0, metaClass); //the last point in time is 0 = time origin
        raw.setDoubleArrayElem(index, STEP, 0, metaClass); //Number of step
        raw.setDoubleArrayElem(index, WEIGHTS, value, metaClass);
    }


    @Override
    public void mutate(KObject current, KMetaAttribute attribute, Object payload, KInternalDataManager dataManager) {
        KObjectChunk raw = dataManager.closestChunk(current.universe(), current.now(), current.uuid(), current.metaClass(), ((AbstractKObject) current).previousResolved());
        if (raw.getDoubleArraySize(attribute.index(), current.metaClass()) == 0) {
            raw = dataManager.preciseChunk(current.universe(), current.now(), current.uuid(), current.metaClass(), ((AbstractKObject) current).previousResolved());
        }
        if (!insert(current.now(), castNumber(payload), raw.time(), raw, attribute.index(), attribute.precision(), current.metaClass())) {
            long prevTime = (long) raw.getDoubleArrayElem(attribute.index(), LASTTIME, current.metaClass()) + raw.time();
            double val = extrapolateValue(raw, current.metaClass(), attribute.index(), prevTime, raw.time());
            KObjectChunk newSegment = dataManager.preciseChunk(current.universe(), prevTime, current.uuid(), current.metaClass(), ((AbstractKObject) current).previousResolved());
            newSegment.clearDoubleArray(attribute.index(), current.metaClass());
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
            return PrimitiveHelper.parseDouble(payload.toString());
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
