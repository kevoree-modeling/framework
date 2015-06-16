package org.kevoree.modeling.defer;

import org.kevoree.modeling.KCallback;

public interface KDefer {

    KCallback wait(String resultName);

    KDefer waitDefer(KDefer previous);

    boolean isDone();

    Object getResult(String resultName) throws Exception;

    /**
     * @ignore ts
     */
    <A> A getResult(String resultName, Class<A> casted) throws Exception;

    void then(KCallback cb);

    KDefer next();

}
