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
        if (query == null || query.length() == 0) {
            callback.on(new KObject[0]);
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
                        relationName = query.substring(previousKQueryStart, previousKQueryNameEnd);

                        if (relationName.startsWith(">>")) {
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
                            traversal.eval(relationName, callback);
                            endEval = true;
                            //expression
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
                traversal.then(new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        callback.on(kObjects);
                    }
                });
            }
        }
    }

}
