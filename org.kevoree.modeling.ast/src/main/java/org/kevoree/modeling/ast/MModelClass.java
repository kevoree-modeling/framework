package org.kevoree.modeling.ast;

import java.util.*;

public class MModelClass extends MModelClassifier {

    //public int globalIndex = 0;

    private Map<String, MModelAttribute> attributes = new HashMap<String, MModelAttribute>();
    private Map<String, MModelRelation> references = new HashMap<String, MModelRelation>();
    private Map<String, MModelClass> parents = new HashMap<String, MModelClass>();
    private Map<String, MModelOperation> operations = new HashMap<String, MModelOperation>();

    private Map<String, MModelInput> inputs = new HashMap<String, MModelInput>();
    private Map<String, MModelOutput> outputs = new HashMap<String, MModelOutput>();
    
    private MModelDependencies dependencies = null;

    public MModelClass(String name) {
        this.name = name;
    }

    public boolean isInferred() {
        return this.inference != null;
    }

    private String inference = null;
    private Long temporalResolution = null;
    private Boolean canHaveInstance = true;

    public Boolean getCanHaveInstance() {
        return canHaveInstance;
    }

    public void setCanHaveInstance(Boolean instance) {
        this.canHaveInstance = instance;
    }

    public Long getTemporalLimit() {
        return temporalLimit;
    }

    public void setTemporalLimit(Long temporalLimit) {
        this.temporalLimit = temporalLimit;
    }

    public Long getTemporalResolution() {
        return temporalResolution;
    }

    public void setTemporalResolution(Long temporalResolution) {
        this.temporalResolution = temporalResolution;
    }

    public String getInference() {
        return inference;
    }

    public void setInference(String inference) {
        this.inference = inference;
    }

    private Long temporalLimit = null;

    public void addAttribute(MModelAttribute att) {
        attributes.put(att.getName(), att);
    }

    public Collection<MModelAttribute> getAttributes() {
        HashMap<String, MModelAttribute> collected = new HashMap<String, MModelAttribute>();
        HashMap<String, MModelClass> passed = new HashMap<String, MModelClass>();
        deep_collect_atts(collected, passed);
        for (String collectedKey : collected.keySet()) {
            if (!attributes.containsKey(collectedKey)) {
                attributes.put(collectedKey, collected.get(collectedKey).clone());
            }
        }
        return attributes.values();
    }

    private void deep_collect_atts(HashMap<String, MModelAttribute> collector, HashMap<String, MModelClass> passed) {
        if (passed.containsKey(this.getName())) {
            return;
        } else {
            for (String key : attributes.keySet()) {
                if (!collector.containsKey(key)) {
                    collector.put(key, attributes.get(key));
                }
            }
            passed.put(getName(), this);
            for (MModelClass parent : getParents()) {
                parent.deep_collect_atts(collector, passed);
            }
        }
    }

    public void addReference(MModelRelation ref) {
        references.put(ref.getName(), ref);
    }

    public void addDependency(MModelDependency el) {
        if (dependencies == null) {
            dependencies = new MModelDependencies("dependencies", null);
        }
        dependencies.dependencies.put(el.getName(), el);
    }

    public MModelDependencies dependencies() {
        return dependencies;
    }

    public MModelDependency[] getDependencies() {
        MModelDependency[] flat = dependencies.dependencies.values().toArray(new MModelDependency[dependencies.dependencies.size()]);
        Arrays.sort(flat, new Comparator<MModelDependency>() {
            @Override
            public int compare(MModelDependency o1, MModelDependency o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });
        return flat;
    }

    public void addInput(MModelInput el) {
        inputs.put(el.getName(), el);
    }

    public Collection<MModelInput> getInputs() {
        return inputs.values();
    }

    public void addOutput(MModelOutput el) {
        outputs.put(el.getName(), el);
    }

    public MModelOutput[] getOutputs() {
        MModelOutput[] flat = outputs.values().toArray(new MModelOutput[outputs.size()]);
        Arrays.sort(flat, new Comparator<MModelOutput>() {
            @Override
            public int compare(MModelOutput o1, MModelOutput o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });
        return flat;
    }

    public boolean multipleOutput() {
        return this.getOutputs().length > 1;
    }

    public Collection<MModelRelation> getReferences() {
        HashMap<String, MModelRelation> collected = new HashMap<String, MModelRelation>();
        HashMap<String, MModelClass> passed = new HashMap<String, MModelClass>();
        deep_collect_refs(collected, passed);
        for (String collectedKey : collected.keySet()) {
            if (!references.containsKey(collectedKey)) {
                references.put(collectedKey, collected.get(collectedKey).clone());
            }
        }
        return references.values();
    }

    private void deep_collect_refs(HashMap<String, MModelRelation> collector, HashMap<String, MModelClass> passed) {
        if (passed.containsKey(this.getName())) {
            return;
        } else {
            for (String key : references.keySet()) {
                if (!collector.containsKey(key)) {
                    collector.put(key, references.get(key));
                }
            }
            passed.put(getName(), this);
            for (MModelClass parent : getParents()) {
                parent.deep_collect_refs(collector, passed);
            }
        }
    }

    public void addParent(MModelClass cls) {
        parents.put(cls.getName(), cls);
    }

    public Collection<MModelClass> getParents() {
        return parents.values();
    }

    public Collection<MModelOperation> getOperations() {
        HashMap<String, MModelOperation> collected = new HashMap<String, MModelOperation>();
        deep_collect_ops(collected, new HashMap<String, MModelClass>());
        for (String collectedKey : collected.keySet()) {
            if (!operations.containsKey(collectedKey)) {
                operations.put(collectedKey, collected.get(collectedKey).clone());
            }
        }
        return operations.values();
    }

    private void deep_collect_ops(HashMap<String, MModelOperation> collector, HashMap<String, MModelClass> passed) {
        if (!passed.containsKey(this.getName())) {
            for (String key : operations.keySet()) {
                if (!collector.containsKey(key)) {
                    collector.put(key, operations.get(key));
                }
            }
            passed.put(getName(), this);
            for (MModelClass parent : getParents()) {
                parent.deep_collect_ops(collector, passed);
            }
        }
    }

    public void addOperation(MModelOperation operation) {
        this.operations.put(operation.getName(), operation);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ");
        sb.append(getPack() + "." + getName());
        sb.append(", parent:");
        sb.append(getParents());
        sb.append("{\n \tattributes{\n");
        for (MModelAttribute att : attributes.values()) {
            sb.append("\t\t" + att.getName());
            sb.append(":");
            sb.append(att.getType());
            sb.append("\n");
        }
        sb.append("\t}\n");
        sb.append("\treferences:{\n");
        for (MModelRelation att : references.values()) {
            sb.append("\t\t" + att.getName());
            sb.append(":");
            sb.append(att.getType().getName());
            if (att.getOpposite() != null) {
                sb.append(" opposite ");
                sb.append(att.getOpposite());
            }
            sb.append("\n");
        }
        sb.append("\t}\n\tfunctions:{\n");
        for (MModelOperation op : operations.values()) {
            sb.append("\t\t" + op.getName());
            sb.append("(");
            boolean isFirst = true;
            for (MModelOperationParam param : op.inputParams) {
                if (!isFirst) {
                    sb.append(", ");
                }
                sb.append(param.getName());
                sb.append(" : ");
                sb.append(param.getType());
                isFirst = false;
            }
            sb.append(")");
            if (op.getReturnType() != null) {
                sb.append(":");
                sb.append(op.getReturnType());
            }
            sb.append("\n");
        }
        sb.append("\t}\n}\n");
        return sb.toString();
    }

    public boolean containsDependencies() {
        return this.dependencies != null && this.dependencies.dependencies.size() > 0;
    }
}
