package org.kevoree.modeling;

public interface KOperation<SourceObject extends KObject, ResultType> {

    void on(SourceObject source, Object[] params, KCallback<ResultType> result);

}
