package org.kevoree.modeling.extrapolation.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.extrapolation.Extrapolation;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.util.PrimitiveHelper;
import org.kevoree.modeling.util.maths.PolynomialFit;

public class DoublePolynomialExtrapolation implements Extrapolation {
    private static final double _TIMERR = 0.001;
    private static int _maxDegree = 20;
    private static int _maxTimeDegree = 7;

    private final static int TIMEDEG = 0;
    private final static int NUMSAMPLES = 1;
    private final static int POLYDEG = 2;
    private final static int STEP=3;
    private final static int TIMEWEIGHT=4;




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

    private int getPolyWeightIndex(KObjectChunk segment,KMetaClass meta, int index){
        return  TIMEWEIGHT+ (int) segment.getDoubleArrayElem(index, TIMEDEG, meta)+1;
    }

    private double getTime(int num, KObjectChunk segment,KMetaClass meta, int index){
        if (segment.getDoubleArraySize(index, meta) == 0) {
            return 0.0;
        }
        double result = 0;
        double power = 1;

        if (segment.getDoubleArrayElem(index, TIMEDEG, meta) == 0) {
            return segment.getDoubleArrayElem(index, TIMEWEIGHT, meta);
        }
        double t = num;
        double inferArraySize = segment.getDoubleArrayElem(index, POLYDEG, meta);
        for (int j = 0; j <= inferArraySize; j++) {
            result += segment.getDoubleArrayElem(index, (j + TIMEWEIGHT), meta) * power;
            power = power * t;
        }
        return result;
    }

    private double getLastTime(KObjectChunk segment,KMetaClass meta, int index){
        return getTime((int) segment.getDoubleArrayElem(index,NUMSAMPLES,meta)-1,segment,meta,index);
    }



        private Double extrapolateValue(KObjectChunk segment, KMetaClass meta, int index, long time, long timeOrigin) {
        if (segment.getDoubleArraySize(index, meta) == 0) {
            return 0.0;
        }
        double result = 0;
        double power = 1;
        double inferSTEP = segment.getDoubleArrayElem(index, STEP, meta);
        int polyw=getPolyWeightIndex(segment,meta,index);
        if (inferSTEP == 0) {
            return segment.getDoubleArrayElem(index, polyw, meta);
        }
        double t = (time - timeOrigin) / inferSTEP;
        double inferArraySize = segment.getDoubleArrayElem(index, POLYDEG, meta);
        for (int j = 0; j <= inferArraySize; j++) {
            result += segment.getDoubleArrayElem(index, (j + polyw), meta) * power;
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
        int timedeg = (int) raw.getDoubleArrayElem(index, TIMEDEG, metaClass);
        int deg = (int) raw.getDoubleArrayElem(index, POLYDEG, metaClass);
        int num = (int) raw.getDoubleArrayElem(index, NUMSAMPLES, metaClass);
        double maxError = maxErr(precision, deg);

        double normTime=(time - timeOrigin) / raw.getDoubleArrayElem(index, STEP, metaClass);

        //Try to check if time can get extrapolated
        if (Math.abs(getTime(num, raw, metaClass, index) - time)>_TIMERR){
            //try to increase timePolynomial
            if (timedeg < Math.min(num, _maxTimeDegree)) {
                timedeg++;
                int ss = Math.min(deg * 2, num);
                double[] times = new double[ss + 1];
                double[] values = new double[ss + 1];
                for (int i = 0; i < ss; i++) {
                    times[i] = (i * num / ss);
                    values[i] = getTime((int) times[i], raw, metaClass, index);
                }
                times[ss] = num;
                values[ss] = normTime;
                PolynomialFit pf = new PolynomialFit(timedeg);
                pf.fit(times, values);

                //add one weight to time polynomial and shift all other weights
                //TODO extend array
                //raw.extendMiddleDoubleArray(index,TIMEWEIGHT,0,metaClass);

                for (int i = 0; i < pf.getCoef().length; i++) {
                    raw.setDoubleArrayElem(index, i + TIMEWEIGHT, pf.getCoef()[i], metaClass);
                }
                raw.setDoubleArrayElem(index, TIMEDEG, timedeg, metaClass);

             /*   if (timeTempError(pf.getCoef(), times, values) > _TIMERR) {
                    return false;
                }
                else{
                }*/
            }
            else{
                return false;
            }

        }

        //If the current createModel fits well the new value, return
        if (Math.abs(extrapolateValue(raw, metaClass, index, time, timeOrigin) - value) <= maxError) {
            double nexNumSamples = raw.getDoubleArrayElem(index, NUMSAMPLES, metaClass) + 1;
            raw.setDoubleArrayElem(index, NUMSAMPLES, nexNumSamples, metaClass);
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
                times[i] = getTime(i*num/ss,raw,metaClass,index);
                values[i] = internal_extrapolate(times[i], raw, index, metaClass);
            }
            times[ss] = normTime;
            values[ss] = value;
            PolynomialFit pf = new PolynomialFit(deg);
            pf.fit(times, values);
            if (tempError(pf.getCoef(), times, values) <= maxError) {
                int pWeight=getPolyWeightIndex(raw,metaClass,index);
                raw.extendDoubleArray(index, (raw.getDoubleArraySize(index, metaClass) + 1), metaClass);
                for (int i = 0; i < pf.getCoef().length; i++) {
                    raw.setDoubleArrayElem(index, i + pWeight, pf.getCoef()[i], metaClass);
                }
                raw.setDoubleArrayElem(index, POLYDEG, deg, metaClass);
                raw.setDoubleArrayElem(index, NUMSAMPLES, num + 1, metaClass);
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
        int pWeight=getPolyWeightIndex(raw,metaClass,index);
        if (raw.getDoubleArrayElem(index, STEP, metaClass) == 0) {
            return raw.getDoubleArrayElem(index, pWeight, metaClass);
        }
        for (int j = 0; j <= raw.getDoubleArrayElem(index, POLYDEG, metaClass); j++) {
            result += raw.getDoubleArrayElem(index, (j + pWeight), metaClass) * power;
            power = power * t;
        }
        return result;
    }

    private void initial_feed(long time, double value, KObjectChunk raw, int index, KMetaClass metaClass) {
        //Create initial array of the constant elements + 1 for weights.
        raw.extendDoubleArray(index, TIMEWEIGHT + 2, metaClass); //Create N constants and 1 for the weights
        raw.setDoubleArrayElem(index, TIMEDEG, 0, metaClass); //polynomial degree of 0 for time
        raw.setDoubleArrayElem(index, NUMSAMPLES, 1, metaClass); //contains 1 sample
        raw.setDoubleArrayElem(index, POLYDEG, 0, metaClass); //polynomial degree of 0 for value
        raw.setDoubleArrayElem(index, STEP, 0, metaClass); //Number of step
        raw.setDoubleArrayElem(index, TIMEWEIGHT, 0, metaClass); //0 time weight
        raw.setDoubleArrayElem(index, TIMEWEIGHT+1, value, metaClass); //First value of the polynomial

    }


    @Override
    public void mutate(KObject current, KMetaAttribute attribute, Object payload, KInternalDataManager dataManager) {
        KObjectChunk raw = dataManager.closestChunk(current.universe(), current.now(), current.uuid(), current.metaClass(), ((AbstractKObject) current).previousResolved());
        if (raw.getDoubleArraySize(attribute.index(), current.metaClass()) == 0) {
            raw = dataManager.preciseChunk(current.universe(), current.now(), current.uuid(), current.metaClass(), ((AbstractKObject) current).previousResolved());
        }
        if (!insert(current.now(), castNumber(payload), raw.time(), raw, attribute.index(), attribute.precision(), current.metaClass())) {
            long prevTime = getLastTimeLong(raw,current.metaClass(),attribute.index())+ raw.time();
            double val = extrapolateValue(raw, current.metaClass(), attribute.index(), prevTime, raw.time());
            KObjectChunk newSegment = dataManager.preciseChunk(current.universe(), prevTime, current.uuid(), current.metaClass(), ((AbstractKObject) current).previousResolved());
            newSegment.clearDoubleArray(attribute.index(),current.metaClass());
            insert(prevTime, val, prevTime, newSegment, attribute.index(), attribute.precision(), current.metaClass());
            insert(current.now(), castNumber(payload), prevTime, newSegment, attribute.index(), attribute.precision(), current.metaClass());
        }
    }

    private long getLastTimeLong(KObjectChunk raw, KMetaClass kMetaClass, int index) {
        return (long) (getLastTime(raw,kMetaClass,index)*raw.getDoubleArrayElem(index,STEP,kMetaClass));
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

    private static DoublePolynomialExtrapolation INSTANCE;

    public static Extrapolation instance() {
        if (INSTANCE == null) {
            INSTANCE = new DoublePolynomialExtrapolation();
        }
        return INSTANCE;
    }


}
