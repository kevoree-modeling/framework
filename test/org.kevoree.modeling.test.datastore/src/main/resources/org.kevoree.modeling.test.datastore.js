var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
var geometry;
(function (geometry) {
    var GeometryModel = (function (_super) {
        __extends(GeometryModel, _super);
        function GeometryModel() {
            _super.call(this);
            this._metaModel = new org.kevoree.modeling.meta.impl.MetaModel("Geometry");
            var tempMetaClasses = new Array();
            tempMetaClasses[1] = geometry.meta.MetaShape.getInstance();
            tempMetaClasses[0] = geometry.meta.MetaLibrary.getInstance();
            this._metaModel.init(tempMetaClasses);
        }
        GeometryModel.prototype.internalCreateUniverse = function (key) {
            return new geometry.GeometryUniverse(key, this._manager);
        };
        GeometryModel.prototype.metaModel = function () {
            return this._metaModel;
        };
        GeometryModel.prototype.internalCreateObject = function (universe, time, uuid, p_clazz) {
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
        };
        GeometryModel.prototype.createShape = function (universe, time) {
            return this.create(geometry.meta.MetaShape.getInstance(), universe, time);
        };
        GeometryModel.prototype.createLibrary = function (universe, time) {
            return this.create(geometry.meta.MetaLibrary.getInstance(), universe, time);
        };
        return GeometryModel;
    })(org.kevoree.modeling.abs.AbstractKModel);
    geometry.GeometryModel = GeometryModel;
    var GeometryUniverse = (function (_super) {
        __extends(GeometryUniverse, _super);
        function GeometryUniverse(p_key, p_manager) {
            _super.call(this, p_key, p_manager);
        }
        GeometryUniverse.prototype.internal_create = function (timePoint) {
            return new geometry.impl.GeometryViewImpl(this._universe, timePoint, this._manager);
        };
        return GeometryUniverse;
    })(org.kevoree.modeling.abs.AbstractKUniverse);
    geometry.GeometryUniverse = GeometryUniverse;
    var impl;
    (function (impl) {
        var GeometryViewImpl = (function (_super) {
            __extends(GeometryViewImpl, _super);
            function GeometryViewImpl(p_universe, _time, p_manager) {
                _super.call(this, p_universe, _time, p_manager);
            }
            GeometryViewImpl.prototype.createShape = function () {
                return this.create(geometry.meta.MetaShape.getInstance());
            };
            GeometryViewImpl.prototype.createLibrary = function () {
                return this.create(geometry.meta.MetaLibrary.getInstance());
            };
            return GeometryViewImpl;
        })(org.kevoree.modeling.abs.AbstractKView);
        impl.GeometryViewImpl = GeometryViewImpl;
        var LibraryImpl = (function (_super) {
            __extends(LibraryImpl, _super);
            function LibraryImpl(p_universe, p_time, p_uuid, p_metaClass, p_manager) {
                _super.call(this, p_universe, p_time, p_uuid, p_metaClass, p_manager);
            }
            LibraryImpl.prototype.addShapes = function (p_obj) {
                this.mutate(org.kevoree.modeling.KActionType.ADD, geometry.meta.MetaLibrary.REF_SHAPES, p_obj);
                return this;
            };
            LibraryImpl.prototype.removeShapes = function (p_obj) {
                this.mutate(org.kevoree.modeling.KActionType.REMOVE, geometry.meta.MetaLibrary.REF_SHAPES, p_obj);
                return this;
            };
            LibraryImpl.prototype.getShapes = function (cb) {
                if (cb == null) {
                    return;
                }
                this.ref(geometry.meta.MetaLibrary.REF_SHAPES, function (kObjects) {
                    var casted = new Array();
                    for (var i = 0; i < kObjects.length; i++) {
                        casted[i] = kObjects[i];
                    }
                    cb(casted);
                });
            };
            LibraryImpl.prototype.sizeOfShapes = function () {
                return this.size(geometry.meta.MetaLibrary.REF_SHAPES);
            };
            LibraryImpl.prototype.addShape = function (p_shapeName, p_result) {
                var addShape_params = new Array();
                addShape_params[0] = p_shapeName;
                this._manager.operationManager().call(this, geometry.meta.MetaLibrary.OP_ADDSHAPE, addShape_params, function (o) {
                    p_result(o);
                });
            };
            return LibraryImpl;
        })(org.kevoree.modeling.abs.AbstractKObject);
        impl.LibraryImpl = LibraryImpl;
        var ShapeImpl = (function (_super) {
            __extends(ShapeImpl, _super);
            function ShapeImpl(p_universe, p_time, p_uuid, p_metaClass, p_manager) {
                _super.call(this, p_universe, p_time, p_uuid, p_metaClass, p_manager);
            }
            ShapeImpl.prototype.getColor = function () {
                return this.get(geometry.meta.MetaShape.ATT_COLOR);
            };
            ShapeImpl.prototype.setColor = function (p_obj) {
                this.set(geometry.meta.MetaShape.ATT_COLOR, p_obj);
                return this;
            };
            ShapeImpl.prototype.getName = function () {
                return this.get(geometry.meta.MetaShape.ATT_NAME);
            };
            ShapeImpl.prototype.setName = function (p_obj) {
                this.set(geometry.meta.MetaShape.ATT_NAME, p_obj);
                return this;
            };
            return ShapeImpl;
        })(org.kevoree.modeling.abs.AbstractKObject);
        impl.ShapeImpl = ShapeImpl;
    })(impl = geometry.impl || (geometry.impl = {}));
    var meta;
    (function (meta) {
        var MetaLibrary = (function (_super) {
            __extends(MetaLibrary, _super);
            function MetaLibrary() {
                _super.call(this, "geometry.Library", 0);
                var temp_all = new Array();
                var temp_references = new Array();
                temp_all[0] = MetaLibrary.REF_SHAPES;
                var temp_operations = new Array();
                temp_all[1] = MetaLibrary.OP_ADDSHAPE;
                this.init(temp_all);
            }
            MetaLibrary.getInstance = function () {
                if (MetaLibrary.INSTANCE == null) {
                    MetaLibrary.INSTANCE = new geometry.meta.MetaLibrary();
                }
                return MetaLibrary.INSTANCE;
            };
            MetaLibrary.INSTANCE = null;
            MetaLibrary.REF_SHAPES = new org.kevoree.modeling.meta.impl.MetaReference("shapes", 0, true, false, function () {
                return geometry.meta.MetaShape.getInstance();
            }, "op_shapes", function () {
                return geometry.meta.MetaLibrary.getInstance();
            });
            MetaLibrary.OP_ADDSHAPE = new org.kevoree.modeling.meta.impl.MetaOperation("addShape", 1, function () {
                return geometry.meta.MetaLibrary.getInstance();
            });
            return MetaLibrary;
        })(org.kevoree.modeling.meta.impl.MetaClass);
        meta.MetaLibrary = MetaLibrary;
        var MetaShape = (function (_super) {
            __extends(MetaShape, _super);
            function MetaShape() {
                _super.call(this, "geometry.Shape", 1);
                var temp_all = new Array();
                temp_all[0] = MetaShape.ATT_COLOR;
                temp_all[1] = MetaShape.ATT_NAME;
                var temp_references = new Array();
                temp_all[2] = MetaShape.REF_OP_SHAPES;
                var temp_operations = new Array();
                this.init(temp_all);
            }
            MetaShape.getInstance = function () {
                if (MetaShape.INSTANCE == null) {
                    MetaShape.INSTANCE = new geometry.meta.MetaShape();
                }
                return MetaShape.INSTANCE;
            };
            MetaShape.INSTANCE = null;
            MetaShape.ATT_COLOR = new org.kevoree.modeling.meta.impl.MetaAttribute("color", 0, 0, false, org.kevoree.modeling.meta.KPrimitiveTypes.STRING, org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation.instance());
            MetaShape.ATT_NAME = new org.kevoree.modeling.meta.impl.MetaAttribute("name", 1, 0, true, org.kevoree.modeling.meta.KPrimitiveTypes.STRING, org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation.instance());
            MetaShape.REF_OP_SHAPES = new org.kevoree.modeling.meta.impl.MetaReference("op_shapes", 2, false, false, function () {
                return geometry.meta.MetaLibrary.getInstance();
            }, "shapes", function () {
                return geometry.meta.MetaShape.getInstance();
            });
            return MetaShape;
        })(org.kevoree.modeling.meta.impl.MetaClass);
        meta.MetaShape = MetaShape;
    })(meta = geometry.meta || (geometry.meta = {}));
})(geometry || (geometry = {}));
//# sourceMappingURL=org.kevoree.modeling.test.datastore.js.map