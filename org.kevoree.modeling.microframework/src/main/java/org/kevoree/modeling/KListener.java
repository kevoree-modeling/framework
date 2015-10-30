package org.kevoree.modeling;

public interface KListener {

    long universe();

    long[] listenObjects();

    void listen(KObject obj);

    void destroy();

    void then(KCallback updatedObjects);

}
