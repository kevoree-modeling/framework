package org.kevoree.modeling.test.datastore;

import org.kevoree.modeling.KCallback;

/**
 * Created by gregory.nain on 10/11/14.
 */
public class Utils {

    public static KCallback<Throwable> DefaultPrintStackTraceCallback = err->{
        if(err != null) {
            err.printStackTrace();
        }
    };


}
