module geometry {
    export class GeometryModel extends org.kevoree.modeling.abs.AbstractKModel<any> {

        private _metaModel: org.kevoree.modeling.meta.KMetaModel;
        constructor() {
            super();
            this._metaModel = new org.kevoree.modeling.meta.impl.MetaModel("Geometry");
            var tempMetaClasses: org.kevoree.modeling.meta.KMetaClass[] = new Array();
            tempMetaClasses[1] = geometry.meta.MetaShape.getInstance();
            tempMetaClasses[0] = geometry.meta.MetaLibrary.getInstance();
            (<org.kevoree.modeling.meta.impl.MetaModel>this._metaModel).init(tempMetaClasses);
        }

        public internalCreateUniverse(key: number): geometry.GeometryUniverse {
            return new geometry.GeometryUniverse(key, this._manager);
        }

        public metaModel(): org.kevoree.modeling.meta.KMetaModel {
            return this._metaModel;
        }

        public internalCreateObject(universe: number, time: number, uuid: number, p_clazz: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.KObject {
            if (p_clazz == null) {
                return null;
            }
            switch (p_clazz.index()) {
                case 1: 
                return new geometry.impl.ShapeImpl(universe, time, uuid, p_clazz, this._manager);
                case 0: 
                return new geometry.impl.LibraryImpl(universe, time, uuid, p_clazz, this._manager);
                default: 
                return new org.kevoree.modeling.meta.impl.GenericObject(universe, time, uuid, p_clazz, this._manager);
            }
        }

        public createShape(universe: number, time: number): geometry.Shape {
            return <geometry.Shape>this.create(geometry.meta.MetaShape.getInstance(), universe, time);
        }

        public createLibrary(universe: number, time: number): geometry.Library {
            return <geometry.Library>this.create(geometry.meta.MetaLibrary.getInstance(), universe, time);
        }

    }

    export class GeometryUniverse extends org.kevoree.modeling.abs.AbstractKUniverse<any, any, any> {

        constructor(p_key: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
            super(p_key, p_manager);
        }

        public internal_create(timePoint: number): geometry.GeometryView {
            return new geometry.impl.GeometryViewImpl(this._universe, timePoint, this._manager);
        }

    }

    export interface GeometryView extends org.kevoree.modeling.KView {

        createShape(): geometry.Shape;

        createLibrary(): geometry.Library;

    }

    export interface Library extends org.kevoree.modeling.KObject {

        addShapes(p_obj: geometry.Shape): geometry.Library;

        removeShapes(p_obj: geometry.Shape): geometry.Library;

        getShapes(cb: (p : geometry.Shape[]) => void): void;

        sizeOfShapes(): number;

        addShape(shapeName: string, result: (p : boolean) => void): void;

    }

    export interface Shape extends org.kevoree.modeling.KObject {

        getColor(): string;

        setColor(p_obj: string): geometry.Shape;

        getName(): string;

        setName(p_obj: string): geometry.Shape;

    }

    export module impl {
        export class GeometryViewImpl extends org.kevoree.modeling.abs.AbstractKView implements geometry.GeometryView {

            constructor(p_universe: number, _time: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
                super(p_universe, _time, p_manager);
            }

            public createShape(): geometry.Shape {
                return <geometry.Shape>this.create(geometry.meta.MetaShape.getInstance());
            }

            public createLibrary(): geometry.Library {
                return <geometry.Library>this.create(geometry.meta.MetaLibrary.getInstance());
            }

        }

        export class LibraryImpl extends org.kevoree.modeling.abs.AbstractKObject implements geometry.Library {

            constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
                super(p_universe, p_time, p_uuid, p_metaClass, p_manager);
            }

            public addShapes(p_obj: geometry.Shape): geometry.Library {
                this.mutate(org.kevoree.modeling.KActionType.ADD, geometry.meta.MetaLibrary.REF_SHAPES, p_obj);
                return this;
            }

            public removeShapes(p_obj: geometry.Shape): geometry.Library {
                this.mutate(org.kevoree.modeling.KActionType.REMOVE, geometry.meta.MetaLibrary.REF_SHAPES, p_obj);
                return this;
            }

            public getShapes(cb: (p : geometry.Shape[]) => void): void {
                if (cb == null) {
                    return;
                }
                this.ref(geometry.meta.MetaLibrary.REF_SHAPES,  (kObjects : org.kevoree.modeling.KObject[]) => {
                    var casted: geometry.Shape[] = new Array();
                    for (var i: number = 0; i < kObjects.length; i++) {
                        casted[i] = <geometry.Shape>kObjects[i];
                    }
                    cb(casted);
                });
            }

            public sizeOfShapes(): number {
                return this.size(geometry.meta.MetaLibrary.REF_SHAPES);
            }

            public addShape(p_shapeName: string, p_result: (p : boolean) => void): void {
                var addShape_params: any[] = new Array();
                addShape_params[0] = p_shapeName;
                this._manager.operationManager().call(this, geometry.meta.MetaLibrary.OP_ADDSHAPE, addShape_params,  (o : any) => {
                    p_result(<boolean>o);
                });
            }

        }

        export class ShapeImpl extends org.kevoree.modeling.abs.AbstractKObject implements geometry.Shape {

            constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
                super(p_universe, p_time, p_uuid, p_metaClass, p_manager);
            }

            public getColor(): string {
                return <string>this.get(geometry.meta.MetaShape.ATT_COLOR);
            }

            public setColor(p_obj: string): geometry.Shape {
                this.set(geometry.meta.MetaShape.ATT_COLOR, p_obj);
                return this;
            }

            public getName(): string {
                return <string>this.get(geometry.meta.MetaShape.ATT_NAME);
            }

            public setName(p_obj: string): geometry.Shape {
                this.set(geometry.meta.MetaShape.ATT_NAME, p_obj);
                return this;
            }

        }

    }
    export module meta {
        export class MetaLibrary extends org.kevoree.modeling.meta.impl.MetaClass {

            private static INSTANCE: geometry.meta.MetaLibrary = null;
            public static REF_SHAPES: org.kevoree.modeling.meta.KMetaReference = new org.kevoree.modeling.meta.impl.MetaReference("shapes", 0, true, false,  () => {
                return geometry.meta.MetaShape.getInstance();
            }, "op_shapes",  () => {
                return geometry.meta.MetaLibrary.getInstance();
            });
            public static OP_ADDSHAPE: org.kevoree.modeling.meta.KMetaOperation = new org.kevoree.modeling.meta.impl.MetaOperation("addShape", 1,  () => {
                return geometry.meta.MetaLibrary.getInstance();
            });
            public static getInstance(): geometry.meta.MetaLibrary {
                if (MetaLibrary.INSTANCE == null) {
                    MetaLibrary.INSTANCE = new geometry.meta.MetaLibrary();
                }
                return MetaLibrary.INSTANCE;
            }

            constructor() {
                super("geometry.Library", 0);
                var temp_all: org.kevoree.modeling.meta.KMeta[] = new Array();
                var temp_references: org.kevoree.modeling.meta.KMetaReference[] = new Array();
                temp_all[0] = MetaLibrary.REF_SHAPES;
                var temp_operations: org.kevoree.modeling.meta.KMetaOperation[] = new Array();
                temp_all[1] = MetaLibrary.OP_ADDSHAPE;
                this.init(temp_all);
            }

        }

        export class MetaShape extends org.kevoree.modeling.meta.impl.MetaClass {

            private static INSTANCE: geometry.meta.MetaShape = null;
            public static ATT_COLOR: org.kevoree.modeling.meta.KMetaAttribute = new org.kevoree.modeling.meta.impl.MetaAttribute("color", 0, 0, false, org.kevoree.modeling.meta.KPrimitiveTypes.STRING, org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation.instance());
            public static ATT_NAME: org.kevoree.modeling.meta.KMetaAttribute = new org.kevoree.modeling.meta.impl.MetaAttribute("name", 1, 0, true, org.kevoree.modeling.meta.KPrimitiveTypes.STRING, org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation.instance());
            public static REF_OP_SHAPES: org.kevoree.modeling.meta.KMetaReference = new org.kevoree.modeling.meta.impl.MetaReference("op_shapes", 2, false, false,  () => {
                return geometry.meta.MetaLibrary.getInstance();
            }, "shapes",  () => {
                return geometry.meta.MetaShape.getInstance();
            });
            public static getInstance(): geometry.meta.MetaShape {
                if (MetaShape.INSTANCE == null) {
                    MetaShape.INSTANCE = new geometry.meta.MetaShape();
                }
                return MetaShape.INSTANCE;
            }

            constructor() {
                super("geometry.Shape", 1);
                var temp_all: org.kevoree.modeling.meta.KMeta[] = new Array();
                temp_all[0] = MetaShape.ATT_COLOR;
                temp_all[1] = MetaShape.ATT_NAME;
                var temp_references: org.kevoree.modeling.meta.KMetaReference[] = new Array();
                temp_all[2] = MetaShape.REF_OP_SHAPES;
                var temp_operations: org.kevoree.modeling.meta.KMetaOperation[] = new Array();
                this.init(temp_all);
            }

        }

    }
}
