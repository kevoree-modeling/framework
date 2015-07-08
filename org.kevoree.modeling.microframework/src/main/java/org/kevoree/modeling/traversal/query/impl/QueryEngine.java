package org.kevoree.modeling.traversal.query.impl;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.traversal.KTraversal;
import org.kevoree.modeling.traversal.impl.Traversal;
import org.kevoree.modeling.traversal.query.KQueryEngine;

public class QueryEngine implements KQueryEngine {

    private static KQueryEngine INSTANCE = null;

    public static KQueryEngine getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new QueryEngine();
        }
        return INSTANCE;
    }

    public static final char OPEN_BRACKET = '[';
    public static final char CLOSE_BRACKET = ']';
    public static final char PIPE_SEP = '|';


    @Override
    public void eval(String query, KObject[] origins, KCallback<Object[]> callback) {
        if (callback != null) {
            internal_eval(query, origins, null, callback);
        }
    }

    @Override
    public void traverse(String query, KObject[] origins, KCallback<KObject[]> callback) {
        if (callback != null) {
            internal_eval(query, origins, callback, null);
        }
    }

    private void internal_eval(String query, KObject[] origins, KCallback<KObject[]> callbackKObject, KCallback<Object[]> callbackGenericObject) {
        if (query == null || query.length() == 0) {
            if (callbackKObject != null) {
                callbackKObject.on(new KObject[0]);
            }
            if (callbackGenericObject != null) {
                callbackGenericObject.on(new Object[0]);
            }
        } else {
            KTraversal traversal = new Traversal(origins);
            int i = 0;
            boolean escaped = false;
            int previousKQueryStart = 0;
            int previousKQueryNameEnd = -1;
            int previousKQueryAttributesEnd = -1;
            int previousKQueryAttributesStart = 0;
            boolean endEval = false;
            while (i < query.length() && !endEval) {
                boolean notLastElem = (i + 1) != query.length();
                if (escaped && notLastElem) {
                    escaped = false;
                } else {
                    char currentChar = query.charAt(i);
                    if (currentChar == CLOSE_BRACKET && notLastElem) {
                        previousKQueryAttributesEnd = i;
                    } else if (currentChar == '\\' && notLastElem) {
                        escaped = true;
                    } else if (currentChar == OPEN_BRACKET && notLastElem) {
                        previousKQueryNameEnd = i;
                        previousKQueryAttributesStart = i + 1;
                    } else if (currentChar == PIPE_SEP || !notLastElem) {
                        String relationName;
                        String atts = null;
                        if (previousKQueryNameEnd == -1) {
                            if (notLastElem) {
                                previousKQueryNameEnd = i;
                            } else {
                                previousKQueryNameEnd = i + 1;
                            }
                        } else {
                            if (previousKQueryAttributesStart != -1) {
                                if (previousKQueryAttributesEnd == -1) {
                                    if (notLastElem || currentChar == PIPE_SEP || currentChar == CLOSE_BRACKET) {
                                        previousKQueryAttributesEnd = i;
                                    } else {
                                        previousKQueryAttributesEnd = i + 1;
                                    }
                                }
                                atts = query.substring(previousKQueryAttributesStart, previousKQueryAttributesEnd);
                                if (atts.length() == 0) {
                                    atts = null;
                                }
                            }
                        }
                        relationName = query.substring(previousKQueryStart, previousKQueryNameEnd).trim();
                        if (relationName.startsWith("=")) {
                            if (callbackGenericObject != null) {
                                traversal.eval(relationName.substring(1), callbackGenericObject);
                                endEval = true;
                            }
                        } else if (relationName.startsWith(">>")) {
                            traversal = traversal.traverseQuery(relationName.substring(2));
                            if (atts != null) {
                                traversal = traversal.attributeQuery(atts);
                            }
                        } else if (relationName.startsWith("<<")) {
                            traversal = traversal.traverseQuery(relationName);
                            if (atts != null) {
                                traversal = traversal.attributeQuery(atts);
                            }
                        } else {
                            traversal = traversal.traverseQuery(relationName);
                            if (atts != null) {
                                traversal = traversal.attributeQuery(atts);
                            }
                        }
                        //ReInit
                        previousKQueryStart = i + 1;
                        previousKQueryNameEnd = -1;
                        previousKQueryAttributesEnd = -1;
                        previousKQueryAttributesStart = -1;
                    }
                }
                i = i + 1;
            }
            if (!endEval) {
                if (callbackKObject != null) {
                    traversal.then(callbackKObject);
                } else {
                    traversal.then(new KCallback<KObject[]>() {
                        @Override
                        public void on(KObject[] kObjects) {
                            callbackGenericObject.on(kObjects);
                        }
                    });
                }
            }
        }
    }

}
