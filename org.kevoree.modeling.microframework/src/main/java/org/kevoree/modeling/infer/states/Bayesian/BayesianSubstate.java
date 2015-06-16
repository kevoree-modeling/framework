package org.kevoree.modeling.infer.states.Bayesian;

/**
 * Created by assaad on 17/02/15.
 */
public abstract class BayesianSubstate {
    public abstract double calculateProbability(Object feature);

    public abstract void train(Object feature);

    public abstract String save(String separator);

    public abstract void load(String payload, String separator);

    public abstract BayesianSubstate cloneState();
}
