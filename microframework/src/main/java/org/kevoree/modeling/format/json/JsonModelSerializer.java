package org.kevoree.modeling.format.json;

import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.traversal.visitor.KModelVisitor;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.traversal.visitor.KVisitResult;
import org.kevoree.modeling.abs.AbstractKObject;

public class JsonModelSerializer {

    public static void serialize(final KObject model, final KCallback<String> callback) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[\n");
        printJSON(model, builder);
        model.visit(new KModelVisitor() {
            @Override
            public KVisitResult visit(KObject elem) {
                boolean isRoot2 = false;
                builder.append(",\n");
                try {
                    printJSON(elem, builder);
                } catch (Exception e) {
                    e.printStackTrace();
                    builder.append("{}");
                }
                return KVisitResult.CONTINUE;
            }
        }, new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                builder.append("\n]\n");
                callback.on(builder.toString());
            }
        });
    }

    public static void printJSON(KObject elem, StringBuilder builder) {
        if (elem != null) {
            KObjectChunk raw = ((AbstractKObject) elem)._manager.closestChunk(elem.universe(), elem.now(), elem.uuid(), elem.metaClass(), ((AbstractKObject) elem).previousResolved());
            if (raw != null) {
                builder.append(JsonRaw.encode(raw, elem.uuid(), elem.metaClass()));
            }
        }
    }

}

