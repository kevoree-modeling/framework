package org.kevoree.modeling.format.json;

import org.kevoree.modeling.traversal.visitor.KModelVisitor;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.traversal.visitor.KVisitResult;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.manager.AccessMode;

public class JsonModelSerializer {

    public static void serialize(final KObject model, final KCallback<String> callback) {
        ((AbstractKObject) model)._manager.getRoot(model.universe(), model.now(), new KCallback<KObject>() {
            @Override
            public void on(final KObject rootObj) {
                boolean isRoot = false;
                if (rootObj != null) {
                    isRoot = rootObj.uuid() == model.uuid();
                }
                final StringBuilder builder = new StringBuilder();
                builder.append("[\n");
                printJSON(model, builder, isRoot);
                model.visit(new KModelVisitor() {
                    @Override
                    public KVisitResult visit(KObject elem) {
                        boolean isRoot2 = false;
                        if (rootObj != null) {
                            isRoot2 = rootObj.uuid() == elem.uuid();
                        }
                        builder.append(",\n");
                        try {
                            printJSON(elem, builder, isRoot2);
                        } catch (Exception e) {
                            e.printStackTrace();
                            builder.append("{}");
                        }
                        return KVisitResult.CONTINUE;
                    }
                },new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {
                        builder.append("\n]\n");
                        callback.on(builder.toString());
                    }
                });
            }
        });
    }

    public static void printJSON(KObject elem, StringBuilder builder, boolean isRoot) {
        if (elem != null) {
            KMemorySegment raw = ((AbstractKObject) elem)._manager.segment(elem.universe(),elem.now(),elem.uuid(), AccessMode.RESOLVE,elem.metaClass(), null);
            if (raw != null) {
                builder.append(JsonRaw.encode(raw, elem.uuid(), elem.metaClass(), isRoot));
            }
        }
    }

}

