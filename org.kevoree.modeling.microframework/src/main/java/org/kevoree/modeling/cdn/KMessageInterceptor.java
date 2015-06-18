package org.kevoree.modeling.cdn;

import org.kevoree.modeling.message.KMessage;

public interface KMessageInterceptor {

    boolean on(KMessage msg);

}
