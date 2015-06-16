package org.kevoree.modeling.generator;

import org.kevoree.modeling.ast.*;
import org.kevoree.modeling.idea.psi.*;

import java.util.function.Consumer;

/**
 * Created by gregory.nain on 14/10/2014.
 */
public class MMPsiVisitor extends MetaModelVisitor {

    private GenerationContext context;

    private Boolean enumOnly = false;

    public MMPsiVisitor(GenerationContext context, Boolean enumOnly) {
        this.context = context;
        this.enumOnly = enumOnly;
    }

    @Override
    public void visitDeclaration(MetaModelDeclaration o) {

        if (enumOnly) {
            o.acceptChildren(enumVisitor);
        } else {
            o.acceptChildren(classVisitor);
        }
    }


    MetaModelVisitor enumVisitor = new MetaModelVisitor() {

        @Override
        public void visitDeclaration(MetaModelDeclaration o) {
            o.acceptChildren(this);
        }

        @Override
        public void visitEnumDeclaration(MetaModelEnumDeclaration o) {
            String enumFqn = o.getTypeDeclaration().getName();
            final MModelEnum enumClass = getOrAddEnum(enumFqn);
            o.getEnumElemDeclarationList().forEach(new Consumer<MetaModelEnumElemDeclaration>() {
                @Override
                public void accept(MetaModelEnumElemDeclaration enumElement) {
                    enumClass.addLitteral(enumElement.getText());
                }
            });
        }
    };

    MetaModelVisitor classVisitor = new MetaModelVisitor() {
        @Override
        public void visitDeclaration(MetaModelDeclaration o) {
            o.acceptChildren(this);
        }

        @Override
        public void visitClassDeclaration(MetaModelClassDeclaration o) {
            String classFqn = o.getTypeDeclaration().getName();
            final MModelClass thisClassDeclaration = getOrAddClass(classFqn);
            o.getClassElemDeclarationList().forEach(new Consumer<MetaModelClassElemDeclaration>() {
                @Override
                public void accept(MetaModelClassElemDeclaration decl) {
                    if (decl.getRelationDeclaration() != null) {
                        MetaModelRelationDeclaration relationDecl = decl.getRelationDeclaration();
                        if (ProcessorHelper.getInstance().isPrimitive(relationDecl.getTypeDeclaration()) || ProcessorHelper.getInstance().isEnum(context, relationDecl.getTypeDeclaration())) {
                            final MModelAttribute attribute = new MModelAttribute(relationDecl.getRelationName().getText(), relationDecl.getTypeDeclaration().getName());
                            if (relationDecl.getAnnotations() != null) {
                                relationDecl.getAnnotations().getAnnotationList().forEach(new Consumer<MetaModelAnnotation>() {
                                    @Override
                                    public void accept(MetaModelAnnotation ann) {
                                        if (ann.getText().equalsIgnoreCase("@id")) {
                                            attribute.setId(true);
                                        } else if (ann.getText().toLowerCase().startsWith("@precision")) {
                                            MetaModelAnnotationParam param = ann.getAnnotationParam();
                                            if (param != null) {
                                                try {
                                                    double precision = Double.parseDouble(param.getNumber().getText());
                                                    attribute.setPrecision(precision);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    //noop
                                                }
                                            }
                                        } else {
                                            System.out.println("Unrecognized Annotation on Attribute:" + ann.getText());
                                        }
                                    }
                                });
                            }
                            if (relationDecl.getMultiplicityDeclaration() != null) {
                                if (relationDecl.getMultiplicityDeclaration().getMultiplicityDeclarationUpper().getText().equals("*")) {
                                    attribute.setSingle(false);
                                } else {
                                    attribute.setSingle(true);
                                }
                            } else {
                                attribute.setSingle(true);
                            }
                            thisClassDeclaration.addAttribute(attribute);
                        } else {
                            String relationTypeFqn = relationDecl.getTypeDeclaration().getName();
                            MModelClass relationType = getOrAddClass(relationTypeFqn);
                            final MModelReference reference = getOrAddReference(thisClassDeclaration, relationDecl.getRelationName().getText(), relationType);
                            /*
                            if (relationDecl.getAnnotations() != null) {
                                relationDecl.getAnnotations().getAnnotationList().forEach(new Consumer<MetaModelAnnotation>() {
                                    @Override
                                    public void accept(MetaModelAnnotation ann) {
                                        if (ann.getText().equalsIgnoreCase("@contained")) {
                                            reference.setContained(true);
                                        } else {
                                            System.out.println("Unrecognized Annotation on Reference:" + ann.getText());
                                        }
                                    }
                                });
                            }
                            */
                            if (relationDecl.getMultiplicityDeclaration() != null) {
                                if (relationDecl.getMultiplicityDeclaration().getMultiplicityDeclarationUpper().getText().equals("*")) {
                                    reference.setSingle(false);
                                } else {
                                    reference.setSingle(true);
                                }
                            } else {
                                reference.setSingle(true);
                            }

                            if (relationDecl.getRelationOpposite() != null) {
                                MModelReference oppositeRef = getOrAddReference(relationType, relationDecl.getRelationOpposite().getIdent().getText(), thisClassDeclaration);
                                reference.setOpposite(oppositeRef);
                                oppositeRef.setOpposite(reference);

                            }
                        }
                    } else if (decl.getOperationDeclaration() != null) {
                        MetaModelOperationDeclaration opDecl = decl.getOperationDeclaration();
                        MModelOperation operationDefinition = new MModelOperation(opDecl.getOperationName().getIdent().getText());
                        if (opDecl.getOperationReturn() != null) {
                            MModelOperationParam returnType = new MModelOperationParam();
                            returnType.type = ProcessorHelper.getInstance().convertToJavaType(opDecl.getOperationReturn().getTypeDeclaration().getName());
                            operationDefinition.returnParam = returnType;
                        }
                        if (opDecl.getOperationParams() != null) {
                            for (MetaModelOperationParam param : opDecl.getOperationParams().getOperationParamList()) {
                                MModelOperationParam param1 = new MModelOperationParam();
                                param1.type = ProcessorHelper.getInstance().convertToJavaType(param.getTypeDeclaration().getName());
                                param1.name = param.getIdent().getText();
                                operationDefinition.inputParams.add(param1);
                            }
                        }
                        thisClassDeclaration.addOperation(operationDefinition);
                    }
                }
            });

            if (o.getParentsDeclaration() != null && o.getParentsDeclaration().getTypeDeclarationList() != null) {
                o.getParentsDeclaration().getTypeDeclarationList().forEach(new Consumer<MetaModelTypeDeclaration>() {
                    @Override
                    public void accept(MetaModelTypeDeclaration parent) {
                        String parentTypeFqn = parent.getName();
                        MModelClass parentType = (MModelClass) context.getModel().get(parentTypeFqn);
                        if (parentType == null) {
                            String parentTypePackage = parentTypeFqn.substring(0, parentTypeFqn.lastIndexOf("."));
                            String parentTypeName = parentTypeFqn.substring(parentTypeFqn.lastIndexOf(".") + 1);
                            parentType = new MModelClass(parentTypeName);
                            parentType.setPack(parentTypePackage);
                            context.getModel().addClassifier(parentType);
                        }
                        thisClassDeclaration.addParent(parentType);
                    }
                });

            }
        }
    };

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


    private MModelClass getOrAddClass(String clazz) {
        MModelClassifier resolved = context.getModel().get(clazz);
        if (resolved == null) {
            String relationTypePackage = clazz.substring(0, clazz.lastIndexOf("."));
            String relationTypeName = clazz.substring(clazz.lastIndexOf(".") + 1);
            resolved = new MModelClass(relationTypeName);
            resolved.setPack(relationTypePackage);
            context.getModel().addClassifier(resolved);
            return (MModelClass) resolved;
        } else {
            if (resolved instanceof MModelClass) {
                return (MModelClass) resolved;
            } else {
                throw new RuntimeException("Naming conflict for " + clazz + ", cannot merge an enum and a class declaration");
            }
        }
    }

    private MModelEnum getOrAddEnum(String clazz) {
        MModelClassifier resolved = context.getModel().get(clazz);
        if (resolved == null) {
            String relationTypePackage = clazz.substring(0, clazz.lastIndexOf("."));
            String relationTypeName = clazz.substring(clazz.lastIndexOf(".") + 1);
            resolved = new MModelEnum(relationTypeName);
            resolved.setPack(relationTypePackage);
            context.getModel().addClassifier(resolved);
            return (MModelEnum) resolved;
        } else {
            if (resolved instanceof MModelEnum) {
                return (MModelEnum) resolved;
            } else {
                throw new RuntimeException("Naming conflict for " + clazz + ", cannot merge an enum and a class declaration");
            }
        }
    }

}
