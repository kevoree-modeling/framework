package org.kevoree.modeling.ast;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MModel {

    private String version = null;

    private String kmfVersion = null;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getKmfVersion() {
        return kmfVersion;
    }

    public void setKmfVersion(String kmfVersion) {
        this.kmfVersion = kmfVersion;
    }

    private HashMap<String, MModelClassifier> classifiers = new HashMap<>();

    private Integer classIndex = 0;
    private Integer enumIndex = 0;

    public Collection<MModelClassifier> getClassifiers() {
        return classifiers.values();
    }

    public MModelClassifier get(String fqn) {
        return classifiers.get(fqn);
    }

    public void addClassifier(MModelClassifier classifier) {
        if (classifier instanceof MModelClass) {
            classifier.setIndex(classIndex);
            classIndex = classIndex + 1;
        } else if (classifier instanceof MModelEnum) {
            classifier.setIndex(enumIndex);
            enumIndex = enumIndex + 1;
        }
        classifiers.put(classifier.getFqn(), classifier);
    }

    public Collection<MModelClass> getClasses() {
        ArrayList<MModelClass> classes = new ArrayList<MModelClass>();
        for (MModelClassifier cls : classifiers.values()) {
            if (cls instanceof MModelClass) {
                classes.add((MModelClass) cls);
            }
        }
        return classes;
    }

    public Collection<MModelEnum> getEnums() {
        ArrayList<MModelEnum> classes = new ArrayList<MModelEnum>();
        for (MModelClassifier cls : classifiers.values()) {
            if (cls instanceof MModelEnum) {
                classes.add((MModelEnum) cls);
            }
        }
        return classes;
    }

    private final static Character ESCAPE_CHAR = '\\';

    private static String cleanString(String in) {
        String cleaned = in.substring(1, in.length() - 1);
        if (cleaned.length() == 0) {
            return cleaned;
        }
        StringBuilder builder = null;
        int i = 0;
        while (i < cleaned.length()) {
            Character current = cleaned.charAt(i);
            if (current == ESCAPE_CHAR) {
                if (builder == null) {
                    builder = new StringBuilder();
                    builder.append(cleaned.substring(0, i));
                }
                i++;
                Character current2 = cleaned.charAt(i);
                switch (current2) {
                    case '"':
                        builder.append('\"');
                        break;
                    case '\\':
                        builder.append(current2);
                        break;
                    case '/':
                        builder.append(current2);
                        break;
                    case 'b':
                        builder.append('\b');
                        break;
                    case 'f':
                        builder.append('\f');
                        break;
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;
                    case '{':
                        builder.append("\\{");
                        break;
                    case '}':
                        builder.append("\\}");
                        break;
                    case '[':
                        builder.append("\\[");
                        break;
                    case ']':
                        builder.append("\\]");
                        break;
                    case ',':
                        builder.append("\\,");
                        break;
                }
            } else {
                if (builder != null) {
                    builder = builder.append(current);
                }
            }
            i++;
        }
        if (builder != null) {
            return builder.toString();
        } else {
            return cleaned;
        }
    }

    public static MModel build(File mmFile) throws IOException {
        MModel model = new MModel();
        ANTLRFileStream fileStream = new ANTLRFileStream(mmFile.getAbsolutePath());
        BufferedTokenStream tokens = new CommonTokenStream(new org.kevoree.modeling.ast.MetaModelLexer(fileStream));
        org.kevoree.modeling.ast.MetaModelParser parser = new org.kevoree.modeling.ast.MetaModelParser(tokens);
        org.kevoree.modeling.ast.MetaModelParser.MetamodelContext mmctx = parser.metamodel();
        //first we do version
        for (org.kevoree.modeling.ast.MetaModelParser.AnnotationDeclrContext annotationDeclrContext : mmctx.annotationDeclr()) {
            if (annotationDeclrContext.IDENT().getText().toLowerCase().equals("version") && annotationDeclrContext.STRING() != null) {
                model.setVersion(cleanString(annotationDeclrContext.STRING().getText()));
            }
            if (annotationDeclrContext.IDENT().getText().toLowerCase().equals("kmfversion") && annotationDeclrContext.STRING() != null) {
                model.setKmfVersion(cleanString(annotationDeclrContext.STRING().getText()));
            }
        }
        //Second we do enum mapping
        for (org.kevoree.modeling.ast.MetaModelParser.DeclContext decl : mmctx.decl()) {
            if (decl.enumDeclr() != null) {
                org.kevoree.modeling.ast.MetaModelParser.EnumDeclrContext enumDeclrContext = decl.enumDeclr();
                String enumFqn = enumDeclrContext.TYPE_NAME().getText();
                final MModelEnum enumClass = model.getOrAddEnum(enumFqn);
                for (TerminalNode literal : enumDeclrContext.IDENT()) {
                    enumClass.addLitteral(literal.getText());
                }
            }
        }
        //thirdly we do classDeclr
        for (org.kevoree.modeling.ast.MetaModelParser.DeclContext decl : mmctx.decl()) {
            if (decl.classDeclr() != null) {
                org.kevoree.modeling.ast.MetaModelParser.ClassDeclrContext classDeclrContext = decl.classDeclr();
                String classFqn = classDeclrContext.TYPE_NAME().getText();
                final MModelClass newClass = model.getOrAddClass(classFqn);
                for (org.kevoree.modeling.ast.MetaModelParser.AttributeDeclarationContext attDecl : classDeclrContext.attributeDeclaration()) {
                    String name = attDecl.IDENT().getText();
                    org.kevoree.modeling.ast.MetaModelParser.AttributeTypeContext attType = attDecl.attributeType();
                    String value;
                    if (attType.TYPE_NAME() != null) {
                        value = attType.TYPE_NAME().getText();
                    } else {
                        value = attType.getText();
                    }
                    final MModelAttribute attribute = new MModelAttribute(name, value);
                    for (org.kevoree.modeling.ast.MetaModelParser.AnnotationDeclrContext annotDecl : attDecl.annotationDeclr()) {
                        if (annotDecl.IDENT().getText().toLowerCase().equals("precision") && annotDecl.NUMBER() != null) {
                            attribute.setPrecision(Double.parseDouble(annotDecl.NUMBER().getText()));
                        }
                        if (annotDecl.IDENT().getText().toLowerCase().equals("index")) {
                            attribute.setIndexed();
                        }
                    }
                    newClass.addAttribute(attribute);
                }
                for (org.kevoree.modeling.ast.MetaModelParser.ReferenceDeclarationContext refDecl : classDeclrContext.referenceDeclaration()) {
                    final MModelClass refType = model.getOrAddClass(refDecl.TYPE_NAME().getText());
                    MModelRelation reference = model.getOrAddReference(newClass, refDecl.IDENT().getText(), refType);
                    for (org.kevoree.modeling.ast.MetaModelParser.AnnotationDeclrContext annotDecl : refDecl.annotationDeclr()) {
                        if (annotDecl.IDENT().getText().toLowerCase().equals("opposite") && annotDecl.STRING() != null) {
                            reference.setOpposite(cleanString(annotDecl.STRING().getText()));
                            MModelRelation oppRef = model.getOrAddReference(refType, reference.getOpposite(), newClass);
                            oppRef.setVisible(true);
                            oppRef.setOpposite(reference.getName());
                        }
                        if (annotDecl.IDENT().getText().toLowerCase().equals("maxbound") && annotDecl.NUMBER() != null) {
                            reference.setMaxBound(Integer.parseInt(annotDecl.NUMBER().getText()));
                        }
                    }
                    newClass.addReference(reference);
                }

                for (org.kevoree.modeling.ast.MetaModelParser.DependencyDeclarationContext dependencyDeclarationContext : classDeclrContext.dependencyDeclaration()) {
                    final MModelClass depType = model.getOrAddClass(dependencyDeclarationContext.TYPE_NAME().getText());
                    MModelDependency dependency = new MModelDependency(dependencyDeclarationContext.IDENT().getText(), depType);
                    newClass.addDependency(dependency);
                }
                for (org.kevoree.modeling.ast.MetaModelParser.InputDeclarationContext inputDeclarationContext : classDeclrContext.inputDeclaration()) {
                    MModelInput input = new MModelInput(inputDeclarationContext.IDENT().getText(), cleanString(inputDeclarationContext.STRING().getText()));
                    newClass.addInput(input);
                }
                for (org.kevoree.modeling.ast.MetaModelParser.OutputDeclarationContext outputDeclarationContext : classDeclrContext.outputDeclaration()) {
                    org.kevoree.modeling.ast.MetaModelParser.AttributeTypeContext attType = outputDeclarationContext.attributeType();
                    String typeName;
                    if (attType.TYPE_NAME() != null) {
                        typeName = attType.TYPE_NAME().getText();
                    } else {
                        typeName = attType.getText();
                    }
                    MModelOutput output = new MModelOutput(outputDeclarationContext.IDENT().getText(), typeName);
                    newClass.addOutput(output);
                }
                if (classDeclrContext.classParentDeclr() != null) {
                    org.kevoree.modeling.ast.MetaModelParser.ClassParentDeclrContext parentDeclrContext = classDeclrContext.classParentDeclr();
                    for (TerminalNode tt : parentDeclrContext.TYPE_NAME()) {
                        final MModelClass newClassTT = model.getOrAddClass(tt.getText());
                        newClass.addParent(newClassTT);
                    }
                }
                for (org.kevoree.modeling.ast.MetaModelParser.AnnotationDeclrContext annotDecl : classDeclrContext.annotationDeclr()) {
                    if (annotDecl.IDENT().getText().toLowerCase().equals("temporalLimit") && annotDecl.NUMBER() != null) {
                        newClass.setTemporalLimit(Long.parseLong(annotDecl.NUMBER().getText()));
                    }
                    if (annotDecl.IDENT().getText().toLowerCase().equals("temporalResolution") && annotDecl.NUMBER() != null) {
                        newClass.setTemporalResolution(Long.parseLong(annotDecl.NUMBER().getText()));
                    }
                    if (annotDecl.IDENT().getText().toLowerCase().equals("inference") && annotDecl.STRING() != null) {
                        newClass.setInference(cleanString(annotDecl.STRING().getText()));
                    }
                    if(annotDecl.IDENT().getText().toLowerCase().equals("instantiation") && annotDecl.STRING() != null){
                        String value = cleanString(annotDecl.STRING().getText().toLowerCase());
                        if(value.equals("true") || value.equals("false")) {
                            newClass.setCanHaveInstance(Boolean.valueOf(value));
                        } else {
                            throw new RuntimeException("The instantiation value must be a boolean : \"true\" or \"false\". " + value);
                        }
                    }
                }
                for (org.kevoree.modeling.ast.MetaModelParser.FunctionDeclarationContext functionDeclarationContext : classDeclrContext.functionDeclaration()) {
                    MModelOperation operation = new MModelOperation(functionDeclarationContext.IDENT().getText());
                    if (functionDeclarationContext.functionDeclarationReturnType() != null) {
                        operation.returnType = functionDeclarationContext.functionDeclarationReturnType().attributeType().getText();
                        if (functionDeclarationContext.functionDeclarationReturnType().functionDeclarationMultiplicity() != null) {
                            operation.returnTypeIsArray = true;
                        }
                    }
                    if (functionDeclarationContext.functionDeclarationParameters() != null) {
                        for (org.kevoree.modeling.ast.MetaModelParser.FunctionDeclarationParameterContext param : functionDeclarationContext.functionDeclarationParameters().functionDeclarationParameter()) {
                            MModelOperationParam newParam = new MModelOperationParam();
                            newParam.name = param.IDENT().getText();
                            newParam.type = param.attributeType().getText();
                            operation.inputParams.add(newParam);
                            if (param.functionDeclarationMultiplicity() != null) {
                                newParam.isArray = true;
                            }
                        }
                    }
                    newClass.addOperation(operation);
                }
            }
        }
        //opposite completion
        model.completeOppositeReferences();
        model.consolidateIndexes();
        model.consolidateTypeIds();
        return model;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(build(new File("/Users/duke/Documents/dev/kevoree-modeling/framework/org.kevoree.modeling.ast/src/main/exemples/cloud.mm")));
    }

    private MModelRelation getOrAddReference(MModelClass owner, String refName, MModelClass refType) {
        for (MModelRelation registeredRef : owner.getReferences()) {
            if (registeredRef.getName().equals(refName)) {
                return registeredRef;
            }
        }
        MModelRelation reference = new MModelRelation(refName, refType);
        owner.addReference(reference);
        return reference;
    }

    private MModelEnum getOrAddEnum(String clazz) {
        MModelClassifier resolved = get(clazz);
        if (resolved == null) {
            String relationTypePackage = clazz.substring(0, clazz.lastIndexOf("."));
            String relationTypeName = clazz.substring(clazz.lastIndexOf(".") + 1);
            resolved = new MModelEnum(relationTypeName);
            resolved.setPack(relationTypePackage);
            addClassifier(resolved);
            return (MModelEnum) resolved;
        } else {
            if (resolved instanceof MModelEnum) {
                return (MModelEnum) resolved;
            } else {
                throw new RuntimeException("Naming conflict for " + clazz + ", cannot merge an enum and a class declaration");
            }
        }
    }

    private MModelClass getOrAddClass(String clazz) {
        MModelClassifier resolved = get(clazz);
        if (resolved == null) {
            String relationTypePackage = clazz.substring(0, clazz.lastIndexOf("."));
            String relationTypeName = clazz.substring(clazz.lastIndexOf(".") + 1);
            resolved = new MModelClass(relationTypeName);
            resolved.setPack(relationTypePackage);
            addClassifier(resolved);
            return (MModelClass) resolved;
        } else {
            if (resolved instanceof MModelClass) {
                return (MModelClass) resolved;
            } else {
                throw new RuntimeException("Naming conflict for " + clazz + ", cannot merge an enum and a class declaration");
            }
        }
    }

    private void completeOppositeReferences() {
        for (MModelClass classDecl : getClasses()) {
            for (MModelRelation ref : classDecl.getReferences().toArray(new MModelRelation[classDecl.getReferences().size()])) {
                if (ref.getOpposite() == null) {
                    //Create opposite relation

                    MModelRelation op_ref = getOrAddReference(ref.getType(), "op_" + classDecl.getName() + "_" + ref.getName(), classDecl);
                    op_ref.setVisible(false);
                    op_ref.setOpposite(ref.getName());

                    //add the relation on  the other side
                    ref.getType().addReference(op_ref);
                    ref.setOpposite(op_ref.getName());
                }
            }
        }
    }

    public void consolidateIndexes() {
        for (MModelClass decl : getClasses()) {
            internal_consolidate(decl);
        }
    }

    public void consolidateTypeIds() {
        for (MModelClass decl : getClasses()) {
            for (MModelAttribute att : decl.getAttributes()) {
                if (MMTypeHelper.isPrimitiveTYpe(att.getType())) {
                    att.typeId = MMTypeHelper.toId(att.getType());
                } else {
                    for (MModelEnum en : getEnums()) {
                        if (en.getFqn().endsWith(att.getType())) {
                            att.typeId = en.index;
                        }
                    }
                }
            }
            for (MModelOutput out : decl.getOutputs()) {
                if (MMTypeHelper.isPrimitiveTYpe(out.getType())) {
                    out.typeId = MMTypeHelper.toId(out.getType());
                } else {
                    for (MModelEnum en : getEnums()) {
                        if (en.getFqn().endsWith(out.getType())) {
                            out.typeId = en.index;
                        }
                    }
                }
            }
            for (MModelOperation out : decl.getOperations()) {
                if (out.getReturnType() != null) {
                    if (MMTypeHelper.isPrimitiveTYpe(out.getReturnType())) {
                        out.returnTypeId = MMTypeHelper.toId(out.getReturnType());
                    } else {
                        for (MModelEnum en : getEnums()) {
                            if (en.getFqn().endsWith(out.getReturnType())) {
                                out.returnTypeId = en.index;
                            }
                        }
                    }
                }
                for (MModelOperationParam param : out.inputParams) {
                    if (MMTypeHelper.isPrimitiveTYpe(param.getType())) {
                        param.typeId = MMTypeHelper.toId(param.getType());
                    } else {
                        for (MModelEnum en : getEnums()) {
                            if (en.getFqn().endsWith(param.getType())) {
                                param.typeId = en.index;
                            }
                        }
                    }
                }
            }

        }
    }

    private void internal_consolidate(MModelClass classRelDecls) {
        int globalIndex = 0;
        for (MModelAttribute att : classRelDecls.getAttributes()) {
            att.setIndex(globalIndex);
            globalIndex++;
        }
        for (MModelRelation ref : classRelDecls.getReferences()) {
            ref.setIndex(globalIndex);
            globalIndex++;
        }
        for (MModelOperation op : classRelDecls.getOperations()) {
            op.setIndex(globalIndex);
            globalIndex++;
        }
        for (MModelInput inp : classRelDecls.getInputs()) {
            inp.setIndex(globalIndex);
            globalIndex++;
        }
        for (MModelOutput out : classRelDecls.getOutputs()) {
            out.setIndex(globalIndex);
            globalIndex++;
        }
        if (classRelDecls.dependencies() != null) {
            classRelDecls.dependencies().setIndex(globalIndex);
            globalIndex++;
            int localIndex = 0;
            for (MModelDependency dep : classRelDecls.dependencies().dependencies.values()) {
                dep.setIndex(localIndex);
                localIndex++;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (MModelClassifier cl : classifiers.values()) {
            builder.append(cl.toString());
        }
        return builder.toString();
    }
}
