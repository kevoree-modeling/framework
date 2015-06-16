package org.kevoree.modeling.format.xmi;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KActionType;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.meta.impl.MetaReference;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.KMetaReference;
import org.kevoree.modeling.meta.MetaType;

public class XMIModelLoader {

    public static final String LOADER_XMI_LOCAL_NAME = "type";
    public static final String LOADER_XMI_XSI = "xsi";
    public static final String LOADER_XMI_NS_URI = "nsURI";

    public static String unescapeXml(String src) {
        StringBuilder builder = null;
        int i = 0;
        while (i < src.length()) {
            char c = src.charAt(i);
            if (c == '&') {
                if (builder == null) {
                    builder = new StringBuilder();
                    builder.append(src.substring(0, i));
                }
                if (src.charAt(i + 1) == 'a') {
                    if (src.charAt(i + 2) == 'm') {
                        builder.append("&");
                        i = i + 5;
                    } else if (src.charAt(i + 2) == 'p') {
                        builder.append("'");
                        i = i + 6;
                    }
                } else if (src.charAt(i + 1) == 'q') {
                    builder.append("\"");
                    i = i + 6;
                } else if (src.charAt(i + 1) == 'l') {
                    builder.append("<");
                    i = i + 4;
                } else if (src.charAt(i + 1) == 'g') {
                    builder.append(">");
                    i = i + 4;
                }
            } else {
                if (builder != null) {
                    builder.append(c);
                }
                i++;
            }
        }
        if (builder != null) {
            return builder.toString();
        } else {
            return src;
        }
    }

    public static void load(KMemoryManager manager, long universe, long time, String str, KCallback<Throwable> callback) {
        XmlParser parser = new XmlParser(str);
        if (!parser.hasNext()) {
            callback.on(null);
        } else {
            XMILoadingContext context = new XMILoadingContext();
            context.successCallback = callback;
            context.xmiReader = parser;
            deserialize(manager, universe, time, context);
        }
    }


    private static void deserialize(KMemoryManager manager, long universe, long time, XMILoadingContext context) {
        try {
            String nsURI;
            XmlParser reader = context.xmiReader;
            while (reader.hasNext()) {
                XmlToken nextTag = reader.next();
                if (nextTag.equals(XmlToken.START_TAG)) {
                    String localName = reader.getLocalName();
                    if (localName != null) {
                        ArrayStringMap<String> ns = new ArrayStringMap<String>(reader.getAttributeCount(), KConfig.CACHE_LOAD_FACTOR);
                        for (int i = 0; i < reader.getAttributeCount() - 1; i++) {
                            String attrLocalName = reader.getAttributeLocalName(i);
                            String attrLocalValue = reader.getAttributeValue(i);
                            if (attrLocalName.equals(LOADER_XMI_NS_URI)) {
                                nsURI = attrLocalValue;
                            }
                            ns.put(attrLocalName, attrLocalValue);
                        }
                        String xsiType = reader.getTagPrefix();
                        String realTypeName = ns.get(xsiType);
                        if (realTypeName == null) {
                            realTypeName = xsiType;
                        }
                        context.loadedRoots = loadObject(manager, universe, time, context, "/", xsiType + "." + localName);
                    }
                }
            }
            for (int i = 0; i < context.resolvers.size(); i++) {
                context.resolvers.get(i).run();
            }
            manager.setRoot(context.loadedRoots, null);
            context.successCallback.on(null);
        } catch (Exception e) {
            context.successCallback.on(e);
        }
    }

    private static KObject callFactory(KMemoryManager manager, long universe, long time, XMILoadingContext ctx, String objectType) {
        KObject modelElem = null;
        if (objectType != null) {
            modelElem = manager.model().createByName(objectType, universe, time);
            if (modelElem == null) {
                String xsiType = null;
                for (int i = 0; i < (ctx.xmiReader.getAttributeCount() - 1); i++) {
                    String localName = ctx.xmiReader.getAttributeLocalName(i);
                    String xsi = ctx.xmiReader.getAttributePrefix(i);
                    if (localName.equals(LOADER_XMI_LOCAL_NAME) && xsi.equals(LOADER_XMI_XSI)) {
                        xsiType = ctx.xmiReader.getAttributeValue(i);
                        break;
                    }
                }
                if (xsiType != null) {
                    String realTypeName = xsiType.substring(0, xsiType.lastIndexOf(":"));
                    String realName = xsiType.substring(xsiType.lastIndexOf(":") + 1, xsiType.length());
                    modelElem = manager.model().createByName(realTypeName + "." + realName, universe, time);
                }
            }

        } else {
            modelElem = manager.model().createByName(ctx.xmiReader.getLocalName(), universe, time);
        }
        return modelElem;
    }

    private static KObject loadObject(KMemoryManager manager, long universe, long time, XMILoadingContext ctx, String xmiAddress, String objectType) throws Exception {
        String elementTagName = ctx.xmiReader.getLocalName();
        KObject modelElem = callFactory(manager, universe, time, ctx, objectType);
        if (modelElem == null) {
            throw new Exception("Could not create an object for local name " + elementTagName);
        }
        ctx.map.put(xmiAddress, modelElem);
        /* Read attributes and References */
        for (int i = 0; i < ctx.xmiReader.getAttributeCount(); i++) {
            String prefix = ctx.xmiReader.getAttributePrefix(i);
            if (prefix == null || prefix.equals("")) {
                String attrName = ctx.xmiReader.getAttributeLocalName(i).trim();
                String valueAtt = ctx.xmiReader.getAttributeValue(i).trim();
                if (valueAtt != null) {
                    KMeta metaElement = modelElem.metaClass().metaByName(attrName);
                    if (metaElement != null && metaElement.metaType().equals(MetaType.ATTRIBUTE)) {
                        modelElem.set((KMetaAttribute) metaElement, unescapeXml(valueAtt));
                    } else {
                        if (metaElement != null && metaElement instanceof MetaReference) {
                            String[] referenceArray = valueAtt.split(" ");
                            for (int j = 0; j < referenceArray.length; j++) {
                                String xmiRef = referenceArray[j];
                                String adjustedRef = (xmiRef.startsWith("#") ? xmiRef.substring(1) : xmiRef);
                                //adjustedRef = (adjustedRef.startsWith("//") ? "/0" + adjustedRef.substring(1) : adjustedRef);
                                adjustedRef = adjustedRef.replace(".0", "");
                                KObject ref = ctx.map.get(adjustedRef);
                                if (ref != null) {
                                    modelElem.mutate(KActionType.ADD, (KMetaReference) metaElement, ref);
                                } else {
                                    ctx.resolvers.add(new XMIResolveCommand(ctx, modelElem, KActionType.ADD, attrName, adjustedRef));
                                }
                            }
                        } else {
                            //attribute ignored
                        }
                    }
                }
            }
        }
        boolean done = false;
        while (!done) {
            if (ctx.xmiReader.hasNext()) {
                XmlToken tok = ctx.xmiReader.next();
                if (tok.equals(XmlToken.START_TAG)) {
                    String subElemName = ctx.xmiReader.getLocalName();
                    String key = xmiAddress + "/@" + subElemName;
                    Integer i = ctx.elementsCount.get(key);
                    if (i == null) {
                        i = 0;
                        ctx.elementsCount.put(key, i);
                    }
                    String subElementId = xmiAddress + "/@" + subElemName + (i != 0 ? "." + i : "");
                    KObject containedElement = loadObject(manager, universe, time, ctx, subElementId, subElemName);
                    modelElem.mutate(KActionType.ADD, (KMetaReference) modelElem.metaClass().metaByName(subElemName), containedElement);
                    ctx.elementsCount.put(xmiAddress + "/@" + subElemName, i + 1);
                } else if (tok.equals(XmlToken.END_TAG)) {
                    if (ctx.xmiReader.getLocalName().equals(elementTagName)) {
                        done = true;
                    }
                }
            } else {
                done = true;
            }
        }
        return modelElem;
    }

}


