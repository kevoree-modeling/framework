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
        } else {

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

    public static MModel build(File mmFile) throws IOException {
        MModel model = new MModel();
        ANTLRFileStream fileStream = new ANTLRFileStream(mmFile.getAbsolutePath());
        BufferedTokenStream tokens = new CommonTokenStream(new org.kevoree.modeling.ast.MetaModelLexer(fileStream));
        org.kevoree.modeling.ast.MetaModelParser parser = new org.kevoree.modeling.ast.MetaModelParser(tokens);
        org.kevoree.modeling.ast.MetaModelParser.MetamodelContext mmctx = parser.metamodel();
        //first we do version
        for (org.kevoree.modeling.ast.MetaModelParser.DeclContext decl : mmctx.decl()) {
            if (decl.versionDeclr() != null) {
                //MetaModelParser.VersionDeclrContext versionDeclrContext = decl.versionDeclr();
                //System.err.println(versionDeclrContext);
            }
            if (decl.kmfVersionDeclr() != null) {
                //MetaModelParser.VersionDeclrContext versionDeclrContext = decl.versionDeclr();
                //System.err.println(versionDeclrContext);
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
                    if (attDecl.NUMBER() != null) {
                        attribute.setPrecision(Double.parseDouble(attDecl.NUMBER().getText()));
                    }
                    newClass.addAttribute(attribute);
                }
                for (org.kevoree.modeling.ast.MetaModelParser.ReferenceDeclarationContext refDecl : classDeclrContext.referenceDeclaration()) {
                    final MModelClass refType = model.getOrAddClass(refDecl.TYPE_NAME().getText());
                    MModelReference reference = new MModelReference(refDecl.TYPE_NAME().toString(), refType);
                    if (refDecl.getText().trim().startsWith("ref*")) {
                        reference.setSingle(false);
                    }
                    if (refDecl.IDENT().size() > 1) {
                        reference.setOpposite(refDecl.IDENT(refDecl.IDENT().size() - 1).getText());
                    }
                    newClass.addReference(reference);
                }
                for (org.kevoree.modeling.ast.MetaModelParser.DependencyDeclarationContext dependencyDeclarationContext : classDeclrContext.dependencyDeclaration()) {
                    final MModelClass depType = model.getOrAddClass(dependencyDeclarationContext.TYPE_NAME().getText());
                    MModelDependency dependency = new MModelDependency(dependencyDeclarationContext.IDENT().getText(), depType);
                    newClass.addDependency(dependency);
                }
                for (org.kevoree.modeling.ast.MetaModelParser.InputDeclarationContext inputDeclarationContext : classDeclrContext.inputDeclaration()) {
                    MModelInput input = new MModelInput(inputDeclarationContext.IDENT().getText(), inputDeclarationContext.STRING().getText());
                    newClass.addInput(input);
                }
                for (org.kevoree.modeling.ast.MetaModelParser.OutputDeclarationContext outputDeclarationContext : classDeclrContext.outputDeclaration()) {
                    MModelOutput output = new MModelOutput(outputDeclarationContext.IDENT(0).getText(), outputDeclarationContext.IDENT(1).getText());
                    newClass.addOutput(output);
                }
            }
        }
        return model;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(build(new File("/Users/duke/Documents/dev/kevoree-modeling/framework/org.kevoree.modeling.ast/src/main/exemples/cloud.mm")));
    }

    private MModelReference getOrAddReference(MModelClass owner, String refName, MModelClass refType) {
        for (MModelReference registeredRef : owner.getReferences()) {
            if (registeredRef.getName().equals(refName)) {
                return registeredRef;
            }
        }
        MModelReference reference = new MModelReference(refName, refType);
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (MModelClassifier cl : classifiers.values()) {
            builder.append(cl.toString());
        }
        return builder.toString();
    }
}
