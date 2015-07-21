package org.kevoree.modeling;

public interface KListener {

    long universe();

    long[] listenObjects();

    void listen(KObject obj);

    void delete();

    void then(KCallback<KObject[]> updatedObjects);

}
