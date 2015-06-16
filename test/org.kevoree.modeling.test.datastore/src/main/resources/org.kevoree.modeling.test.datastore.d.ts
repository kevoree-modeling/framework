declare module geometry {
    class GeometryModel extends org.kevoree.modeling.abs.AbstractKModel<any> {
        private _metaModel;
        constructor();
        internalCreateUniverse(key: number): geometry.GeometryUniverse;
        metaModel(): org.kevoree.modeling.meta.KMetaModel;
        internalCreateObject(universe: number, time: number, uuid: number, p_clazz: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.KObject;
        createShape(universe: number, time: number): geometry.Shape;
        createLibrary(universe: number, time: number): geometry.Library;
    }
    class GeometryUniverse extends org.kevoree.modeling.abs.AbstractKUniverse<any, any, any> {
        constructor(p_key: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
        internal_create(timePoint: number): geometry.GeometryView;
    }
    interface GeometryView extends org.kevoree.modeling.KView {
        createShape(): geometry.Shape;
        createLibrary(): geometry.Library;
    }
    interface Library extends org.kevoree.modeling.KObject {
        addShapes(p_obj: geometry.Shape): geometry.Library;
        removeShapes(p_obj: geometry.Shape): geometry.Library;
        getShapes(cb: (p: geometry.Shape[]) => void): void;
        sizeOfShapes(): number;
        addShape(shapeName: string, result: (p: boolean) => void): void;
    }
    interface Shape extends org.kevoree.modeling.KObject {
        getColor(): string;
        setColor(p_obj: string): geometry.Shape;
        getName(): string;
        setName(p_obj: string): geometry.Shape;
    }
    module impl {
        class GeometryViewImpl extends org.kevoree.modeling.abs.AbstractKView implements geometry.GeometryView {
            constructor(p_universe: number, _time: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
            createShape(): geometry.Shape;
            createLibrary(): geometry.Library;
        }
        class LibraryImpl extends org.kevoree.modeling.abs.AbstractKObject implements geometry.Library {
            constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
            addShapes(p_obj: geometry.Shape): geometry.Library;
            removeShapes(p_obj: geometry.Shape): geometry.Library;
            getShapes(cb: (p: geometry.Shape[]) => void): void;
            sizeOfShapes(): number;
            addShape(p_shapeName: string, p_result: (p: boolean) => void): void;
        }
        class ShapeImpl extends org.kevoree.modeling.abs.AbstractKObject implements geometry.Shape {
            constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
            getColor(): string;
            setColor(p_obj: string): geometry.Shape;
            getName(): string;
            setName(p_obj: string): geometry.Shape;
        }
    }
    module meta {
        class MetaLibrary extends org.kevoree.modeling.meta.impl.MetaClass {
            private static INSTANCE;
            static REF_SHAPES: org.kevoree.modeling.meta.KMetaReference;
            static OP_ADDSHAPE: org.kevoree.modeling.meta.KMetaOperation;
            static getInstance(): geometry.meta.MetaLibrary;
            constructor();
        }
        class MetaShape extends org.kevoree.modeling.meta.impl.MetaClass {
            private static INSTANCE;
            static ATT_COLOR: org.kevoree.modeling.meta.KMetaAttribute;
            static ATT_NAME: org.kevoree.modeling.meta.KMetaAttribute;
            static REF_OP_SHAPES: org.kevoree.modeling.meta.KMetaReference;
            static getInstance(): geometry.meta.MetaShape;
            constructor();
        }
    }
}
