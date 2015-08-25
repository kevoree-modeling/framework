package org.kevoree.modeling.message;

public interface KMessage {

    Integer id();

    void setID(Integer val);

    Integer type();

    void setType(Integer val);

    String operationName();

    void setOperationName(String val);

    String className();

    void setClassName(String val);

    long[] keys();

    void setKeys(long[] val);

    String[] values();

    void setValues(String[] val);

    String peer();

    void setPeer(String val);

    String json();

}
