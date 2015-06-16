package org.kevoree.modeling.traversal.impl.selector;

import java.util.ArrayList;
import java.util.List;

public class Query {

    public static char OPEN_BRACKET = '[';
    public static char CLOSE_BRACKET = ']';
    public static char QUERY_SEP = '/';

    public String relationName;
    public String params;

    private Query(String relationName, String params) {
        this.relationName = relationName;
        this.params = params;
    }

    @Override
    public String toString() {
        return "KQuery{" +
                "relationName='" + relationName + '\'' +
                ", params='" + params + '\'' +
                '}';
    }

    public static List<Query> buildChain(String query) {
        List<Query> result = new ArrayList<Query>();
        if (query == null || query.length() == 0) {
            return null;
        }
        int i = 0;
        boolean escaped = false;
        int previousKQueryStart = 0;
        int previousKQueryNameEnd = -1;
        int previousKQueryAttributesEnd = -1;
        int previousKQueryAttributesStart = 0;
        while (i < query.length()) {
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
                } else if (currentChar == QUERY_SEP || !notLastElem) {
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
                                if (notLastElem || currentChar == QUERY_SEP || currentChar == CLOSE_BRACKET) {
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
                    Query additionalQuery = new Query(relationName, atts);
                    result.add(additionalQuery);
                    //ReInit
                    previousKQueryStart = i + 1;
                    previousKQueryNameEnd = -1;
                    previousKQueryAttributesEnd = -1;
                    previousKQueryAttributesStart = -1;
                }
            }
            i = i + 1;
        }
        return result;
    }

}
