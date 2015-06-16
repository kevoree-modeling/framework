package org.kevoree.modeling.format.xmi;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;

public class XMIModelSerializer {

    public static void save(KObject model, final KCallback<String> callback) {
        callback.on(null);
/*
        if (model == null) {
        } else {
            final SerializationContext context = new SerializationContext();
            context.model = model;
            context.finishCallback = callback;
            context.attributesVisitor = new ModelAttributeVisitor() {
                @Override
                public void visit(MetaAttribute metaAttribute, Object value) {
                    if (value != null) {
                        if (context.ignoreGeneratedID && metaAttribute.metaName().equals("generated_KMF_ID")) {
                            return;
                        }
                        context.printer.append(" " + metaAttribute.metaName() + "=\"");
                        XMIModelSerializer.escapeXml(context.printer, value.toString());
                        context.printer.append("\"");
                    }
                }
            };

            context.printer = new StringBuilder();
            //First Pass for building address table
            context.addressTable.put(model.uuid(), "/");

            KDefer addressCreationTask = context.model.visit(VisitRequest.CONTAINED, new ModelVisitor() {
                @Override
                public VisitResult visit(KObject elem) {
                    String parentXmiAddress = context.addressTable.get(elem.parentUuid());
                    String key = parentXmiAddress + "/@" + elem.referenceInParent().metaName();
                    Integer i = context.elementsCount.get(key);
                    if (i == null) {
                        i = 0;
                        context.elementsCount.put(key, i);
                    }
                    context.addressTable.put(elem.uuid(), parentXmiAddress + "/@" + elem.referenceInParent().metaName() + "." + i);
                    context.elementsCount.put(parentXmiAddress + "/@" + elem.referenceInParent().metaName(), i + 1);
                    String pack = elem.metaClass().metaName().substring(0, elem.metaClass().metaName().lastIndexOf('.'));
                    if (!context.packageList.contains(pack)) {
                        context.packageList.add(pack);
                    }
                    return VisitResult.CONTINUE;
                }
            });

            KDefer serializationTask = ((AbstractKObject) context.model)._manager.model().defer();
            serializationTask.wait(addressCreationTask);
            serializationTask.setJob(new KJob() {
                @Override
                public void run(KCurrentDefer currentTask) {
                    context.printer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                    context.printer.append("<" + XMIModelSerializer.formatMetaClassName(context.model.metaClass().metaName()).replace(".", "_"));
                    context.printer.append(" xmlns:xsi=\"http://wwww.w3.org/2001/XMLSchema-instance\"");
                    context.printer.append(" xmi:version=\"2.0\"");
                    context.printer.append(" xmlns:xmi=\"http://www.omg.org/XMI\"");

                    int index = 0;
                    while (index < context.packageList.size()) {
                        context.printer.append(" xmlns:" + context.packageList.get(index).replace(".", "_") + "=\"http://" + context.packageList.get(index) + "\"");
                        index++;
                    }
                    context.model.visitAttributes(context.attributesVisitor);

                    KDefer nonContainedRefsTasks = ((AbstractKObject) context.model)._manager.model().defer();
                    for (int i = 0; i < context.model.metaClass().metaReferences().length; i++) {
                        if (!context.model.metaClass().metaReferences()[i].contained()) {
                            nonContainedRefsTasks.wait(nonContainedReferenceTaskMaker(context.model.metaClass().metaReferences()[i], context, context.model));
                        }
                    }
                    nonContainedRefsTasks.setJob(new KJob() {
                        @Override
                        public void run(KCurrentDefer currentTask) {
                            context.printer.append(">\n");

                            KDefer containedRefsTasks = ((AbstractKObject) context.model)._manager.model().defer();
                            for (int i = 0; i < context.model.metaClass().metaReferences().length; i++) {
                                if (context.model.metaClass().metaReferences()[i].contained()) {
                                    containedRefsTasks.wait(containedReferenceTaskMaker(context.model.metaClass().metaReferences()[i], context, context.model));
                                }
                            }
                            containedRefsTasks.setJob(new KJob() {
                                @Override
                                public void run(KCurrentDefer currentTask) {
                                    context.printer.append("</" + XMIModelSerializer.formatMetaClassName(context.model.metaClass().metaName()).replace(".", "_") + ">\n");
                                    context.finishCallback.on(context.printer.toString());
                                }
                            });
                            containedRefsTasks.ready();
                        }
                    });
                    nonContainedRefsTasks.ready();
                }
            });
            serializationTask.ready();
        }
    }


    public static void escapeXml(StringBuilder ostream, String chain) {
        if (chain == null) {
            return;
        }
        int i = 0;
        int max = chain.length();
        while (i < max) {
            char c = chain.charAt(i);
            if (c == '"') {
                ostream.append("&quot;");
            } else if (c == '&') {
                ostream.append("&amp;");
            } else if (c == '\'') {
                ostream.append("&apos;");
            } else if (c == '<') {
                ostream.append("&lt;");
            } else if (c == '>') {
                ostream.append("&gt;");
            } else {
                ostream.append(c);
            }
            i = i + 1;
        }
    }

    public static String formatMetaClassName(String metaClassName) {
        int lastPoint = metaClassName.lastIndexOf('.');
        String pack = metaClassName.substring(0, lastPoint);
        String cls = metaClassName.substring(lastPoint + 1);
        return pack + ":" + cls;
    }

    private static KDefer nonContainedReferenceTaskMaker(final MetaReference ref, final SerializationContext p_context, KObject p_currentElement) {
        final KDefer allTask = p_currentElement.ref(ref);
        KDefer thisTask = ((AbstractKObject) p_context.model)._manager.model().defer();
        thisTask.wait(allTask);
        thisTask.setJob(new KJob() {
            @Override
            public void run(KCurrentDefer currentTask) {
                try {
                    KObject[] objects = ((KObject[]) currentTask.resultByDefer(allTask));
                    for (int i = 0; i < objects.length; i++) {
                        String adjustedAddress = p_context.addressTable.get(objects[i].uuid());
                        p_context.printer.append(" " + ref.metaName() + "=\"" + adjustedAddress + "\"");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thisTask.ready();
        return thisTask;
    }

    private static KDefer containedReferenceTaskMaker(final MetaReference ref, final SerializationContext context, KObject currentElement) {
        final KDefer allTask = currentElement.ref(ref);
        KDefer thisTask = ((AbstractKObject) context.model)._manager.model().defer();
        thisTask.wait(allTask);
        thisTask.setJob(new KJob() {
            @Override
            public void run(KCurrentDefer currentTask) {
                try {
                    if (currentTask.resultByDefer(allTask) != null) {
                        KObject[] objs = ((KObject[]) currentTask.resultByDefer(allTask));
                        for (int i = 0; i < objs.length; i++) {
                            final KObject elem = objs[i];
                            context.printer.append("<");
                            context.printer.append(ref.metaName());
                            context.printer.append(" xsi:type=\"" + XMIModelSerializer.formatMetaClassName(elem.metaClass().metaName()) + "\"");
                            elem.visitAttributes(context.attributesVisitor);
                            KDefer nonContainedRefsTasks = ((AbstractKObject) context.model)._manager.model().defer();
                            for (int j = 0; j < elem.metaClass().metaReferences().length; j++) {
                                if (!elem.metaClass().metaReferences()[i].contained()) {
                                    nonContainedRefsTasks.wait(nonContainedReferenceTaskMaker(elem.metaClass().metaReferences()[i], context, elem));
                                }
                            }
                            nonContainedRefsTasks.setJob(new KJob() {
                                @Override
                                public void run(KCurrentDefer currentTask) {
                                    context.printer.append(">\n");
                                    KDefer containedRefsTasks = ((AbstractKObject) context.model)._manager.model().defer();
                                    for (int i = 0; i < elem.metaClass().metaReferences().length; i++) {
                                        if (elem.metaClass().metaReferences()[i].contained()) {
                                            containedRefsTasks.wait(containedReferenceTaskMaker(elem.metaClass().metaReferences()[i], context, elem));
                                        }
                                    }
                                    containedRefsTasks.setJob(new KJob() {
                                        @Override
                                        public void run(KCurrentDefer currentTask) {
                                            context.printer.append("</");
                                            context.printer.append(ref.metaName());
                                            context.printer.append('>');
                                            context.printer.append("\n");
                                        }
                                    });
                                    containedRefsTasks.ready();
                                }
                            });
                            nonContainedRefsTasks.ready();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thisTask.ready();
        return thisTask;
        */
    }
}