package org.kevoree.modeling.message.impl;

import org.kevoree.modeling.message.KMessageLoader;

public class MessageHelper {

    public static void printJsonStart(StringBuilder builder) {
        builder.append("{\n");
    }

    public static void printJsonEnd(StringBuilder builder) {
        builder.append("}\n");
    }

    public static void printType(StringBuilder builder, int type) {
        builder.append("\"");
        builder.append(KMessageLoader.TYPE_NAME);
        builder.append("\":\"");
        builder.append(type);
        builder.append("\"\n");
    }

    public static void printElem(Object elem, String name, StringBuilder builder) {
        if (elem != null) {
            builder.append(",");
            builder.append("\"");
            builder.append(name);
            builder.append("\":\"");
            builder.append(elem.toString());
            builder.append("\"\n");
        }
    }


}
