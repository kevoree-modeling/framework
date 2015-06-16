module org {
    export module kevoree {
        export module modeling {
            export class KActionType {

                public static CALL: KActionType = new KActionType();
                public static CALL_RESPONSE: KActionType = new KActionType();
                public static SET: KActionType = new KActionType();
                public static ADD: KActionType = new KActionType();
                public static REMOVE: KActionType = new KActionType();
                public static NEW: KActionType = new KActionType();
                public equals(other: any): boolean {
                    return this == other;
                }
                public static _KActionTypeVALUES : KActionType[] = [
                    KActionType.CALL
                    ,KActionType.CALL_RESPONSE
                    ,KActionType.SET
                    ,KActionType.ADD
                    ,KActionType.REMOVE
                    ,KActionType.NEW
                ];
                public static values():KActionType[]{
                    return KActionType._KActionTypeVALUES;
                }
            }

            export interface KCallback<A> {

                on(a: A): void;

            }

            export class KConfig {

                public static TREE_CACHE_SIZE: number = 3;
                public static CALLBACK_HISTORY: number = 1000;
                public static LONG_SIZE: number = 53;
                public static PREFIX_SIZE: number = 16;
                public static BEGINNING_OF_TIME: number = -0x001FFFFFFFFFFFFE;
                public static END_OF_TIME: number = 0x001FFFFFFFFFFFFE;
                public static NULL_LONG: number = 0x001FFFFFFFFFFFFF;
                public static KEY_PREFIX_MASK: number = 0x0000001FFFFFFFFF;
                public static KEY_SEP: string = '/';
                public static KEY_SIZE: number = 3;
                public static CACHE_INIT_SIZE: number = 16;
                public static CACHE_LOAD_FACTOR: number = (<number>75 / <number>100);
            }

            export class KContentKey {

                public universe: number;
                public time: number;
                public obj: number;
                constructor(p_universeID: number, p_timeID: number, p_objID: number) {
                    this.universe = p_universeID;
                    this.time = p_timeID;
                    this.obj = p_objID;
                }

                public static createUniverseTree(p_objectID: number): org.kevoree.modeling.KContentKey {
                    return new org.kevoree.modeling.KContentKey(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, p_objectID);
                }

                public static createTimeTree(p_universeID: number, p_objectID: number): org.kevoree.modeling.KContentKey {
                    return new org.kevoree.modeling.KContentKey(p_universeID, org.kevoree.modeling.KConfig.NULL_LONG, p_objectID);
                }

                public static createObject(p_universeID: number, p_quantaID: number, p_objectID: number): org.kevoree.modeling.KContentKey {
                    return new org.kevoree.modeling.KContentKey(p_universeID, p_quantaID, p_objectID);
                }

                public static createGlobalUniverseTree(): org.kevoree.modeling.KContentKey {
                    return new org.kevoree.modeling.KContentKey(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG);
                }

                public static createRootUniverseTree(): org.kevoree.modeling.KContentKey {
                    return new org.kevoree.modeling.KContentKey(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.END_OF_TIME);
                }

                public static createRootTimeTree(universeID: number): org.kevoree.modeling.KContentKey {
                    return new org.kevoree.modeling.KContentKey(universeID, org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.END_OF_TIME);
                }

                public static createLastPrefix(): org.kevoree.modeling.KContentKey {
                    return new org.kevoree.modeling.KContentKey(org.kevoree.modeling.KConfig.END_OF_TIME, org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG);
                }

                public static createLastObjectIndexFromPrefix(prefix: number): org.kevoree.modeling.KContentKey {
                    return new org.kevoree.modeling.KContentKey(org.kevoree.modeling.KConfig.END_OF_TIME, org.kevoree.modeling.KConfig.NULL_LONG, java.lang.Long.parseLong(prefix.toString()));
                }

                public static createLastUniverseIndexFromPrefix(prefix: number): org.kevoree.modeling.KContentKey {
                    return new org.kevoree.modeling.KContentKey(org.kevoree.modeling.KConfig.END_OF_TIME, org.kevoree.modeling.KConfig.NULL_LONG, java.lang.Long.parseLong(prefix.toString()));
                }

                public static create(payload: string): org.kevoree.modeling.KContentKey {
                    if (payload == null || payload.length == 0) {
                        return null;
                    } else {
                        var temp: number[] = new Array();
                        for (var i: number = 0; i < org.kevoree.modeling.KConfig.KEY_SIZE; i++) {
                            temp[i] = org.kevoree.modeling.KConfig.NULL_LONG;
                        }
                        var maxRead: number = payload.length;
                        var indexStartElem: number = -1;
                        var indexElem: number = 0;
                        for (var i: number = 0; i < maxRead; i++) {
                            if (payload.charAt(i) == org.kevoree.modeling.KConfig.KEY_SEP) {
                                if (indexStartElem != -1) {
                                    try {
                                        temp[indexElem] = java.lang.Long.parseLong(payload.substring(indexStartElem, i));
                                    } catch ($ex$) {
                                        if ($ex$ instanceof java.lang.Exception) {
                                            var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                            e.printStackTrace();
                                        } else {
                                            throw $ex$;
                                        }
                                    }
                                }
                                indexStartElem = -1;
                                indexElem = indexElem + 1;
                            } else {
                                if (indexStartElem == -1) {
                                    indexStartElem = i;
                                }
                            }
                        }
                        if (indexStartElem != -1) {
                            try {
                                temp[indexElem] = java.lang.Long.parseLong(payload.substring(indexStartElem, maxRead));
                            } catch ($ex$) {
                                if ($ex$ instanceof java.lang.Exception) {
                                    var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                    e.printStackTrace();
                                } else {
                                    throw $ex$;
                                }
                            }
                        }
                        return new org.kevoree.modeling.KContentKey(temp[0], temp[1], temp[2]);
                    }
                }

                public toString(): string {
                    var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
                    if (this.universe != org.kevoree.modeling.KConfig.NULL_LONG) {
                        buffer.append(this.universe);
                    }
                    buffer.append(org.kevoree.modeling.KConfig.KEY_SEP);
                    if (this.time != org.kevoree.modeling.KConfig.NULL_LONG) {
                        buffer.append(this.time);
                    }
                    buffer.append(org.kevoree.modeling.KConfig.KEY_SEP);
                    if (this.obj != org.kevoree.modeling.KConfig.NULL_LONG) {
                        buffer.append(this.obj);
                    }
                    return buffer.toString();
                }

                public equals(param: any): boolean {
                    if (param instanceof org.kevoree.modeling.KContentKey) {
                        var remote: org.kevoree.modeling.KContentKey = <org.kevoree.modeling.KContentKey>param;
                        return remote.universe == this.universe && remote.time == this.time && remote.obj == this.obj;
                    } else {
                        return false;
                    }
                }

            }

            export interface KModel<A extends org.kevoree.modeling.KUniverse<any, any, any>> {

                key(): number;

                newUniverse(): A;

                universe(key: number): A;

                manager(): org.kevoree.modeling.memory.manager.KMemoryManager;

                setContentDeliveryDriver(dataBase: org.kevoree.modeling.cdn.KContentDeliveryDriver): org.kevoree.modeling.KModel<any>;

                setScheduler(scheduler: org.kevoree.modeling.scheduler.KScheduler): org.kevoree.modeling.KModel<any>;

                setOperation(metaOperation: org.kevoree.modeling.meta.KMetaOperation, operation: (p : org.kevoree.modeling.KObject, p1 : any[], p2 : (p : any) => void) => void): void;

                setInstanceOperation(metaOperation: org.kevoree.modeling.meta.KMetaOperation, target: org.kevoree.modeling.KObject, operation: (p : org.kevoree.modeling.KObject, p1 : any[], p2 : (p : any) => void) => void): void;

                metaModel(): org.kevoree.modeling.meta.KMetaModel;

                defer(): org.kevoree.modeling.defer.KDefer;

                save(cb: (p : any) => void): void;

                discard(cb: (p : any) => void): void;

                connect(cb: (p : any) => void): void;

                close(cb: (p : any) => void): void;

                clearListenerGroup(groupID: number): void;

                nextGroup(): number;

                createByName(metaClassName: string, universe: number, time: number): org.kevoree.modeling.KObject;

                create(clazz: org.kevoree.modeling.meta.KMetaClass, universe: number, time: number): org.kevoree.modeling.KObject;

            }

            export interface KObject {

                universe(): number;

                now(): number;

                uuid(): number;

                delete(cb: (p : any) => void): void;

                select(query: string, cb: (p : org.kevoree.modeling.KObject[]) => void): void;

                listen(groupId: number, listener: (p : org.kevoree.modeling.KObject, p1 : org.kevoree.modeling.meta.KMeta[]) => void): void;

                visitAttributes(visitor: (p : org.kevoree.modeling.meta.KMetaAttribute, p1 : any) => void): void;

                visit(visitor: (p : org.kevoree.modeling.KObject) => org.kevoree.modeling.traversal.visitor.KVisitResult, cb: (p : any) => void): void;

                timeWalker(): org.kevoree.modeling.KTimeWalker;

                metaClass(): org.kevoree.modeling.meta.KMetaClass;

                mutate(actionType: org.kevoree.modeling.KActionType, metaReference: org.kevoree.modeling.meta.KMetaReference, param: org.kevoree.modeling.KObject): void;

                ref(metaReference: org.kevoree.modeling.meta.KMetaReference, cb: (p : org.kevoree.modeling.KObject[]) => void): void;

                traversal(): org.kevoree.modeling.traversal.KTraversal;

                get(attribute: org.kevoree.modeling.meta.KMetaAttribute): any;

                getByName(atributeName: string): any;

                set(attribute: org.kevoree.modeling.meta.KMetaAttribute, payload: any): void;

                setByName(atributeName: string, payload: any): void;

                toJSON(): string;

                equals(other: any): boolean;

                jump(time: number, callback: (p : org.kevoree.modeling.KObject) => void): void;

                referencesWith(o: org.kevoree.modeling.KObject): org.kevoree.modeling.meta.KMetaReference[];

                call(operation: org.kevoree.modeling.meta.KMetaOperation, params: any[], cb: (p : any) => void): void;

                manager(): org.kevoree.modeling.memory.manager.KMemoryManager;

            }

            export interface KTimeWalker {

                allTimes(cb: (p : number[]) => void): void;

                timesBefore(endOfSearch: number, cb: (p : number[]) => void): void;

                timesAfter(beginningOfSearch: number, cb: (p : number[]) => void): void;

                timesBetween(beginningOfSearch: number, endOfSearch: number, cb: (p : number[]) => void): void;

            }

            export interface KType {

                name(): string;

                isEnum(): boolean;

            }

            export interface KUniverse<A extends org.kevoree.modeling.KView, B extends org.kevoree.modeling.KUniverse<any, any, any>, C extends org.kevoree.modeling.KModel<any>> {

                key(): number;

                time(timePoint: number): A;

                model(): C;

                equals(other: any): boolean;

                diverge(): B;

                origin(): B;

                descendants(): java.util.List<B>;

                delete(cb: (p : any) => void): void;

                lookupAllTimes(uuid: number, times: number[], cb: (p : org.kevoree.modeling.KObject[]) => void): void;

                listenAll(groupId: number, objects: number[], multiListener: (p : org.kevoree.modeling.KObject[]) => void): void;

            }

            export interface KView {

                createByName(metaClassName: string): org.kevoree.modeling.KObject;

                create(clazz: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.KObject;

                select(query: string, cb: (p : org.kevoree.modeling.KObject[]) => void): void;

                lookup(key: number, cb: (p : org.kevoree.modeling.KObject) => void): void;

                lookupAll(keys: number[], cb: (p : org.kevoree.modeling.KObject[]) => void): void;

                universe(): number;

                now(): number;

                json(): org.kevoree.modeling.format.KModelFormat;

                xmi(): org.kevoree.modeling.format.KModelFormat;

                equals(other: any): boolean;

                setRoot(elem: org.kevoree.modeling.KObject, cb: (p : any) => void): void;

                getRoot(cb: (p : org.kevoree.modeling.KObject) => void): void;

            }

            export module abs {
                export class AbstractDataType implements org.kevoree.modeling.KType {

                    private _name: string;
                    private _isEnum: boolean;
                    constructor(p_name: string, p_isEnum: boolean) {
                        this._name = p_name;
                        this._isEnum = p_isEnum;
                    }

                    public name(): string {
                        return this._name;
                    }

                    public isEnum(): boolean {
                        return this._isEnum;
                    }

                }

                export class AbstractKModel<A extends org.kevoree.modeling.KUniverse<any, any, any>> implements org.kevoree.modeling.KModel<any> {

                    public _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                    private _key: number;
                    constructor() {
                        this._manager = new org.kevoree.modeling.memory.manager.impl.HeapMemoryManager(this);
                        this._key = this._manager.nextModelKey();
                    }

                    public metaModel(): org.kevoree.modeling.meta.KMetaModel {
                        throw "Abstract method";
                    }

                    public connect(cb: (p : any) => void): void {
                        this._manager.connect(cb);
                    }

                    public close(cb: (p : any) => void): void {
                        this._manager.close(cb);
                    }

                    public manager(): org.kevoree.modeling.memory.manager.KMemoryManager {
                        return this._manager;
                    }

                    public newUniverse(): A {
                        var nextKey: number = this._manager.nextUniverseKey();
                        var newDimension: A = this.internalCreateUniverse(nextKey);
                        this.manager().initUniverse(newDimension, null);
                        return newDimension;
                    }

                    public internalCreateUniverse(universe: number): A {
                        throw "Abstract method";
                    }

                    public internalCreateObject(universe: number, time: number, uuid: number, clazz: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.KObject {
                        throw "Abstract method";
                    }

                    public createProxy(universe: number, time: number, uuid: number, clazz: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.KObject {
                        return this.internalCreateObject(universe, time, uuid, clazz);
                    }

                    public universe(key: number): A {
                        var newDimension: A = this.internalCreateUniverse(key);
                        this.manager().initUniverse(newDimension, null);
                        return newDimension;
                    }

                    public save(cb: (p : any) => void): void {
                        this._manager.save(cb);
                    }

                    public discard(cb: (p : any) => void): void {
                        this._manager.discard(null, cb);
                    }

                    public setContentDeliveryDriver(p_driver: org.kevoree.modeling.cdn.KContentDeliveryDriver): org.kevoree.modeling.KModel<any> {
                        this.manager().setContentDeliveryDriver(p_driver);
                        return this;
                    }

                    public setScheduler(p_scheduler: org.kevoree.modeling.scheduler.KScheduler): org.kevoree.modeling.KModel<any> {
                        this.manager().setScheduler(p_scheduler);
                        return this;
                    }

                    public setOperation(metaOperation: org.kevoree.modeling.meta.KMetaOperation, operation: (p : org.kevoree.modeling.KObject, p1 : any[], p2 : (p : any) => void) => void): void {
                        this.manager().operationManager().registerOperation(metaOperation, operation, null);
                    }

                    public setInstanceOperation(metaOperation: org.kevoree.modeling.meta.KMetaOperation, target: org.kevoree.modeling.KObject, operation: (p : org.kevoree.modeling.KObject, p1 : any[], p2 : (p : any) => void) => void): void {
                        this.manager().operationManager().registerOperation(metaOperation, operation, target);
                    }

                    public defer(): org.kevoree.modeling.defer.KDefer {
                        return new org.kevoree.modeling.defer.impl.Defer();
                    }

                    public key(): number {
                        return this._key;
                    }

                    public clearListenerGroup(groupID: number): void {
                        this.manager().cdn().unregisterGroup(groupID);
                    }

                    public nextGroup(): number {
                        return this.manager().nextGroupKey();
                    }

                    public create(clazz: org.kevoree.modeling.meta.KMetaClass, universe: number, time: number): org.kevoree.modeling.KObject {
                        if (!org.kevoree.modeling.util.Checker.isDefined(clazz)) {
                            return null;
                        }
                        var newObj: org.kevoree.modeling.KObject = this.internalCreateObject(universe, time, this._manager.nextObjectKey(), clazz);
                        if (newObj != null) {
                            this._manager.initKObject(newObj);
                        }
                        return newObj;
                    }

                    public createByName(metaClassName: string, universe: number, time: number): org.kevoree.modeling.KObject {
                        return this.create(this._manager.model().metaModel().metaClassByName(metaClassName), universe, time);
                    }

                }

                export class AbstractKObject implements org.kevoree.modeling.KObject {

                    public _uuid: number;
                    public _time: number;
                    public _universe: number;
                    private _metaClass: org.kevoree.modeling.meta.KMetaClass;
                    public _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                    private static OUT_OF_CACHE_MSG: string = "Out of cache Error";
                    constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
                        this._universe = p_universe;
                        this._time = p_time;
                        this._uuid = p_uuid;
                        this._metaClass = p_metaClass;
                        this._manager = p_manager;
                        this._manager.cache().monitor(this);
                    }

                    public uuid(): number {
                        return this._uuid;
                    }

                    public metaClass(): org.kevoree.modeling.meta.KMetaClass {
                        return this._metaClass;
                    }

                    public now(): number {
                        return this._time;
                    }

                    public universe(): number {
                        return this._universe;
                    }

                    public timeWalker(): org.kevoree.modeling.KTimeWalker {
                        return new org.kevoree.modeling.abs.AbstractTimeWalker(this);
                    }

                    public delete(cb: (p : any) => void): void {
                        var selfPointer: org.kevoree.modeling.KObject = this;
                        var rawPayload: org.kevoree.modeling.memory.struct.segment.KMemorySegment = this._manager.segment(this._universe, this._time, this._uuid, org.kevoree.modeling.memory.manager.AccessMode.DELETE, this._metaClass, null);
                        if (rawPayload == null) {
                            cb(new java.lang.Exception(AbstractKObject.OUT_OF_CACHE_MSG));
                        } else {
                            var collector: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                            var metaElements: org.kevoree.modeling.meta.KMeta[] = this._metaClass.metaElements();
                            for (var i: number = 0; i < metaElements.length; i++) {
                                if (metaElements[i] instanceof org.kevoree.modeling.meta.impl.MetaReference) {
                                    var inboundsKeys: number[] = rawPayload.getRef(metaElements[i].index(), this._metaClass);
                                    for (var j: number = 0; j < inboundsKeys.length; j++) {
                                        collector.put(inboundsKeys[j], inboundsKeys[j]);
                                    }
                                }
                            }
                            var flatCollected: number[] = new Array();
                            var indexI: number[] = new Array();
                            indexI[0] = 0;
                            collector.each( (key : number, value : number) => {
                                flatCollected[indexI[0]] = key;
                                indexI[0]++;
                            });
                            this._manager.lookupAllobjects(this._universe, this._time, flatCollected,  (resolved : org.kevoree.modeling.KObject[]) => {
                                for (var i: number = 0; i < resolved.length; i++) {
                                    if (resolved[i] != null) {
                                        var linkedReferences: org.kevoree.modeling.meta.KMetaReference[] = resolved[i].referencesWith(selfPointer);
                                        for (var j: number = 0; j < linkedReferences.length; j++) {
                                            (<org.kevoree.modeling.abs.AbstractKObject>resolved[i]).internal_mutate(org.kevoree.modeling.KActionType.REMOVE, linkedReferences[j], selfPointer, false);
                                        }
                                    }
                                }
                                if (cb != null) {
                                    cb(null);
                                }
                            });
                        }
                    }

                    public select(query: string, cb: (p : org.kevoree.modeling.KObject[]) => void): void {
                        if (!org.kevoree.modeling.util.Checker.isDefined(query)) {
                            cb(new Array());
                        } else {
                            var cleanedQuery: string = query;
                            if (cleanedQuery.startsWith("/")) {
                                cleanedQuery = cleanedQuery.substring(1);
                            }
                            if (query.startsWith("/")) {
                                var finalCleanedQuery: string = cleanedQuery;
                                this._manager.getRoot(this._universe, this._time,  (rootObj : org.kevoree.modeling.KObject) => {
                                    if (rootObj == null) {
                                        cb(new Array());
                                    } else {
                                        org.kevoree.modeling.traversal.impl.selector.Selector.select(rootObj, finalCleanedQuery, cb);
                                    }
                                });
                            } else {
                                org.kevoree.modeling.traversal.impl.selector.Selector.select(this, query, cb);
                            }
                        }
                    }

                    public listen(groupId: number, listener: (p : org.kevoree.modeling.KObject, p1 : org.kevoree.modeling.meta.KMeta[]) => void): void {
                        this._manager.cdn().registerListener(groupId, this, listener);
                    }

                    public get(p_attribute: org.kevoree.modeling.meta.KMetaAttribute): any {
                        var transposed: org.kevoree.modeling.meta.KMetaAttribute = this.internal_transpose_att(p_attribute);
                        if (transposed == null) {
                            throw new java.lang.RuntimeException("Bad KMF usage, the attribute named " + p_attribute.metaName() + " is not part of " + this.metaClass().metaName());
                        } else {
                            return transposed.strategy().extrapolate(this, transposed);
                        }
                    }

                    public getByName(atributeName: string): any {
                        var transposed: org.kevoree.modeling.meta.KMetaAttribute = this._metaClass.attribute(atributeName);
                        if (transposed != null) {
                            return transposed.strategy().extrapolate(this, transposed);
                        } else {
                            return null;
                        }
                    }

                    public set(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, payload: any): void {
                        var transposed: org.kevoree.modeling.meta.KMetaAttribute = this.internal_transpose_att(p_attribute);
                        if (transposed == null) {
                            throw new java.lang.RuntimeException("Bad KMF usage, the attribute named " + p_attribute.metaName() + " is not part of " + this.metaClass().metaName());
                        } else {
                            transposed.strategy().mutate(this, transposed, payload);
                        }
                    }

                    public setByName(atributeName: string, payload: any): void {
                        var transposed: org.kevoree.modeling.meta.KMetaAttribute = this._metaClass.attribute(atributeName);
                        if (transposed != null) {
                            transposed.strategy().mutate(this, transposed, payload);
                        }
                    }

                    public mutate(actionType: org.kevoree.modeling.KActionType, metaReference: org.kevoree.modeling.meta.KMetaReference, param: org.kevoree.modeling.KObject): void {
                        this.internal_mutate(actionType, metaReference, param, true);
                    }

                    public internal_mutate(actionType: org.kevoree.modeling.KActionType, metaReferenceP: org.kevoree.modeling.meta.KMetaReference, param: org.kevoree.modeling.KObject, setOpposite: boolean): void {
                        var metaReference: org.kevoree.modeling.meta.KMetaReference = this.internal_transpose_ref(metaReferenceP);
                        if (metaReference == null) {
                            if (metaReferenceP == null) {
                                throw new java.lang.RuntimeException("Bad KMF usage, the reference " + " is null in metaClass named " + this.metaClass().metaName());
                            } else {
                                throw new java.lang.RuntimeException("Bad KMF usage, the reference named " + metaReferenceP.metaName() + " is not part of " + this.metaClass().metaName());
                            }
                        }
                        if (actionType.equals(org.kevoree.modeling.KActionType.ADD)) {
                            if (metaReference.single()) {
                                this.internal_mutate(org.kevoree.modeling.KActionType.SET, metaReference, param, setOpposite);
                            } else {
                                var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = this._manager.segment(this._universe, this._time, this._uuid, org.kevoree.modeling.memory.manager.AccessMode.NEW, this._metaClass, null);
                                if (raw != null) {
                                    if (raw.addRef(metaReference.index(), param.uuid(), this._metaClass)) {
                                        if (setOpposite) {
                                            (<org.kevoree.modeling.abs.AbstractKObject>param).internal_mutate(org.kevoree.modeling.KActionType.ADD, metaReference.opposite(), this, false);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (actionType.equals(org.kevoree.modeling.KActionType.SET)) {
                                if (!metaReference.single()) {
                                    this.internal_mutate(org.kevoree.modeling.KActionType.ADD, metaReference, param, setOpposite);
                                } else {
                                    if (param == null) {
                                        this.internal_mutate(org.kevoree.modeling.KActionType.REMOVE, metaReference, null, setOpposite);
                                    } else {
                                        var payload: org.kevoree.modeling.memory.struct.segment.KMemorySegment = this._manager.segment(this._universe, this._time, this._uuid, org.kevoree.modeling.memory.manager.AccessMode.NEW, this._metaClass, null);
                                        var previous: number[] = payload.getRef(metaReference.index(), this._metaClass);
                                        var singleValue: number[] = new Array();
                                        singleValue[0] = param.uuid();
                                        payload.set(metaReference.index(), singleValue, this._metaClass);
                                        if (setOpposite) {
                                            if (previous != null) {
                                                var self: org.kevoree.modeling.KObject = this;
                                                this._manager.lookupAllobjects(this._universe, this._time, previous,  (kObjects : org.kevoree.modeling.KObject[]) => {
                                                    for (var i: number = 0; i < kObjects.length; i++) {
                                                        (<org.kevoree.modeling.abs.AbstractKObject>kObjects[i]).internal_mutate(org.kevoree.modeling.KActionType.REMOVE, metaReference.opposite(), self, false);
                                                    }
                                                    (<org.kevoree.modeling.abs.AbstractKObject>param).internal_mutate(org.kevoree.modeling.KActionType.ADD, metaReference.opposite(), self, false);
                                                });
                                            } else {
                                                (<org.kevoree.modeling.abs.AbstractKObject>param).internal_mutate(org.kevoree.modeling.KActionType.ADD, metaReference.opposite(), this, false);
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (actionType.equals(org.kevoree.modeling.KActionType.REMOVE)) {
                                    if (metaReference.single()) {
                                        var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = this._manager.segment(this._universe, this._time, this._uuid, org.kevoree.modeling.memory.manager.AccessMode.NEW, this._metaClass, null);
                                        var previousKid: number[] = raw.getRef(metaReference.index(), this._metaClass);
                                        raw.set(metaReference.index(), null, this._metaClass);
                                        if (setOpposite) {
                                            if (previousKid != null) {
                                                var self: org.kevoree.modeling.KObject = this;
                                                this._manager.lookupAllobjects(this._universe, this._time, previousKid,  (resolvedParams : org.kevoree.modeling.KObject[]) => {
                                                    if (resolvedParams != null) {
                                                        for (var dd: number = 0; dd < resolvedParams.length; dd++) {
                                                            if (resolvedParams[dd] != null) {
                                                                (<org.kevoree.modeling.abs.AbstractKObject>resolvedParams[dd]).internal_mutate(org.kevoree.modeling.KActionType.REMOVE, metaReference.opposite(), self, false);
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    } else {
                                        var payload: org.kevoree.modeling.memory.struct.segment.KMemorySegment = this._manager.segment(this._universe, this._time, this._uuid, org.kevoree.modeling.memory.manager.AccessMode.NEW, this._metaClass, null);
                                        if (payload != null) {
                                            if (payload.removeRef(metaReference.index(), param.uuid(), this._metaClass)) {
                                                if (setOpposite) {
                                                    (<org.kevoree.modeling.abs.AbstractKObject>param).internal_mutate(org.kevoree.modeling.KActionType.REMOVE, metaReference.opposite(), this, false);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    public size(p_metaReference: org.kevoree.modeling.meta.KMetaReference): number {
                        var transposed: org.kevoree.modeling.meta.KMetaReference = this.internal_transpose_ref(p_metaReference);
                        if (transposed == null) {
                            throw new java.lang.RuntimeException("Bad KMF usage, the attribute named " + p_metaReference.metaName() + " is not part of " + this.metaClass().metaName());
                        } else {
                            var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = this._manager.segment(this._universe, this._time, this._uuid, org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, this._metaClass, null);
                            if (raw != null) {
                                var ref: any = raw.get(transposed.index(), this._metaClass);
                                if (ref == null) {
                                    return 0;
                                } else {
                                    try {
                                        var castedRefArray: number[] = <number[]>ref;
                                        return castedRefArray.length;
                                    } catch ($ex$) {
                                        if ($ex$ instanceof java.lang.Exception) {
                                            var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                            e.printStackTrace();
                                            return 0;
                                        } else {
                                            throw $ex$;
                                        }
                                    }
                                }
                            } else {
                                return 0;
                            }
                        }
                    }

                    public ref(p_metaReference: org.kevoree.modeling.meta.KMetaReference, cb: (p : org.kevoree.modeling.KObject[]) => void): void {
                        var transposed: org.kevoree.modeling.meta.KMetaReference = this.internal_transpose_ref(p_metaReference);
                        if (transposed == null) {
                            throw new java.lang.RuntimeException("Bad KMF usage, the reference named " + p_metaReference.metaName() + " is not part of " + this.metaClass().metaName());
                        } else {
                            var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = this._manager.segment(this._universe, this._time, this._uuid, org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, this._metaClass, null);
                            if (raw == null) {
                                cb(new Array());
                            } else {
                                var o: number[] = raw.getRef(transposed.index(), this._metaClass);
                                if (o == null) {
                                    cb(new Array());
                                } else {
                                    this._manager.lookupAllobjects(this._universe, this._time, o, cb);
                                }
                            }
                        }
                    }

                    public visitAttributes(visitor: (p : org.kevoree.modeling.meta.KMetaAttribute, p1 : any) => void): void {
                        if (!org.kevoree.modeling.util.Checker.isDefined(visitor)) {
                            return;
                        }
                        var metaElements: org.kevoree.modeling.meta.KMeta[] = this.metaClass().metaElements();
                        for (var i: number = 0; i < metaElements.length; i++) {
                            if (metaElements[i] instanceof org.kevoree.modeling.meta.impl.MetaAttribute) {
                                var metaAttribute: org.kevoree.modeling.meta.KMetaAttribute = <org.kevoree.modeling.meta.KMetaAttribute>metaElements[i];
                                visitor(metaAttribute, this.get(metaAttribute));
                            }
                        }
                    }

                    public visit(p_visitor: (p : org.kevoree.modeling.KObject) => org.kevoree.modeling.traversal.visitor.KVisitResult, cb: (p : any) => void): void {
                        this.internal_visit(p_visitor, cb, new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR), new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR));
                    }

                    private internal_visit(visitor: (p : org.kevoree.modeling.KObject) => org.kevoree.modeling.traversal.visitor.KVisitResult, end: (p : any) => void, visited: org.kevoree.modeling.memory.struct.map.KLongLongMap, traversed: org.kevoree.modeling.memory.struct.map.KLongLongMap): void {
                        if (!org.kevoree.modeling.util.Checker.isDefined(visitor)) {
                            return;
                        }
                        if (traversed != null) {
                            traversed.put(this._uuid, this._uuid);
                        }
                        var toResolveIds: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                        var metaElements: org.kevoree.modeling.meta.KMeta[] = this.metaClass().metaElements();
                        for (var i: number = 0; i < metaElements.length; i++) {
                            if (metaElements[i] instanceof org.kevoree.modeling.meta.impl.MetaReference) {
                                var reference: org.kevoree.modeling.meta.KMetaReference = <org.kevoree.modeling.meta.KMetaReference>metaElements[i];
                                var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = this._manager.segment(this._universe, this._time, this._uuid, org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, this._metaClass, null);
                                if (raw != null) {
                                    var idArr: number[] = raw.getRef(reference.index(), this._metaClass);
                                    if (idArr != null) {
                                        try {
                                            for (var k: number = 0; k < idArr.length; k++) {
                                                if (traversed == null || !traversed.contains(idArr[k])) {
                                                    toResolveIds.put(idArr[k], idArr[k]);
                                                }
                                            }
                                        } catch ($ex$) {
                                            if ($ex$ instanceof java.lang.Exception) {
                                                var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                e.printStackTrace();
                                            } else {
                                                throw $ex$;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (toResolveIds.size() == 0) {
                            if (org.kevoree.modeling.util.Checker.isDefined(end)) {
                                end(null);
                            }
                        } else {
                            var trimmed: number[] = new Array();
                            var inserted: number[] = [0];
                            toResolveIds.each( (key : number, value : number) => {
                                trimmed[inserted[0]] = value;
                                inserted[0]++;
                            });
                            this._manager.lookupAllobjects(this._universe, this._time, trimmed,  (resolvedArr : org.kevoree.modeling.KObject[]) => {
                                var nextDeep: java.util.List<org.kevoree.modeling.KObject> = new java.util.ArrayList<org.kevoree.modeling.KObject>();
                                for (var i: number = 0; i < resolvedArr.length; i++) {
                                    var resolved: org.kevoree.modeling.KObject = resolvedArr[i];
                                    var result: org.kevoree.modeling.traversal.visitor.KVisitResult = org.kevoree.modeling.traversal.visitor.KVisitResult.CONTINUE;
                                    if (resolved != null) {
                                        if (visitor != null && (visited == null || !visited.contains(resolved.uuid()))) {
                                            result = visitor(resolved);
                                        }
                                        if (visited != null) {
                                            visited.put(resolved.uuid(), resolved.uuid());
                                        }
                                    }
                                    if (result != null && result.equals(org.kevoree.modeling.traversal.visitor.KVisitResult.STOP)) {
                                        if (org.kevoree.modeling.util.Checker.isDefined(end)) {
                                            end(null);
                                        }
                                    } else {
                                        if (result.equals(org.kevoree.modeling.traversal.visitor.KVisitResult.CONTINUE)) {
                                            if (traversed == null || !traversed.contains(resolved.uuid())) {
                                                nextDeep.add(resolved);
                                            }
                                        }
                                    }
                                }
                                if (!nextDeep.isEmpty()) {
                                    var index: number[] = new Array();
                                    index[0] = 0;
                                    var next: java.util.List<(p : java.lang.Throwable) => void> = new java.util.ArrayList<(p : java.lang.Throwable) => void>();
                                    next.add( (throwable : java.lang.Throwable) => {
                                        index[0] = index[0] + 1;
                                        if (index[0] == nextDeep.size()) {
                                            if (org.kevoree.modeling.util.Checker.isDefined(end)) {
                                                end(null);
                                            }
                                        } else {
                                            var abstractKObject: org.kevoree.modeling.abs.AbstractKObject = <org.kevoree.modeling.abs.AbstractKObject>nextDeep.get(index[0]);
                                            abstractKObject.internal_visit(visitor, next.get(0), visited, traversed);
                                        }
                                    });
                                    var abstractKObject: org.kevoree.modeling.abs.AbstractKObject = <org.kevoree.modeling.abs.AbstractKObject>nextDeep.get(index[0]);
                                    abstractKObject.internal_visit(visitor, next.get(0), visited, traversed);
                                } else {
                                    if (org.kevoree.modeling.util.Checker.isDefined(end)) {
                                        end(null);
                                    }
                                }
                            });
                        }
                    }

                    public toJSON(): string {
                        var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = this._manager.segment(this._universe, this._time, this._uuid, org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, this._metaClass, null);
                        if (raw != null) {
                            return org.kevoree.modeling.format.json.JsonRaw.encode(raw, this._uuid, this._metaClass, false);
                        } else {
                            return null;
                        }
                    }

                    public toString(): string {
                        return this.toJSON();
                    }

                    public equals(obj: any): boolean {
                        if (!(obj instanceof org.kevoree.modeling.abs.AbstractKObject)) {
                            return false;
                        } else {
                            var casted: org.kevoree.modeling.abs.AbstractKObject = <org.kevoree.modeling.abs.AbstractKObject>obj;
                            return casted._uuid == this._uuid && casted._time == this._time && casted._universe == this._universe;
                        }
                    }

                    public hashCode(): number {
                        return <number>(this._universe ^ this._time ^ this._uuid);
                    }

                    public jump(p_time: number, p_callback: (p : org.kevoree.modeling.KObject) => void): void {
                        var resolve_entry: org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment = <org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment>this._manager.cache().get(this._universe, p_time, this._uuid);
                        if (resolve_entry != null) {
                            var timeTree: org.kevoree.modeling.memory.struct.tree.KLongTree = <org.kevoree.modeling.memory.struct.tree.KLongTree>this._manager.cache().get(this._universe, org.kevoree.modeling.KConfig.NULL_LONG, this._uuid);
                            timeTree.inc();
                            var universeTree: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap = <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>this._manager.cache().get(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, this._uuid);
                            universeTree.inc();
                            resolve_entry.inc();
                            p_callback((<org.kevoree.modeling.abs.AbstractKModel<any>>this._manager.model()).createProxy(this._universe, p_time, this._uuid, this._metaClass));
                        } else {
                            var timeTree: org.kevoree.modeling.memory.struct.tree.KLongTree = <org.kevoree.modeling.memory.struct.tree.KLongTree>this._manager.cache().get(this._universe, org.kevoree.modeling.KConfig.NULL_LONG, this._uuid);
                            if (timeTree != null) {
                                var resolvedTime: number = timeTree.previousOrEqual(p_time);
                                if (resolvedTime != org.kevoree.modeling.KConfig.NULL_LONG) {
                                    var entry: org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment = <org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment>this._manager.cache().get(this._universe, resolvedTime, this._uuid);
                                    if (entry != null) {
                                        var universeTree: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap = <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>this._manager.cache().get(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, this._uuid);
                                        universeTree.inc();
                                        timeTree.inc();
                                        entry.inc();
                                        p_callback((<org.kevoree.modeling.abs.AbstractKModel<any>>this._manager.model()).createProxy(this._universe, p_time, this._uuid, this._metaClass));
                                    } else {
                                        this._manager.lookup(this._universe, p_time, this._uuid, p_callback);
                                    }
                                }
                            } else {
                                this._manager.lookup(this._universe, p_time, this._uuid, p_callback);
                            }
                        }
                    }

                    public internal_transpose_ref(p: org.kevoree.modeling.meta.KMetaReference): org.kevoree.modeling.meta.KMetaReference {
                        if (!org.kevoree.modeling.util.Checker.isDefined(p)) {
                            return null;
                        } else {
                            return <org.kevoree.modeling.meta.KMetaReference>this.metaClass().metaByName(p.metaName());
                        }
                    }

                    public internal_transpose_att(p: org.kevoree.modeling.meta.KMetaAttribute): org.kevoree.modeling.meta.KMetaAttribute {
                        if (!org.kevoree.modeling.util.Checker.isDefined(p)) {
                            return null;
                        } else {
                            return <org.kevoree.modeling.meta.KMetaAttribute>this.metaClass().metaByName(p.metaName());
                        }
                    }

                    public internal_transpose_op(p: org.kevoree.modeling.meta.KMetaOperation): org.kevoree.modeling.meta.KMetaOperation {
                        if (!org.kevoree.modeling.util.Checker.isDefined(p)) {
                            return null;
                        } else {
                            return <org.kevoree.modeling.meta.KMetaOperation>this.metaClass().metaByName(p.metaName());
                        }
                    }

                    public traversal(): org.kevoree.modeling.traversal.KTraversal {
                        return new org.kevoree.modeling.traversal.impl.Traversal(this);
                    }

                    public referencesWith(o: org.kevoree.modeling.KObject): org.kevoree.modeling.meta.KMetaReference[] {
                        if (org.kevoree.modeling.util.Checker.isDefined(o)) {
                            var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = this._manager.segment(this._universe, this._time, this._uuid, org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, this._metaClass, null);
                            if (raw != null) {
                                var metaElements: org.kevoree.modeling.meta.KMeta[] = this.metaClass().metaElements();
                                var selected: java.util.List<org.kevoree.modeling.meta.KMetaReference> = new java.util.ArrayList<org.kevoree.modeling.meta.KMetaReference>();
                                for (var i: number = 0; i < metaElements.length; i++) {
                                    if (metaElements[i] instanceof org.kevoree.modeling.meta.impl.MetaReference) {
                                        var rawI: number[] = raw.getRef((metaElements[i].index()), this._metaClass);
                                        if (rawI != null) {
                                            var oUUID: number = o.uuid();
                                            for (var h: number = 0; h < rawI.length; h++) {
                                                if (rawI[h] == oUUID) {
                                                    selected.add(<org.kevoree.modeling.meta.KMetaReference>metaElements[i]);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                return selected.toArray(new Array());
                            } else {
                                return new Array();
                            }
                        } else {
                            return new Array();
                        }
                    }

                    public call(p_operation: org.kevoree.modeling.meta.KMetaOperation, p_params: any[], cb: (p : any) => void): void {
                        this._manager.operationManager().call(this, p_operation, p_params, cb);
                    }

                    public manager(): org.kevoree.modeling.memory.manager.KMemoryManager {
                        return this._manager;
                    }

                }

                export class AbstractKUniverse<A extends org.kevoree.modeling.KView, B extends org.kevoree.modeling.KUniverse<any, any, any>, C extends org.kevoree.modeling.KModel<any>> implements org.kevoree.modeling.KUniverse<any, any, any> {

                    public _universe: number;
                    public _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                    constructor(p_key: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
                        this._universe = p_key;
                        this._manager = p_manager;
                    }

                    public key(): number {
                        return this._universe;
                    }

                    public model(): C {
                        return <C>this._manager.model();
                    }

                    public delete(cb: (p : any) => void): void {
                        this.model().manager().delete(this, cb);
                    }

                    public time(timePoint: number): A {
                        if (timePoint <= org.kevoree.modeling.KConfig.END_OF_TIME && timePoint >= org.kevoree.modeling.KConfig.BEGINNING_OF_TIME) {
                            return this.internal_create(timePoint);
                        } else {
                            throw new java.lang.RuntimeException("The selected Time " + timePoint + " is out of the range of KMF managed time");
                        }
                    }

                    public internal_create(timePoint: number): A {
                        throw "Abstract method";
                    }

                    public equals(obj: any): boolean {
                        if (!(obj instanceof org.kevoree.modeling.abs.AbstractKUniverse)) {
                            return false;
                        } else {
                            var casted: org.kevoree.modeling.abs.AbstractKUniverse<any, any, any> = <org.kevoree.modeling.abs.AbstractKUniverse<any, any, any>>obj;
                            return casted._universe == this._universe;
                        }
                    }

                    public origin(): B {
                        return <B>this._manager.model().universe(this._manager.parentUniverseKey(this._universe));
                    }

                    public diverge(): B {
                        var casted: org.kevoree.modeling.abs.AbstractKModel<any> = <org.kevoree.modeling.abs.AbstractKModel<any>>this._manager.model();
                        var nextKey: number = this._manager.nextUniverseKey();
                        var newUniverse: B = <B>casted.internalCreateUniverse(nextKey);
                        this._manager.initUniverse(newUniverse, this);
                        return newUniverse;
                    }

                    public descendants(): java.util.List<B> {
                        var descendentsKey: number[] = this._manager.descendantsUniverseKeys(this._universe);
                        var childs: java.util.List<B> = new java.util.ArrayList<B>();
                        for (var i: number = 0; i < descendentsKey.length; i++) {
                            childs.add(<B>this._manager.model().universe(descendentsKey[i]));
                        }
                        return childs;
                    }

                    public lookupAllTimes(uuid: number, times: number[], cb: (p : org.kevoree.modeling.KObject[]) => void): void {
                        throw new java.lang.RuntimeException("Not implemented Yet !");
                    }

                    public listenAll(groupId: number, objects: number[], multiListener: (p : org.kevoree.modeling.KObject[]) => void): void {
                        this.model().manager().cdn().registerMultiListener(groupId, this, objects, multiListener);
                    }

                }

                export class AbstractKView implements org.kevoree.modeling.KView {

                    public _time: number;
                    public _universe: number;
                    public _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                    constructor(p_universe: number, _time: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
                        this._universe = p_universe;
                        this._time = _time;
                        this._manager = p_manager;
                    }

                    public now(): number {
                        return this._time;
                    }

                    public universe(): number {
                        return this._universe;
                    }

                    public setRoot(elem: org.kevoree.modeling.KObject, cb: (p : any) => void): void {
                        this._manager.setRoot(elem, cb);
                    }

                    public getRoot(cb: (p : any) => void): void {
                        this._manager.getRoot(this._universe, this._time, cb);
                    }

                    public select(query: string, cb: (p : org.kevoree.modeling.KObject[]) => void): void {
                        if (org.kevoree.modeling.util.Checker.isDefined(cb)) {
                            if (query == null || query.length == 0) {
                                cb(new Array());
                            } else {
                                this._manager.getRoot(this._universe, this._time,  (rootObj : org.kevoree.modeling.KObject) => {
                                    if (rootObj == null) {
                                        cb(new Array());
                                    } else {
                                        var cleanedQuery: string = query;
                                        if (query.length == 1 && query.charAt(0) == '/') {
                                            var param: org.kevoree.modeling.KObject[] = new Array();
                                            param[0] = rootObj;
                                            cb(param);
                                        } else {
                                            if (cleanedQuery.charAt(0) == '/') {
                                                cleanedQuery = cleanedQuery.substring(1);
                                            }
                                            org.kevoree.modeling.traversal.impl.selector.Selector.select(rootObj, cleanedQuery, cb);
                                        }
                                    }
                                });
                            }
                        }
                    }

                    public lookup(kid: number, cb: (p : org.kevoree.modeling.KObject) => void): void {
                        this._manager.lookup(this._universe, this._time, kid, cb);
                    }

                    public lookupAll(keys: number[], cb: (p : org.kevoree.modeling.KObject[]) => void): void {
                        this._manager.lookupAllobjects(this._universe, this._time, keys, cb);
                    }

                    public create(clazz: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.KObject {
                        return this._manager.model().create(clazz, this._universe, this._time);
                    }

                    public createByName(metaClassName: string): org.kevoree.modeling.KObject {
                        return this.create(this._manager.model().metaModel().metaClassByName(metaClassName));
                    }

                    public json(): org.kevoree.modeling.format.KModelFormat {
                        return new org.kevoree.modeling.format.json.JsonFormat(this._universe, this._time, this._manager);
                    }

                    public xmi(): org.kevoree.modeling.format.KModelFormat {
                        return new org.kevoree.modeling.format.xmi.XmiFormat(this._universe, this._time, this._manager);
                    }

                    public equals(obj: any): boolean {
                        if (!org.kevoree.modeling.util.Checker.isDefined(obj)) {
                            return false;
                        }
                        if (!(obj instanceof org.kevoree.modeling.abs.AbstractKView)) {
                            return false;
                        } else {
                            var casted: org.kevoree.modeling.abs.AbstractKView = <org.kevoree.modeling.abs.AbstractKView>obj;
                            return casted._time == this._time && casted._universe == this._universe;
                        }
                    }

                }

                export class AbstractTimeWalker implements org.kevoree.modeling.KTimeWalker {

                    private _origin: org.kevoree.modeling.abs.AbstractKObject = null;
                    constructor(p_origin: org.kevoree.modeling.abs.AbstractKObject) {
                        this._origin = p_origin;
                    }

                    private internal_times(start: number, end: number, cb: (p : number[]) => void): void {
                        var keys: org.kevoree.modeling.KContentKey[] = new Array();
                        keys[0] = org.kevoree.modeling.KContentKey.createGlobalUniverseTree();
                        keys[1] = org.kevoree.modeling.KContentKey.createUniverseTree(this._origin.uuid());
                        var manager: org.kevoree.modeling.memory.manager.impl.HeapMemoryManager = <org.kevoree.modeling.memory.manager.impl.HeapMemoryManager>this._origin._manager;
                        manager.bumpKeysToCache(keys,  (kMemoryElements : org.kevoree.modeling.memory.KMemoryElement[]) => {
                            var objUniverse: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap = <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>kMemoryElements[1];
                            if (kMemoryElements[0] == null || kMemoryElements[1] == null) {
                                cb(null);
                            } else {
                                var collectedUniverse: number[] = org.kevoree.modeling.memory.manager.impl.ResolutionHelper.universeSelectByRange(<org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>kMemoryElements[0], <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>kMemoryElements[1], start, end, this._origin.universe());
                                var timeTreeToLoad: org.kevoree.modeling.KContentKey[] = new Array();
                                for (var i: number = 0; i < collectedUniverse.length; i++) {
                                    timeTreeToLoad[i] = org.kevoree.modeling.KContentKey.createTimeTree(collectedUniverse[i], this._origin.uuid());
                                }
                                manager.bumpKeysToCache(timeTreeToLoad,  (timeTrees : org.kevoree.modeling.memory.KMemoryElement[]) => {
                                    var collector: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                    var previousDivergenceTime: number = end;
                                    for (var i: number = 0; i < collectedUniverse.length; i++) {
                                        var timeTree: org.kevoree.modeling.memory.struct.tree.KLongTree = <org.kevoree.modeling.memory.struct.tree.KLongTree>timeTrees[i];
                                        if (timeTree != null) {
                                            var currentDivergenceTime: number = objUniverse.get(collectedUniverse[i]);
                                            var finalI: number = i;
                                            var finalPreviousDivergenceTime: number = previousDivergenceTime;
                                            timeTree.range(currentDivergenceTime, previousDivergenceTime,  (t : number) => {
                                                if (collector.size() == 0) {
                                                    collector.put(collector.size(), t);
                                                } else {
                                                    if (t != finalPreviousDivergenceTime) {
                                                        collector.put(collector.size(), t);
                                                    }
                                                }
                                            });
                                            previousDivergenceTime = currentDivergenceTime;
                                        }
                                    }
                                    var orderedTime: number[] = new Array();
                                    for (var i: number = 0; i < collector.size(); i++) {
                                        orderedTime[i] = collector.get(i);
                                    }
                                    cb(orderedTime);
                                });
                            }
                        });
                    }

                    public allTimes(cb: (p : number[]) => void): void {
                        this.internal_times(org.kevoree.modeling.KConfig.BEGINNING_OF_TIME, org.kevoree.modeling.KConfig.END_OF_TIME, cb);
                    }

                    public timesBefore(endOfSearch: number, cb: (p : number[]) => void): void {
                        this.internal_times(org.kevoree.modeling.KConfig.BEGINNING_OF_TIME, endOfSearch, cb);
                    }

                    public timesAfter(beginningOfSearch: number, cb: (p : number[]) => void): void {
                        this.internal_times(beginningOfSearch, org.kevoree.modeling.KConfig.END_OF_TIME, cb);
                    }

                    public timesBetween(beginningOfSearch: number, endOfSearch: number, cb: (p : number[]) => void): void {
                        this.internal_times(beginningOfSearch, endOfSearch, cb);
                    }

                }

                export interface KLazyResolver {

                    meta(): org.kevoree.modeling.meta.KMeta;

                }

            }
            export module cdn {
                export interface KContentDeliveryDriver {

                    get(keys: org.kevoree.modeling.KContentKey[], callback: (p : string[]) => void): void;

                    atomicGetIncrement(key: org.kevoree.modeling.KContentKey, cb: (p : number) => void): void;

                    put(request: org.kevoree.modeling.cdn.KContentPutRequest, error: (p : java.lang.Throwable) => void): void;

                    remove(keys: string[], error: (p : java.lang.Throwable) => void): void;

                    connect(callback: (p : java.lang.Throwable) => void): void;

                    close(callback: (p : java.lang.Throwable) => void): void;

                    registerListener(groupId: number, origin: org.kevoree.modeling.KObject, listener: (p : org.kevoree.modeling.KObject, p1 : org.kevoree.modeling.meta.KMeta[]) => void): void;

                    registerMultiListener(groupId: number, origin: org.kevoree.modeling.KUniverse<any, any, any>, objects: number[], listener: (p : org.kevoree.modeling.KObject[]) => void): void;

                    unregisterGroup(groupId: number): void;

                    send(msgs: org.kevoree.modeling.message.KMessage): void;

                    setManager(manager: org.kevoree.modeling.memory.manager.KMemoryManager): void;

                }

                export interface KContentPutRequest {

                    put(p_key: org.kevoree.modeling.KContentKey, p_payload: string): void;

                    getKey(index: number): org.kevoree.modeling.KContentKey;

                    getContent(index: number): string;

                    size(): number;

                }

                export module impl {
                    export class ContentPutRequest implements org.kevoree.modeling.cdn.KContentPutRequest {

                        private _content: any[][];
                        private static KEY_INDEX: number = 0;
                        private static CONTENT_INDEX: number = 1;
                        private static SIZE_INDEX: number = 2;
                        private _size: number = 0;
                        constructor(requestSize: number) {
                            this._content = new Array();
                        }

                        public put(p_key: org.kevoree.modeling.KContentKey, p_payload: string): void {
                            var newLine: any[] = new Array();
                            newLine[ContentPutRequest.KEY_INDEX] = p_key;
                            newLine[ContentPutRequest.CONTENT_INDEX] = p_payload;
                            this._content[this._size] = newLine;
                            this._size = this._size + 1;
                        }

                        public getKey(index: number): org.kevoree.modeling.KContentKey {
                            if (index < this._content.length) {
                                return <org.kevoree.modeling.KContentKey>this._content[index][0];
                            } else {
                                return null;
                            }
                        }

                        public getContent(index: number): string {
                            if (index < this._content.length) {
                                return <string>this._content[index][1];
                            } else {
                                return null;
                            }
                        }

                        public size(): number {
                            return this._size;
                        }

                    }

                    export class MemoryContentDeliveryDriver implements org.kevoree.modeling.cdn.KContentDeliveryDriver {

                        private backend: org.kevoree.modeling.memory.struct.map.KStringMap<any> = new org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                        private _localEventListeners: org.kevoree.modeling.event.impl.LocalEventListeners = new org.kevoree.modeling.event.impl.LocalEventListeners();
                        public static DEBUG: boolean = false;
                        public atomicGetIncrement(key: org.kevoree.modeling.KContentKey, cb: (p : number) => void): void {
                            var result: string = this.backend.get(key.toString());
                            var nextV: number;
                            var previousV: number;
                            if (result != null) {
                                try {
                                    previousV = java.lang.Short.parseShort(result);
                                } catch ($ex$) {
                                    if ($ex$ instanceof java.lang.Exception) {
                                        var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                        e.printStackTrace();
                                        previousV = java.lang.Short.MIN_VALUE;
                                    } else {
                                        throw $ex$;
                                    }
                                }
                            } else {
                                previousV = 0;
                            }
                            if (previousV == java.lang.Short.MAX_VALUE) {
                                nextV = java.lang.Short.MIN_VALUE;
                            } else {
                                nextV = <number>(previousV + 1);
                            }
                            this.backend.put(key.toString(), "" + nextV);
                            cb(previousV);
                        }

                        public get(keys: org.kevoree.modeling.KContentKey[], callback: (p : string[]) => void): void {
                            var values: string[] = new Array();
                            for (var i: number = 0; i < keys.length; i++) {
                                if (keys[i] != null) {
                                    values[i] = this.backend.get(keys[i].toString());
                                }
                                if (MemoryContentDeliveryDriver.DEBUG) {
                                    System.out.println("GET " + keys[i] + "->" + values[i]);
                                }
                            }
                            if (callback != null) {
                                callback(values);
                            }
                        }

                        public put(p_request: org.kevoree.modeling.cdn.KContentPutRequest, p_callback: (p : java.lang.Throwable) => void): void {
                            for (var i: number = 0; i < p_request.size(); i++) {
                                this.backend.put(p_request.getKey(i).toString(), p_request.getContent(i));
                                if (MemoryContentDeliveryDriver.DEBUG) {
                                    System.out.println("PUT " + p_request.getKey(i).toString() + "->" + p_request.getContent(i));
                                }
                            }
                            if (p_callback != null) {
                                p_callback(null);
                            }
                        }

                        public remove(keys: string[], callback: (p : java.lang.Throwable) => void): void {
                            for (var i: number = 0; i < keys.length; i++) {
                                this.backend.remove(keys[i]);
                            }
                            if (callback != null) {
                                callback(null);
                            }
                        }

                        public connect(callback: (p : java.lang.Throwable) => void): void {
                            if (callback != null) {
                                callback(null);
                            }
                        }

                        public close(callback: (p : java.lang.Throwable) => void): void {
                            this._localEventListeners.clear();
                            this.backend.clear();
                            callback(null);
                        }

                        public registerListener(groupId: number, p_origin: org.kevoree.modeling.KObject, p_listener: (p : org.kevoree.modeling.KObject, p1 : org.kevoree.modeling.meta.KMeta[]) => void): void {
                            this._localEventListeners.registerListener(groupId, p_origin, p_listener);
                        }

                        public unregisterGroup(groupId: number): void {
                            this._localEventListeners.unregister(groupId);
                        }

                        public registerMultiListener(groupId: number, origin: org.kevoree.modeling.KUniverse<any, any, any>, objects: number[], listener: (p : org.kevoree.modeling.KObject[]) => void): void {
                            this._localEventListeners.registerListenerAll(groupId, origin.key(), objects, listener);
                        }

                        public send(msgs: org.kevoree.modeling.message.KMessage): void {
                            this._localEventListeners.dispatch(msgs);
                        }

                        public setManager(manager: org.kevoree.modeling.memory.manager.KMemoryManager): void {
                            this._localEventListeners.setManager(manager);
                        }

                    }

                }
            }
            export module defer {
                export interface KDefer {

                    wait(resultName: string): (p : any) => void;

                    waitDefer(previous: org.kevoree.modeling.defer.KDefer): org.kevoree.modeling.defer.KDefer;

                    isDone(): boolean;

                    getResult(resultName: string): any;

                    then(cb: (p : any) => void): void;

                    next(): org.kevoree.modeling.defer.KDefer;

                }

                export module impl {
                    export class Defer implements org.kevoree.modeling.defer.KDefer {

                        private _isDone: boolean = false;
                        public _isReady: boolean = false;
                        private _nbRecResult: number = 0;
                        private _nbExpectedResult: number = 0;
                        private _nextTasks: java.util.ArrayList<org.kevoree.modeling.defer.KDefer> = null;
                        private _results: org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any> = null;
                        private _thenCB: (p : any) => void = null;
                        constructor() {
                            this._results = new org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                        }

                        public setDoneOrRegister(next: org.kevoree.modeling.defer.KDefer): boolean {
                            if (next != null) {
                                if (this._nextTasks == null) {
                                    this._nextTasks = new java.util.ArrayList<org.kevoree.modeling.defer.KDefer>();
                                }
                                this._nextTasks.add(next);
                                return this._isDone;
                            } else {
                                this._isDone = true;
                                if (this._nextTasks != null) {
                                    for (var i: number = 0; i < this._nextTasks.size(); i++) {
                                        (<org.kevoree.modeling.defer.impl.Defer>this._nextTasks.get(i)).informParentEnd(this);
                                    }
                                }
                                return this._isDone;
                            }
                        }

                        public equals(obj: any): boolean {
                            return obj == this;
                        }

                        private informParentEnd(end: org.kevoree.modeling.defer.KDefer): void {
                            if (end == null) {
                                this._nbRecResult = this._nbRecResult + this._nbExpectedResult;
                            } else {
                                if (end != this) {
                                    this._nbRecResult--;
                                }
                            }
                            if (this._nbRecResult == 0 && this._isReady) {
                                this.setDoneOrRegister(null);
                                if (this._thenCB != null) {
                                    this._thenCB(null);
                                }
                            }
                        }

                        public waitDefer(p_previous: org.kevoree.modeling.defer.KDefer): org.kevoree.modeling.defer.KDefer {
                            if (p_previous != this) {
                                if (!(<org.kevoree.modeling.defer.impl.Defer>p_previous).setDoneOrRegister(this)) {
                                    this._nbExpectedResult++;
                                }
                            }
                            return this;
                        }

                        public next(): org.kevoree.modeling.defer.KDefer {
                            var nextTask: org.kevoree.modeling.defer.impl.Defer = new org.kevoree.modeling.defer.impl.Defer();
                            nextTask.waitDefer(this);
                            return nextTask;
                        }

                        public wait(resultName: string): (p : any) => void {
                            return  (o : any) => {
                                this._results.put(resultName, o);
                            };
                        }

                        public isDone(): boolean {
                            return this._isDone;
                        }

                        public getResult(resultName: string): any {
                            if (this._isDone) {
                                return this._results.get(resultName);
                            } else {
                                throw new java.lang.Exception("Task is not executed yet !");
                            }
                        }

                        public then(cb: (p : any) => void): void {
                            this._thenCB = cb;
                            this._isReady = true;
                            this.informParentEnd(null);
                        }

                    }

                }
            }
            export module event {
                export interface KEventListener {

                    on(src: org.kevoree.modeling.KObject, modifications: org.kevoree.modeling.meta.KMeta[]): void;

                }

                export interface KEventMultiListener {

                    on(objects: org.kevoree.modeling.KObject[]): void;

                }

                export module impl {
                    export class LocalEventListeners {

                        private _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                        private _internalListenerKeyGen: org.kevoree.modeling.memory.manager.impl.KeyCalculator;
                        private _simpleListener: org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>;
                        private _multiListener: org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>;
                        private _listener2Object: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;
                        private _listener2Objects: org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>;
                        private _obj2Listener: org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>;
                        private _group2Listener: org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>;
                        constructor() {
                            this._internalListenerKeyGen = new org.kevoree.modeling.memory.manager.impl.KeyCalculator(<number>0, 0);
                            this._simpleListener = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                            this._multiListener = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                            this._obj2Listener = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                            this._listener2Object = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                            this._listener2Objects = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                            this._group2Listener = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                        }

                        public registerListener(groupId: number, origin: org.kevoree.modeling.KObject, listener: (p : org.kevoree.modeling.KObject, p1 : org.kevoree.modeling.meta.KMeta[]) => void): void {
                            var generateNewID: number = this._internalListenerKeyGen.nextKey();
                            this._simpleListener.put(generateNewID, listener);
                            this._listener2Object.put(generateNewID, origin.universe());
                            var subLayer: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap = this._obj2Listener.get(origin.uuid());
                            if (subLayer == null) {
                                subLayer = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                this._obj2Listener.put(origin.uuid(), subLayer);
                            }
                            subLayer.put(generateNewID, origin.universe());
                            subLayer = this._group2Listener.get(groupId);
                            if (subLayer == null) {
                                subLayer = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                this._group2Listener.put(groupId, subLayer);
                            }
                            subLayer.put(generateNewID, 1);
                        }

                        public registerListenerAll(groupId: number, universe: number, objects: number[], listener: (p : org.kevoree.modeling.KObject[]) => void): void {
                            var generateNewID: number = this._internalListenerKeyGen.nextKey();
                            this._multiListener.put(generateNewID, listener);
                            this._listener2Objects.put(generateNewID, objects);
                            var subLayer: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;
                            for (var i: number = 0; i < objects.length; i++) {
                                subLayer = this._obj2Listener.get(objects[i]);
                                if (subLayer == null) {
                                    subLayer = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                    this._obj2Listener.put(objects[i], subLayer);
                                }
                                subLayer.put(generateNewID, universe);
                            }
                            subLayer = this._group2Listener.get(groupId);
                            if (subLayer == null) {
                                subLayer = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                this._group2Listener.put(groupId, subLayer);
                            }
                            subLayer.put(generateNewID, 2);
                        }

                        public unregister(groupId: number): void {
                            var groupLayer: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap = this._group2Listener.get(groupId);
                            if (groupLayer != null) {
                                groupLayer.each( (listenerID : number, value : number) => {
                                    if (value == 1) {
                                        this._simpleListener.remove(listenerID);
                                        var previousObject: number = this._listener2Object.get(listenerID);
                                        this._listener2Object.remove(listenerID);
                                        var _obj2ListenerLayer: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap = this._obj2Listener.get(previousObject);
                                        if (_obj2ListenerLayer != null) {
                                            _obj2ListenerLayer.remove(listenerID);
                                        }
                                    } else {
                                        this._multiListener.remove(listenerID);
                                        var previousObjects: number[] = this._listener2Objects.get(listenerID);
                                        for (var i: number = 0; i < previousObjects.length; i++) {
                                            var _obj2ListenerLayer: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap = this._obj2Listener.get(previousObjects[i]);
                                            if (_obj2ListenerLayer != null) {
                                                _obj2ListenerLayer.remove(listenerID);
                                            }
                                        }
                                        this._listener2Objects.remove(listenerID);
                                    }
                                });
                                this._group2Listener.remove(groupId);
                            }
                        }

                        public clear(): void {
                            this._simpleListener.clear();
                            this._multiListener.clear();
                            this._obj2Listener.clear();
                            this._group2Listener.clear();
                            this._listener2Object.clear();
                            this._listener2Objects.clear();
                        }

                        public setManager(manager: org.kevoree.modeling.memory.manager.KMemoryManager): void {
                            this._manager = manager;
                        }

                        public dispatch(param: org.kevoree.modeling.message.KMessage): void {
                            if (this._manager != null) {
                                var _cacheUniverse: org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any> = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                if (param instanceof org.kevoree.modeling.message.impl.Events) {
                                    var messages: org.kevoree.modeling.message.impl.Events = <org.kevoree.modeling.message.impl.Events>param;
                                    var toLoad: org.kevoree.modeling.KContentKey[] = new Array();
                                    var multiCounters: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap[] = new Array();
                                    for (var i: number = 0; i < messages.size(); i++) {
                                        var loopKey: org.kevoree.modeling.KContentKey = messages.getKey(i);
                                        var listeners: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap = this._obj2Listener.get(loopKey.obj);
                                        var isSelect: boolean[] = [false];
                                        if (listeners != null) {
                                            listeners.each( (listenerKey : number, universeKey : number) => {
                                                if (universeKey == loopKey.universe) {
                                                    isSelect[0] = true;
                                                    if (this._multiListener.contains(listenerKey)) {
                                                        if (multiCounters[0] == null) {
                                                            multiCounters[0] = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                                        }
                                                        var previous: number = 0;
                                                        if (multiCounters[0].contains(listenerKey)) {
                                                            previous = multiCounters[0].get(listenerKey);
                                                        }
                                                        previous++;
                                                        multiCounters[0].put(listenerKey, previous);
                                                    }
                                                }
                                            });
                                        }
                                        if (isSelect[0]) {
                                            toLoad[i] = loopKey;
                                        }
                                    }
                                    (<org.kevoree.modeling.memory.manager.impl.HeapMemoryManager>this._manager).bumpKeysToCache(toLoad,  (kMemoryElements : org.kevoree.modeling.memory.KMemoryElement[]) => {
                                        var multiObjectSets: org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>[] = new Array();
                                        var multiObjectIndexes: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap[] = new Array();
                                        if (multiCounters[0] != null) {
                                            multiObjectSets[0] = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                            multiObjectIndexes[0] = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                            multiCounters[0].each( (listenerKey : number, value : number) => {
                                                multiObjectSets[0].put(listenerKey, new Array());
                                                multiObjectIndexes[0].put(listenerKey, 0);
                                            });
                                        }
                                        var listeners: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap;
                                        for (var i: number = 0; i < messages.size(); i++) {
                                            if (kMemoryElements[i] != null && kMemoryElements[i] instanceof org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment) {
                                                var correspondingKey: org.kevoree.modeling.KContentKey = toLoad[i];
                                                listeners = this._obj2Listener.get(correspondingKey.obj);
                                                if (listeners != null) {
                                                    var cachedUniverse: org.kevoree.modeling.KUniverse<any, any, any> = _cacheUniverse.get(correspondingKey.universe);
                                                    if (cachedUniverse == null) {
                                                        cachedUniverse = this._manager.model().universe(correspondingKey.universe);
                                                        _cacheUniverse.put(correspondingKey.universe, cachedUniverse);
                                                    }
                                                    var segment: org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment = <org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment>kMemoryElements[i];
                                                    var toDispatch: org.kevoree.modeling.KObject = (<org.kevoree.modeling.abs.AbstractKModel<any>>this._manager.model()).createProxy(correspondingKey.universe, correspondingKey.time, correspondingKey.obj, this._manager.model().metaModel().metaClasses()[segment.metaClassIndex()]);
                                                    if (toDispatch != null) {
                                                        kMemoryElements[i].inc();
                                                    }
                                                    var meta: org.kevoree.modeling.meta.KMeta[] = new Array();
                                                    for (var j: number = 0; j < messages.getIndexes(i).length; j++) {
                                                        meta[j] = toDispatch.metaClass().meta(messages.getIndexes(i)[j]);
                                                    }
                                                    listeners.each( (listenerKey : number, value : number) => {
                                                        var listener: (p : org.kevoree.modeling.KObject, p1 : org.kevoree.modeling.meta.KMeta[]) => void = this._simpleListener.get(listenerKey);
                                                        if (listener != null) {
                                                            listener(toDispatch, meta);
                                                        } else {
                                                            var multiListener: (p : org.kevoree.modeling.KObject[]) => void = this._multiListener.get(listenerKey);
                                                            if (multiListener != null) {
                                                                if (multiObjectSets[0] != null && multiObjectIndexes[0] != null) {
                                                                    var index: number = multiObjectIndexes[0].get(listenerKey);
                                                                    multiObjectSets[0].get(listenerKey)[<number>index] = toDispatch;
                                                                    index = index + 1;
                                                                    multiObjectIndexes[0].put(listenerKey, index);
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                        if (multiObjectSets[0] != null) {
                                            multiObjectSets[0].each( (key : number, value : org.kevoree.modeling.KObject[]) => {
                                                var multiListener: (p : org.kevoree.modeling.KObject[]) => void = this._multiListener.get(key);
                                                if (multiListener != null) {
                                                    multiListener(value);
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }

                    }

                }
            }
            export module extrapolation {
                export interface Extrapolation {

                    extrapolate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute): any;

                    mutate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute, payload: any): void;

                }

                export module impl {
                    export class DiscreteExtrapolation implements org.kevoree.modeling.extrapolation.Extrapolation {

                        private static INSTANCE: org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation;
                        public static instance(): org.kevoree.modeling.extrapolation.Extrapolation {
                            if (DiscreteExtrapolation.INSTANCE == null) {
                                DiscreteExtrapolation.INSTANCE = new org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation();
                            }
                            return DiscreteExtrapolation.INSTANCE;
                        }

                        public extrapolate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute): any {
                            var payload: org.kevoree.modeling.memory.struct.segment.KMemorySegment = (<org.kevoree.modeling.abs.AbstractKObject>current)._manager.segment(current.universe(), current.now(), current.uuid(), org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, current.metaClass(), null);
                            if (payload != null) {
                                return payload.get(attribute.index(), current.metaClass());
                            } else {
                                return null;
                            }
                        }

                        public mutate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute, payload: any): void {
                            var internalPayload: org.kevoree.modeling.memory.struct.segment.KMemorySegment = (<org.kevoree.modeling.abs.AbstractKObject>current)._manager.segment(current.universe(), current.now(), current.uuid(), org.kevoree.modeling.memory.manager.AccessMode.NEW, current.metaClass(), null);
                            if (internalPayload != null) {
                                internalPayload.set(attribute.index(), payload, current.metaClass());
                            }
                        }

                    }

                    export class PolynomialExtrapolation implements org.kevoree.modeling.extrapolation.Extrapolation {

                        private static _maxDegree: number = 20;
                        private static DEGREE: number = 0;
                        private static NUMSAMPLES: number = 1;
                        private static STEP: number = 2;
                        private static LASTTIME: number = 3;
                        private static WEIGHTS: number = 4;
                        private static INSTANCE: org.kevoree.modeling.extrapolation.impl.PolynomialExtrapolation;
                        public extrapolate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute): any {
                            var trace: org.kevoree.modeling.memory.manager.KMemorySegmentResolutionTrace = new org.kevoree.modeling.memory.manager.impl.MemorySegmentResolutionTrace();
                            var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = (<org.kevoree.modeling.abs.AbstractKObject>current)._manager.segment(current.universe(), current.now(), current.uuid(), org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, current.metaClass(), trace);
                            if (raw != null) {
                                var extrapolatedValue: number = this.extrapolateValue(raw, current.metaClass(), attribute.index(), current.now(), trace.getTime());
                                if (attribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.DOUBLE) {
                                    return extrapolatedValue;
                                } else {
                                    if (attribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.LONG) {
                                        return extrapolatedValue.longValue();
                                    } else {
                                        if (attribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.FLOAT) {
                                            return extrapolatedValue.floatValue();
                                        } else {
                                            if (attribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.INT) {
                                                return extrapolatedValue.intValue();
                                            } else {
                                                if (attribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.SHORT) {
                                                    return extrapolatedValue.shortValue();
                                                } else {
                                                    return null;
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                return null;
                            }
                        }

                        private extrapolateValue(segment: org.kevoree.modeling.memory.struct.segment.KMemorySegment, meta: org.kevoree.modeling.meta.KMetaClass, index: number, time: number, timeOrigin: number): number {
                            if (segment.getInferSize(index, meta) == 0) {
                                return 0.0;
                            }
                            var result: number = 0;
                            var power: number = 1;
                            var inferSTEP: number = segment.getInferElem(index, PolynomialExtrapolation.STEP, meta);
                            if (inferSTEP == 0) {
                                return segment.getInferElem(index, PolynomialExtrapolation.WEIGHTS, meta);
                            }
                            var t: number = (time - timeOrigin) / inferSTEP;
                            for (var j: number = 0; j <= segment.getInferElem(index, PolynomialExtrapolation.DEGREE, meta); j++) {
                                result += segment.getInferElem(index, (j + PolynomialExtrapolation.WEIGHTS), meta) * power;
                                power = power * t;
                            }
                            return result;
                        }

                        private maxErr(precision: number, degree: number): number {
                            var tol: number = precision;
                            tol = precision / Math.pow(2, degree + 0.5);
                            return tol;
                        }

                        public insert(time: number, value: number, timeOrigin: number, raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment, index: number, precision: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean {
                            if (raw.getInferSize(index, metaClass) == 0) {
                                this.initial_feed(time, value, raw, index, metaClass);
                                return true;
                            }
                            if (raw.getInferElem(index, PolynomialExtrapolation.NUMSAMPLES, metaClass) == 1) {
                                raw.setInferElem(index, PolynomialExtrapolation.STEP, (time - timeOrigin), metaClass);
                            }
                            var deg: number = <number>raw.getInferElem(index, PolynomialExtrapolation.DEGREE, metaClass);
                            var num: number = <number>raw.getInferElem(index, PolynomialExtrapolation.NUMSAMPLES, metaClass);
                            var maxError: number = this.maxErr(precision, deg);
                            if (Math.abs(this.extrapolateValue(raw, metaClass, index, time, timeOrigin) - value) <= maxError) {
                                var nexNumSamples: number = raw.getInferElem(index, PolynomialExtrapolation.NUMSAMPLES, metaClass) + 1;
                                raw.setInferElem(index, PolynomialExtrapolation.NUMSAMPLES, nexNumSamples, metaClass);
                                raw.setInferElem(index, PolynomialExtrapolation.LASTTIME, time - timeOrigin, metaClass);
                                return true;
                            }
                            var newMaxDegree: number = Math.min(num, PolynomialExtrapolation._maxDegree);
                            if (deg < newMaxDegree) {
                                deg++;
                                var ss: number = Math.min(deg * 2, num);
                                var times: number[] = new Array();
                                var values: number[] = new Array();
                                for (var i: number = 0; i < ss; i++) {
                                    times[i] = (<number>i * num * (raw.getInferElem(index, PolynomialExtrapolation.LASTTIME, metaClass)) / (ss * raw.getInferElem(index, PolynomialExtrapolation.STEP, metaClass)));
                                    values[i] = this.internal_extrapolate(times[i], raw, index, metaClass);
                                }
                                times[ss] = (time - timeOrigin) / raw.getInferElem(index, PolynomialExtrapolation.STEP, metaClass);
                                values[ss] = value;
                                var pf: org.kevoree.modeling.extrapolation.impl.maths.PolynomialFitEjml = new org.kevoree.modeling.extrapolation.impl.maths.PolynomialFitEjml(deg);
                                pf.fit(times, values);
                                if (this.tempError(pf.getCoef(), times, values) <= maxError) {
                                    raw.extendInfer(index, (raw.getInferSize(index, metaClass) + 1), metaClass);
                                    for (var i: number = 0; i < pf.getCoef().length; i++) {
                                        raw.setInferElem(index, i + PolynomialExtrapolation.WEIGHTS, pf.getCoef()[i], metaClass);
                                    }
                                    raw.setInferElem(index, PolynomialExtrapolation.DEGREE, deg, metaClass);
                                    raw.setInferElem(index, PolynomialExtrapolation.NUMSAMPLES, num + 1, metaClass);
                                    raw.setInferElem(index, PolynomialExtrapolation.LASTTIME, time - timeOrigin, metaClass);
                                    return true;
                                }
                            }
                            return false;
                        }

                        private tempError(computedWeights: number[], times: number[], values: number[]): number {
                            var maxErr: number = 0;
                            var temp: number;
                            var ds: number;
                            for (var i: number = 0; i < times.length; i++) {
                                temp = Math.abs(values[i] - this.test_extrapolate(times[i], computedWeights));
                                if (temp > maxErr) {
                                    maxErr = temp;
                                }
                            }
                            return maxErr;
                        }

                        private test_extrapolate(time: number, weights: number[]): number {
                            var result: number = 0;
                            var power: number = 1;
                            for (var j: number = 0; j < weights.length; j++) {
                                result += weights[j] * power;
                                power = power * time;
                            }
                            return result;
                        }

                        private internal_extrapolate(t: number, raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment, index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number {
                            var result: number = 0;
                            var power: number = 1;
                            if (raw.getInferElem(index, PolynomialExtrapolation.STEP, metaClass) == 0) {
                                return raw.getInferElem(index, PolynomialExtrapolation.WEIGHTS, metaClass);
                            }
                            for (var j: number = 0; j <= raw.getInferElem(index, PolynomialExtrapolation.DEGREE, metaClass); j++) {
                                result += raw.getInferElem(index, (j + PolynomialExtrapolation.WEIGHTS), metaClass) * power;
                                power = power * t;
                            }
                            return result;
                        }

                        private initial_feed(time: number, value: number, raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment, index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void {
                            raw.extendInfer(index, PolynomialExtrapolation.WEIGHTS + 1, metaClass);
                            raw.setInferElem(index, PolynomialExtrapolation.DEGREE, 0, metaClass);
                            raw.setInferElem(index, PolynomialExtrapolation.NUMSAMPLES, 1, metaClass);
                            raw.setInferElem(index, PolynomialExtrapolation.LASTTIME, 0, metaClass);
                            raw.setInferElem(index, PolynomialExtrapolation.STEP, 0, metaClass);
                            raw.setInferElem(index, PolynomialExtrapolation.WEIGHTS, value, metaClass);
                        }

                        public mutate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute, payload: any): void {
                            var trace: org.kevoree.modeling.memory.manager.KMemorySegmentResolutionTrace = new org.kevoree.modeling.memory.manager.impl.MemorySegmentResolutionTrace();
                            var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = current.manager().segment(current.universe(), current.now(), current.uuid(), org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, current.metaClass(), trace);
                            if (raw.getInferSize(attribute.index(), current.metaClass()) == 0) {
                                raw = current.manager().segment(current.universe(), current.now(), current.uuid(), org.kevoree.modeling.memory.manager.AccessMode.NEW, current.metaClass(), null);
                            }
                            if (!this.insert(current.now(), this.castNumber(payload), trace.getTime(), raw, attribute.index(), attribute.precision(), current.metaClass())) {
                                var prevTime: number = <number>raw.getInferElem(attribute.index(), PolynomialExtrapolation.LASTTIME, current.metaClass()) + trace.getTime();
                                var val: number = this.extrapolateValue(raw, current.metaClass(), attribute.index(), prevTime, trace.getTime());
                                var newSegment: org.kevoree.modeling.memory.struct.segment.KMemorySegment = current.manager().segment(current.universe(), prevTime, current.uuid(), org.kevoree.modeling.memory.manager.AccessMode.NEW, current.metaClass(), null);
                                this.insert(prevTime, val, prevTime, newSegment, attribute.index(), attribute.precision(), current.metaClass());
                                this.insert(current.now(), this.castNumber(payload), prevTime, newSegment, attribute.index(), attribute.precision(), current.metaClass());
                            }
                        }

                        private castNumber(payload: any): number {
                             return +payload;
                        }

                        public static instance(): org.kevoree.modeling.extrapolation.Extrapolation {
                            if (PolynomialExtrapolation.INSTANCE == null) {
                                PolynomialExtrapolation.INSTANCE = new org.kevoree.modeling.extrapolation.impl.PolynomialExtrapolation();
                            }
                            return PolynomialExtrapolation.INSTANCE;
                        }

                    }

                    export module maths {
                        export class AdjLinearSolverQr {

                            public numRows: number;
                            public numCols: number;
                            private decomposer: org.kevoree.modeling.extrapolation.impl.maths.QRDecompositionHouseholderColumn_D64;
                            public maxRows: number = -1;
                            public maxCols: number = -1;
                            public Q: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            public R: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            private Y: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            private Z: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            public setA(A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): boolean {
                                if (A.numRows > this.maxRows || A.numCols > this.maxCols) {
                                    this.setMaxSize(A.numRows, A.numCols);
                                }
                                this.numRows = A.numRows;
                                this.numCols = A.numCols;
                                if (!this.decomposer.decompose(A)) {
                                    return false;
                                }
                                this.Q.reshape(this.numRows, this.numRows, false);
                                this.R.reshape(this.numRows, this.numCols, false);
                                this.decomposer.getQ(this.Q, false);
                                this.decomposer.getR(this.R, false);
                                return true;
                            }

                            private solveU(U: number[], b: number[], n: number): void {
                                for (var i: number = n - 1; i >= 0; i--) {
                                    var sum: number = b[i];
                                    var indexU: number = i * n + i + 1;
                                    for (var j: number = i + 1; j < n; j++) {
                                        sum -= U[indexU++] * b[j];
                                    }
                                    b[i] = sum / U[i * n + i];
                                }
                            }

                            public solve(B: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, X: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void {
                                var BnumCols: number = B.numCols;
                                this.Y.reshape(this.numRows, 1, false);
                                this.Z.reshape(this.numRows, 1, false);
                                for (var colB: number = 0; colB < BnumCols; colB++) {
                                    for (var i: number = 0; i < this.numRows; i++) {
                                        this.Y.data[i] = B.unsafe_get(i, colB);
                                    }
                                    org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.multTransA(this.Q, this.Y, this.Z);
                                    this.solveU(this.R.data, this.Z.data, this.numCols);
                                    for (var i: number = 0; i < this.numCols; i++) {
                                        X.cset(i, colB, this.Z.data[i]);
                                    }
                                }
                            }

                            constructor() {
                                this.decomposer = new org.kevoree.modeling.extrapolation.impl.maths.QRDecompositionHouseholderColumn_D64();
                            }

                            public setMaxSize(maxRows: number, maxCols: number): void {
                                maxRows += 5;
                                this.maxRows = maxRows;
                                this.maxCols = maxCols;
                                this.Q = new org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F(maxRows, maxRows);
                                this.R = new org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F(maxRows, maxCols);
                                this.Y = new org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F(maxRows, 1);
                                this.Z = new org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F(maxRows, 1);
                            }

                        }

                        export class DenseMatrix64F {

                            public numRows: number;
                            public numCols: number;
                            public data: number[];
                            public static MULT_COLUMN_SWITCH: number = 15;
                            public static multTransA_smallMV(A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, B: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, C: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void {
                                var cIndex: number = 0;
                                for (var i: number = 0; i < A.numCols; i++) {
                                    var total: number = 0.0;
                                    var indexA: number = i;
                                    for (var j: number = 0; j < A.numRows; j++) {
                                        total += A.get(indexA) * B.get(j);
                                        indexA += A.numCols;
                                    }
                                    C.set(cIndex++, total);
                                }
                            }

                            public static multTransA_reorderMV(A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, B: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, C: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void {
                                if (A.numRows == 0) {
                                    org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.fill(C, 0);
                                    return;
                                }
                                var B_val: number = B.get(0);
                                for (var i: number = 0; i < A.numCols; i++) {
                                    C.set(i, A.get(i) * B_val);
                                }
                                var indexA: number = A.numCols;
                                for (var i: number = 1; i < A.numRows; i++) {
                                    B_val = B.get(i);
                                    for (var j: number = 0; j < A.numCols; j++) {
                                        C.plus(j, A.get(indexA++) * B_val);
                                    }
                                }
                            }

                            public static multTransA_reorderMM(a: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, b: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, c: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void {
                                if (a.numCols == 0 || a.numRows == 0) {
                                    org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.fill(c, 0);
                                    return;
                                }
                                var valA: number;
                                for (var i: number = 0; i < a.numCols; i++) {
                                    var indexC_start: number = i * c.numCols;
                                    valA = a.get(i);
                                    var indexB: number = 0;
                                    var end: number = indexB + b.numCols;
                                    var indexC: number = indexC_start;
                                    while (indexB < end){
                                        c.set(indexC++, valA * b.get(indexB++));
                                    }
                                    for (var k: number = 1; k < a.numRows; k++) {
                                        valA = a.unsafe_get(k, i);
                                        end = indexB + b.numCols;
                                        indexC = indexC_start;
                                        while (indexB < end){
                                            c.plus(indexC++, valA * b.get(indexB++));
                                        }
                                    }
                                }
                            }

                            public static multTransA_smallMM(a: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, b: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, c: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void {
                                var cIndex: number = 0;
                                for (var i: number = 0; i < a.numCols; i++) {
                                    for (var j: number = 0; j < b.numCols; j++) {
                                        var indexA: number = i;
                                        var indexB: number = j;
                                        var end: number = indexB + b.numRows * b.numCols;
                                        var total: number = 0;
                                        for (; indexB < end; indexB += b.numCols) {
                                            total += a.get(indexA) * b.get(indexB);
                                            indexA += a.numCols;
                                        }
                                        c.set(cIndex++, total);
                                    }
                                }
                            }

                            public static multTransA(a: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, b: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, c: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void {
                                if (b.numCols == 1) {
                                    if (a.numCols >= org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.MULT_COLUMN_SWITCH) {
                                        org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.multTransA_reorderMV(a, b, c);
                                    } else {
                                        org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.multTransA_smallMV(a, b, c);
                                    }
                                } else {
                                    if (a.numCols >= org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.MULT_COLUMN_SWITCH || b.numCols >= org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.MULT_COLUMN_SWITCH) {
                                        org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.multTransA_reorderMM(a, b, c);
                                    } else {
                                        org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.multTransA_smallMM(a, b, c);
                                    }
                                }
                            }

                            public static setIdentity(mat: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void {
                                var width: number = mat.numRows < mat.numCols ? mat.numRows : mat.numCols;
                                java.util.Arrays.fill(mat.data, 0, mat.getNumElements(), 0);
                                var index: number = 0;
                                for (var i: number = 0; i < width; i++) {
                                    mat.data[index] = 1;
                                    index += mat.numCols + 1;
                                }
                            }

                            public static widentity(width: number): org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F {
                                var ret: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F = new org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F(width, width);
                                for (var i: number = 0; i < width; i++) {
                                    ret.cset(i, i, 1.0);
                                }
                                return ret;
                            }

                            public static identity(numRows: number, numCols: number): org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F {
                                var ret: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F = new org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F(numRows, numCols);
                                var small: number = numRows < numCols ? numRows : numCols;
                                for (var i: number = 0; i < small; i++) {
                                    ret.cset(i, i, 1.0);
                                }
                                return ret;
                            }

                            public static fill(a: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, value: number): void {
                                java.util.Arrays.fill(a.data, 0, a.getNumElements(), value);
                            }

                            public get(index: number): number {
                                return this.data[index];
                            }

                            public set(index: number, val: number): number {
                                return this.data[index] = val;
                            }

                            public plus(index: number, val: number): number {
                                return this.data[index] += val;
                            }

                            constructor(numRows: number, numCols: number) {
                                this.data = new Array();
                                this.numRows = numRows;
                                this.numCols = numCols;
                            }

                            public reshape(numRows: number, numCols: number, saveValues: boolean): void {
                                if (this.data.length < numRows * numCols) {
                                    var d: number[] = new Array();
                                    if (saveValues) {
                                        System.arraycopy(this.data, 0, d, 0, this.getNumElements());
                                    }
                                    this.data = d;
                                }
                                this.numRows = numRows;
                                this.numCols = numCols;
                            }

                            public cset(row: number, col: number, value: number): void {
                                this.data[row * this.numCols + col] = value;
                            }

                            public unsafe_get(row: number, col: number): number {
                                return this.data[row * this.numCols + col];
                            }

                            public getNumElements(): number {
                                return this.numRows * this.numCols;
                            }

                        }

                        export class PolynomialFitEjml {

                            public A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            public coef: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            public y: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            public solver: org.kevoree.modeling.extrapolation.impl.maths.AdjLinearSolverQr;
                            constructor(degree: number) {
                                this.coef = new org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F(degree + 1, 1);
                                this.A = new org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F(1, degree + 1);
                                this.y = new org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F(1, 1);
                                this.solver = new org.kevoree.modeling.extrapolation.impl.maths.AdjLinearSolverQr();
                            }

                            public getCoef(): number[] {
                                return this.coef.data;
                            }

                            public fit(samplePoints: number[], observations: number[]): void {
                                this.y.reshape(observations.length, 1, false);
                                System.arraycopy(observations, 0, this.y.data, 0, observations.length);
                                this.A.reshape(this.y.numRows, this.coef.numRows, false);
                                for (var i: number = 0; i < observations.length; i++) {
                                    var obs: number = 1;
                                    for (var j: number = 0; j < this.coef.numRows; j++) {
                                        this.A.cset(i, j, obs);
                                        obs *= samplePoints[i];
                                    }
                                }
                                this.solver.setA(this.A);
                                this.solver.solve(this.y, this.coef);
                            }

                        }

                        export class QRDecompositionHouseholderColumn_D64 {

                            public dataQR: number[][];
                            public v: number[];
                            public numCols: number;
                            public numRows: number;
                            public minLength: number;
                            public gammas: number[];
                            public gamma: number;
                            public tau: number;
                            public error: boolean;
                            public setExpectedMaxSize(numRows: number, numCols: number): void {
                                this.numCols = numCols;
                                this.numRows = numRows;
                                this.minLength = Math.min(numCols, numRows);
                                var maxLength: number = Math.max(numCols, numRows);
                                if (this.dataQR == null || this.dataQR.length < numCols || this.dataQR[0].length < numRows) {
                                    this.dataQR = new Array(new Array());
                                    for (var i: number = 0; i < numCols; i++) {
                                        this.dataQR[i] = new Array();
                                    }
                                    this.v = new Array();
                                    this.gammas = new Array();
                                }
                                if (this.v.length < maxLength) {
                                    this.v = new Array();
                                }
                                if (this.gammas.length < this.minLength) {
                                    this.gammas = new Array();
                                }
                            }

                            public getQ(Q: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, compact: boolean): org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F {
                                if (compact) {
                                    if (Q == null) {
                                        Q = org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.identity(this.numRows, this.minLength);
                                    } else {
                                        org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.setIdentity(Q);
                                    }
                                } else {
                                    if (Q == null) {
                                        Q = org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.widentity(this.numRows);
                                    } else {
                                        org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F.setIdentity(Q);
                                    }
                                }
                                for (var j: number = this.minLength - 1; j >= 0; j--) {
                                    var u: number[] = this.dataQR[j];
                                    var vv: number = u[j];
                                    u[j] = 1;
                                    org.kevoree.modeling.extrapolation.impl.maths.QRDecompositionHouseholderColumn_D64.rank1UpdateMultR(Q, u, this.gammas[j], j, j, this.numRows, this.v);
                                    u[j] = vv;
                                }
                                return Q;
                            }

                            public getR(R: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, compact: boolean): org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F {
                                if (R == null) {
                                    if (compact) {
                                        R = new org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F(this.minLength, this.numCols);
                                    } else {
                                        R = new org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F(this.numRows, this.numCols);
                                    }
                                } else {
                                    for (var i: number = 0; i < R.numRows; i++) {
                                        var min: number = Math.min(i, R.numCols);
                                        for (var j: number = 0; j < min; j++) {
                                            R.cset(i, j, 0);
                                        }
                                    }
                                }
                                for (var j: number = 0; j < this.numCols; j++) {
                                    var colR: number[] = this.dataQR[j];
                                    var l: number = Math.min(j, this.numRows - 1);
                                    for (var i: number = 0; i <= l; i++) {
                                        var val: number = colR[i];
                                        R.cset(i, j, val);
                                    }
                                }
                                return R;
                            }

                            public decompose(A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): boolean {
                                this.setExpectedMaxSize(A.numRows, A.numCols);
                                this.convertToColumnMajor(A);
                                this.error = false;
                                for (var j: number = 0; j < this.minLength; j++) {
                                    this.householder(j);
                                    this.updateA(j);
                                }
                                return !this.error;
                            }

                            public convertToColumnMajor(A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void {
                                for (var x: number = 0; x < this.numCols; x++) {
                                    var colQ: number[] = this.dataQR[x];
                                    for (var y: number = 0; y < this.numRows; y++) {
                                        colQ[y] = A.data[y * this.numCols + x];
                                    }
                                }
                            }

                            public householder(j: number): void {
                                var u: number[] = this.dataQR[j];
                                var max: number = org.kevoree.modeling.extrapolation.impl.maths.QRDecompositionHouseholderColumn_D64.findMax(u, j, this.numRows - j);
                                if (max == 0.0) {
                                    this.gamma = 0;
                                    this.error = true;
                                } else {
                                    this.tau = org.kevoree.modeling.extrapolation.impl.maths.QRDecompositionHouseholderColumn_D64.computeTauAndDivide(j, this.numRows, u, max);
                                    var u_0: number = u[j] + this.tau;
                                    org.kevoree.modeling.extrapolation.impl.maths.QRDecompositionHouseholderColumn_D64.divideElements(j + 1, this.numRows, u, u_0);
                                    this.gamma = u_0 / this.tau;
                                    this.tau *= max;
                                    u[j] = -this.tau;
                                }
                                this.gammas[j] = this.gamma;
                            }

                            public updateA(w: number): void {
                                var u: number[] = this.dataQR[w];
                                for (var j: number = w + 1; j < this.numCols; j++) {
                                    var colQ: number[] = this.dataQR[j];
                                    var val: number = colQ[w];
                                    for (var k: number = w + 1; k < this.numRows; k++) {
                                        val += u[k] * colQ[k];
                                    }
                                    val *= this.gamma;
                                    colQ[w] -= val;
                                    for (var i: number = w + 1; i < this.numRows; i++) {
                                        colQ[i] -= u[i] * val;
                                    }
                                }
                            }

                            public static findMax(u: number[], startU: number, length: number): number {
                                var max: number = -1;
                                var index: number = startU;
                                var stopIndex: number = startU + length;
                                for (; index < stopIndex; index++) {
                                    var val: number = u[index];
                                    val = (val < 0.0) ? -val : val;
                                    if (val > max) {
                                        max = val;
                                    }
                                }
                                return max;
                            }

                            public static divideElements(j: number, numRows: number, u: number[], u_0: number): void {
                                for (var i: number = j; i < numRows; i++) {
                                    u[i] /= u_0;
                                }
                            }

                            public static computeTauAndDivide(j: number, numRows: number, u: number[], max: number): number {
                                var tau: number = 0;
                                for (var i: number = j; i < numRows; i++) {
                                    var d: number = u[i] /= max;
                                    tau += d * d;
                                }
                                tau = Math.sqrt(tau);
                                if (u[j] < 0) {
                                    tau = -tau;
                                }
                                return tau;
                            }

                            public static rank1UpdateMultR(A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, u: number[], gamma: number, colA0: number, w0: number, w1: number, _temp: number[]): void {
                                for (var i: number = colA0; i < A.numCols; i++) {
                                    _temp[i] = u[w0] * A.data[w0 * A.numCols + i];
                                }
                                for (var k: number = w0 + 1; k < w1; k++) {
                                    var indexA: number = k * A.numCols + colA0;
                                    var valU: number = u[k];
                                    for (var i: number = colA0; i < A.numCols; i++) {
                                        _temp[i] += valU * A.data[indexA++];
                                    }
                                }
                                for (var i: number = colA0; i < A.numCols; i++) {
                                    _temp[i] *= gamma;
                                }
                                for (var i: number = w0; i < w1; i++) {
                                    var valU: number = u[i];
                                    var indexA: number = i * A.numCols + colA0;
                                    for (var j: number = colA0; j < A.numCols; j++) {
                                        A.data[indexA++] -= valU * _temp[j];
                                    }
                                }
                            }

                        }

                    }
                }
            }
            export module format {
                export interface KModelFormat {

                    save(model: org.kevoree.modeling.KObject, cb: (p : string) => void): void;

                    saveRoot(cb: (p : string) => void): void;

                    load(payload: string, cb: (p : any) => void): void;

                }

                export module json {
                    export class JsonFormat implements org.kevoree.modeling.format.KModelFormat {

                        public static KEY_META: string = "@class";
                        public static KEY_UUID: string = "@uuid";
                        public static KEY_ROOT: string = "@root";
                        private _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                        private _universe: number;
                        private _time: number;
                        private static NULL_PARAM_MSG: string = "one parameter is null";
                        constructor(p_universe: number, p_time: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
                            this._manager = p_manager;
                            this._universe = p_universe;
                            this._time = p_time;
                        }

                        public save(model: org.kevoree.modeling.KObject, cb: (p : string) => void): void {
                            if (org.kevoree.modeling.util.Checker.isDefined(model) && org.kevoree.modeling.util.Checker.isDefined(cb)) {
                                org.kevoree.modeling.format.json.JsonModelSerializer.serialize(model, cb);
                            } else {
                                throw new java.lang.RuntimeException(JsonFormat.NULL_PARAM_MSG);
                            }
                        }

                        public saveRoot(cb: (p : string) => void): void {
                            if (org.kevoree.modeling.util.Checker.isDefined(cb)) {
                                this._manager.getRoot(this._universe, this._time,  (root : org.kevoree.modeling.KObject) => {
                                    if (root == null) {
                                        cb(null);
                                    } else {
                                        org.kevoree.modeling.format.json.JsonModelSerializer.serialize(root, cb);
                                    }
                                });
                            }
                        }

                        public load(payload: string, cb: (p : any) => void): void {
                            if (org.kevoree.modeling.util.Checker.isDefined(payload)) {
                                org.kevoree.modeling.format.json.JsonModelLoader.load(this._manager, this._universe, this._time, payload, cb);
                            } else {
                                throw new java.lang.RuntimeException(JsonFormat.NULL_PARAM_MSG);
                            }
                        }

                    }

                    export class JsonModelLoader {

                        public static load(manager: org.kevoree.modeling.memory.manager.KMemoryManager, universe: number, time: number, payload: string, callback: (p : java.lang.Throwable) => void): void {
                             if (payload == null) {
                             callback(null);
                             } else {
                             var toLoadObj = JSON.parse(payload);
                             var rootElem = [];
                             var mappedKeys: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(toLoadObj.length, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                             for(var i = 0; i < toLoadObj.length; i++) {
                             var elem = toLoadObj[i];
                             var kid = elem[org.kevoree.modeling.format.json.JsonFormat.KEY_UUID];
                             mappedKeys.put(<number>kid, manager.nextObjectKey());
                             }
                             for(var i = 0; i < toLoadObj.length; i++) {
                             var elemRaw = toLoadObj[i];
                             var elem2 = new org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>(Object.keys(elemRaw).length, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                             for(var ik in elemRaw){ elem2[ik] = elemRaw[ik]; }
                             try {
                             org.kevoree.modeling.format.json.JsonModelLoader.loadObj(elem2, manager, universe, time, mappedKeys, rootElem);
                             } catch(e){ console.error(e); }
                             }
                             if (rootElem[0] != null) { manager.setRoot(rootElem[0], (throwable : java.lang.Throwable) => { if (callback != null) { callback(throwable); }}); } else { if (callback != null) { callback(null); } }
                             }
                        }

                        private static loadObj(p_param: org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>, manager: org.kevoree.modeling.memory.manager.KMemoryManager, universe: number, time: number, p_mappedKeys: org.kevoree.modeling.memory.struct.map.KLongLongMap, p_rootElem: org.kevoree.modeling.KObject[]): void {
                            var kid: number = java.lang.Long.parseLong(p_param.get(org.kevoree.modeling.format.json.JsonFormat.KEY_UUID).toString());
                            var meta: string = p_param.get(org.kevoree.modeling.format.json.JsonFormat.KEY_META).toString();
                            var metaClass: org.kevoree.modeling.meta.KMetaClass = manager.model().metaModel().metaClassByName(meta);
                            var current: org.kevoree.modeling.KObject = (<org.kevoree.modeling.abs.AbstractKModel<any>>manager.model()).createProxy(universe, time, p_mappedKeys.get(kid), metaClass);
                            manager.initKObject(current);
                            var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = manager.segment(current.universe(), current.now(), current.uuid(), org.kevoree.modeling.memory.manager.AccessMode.NEW, current.metaClass(), null);
                            p_param.each( (metaKey : string, payload_content : any) => {
                                if (metaKey.equals(org.kevoree.modeling.format.json.JsonFormat.KEY_ROOT)) {
                                    p_rootElem[0] = current;
                                } else {
                                    var metaElement: org.kevoree.modeling.meta.KMeta = metaClass.metaByName(metaKey);
                                    if (payload_content != null) {
                                        if (metaElement != null && metaElement.metaType().equals(org.kevoree.modeling.meta.MetaType.ATTRIBUTE)) {
                                            var metaAttribute: org.kevoree.modeling.meta.KMetaAttribute = <org.kevoree.modeling.meta.KMetaAttribute>metaElement;
                                            if (metaAttribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.CONTINUOUS) {
                                                var plainRawSet: string[] = <string[]>p_param.get(metaAttribute.metaName());
                                                var convertedRaw: number[] = new Array();
                                                for (var l: number = 0; l < plainRawSet.length; l++) {
                                                    try {
                                                        convertedRaw[l] = java.lang.Double.parseDouble(plainRawSet[l]);
                                                    } catch ($ex$) {
                                                        if ($ex$ instanceof java.lang.Exception) {
                                                            var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                            e.printStackTrace();
                                                        } else {
                                                            throw $ex$;
                                                        }
                                                    }
                                                }
                                                raw.set(metaElement.index(), convertedRaw, current.metaClass());
                                            } else {
                                                var converted: any = null;
                                                var rawPayload: string = p_param.get(metaElement.metaName()).toString();
                                                if (metaAttribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.STRING) {
                                                    converted = org.kevoree.modeling.format.json.JsonString.unescape(rawPayload);
                                                } else {
                                                    if (metaAttribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.LONG) {
                                                        converted = java.lang.Long.parseLong(rawPayload);
                                                    } else {
                                                        if (metaAttribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.INT) {
                                                            converted = java.lang.Integer.parseInt(rawPayload);
                                                        } else {
                                                            if (metaAttribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.BOOL) {
                                                                converted = java.lang.Boolean.parseBoolean(rawPayload);
                                                            } else {
                                                                if (metaAttribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.SHORT) {
                                                                    converted = java.lang.Short.parseShort(rawPayload);
                                                                } else {
                                                                    if (metaAttribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.DOUBLE) {
                                                                        converted = java.lang.Double.parseDouble(rawPayload);
                                                                    } else {
                                                                        if (metaAttribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.FLOAT) {
                                                                            converted = java.lang.Float.parseFloat(rawPayload);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                raw.set(metaElement.index(), converted, current.metaClass());
                                            }
                                        } else {
                                            if (metaElement != null && metaElement instanceof org.kevoree.modeling.meta.impl.MetaReference) {
                                                try {
                                                    raw.set(metaElement.index(), org.kevoree.modeling.format.json.JsonModelLoader.transposeArr(<java.util.ArrayList<string>>payload_content, p_mappedKeys), current.metaClass());
                                                } catch ($ex$) {
                                                    if ($ex$ instanceof java.lang.Exception) {
                                                        var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                        e.printStackTrace();
                                                    } else {
                                                        throw $ex$;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }

                        private static transposeArr(plainRawSet: java.util.ArrayList<string>, p_mappedKeys: org.kevoree.modeling.memory.struct.map.KLongLongMap): number[] {
                             if (plainRawSet == null) { return null; }
                             var convertedRaw: number[] = new Array();
                             for (var l in plainRawSet) {
                             try {
                             var converted: number = java.lang.Long.parseLong(plainRawSet[l]);
                             if (p_mappedKeys.contains(converted)) { converted = p_mappedKeys.get(converted); }
                             convertedRaw[l] = converted;
                             } catch ($ex$) {
                             if ($ex$ instanceof java.lang.Exception) {
                             var e: java.lang.Exception = <java.lang.Exception>$ex$;
                             e.printStackTrace();
                             }
                             }
                             }
                             return convertedRaw;
                        }

                    }

                    export class JsonModelSerializer {

                        public static serialize(model: org.kevoree.modeling.KObject, callback: (p : string) => void): void {
                            (<org.kevoree.modeling.abs.AbstractKObject>model)._manager.getRoot(model.universe(), model.now(),  (rootObj : org.kevoree.modeling.KObject) => {
                                var isRoot: boolean = false;
                                if (rootObj != null) {
                                    isRoot = rootObj.uuid() == model.uuid();
                                }
                                var builder: java.lang.StringBuilder = new java.lang.StringBuilder();
                                builder.append("[\n");
                                org.kevoree.modeling.format.json.JsonModelSerializer.printJSON(model, builder, isRoot);
                                model.visit( (elem : org.kevoree.modeling.KObject) => {
                                    var isRoot2: boolean = false;
                                    if (rootObj != null) {
                                        isRoot2 = rootObj.uuid() == elem.uuid();
                                    }
                                    builder.append(",\n");
                                    try {
                                        org.kevoree.modeling.format.json.JsonModelSerializer.printJSON(elem, builder, isRoot2);
                                    } catch ($ex$) {
                                        if ($ex$ instanceof java.lang.Exception) {
                                            var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                            e.printStackTrace();
                                            builder.append("{}");
                                        } else {
                                            throw $ex$;
                                        }
                                    }
                                    return org.kevoree.modeling.traversal.visitor.KVisitResult.CONTINUE;
                                },  (throwable : java.lang.Throwable) => {
                                    builder.append("\n]\n");
                                    callback(builder.toString());
                                });
                            });
                        }

                        public static printJSON(elem: org.kevoree.modeling.KObject, builder: java.lang.StringBuilder, isRoot: boolean): void {
                            if (elem != null) {
                                var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = (<org.kevoree.modeling.abs.AbstractKObject>elem)._manager.segment(elem.universe(), elem.now(), elem.uuid(), org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, elem.metaClass(), null);
                                if (raw != null) {
                                    builder.append(org.kevoree.modeling.format.json.JsonRaw.encode(raw, elem.uuid(), elem.metaClass(), isRoot));
                                }
                            }
                        }

                    }

                    export class JsonObjectReader {

                         private readObject:any;
                         public parseObject(payload:string):void {
                         this.readObject = JSON.parse(payload);
                         }
                         public get(name:string):any {
                         return this.readObject[name];
                         }
                         public getAsStringArray(name:string):string[] {
                         return <string[]> this.readObject[name];
                         }
                         public keys():string[] {
                         var keysArr = []
                         for (var key in this.readObject) {
                         keysArr.push(key);
                         }
                         return keysArr;
                         }
                    }

                    export class JsonRaw {

                        public static encode(raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment, uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, isRoot: boolean): string {
                             var builder = {};
                             builder["@class"] = p_metaClass.metaName();
                             builder["@uuid"] = +uuid;
                             if(isRoot){ builder["@root"] = true; }
                             var metaElements = p_metaClass.metaElements();
                             for(var i=0;i<metaElements.length;i++){
                             var subElem;
                             if (metaElements[i] != null && metaElements[i].metaType() === org.kevoree.modeling.meta.MetaType.ATTRIBUTE) {
                             var metaAttribute = <org.kevoree.modeling.meta.KMetaAttribute>metaElements[i];
                             if(metaAttribute.attributeType() == org.kevoree.modeling.meta.KPrimitiveTypes.CONTINUOUS){
                             subElem = raw.getInfer(metaAttribute.index(),p_metaClass);
                             } else {
                             subElem = raw.get(metaAttribute.index(),p_metaClass);
                             }
                             } else {
                             subElem = raw.getRef(metaElements[i].index(),p_metaClass);
                             }
                             if(subElem != null && subElem != undefined){ builder[metaElements[i].metaName()] = subElem; }
                             }
                             return JSON.stringify(builder);
                        }

                    }

                    export class JsonString {

                        private static ESCAPE_CHAR: string = '\\';
                        public static encodeBuffer(buffer: java.lang.StringBuilder, chain: string): void {
                            if (chain == null) {
                                return;
                            }
                            var i: number = 0;
                            while (i < chain.length){
                                var ch: string = chain.charAt(i);
                                if (ch == '"') {
                                    buffer.append(JsonString.ESCAPE_CHAR);
                                    buffer.append('"');
                                } else {
                                    if (ch == JsonString.ESCAPE_CHAR) {
                                        buffer.append(JsonString.ESCAPE_CHAR);
                                        buffer.append(JsonString.ESCAPE_CHAR);
                                    } else {
                                        if (ch == '\n') {
                                            buffer.append(JsonString.ESCAPE_CHAR);
                                            buffer.append('n');
                                        } else {
                                            if (ch == '\r') {
                                                buffer.append(JsonString.ESCAPE_CHAR);
                                                buffer.append('r');
                                            } else {
                                                if (ch == '\t') {
                                                    buffer.append(JsonString.ESCAPE_CHAR);
                                                    buffer.append('t');
                                                } else {
                                                    if (ch == '\u2028') {
                                                        buffer.append(JsonString.ESCAPE_CHAR);
                                                        buffer.append('u');
                                                        buffer.append('2');
                                                        buffer.append('0');
                                                        buffer.append('2');
                                                        buffer.append('8');
                                                    } else {
                                                        if (ch == '\u2029') {
                                                            buffer.append(JsonString.ESCAPE_CHAR);
                                                            buffer.append('u');
                                                            buffer.append('2');
                                                            buffer.append('0');
                                                            buffer.append('2');
                                                            buffer.append('9');
                                                        } else {
                                                            buffer.append(ch);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                i = i + 1;
                            }
                        }

                        public static encode(p_chain: string): string {
                            var sb: java.lang.StringBuilder = new java.lang.StringBuilder();
                            org.kevoree.modeling.format.json.JsonString.encodeBuffer(sb, p_chain);
                            return sb.toString();
                        }

                        public static unescape(p_src: string): string {
                            if (p_src == null) {
                                return null;
                            }
                            if (p_src.length == 0) {
                                return p_src;
                            }
                            var builder: java.lang.StringBuilder = null;
                            var i: number = 0;
                            while (i < p_src.length){
                                var current: string = p_src.charAt(i);
                                if (current == JsonString.ESCAPE_CHAR) {
                                    if (builder == null) {
                                        builder = new java.lang.StringBuilder();
                                        builder.append(p_src.substring(0, i));
                                    }
                                    i++;
                                    var current2: string = p_src.charAt(i);
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
                                return p_src;
                            }
                        }

                    }

                }
                export module xmi {
                    export class SerializationContext {

                        public ignoreGeneratedID: boolean = false;
                        public model: org.kevoree.modeling.KObject;
                        public finishCallback: (p : string) => void;
                        public printer: java.lang.StringBuilder;
                        public attributesVisitor: (p : org.kevoree.modeling.meta.KMetaAttribute, p1 : any) => void;
                        public addressTable: org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any> = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                        public elementsCount: org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any> = new org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                        public packageList: java.util.ArrayList<string> = new java.util.ArrayList<string>();
                    }

                    export class XMILoadingContext {

                        public xmiReader: org.kevoree.modeling.format.xmi.XmlParser;
                        public loadedRoots: org.kevoree.modeling.KObject = null;
                        public resolvers: java.util.ArrayList<org.kevoree.modeling.format.xmi.XMIResolveCommand> = new java.util.ArrayList<org.kevoree.modeling.format.xmi.XMIResolveCommand>();
                        public map: org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any> = new org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                        public elementsCount: org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any> = new org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                        public successCallback: (p : java.lang.Throwable) => void;
                    }

                    export class XMIModelLoader {

                        public static LOADER_XMI_LOCAL_NAME: string = "type";
                        public static LOADER_XMI_XSI: string = "xsi";
                        public static LOADER_XMI_NS_URI: string = "nsURI";
                        public static unescapeXml(src: string): string {
                            var builder: java.lang.StringBuilder = null;
                            var i: number = 0;
                            while (i < src.length){
                                var c: string = src.charAt(i);
                                if (c == '&') {
                                    if (builder == null) {
                                        builder = new java.lang.StringBuilder();
                                        builder.append(src.substring(0, i));
                                    }
                                    if (src.charAt(i + 1) == 'a') {
                                        if (src.charAt(i + 2) == 'm') {
                                            builder.append("&");
                                            i = i + 5;
                                        } else {
                                            if (src.charAt(i + 2) == 'p') {
                                                builder.append("'");
                                                i = i + 6;
                                            }
                                        }
                                    } else {
                                        if (src.charAt(i + 1) == 'q') {
                                            builder.append("\"");
                                            i = i + 6;
                                        } else {
                                            if (src.charAt(i + 1) == 'l') {
                                                builder.append("<");
                                                i = i + 4;
                                            } else {
                                                if (src.charAt(i + 1) == 'g') {
                                                    builder.append(">");
                                                    i = i + 4;
                                                }
                                            }
                                        }
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

                        public static load(manager: org.kevoree.modeling.memory.manager.KMemoryManager, universe: number, time: number, str: string, callback: (p : java.lang.Throwable) => void): void {
                            var parser: org.kevoree.modeling.format.xmi.XmlParser = new org.kevoree.modeling.format.xmi.XmlParser(str);
                            if (!parser.hasNext()) {
                                callback(null);
                            } else {
                                var context: org.kevoree.modeling.format.xmi.XMILoadingContext = new org.kevoree.modeling.format.xmi.XMILoadingContext();
                                context.successCallback = callback;
                                context.xmiReader = parser;
                                org.kevoree.modeling.format.xmi.XMIModelLoader.deserialize(manager, universe, time, context);
                            }
                        }

                        private static deserialize(manager: org.kevoree.modeling.memory.manager.KMemoryManager, universe: number, time: number, context: org.kevoree.modeling.format.xmi.XMILoadingContext): void {
                            try {
                                var nsURI: string;
                                var reader: org.kevoree.modeling.format.xmi.XmlParser = context.xmiReader;
                                while (reader.hasNext()){
                                    var nextTag: org.kevoree.modeling.format.xmi.XmlToken = reader.next();
                                    if (nextTag.equals(org.kevoree.modeling.format.xmi.XmlToken.START_TAG)) {
                                        var localName: string = reader.getLocalName();
                                        if (localName != null) {
                                            var ns: org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any> = new org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>(reader.getAttributeCount(), org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                            for (var i: number = 0; i < reader.getAttributeCount() - 1; i++) {
                                                var attrLocalName: string = reader.getAttributeLocalName(i);
                                                var attrLocalValue: string = reader.getAttributeValue(i);
                                                if (attrLocalName.equals(XMIModelLoader.LOADER_XMI_NS_URI)) {
                                                    nsURI = attrLocalValue;
                                                }
                                                ns.put(attrLocalName, attrLocalValue);
                                            }
                                            var xsiType: string = reader.getTagPrefix();
                                            var realTypeName: string = ns.get(xsiType);
                                            if (realTypeName == null) {
                                                realTypeName = xsiType;
                                            }
                                            context.loadedRoots = org.kevoree.modeling.format.xmi.XMIModelLoader.loadObject(manager, universe, time, context, "/", xsiType + "." + localName);
                                        }
                                    }
                                }
                                for (var i: number = 0; i < context.resolvers.size(); i++) {
                                    context.resolvers.get(i).run();
                                }
                                manager.setRoot(context.loadedRoots, null);
                                context.successCallback(null);
                            } catch ($ex$) {
                                if ($ex$ instanceof java.lang.Exception) {
                                    var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                    context.successCallback(e);
                                } else {
                                    throw $ex$;
                                }
                            }
                        }

                        private static callFactory(manager: org.kevoree.modeling.memory.manager.KMemoryManager, universe: number, time: number, ctx: org.kevoree.modeling.format.xmi.XMILoadingContext, objectType: string): org.kevoree.modeling.KObject {
                            var modelElem: org.kevoree.modeling.KObject = null;
                            if (objectType != null) {
                                modelElem = manager.model().createByName(objectType, universe, time);
                                if (modelElem == null) {
                                    var xsiType: string = null;
                                    for (var i: number = 0; i < (ctx.xmiReader.getAttributeCount() - 1); i++) {
                                        var localName: string = ctx.xmiReader.getAttributeLocalName(i);
                                        var xsi: string = ctx.xmiReader.getAttributePrefix(i);
                                        if (localName.equals(XMIModelLoader.LOADER_XMI_LOCAL_NAME) && xsi.equals(XMIModelLoader.LOADER_XMI_XSI)) {
                                            xsiType = ctx.xmiReader.getAttributeValue(i);
                                            break;
                                        }
                                    }
                                    if (xsiType != null) {
                                        var realTypeName: string = xsiType.substring(0, xsiType.lastIndexOf(":"));
                                        var realName: string = xsiType.substring(xsiType.lastIndexOf(":") + 1, xsiType.length);
                                        modelElem = manager.model().createByName(realTypeName + "." + realName, universe, time);
                                    }
                                }
                            } else {
                                modelElem = manager.model().createByName(ctx.xmiReader.getLocalName(), universe, time);
                            }
                            return modelElem;
                        }

                        private static loadObject(manager: org.kevoree.modeling.memory.manager.KMemoryManager, universe: number, time: number, ctx: org.kevoree.modeling.format.xmi.XMILoadingContext, xmiAddress: string, objectType: string): org.kevoree.modeling.KObject {
                            var elementTagName: string = ctx.xmiReader.getLocalName();
                            var modelElem: org.kevoree.modeling.KObject = org.kevoree.modeling.format.xmi.XMIModelLoader.callFactory(manager, universe, time, ctx, objectType);
                            if (modelElem == null) {
                                throw new java.lang.Exception("Could not create an object for local name " + elementTagName);
                            }
                            ctx.map.put(xmiAddress, modelElem);
                            for (var i: number = 0; i < ctx.xmiReader.getAttributeCount(); i++) {
                                var prefix: string = ctx.xmiReader.getAttributePrefix(i);
                                if (prefix == null || prefix.equals("")) {
                                    var attrName: string = ctx.xmiReader.getAttributeLocalName(i).trim();
                                    var valueAtt: string = ctx.xmiReader.getAttributeValue(i).trim();
                                    if (valueAtt != null) {
                                        var metaElement: org.kevoree.modeling.meta.KMeta = modelElem.metaClass().metaByName(attrName);
                                        if (metaElement != null && metaElement.metaType().equals(org.kevoree.modeling.meta.MetaType.ATTRIBUTE)) {
                                            modelElem.set(<org.kevoree.modeling.meta.KMetaAttribute>metaElement, org.kevoree.modeling.format.xmi.XMIModelLoader.unescapeXml(valueAtt));
                                        } else {
                                            if (metaElement != null && metaElement instanceof org.kevoree.modeling.meta.impl.MetaReference) {
                                                var referenceArray: string[] = valueAtt.split(" ");
                                                for (var j: number = 0; j < referenceArray.length; j++) {
                                                    var xmiRef: string = referenceArray[j];
                                                    var adjustedRef: string = (xmiRef.startsWith("#") ? xmiRef.substring(1) : xmiRef);
                                                    adjustedRef = adjustedRef.replace(".0", "");
                                                    var ref: org.kevoree.modeling.KObject = ctx.map.get(adjustedRef);
                                                    if (ref != null) {
                                                        modelElem.mutate(org.kevoree.modeling.KActionType.ADD, <org.kevoree.modeling.meta.KMetaReference>metaElement, ref);
                                                    } else {
                                                        ctx.resolvers.add(new org.kevoree.modeling.format.xmi.XMIResolveCommand(ctx, modelElem, org.kevoree.modeling.KActionType.ADD, attrName, adjustedRef));
                                                    }
                                                }
                                            } else {
                                            }
                                        }
                                    }
                                }
                            }
                            var done: boolean = false;
                            while (!done){
                                if (ctx.xmiReader.hasNext()) {
                                    var tok: org.kevoree.modeling.format.xmi.XmlToken = ctx.xmiReader.next();
                                    if (tok.equals(org.kevoree.modeling.format.xmi.XmlToken.START_TAG)) {
                                        var subElemName: string = ctx.xmiReader.getLocalName();
                                        var key: string = xmiAddress + "/@" + subElemName;
                                        var i: number = ctx.elementsCount.get(key);
                                        if (i == null) {
                                            i = 0;
                                            ctx.elementsCount.put(key, i);
                                        }
                                        var subElementId: string = xmiAddress + "/@" + subElemName + (i != 0 ? "." + i : "");
                                        var containedElement: org.kevoree.modeling.KObject = org.kevoree.modeling.format.xmi.XMIModelLoader.loadObject(manager, universe, time, ctx, subElementId, subElemName);
                                        modelElem.mutate(org.kevoree.modeling.KActionType.ADD, <org.kevoree.modeling.meta.KMetaReference>modelElem.metaClass().metaByName(subElemName), containedElement);
                                        ctx.elementsCount.put(xmiAddress + "/@" + subElemName, i + 1);
                                    } else {
                                        if (tok.equals(org.kevoree.modeling.format.xmi.XmlToken.END_TAG)) {
                                            if (ctx.xmiReader.getLocalName().equals(elementTagName)) {
                                                done = true;
                                            }
                                        }
                                    }
                                } else {
                                    done = true;
                                }
                            }
                            return modelElem;
                        }

                    }

                    export class XMIModelSerializer {

                        public static save(model: org.kevoree.modeling.KObject, callback: (p : string) => void): void {
                            callback(null);
                        }

                    }

                    export class XMIResolveCommand {

                        private context: org.kevoree.modeling.format.xmi.XMILoadingContext;
                        private target: org.kevoree.modeling.KObject;
                        private mutatorType: org.kevoree.modeling.KActionType;
                        private refName: string;
                        private ref: string;
                        constructor(context: org.kevoree.modeling.format.xmi.XMILoadingContext, target: org.kevoree.modeling.KObject, mutatorType: org.kevoree.modeling.KActionType, refName: string, ref: string) {
                            this.context = context;
                            this.target = target;
                            this.mutatorType = mutatorType;
                            this.refName = refName;
                            this.ref = ref;
                        }

                        public run(): void {
                            var referencedElement: org.kevoree.modeling.KObject = this.context.map.get(this.ref);
                            if (referencedElement != null) {
                                this.target.mutate(this.mutatorType, <org.kevoree.modeling.meta.KMetaReference>this.target.metaClass().metaByName(this.refName), referencedElement);
                                return;
                            }
                            referencedElement = this.context.map.get("/");
                            if (referencedElement != null) {
                                this.target.mutate(this.mutatorType, <org.kevoree.modeling.meta.KMetaReference>this.target.metaClass().metaByName(this.refName), referencedElement);
                                return;
                            }
                            throw new java.lang.Exception("KMF Load error : reference " + this.ref + " not found in map when trying to  " + this.mutatorType + " " + this.refName + "  on " + this.target.metaClass().metaName() + "(uuid:" + this.target.uuid() + ")");
                        }

                    }

                    export class XmiFormat implements org.kevoree.modeling.format.KModelFormat {

                        private _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                        private _universe: number;
                        private _time: number;
                        constructor(p_universe: number, p_time: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
                            this._universe = p_universe;
                            this._time = p_time;
                            this._manager = p_manager;
                        }

                        public save(model: org.kevoree.modeling.KObject, cb: (p : string) => void): void {
                            org.kevoree.modeling.format.xmi.XMIModelSerializer.save(model, cb);
                        }

                        public saveRoot(cb: (p : string) => void): void {
                            this._manager.getRoot(this._universe, this._time,  (root : org.kevoree.modeling.KObject) => {
                                if (root == null) {
                                    if (cb != null) {
                                        cb(null);
                                    }
                                } else {
                                    org.kevoree.modeling.format.xmi.XMIModelSerializer.save(root, cb);
                                }
                            });
                        }

                        public load(payload: string, cb: (p : any) => void): void {
                            org.kevoree.modeling.format.xmi.XMIModelLoader.load(this._manager, this._universe, this._time, payload, cb);
                        }

                    }

                    export class XmlParser {

                        private payload: string;
                        private current: number = 0;
                        private currentChar: string;
                        private tagName: string;
                        private tagPrefix: string;
                        private attributePrefix: string;
                        private readSingleton: boolean = false;
                        private attributesNames: java.util.ArrayList<string> = new java.util.ArrayList<string>();
                        private attributesPrefixes: java.util.ArrayList<string> = new java.util.ArrayList<string>();
                        private attributesValues: java.util.ArrayList<string> = new java.util.ArrayList<string>();
                        private attributeName: java.lang.StringBuilder = new java.lang.StringBuilder();
                        private attributeValue: java.lang.StringBuilder = new java.lang.StringBuilder();
                        constructor(str: string) {
                            this.payload = str;
                            this.currentChar = this.readChar();
                        }

                        public getTagPrefix(): string {
                            return this.tagPrefix;
                        }

                        public hasNext(): boolean {
                            this.read_lessThan();
                            return this.current < this.payload.length;
                        }

                        public getLocalName(): string {
                            return this.tagName;
                        }

                        public getAttributeCount(): number {
                            return this.attributesNames.size();
                        }

                        public getAttributeLocalName(i: number): string {
                            return this.attributesNames.get(i);
                        }

                        public getAttributePrefix(i: number): string {
                            return this.attributesPrefixes.get(i);
                        }

                        public getAttributeValue(i: number): string {
                            return this.attributesValues.get(i);
                        }

                        private readChar(): string {
                            if (this.current < this.payload.length) {
                                var re: string = this.payload.charAt(this.current);
                                this.current++;
                                return re;
                            }
                            return '\0';
                        }

                        public next(): org.kevoree.modeling.format.xmi.XmlToken {
                            if (this.readSingleton) {
                                this.readSingleton = false;
                                return org.kevoree.modeling.format.xmi.XmlToken.END_TAG;
                            }
                            if (!this.hasNext()) {
                                return org.kevoree.modeling.format.xmi.XmlToken.END_DOCUMENT;
                            }
                            this.attributesNames.clear();
                            this.attributesPrefixes.clear();
                            this.attributesValues.clear();
                            this.read_lessThan();
                            this.currentChar = this.readChar();
                            if (this.currentChar == '?') {
                                this.currentChar = this.readChar();
                                this.read_xmlHeader();
                                return org.kevoree.modeling.format.xmi.XmlToken.XML_HEADER;
                            } else {
                                if (this.currentChar == '!') {
                                    do {
                                        this.currentChar = this.readChar();
                                    } while (this.currentChar != '>')
                                    return org.kevoree.modeling.format.xmi.XmlToken.COMMENT;
                                } else {
                                    if (this.currentChar == '/') {
                                        this.currentChar = this.readChar();
                                        this.read_closingTag();
                                        return org.kevoree.modeling.format.xmi.XmlToken.END_TAG;
                                    } else {
                                        this.read_openTag();
                                        if (this.currentChar == '/') {
                                            this.read_upperThan();
                                            this.readSingleton = true;
                                        }
                                        return org.kevoree.modeling.format.xmi.XmlToken.START_TAG;
                                    }
                                }
                            }
                        }

                        private read_lessThan(): void {
                            while (this.currentChar != '<' && this.currentChar != '\0'){
                                this.currentChar = this.readChar();
                            }
                        }

                        private read_upperThan(): void {
                            while (this.currentChar != '>'){
                                this.currentChar = this.readChar();
                            }
                        }

                        private read_xmlHeader(): void {
                            this.read_tagName();
                            this.read_attributes();
                            this.read_upperThan();
                        }

                        private read_closingTag(): void {
                            this.read_tagName();
                            this.read_upperThan();
                        }

                        private read_openTag(): void {
                            this.read_tagName();
                            if (this.currentChar != '>' && this.currentChar != '/') {
                                this.read_attributes();
                            }
                        }

                        private read_tagName(): void {
                            this.tagName = "" + this.currentChar;
                            this.tagPrefix = null;
                            this.currentChar = this.readChar();
                            while (this.currentChar != ' ' && this.currentChar != '>' && this.currentChar != '/'){
                                if (this.currentChar == ':') {
                                    this.tagPrefix = this.tagName;
                                    this.tagName = "";
                                } else {
                                    this.tagName += this.currentChar;
                                }
                                this.currentChar = this.readChar();
                            }
                        }

                        private read_attributes(): void {
                            var end_of_tag: boolean = false;
                            while (this.currentChar == ' '){
                                this.currentChar = this.readChar();
                            }
                            while (!end_of_tag){
                                while (this.currentChar != '='){
                                    if (this.currentChar == ':') {
                                        this.attributePrefix = this.attributeName.toString();
                                        this.attributeName = new java.lang.StringBuilder();
                                    } else {
                                        this.attributeName.append(this.currentChar);
                                    }
                                    this.currentChar = this.readChar();
                                }
                                do {
                                    this.currentChar = this.readChar();
                                } while (this.currentChar != '"')
                                this.currentChar = this.readChar();
                                while (this.currentChar != '"'){
                                    this.attributeValue.append(this.currentChar);
                                    this.currentChar = this.readChar();
                                }
                                this.attributesNames.add(this.attributeName.toString());
                                this.attributesPrefixes.add(this.attributePrefix);
                                this.attributesValues.add(this.attributeValue.toString());
                                this.attributeName = new java.lang.StringBuilder();
                                this.attributePrefix = null;
                                this.attributeValue = new java.lang.StringBuilder();
                                do {
                                    this.currentChar = this.readChar();
                                    if (this.currentChar == '?' || this.currentChar == '/' || this.currentChar == '-' || this.currentChar == '>') {
                                        end_of_tag = true;
                                    }
                                } while (!end_of_tag && this.currentChar == ' ')
                            }
                        }

                    }

                    export class XmlToken {

                        public static XML_HEADER: XmlToken = new XmlToken();
                        public static END_DOCUMENT: XmlToken = new XmlToken();
                        public static START_TAG: XmlToken = new XmlToken();
                        public static END_TAG: XmlToken = new XmlToken();
                        public static COMMENT: XmlToken = new XmlToken();
                        public static SINGLETON_TAG: XmlToken = new XmlToken();
                        public equals(other: any): boolean {
                            return this == other;
                        }
                        public static _XmlTokenVALUES : XmlToken[] = [
                            XmlToken.XML_HEADER
                            ,XmlToken.END_DOCUMENT
                            ,XmlToken.START_TAG
                            ,XmlToken.END_TAG
                            ,XmlToken.COMMENT
                            ,XmlToken.SINGLETON_TAG
                        ];
                        public static values():XmlToken[]{
                            return XmlToken._XmlTokenVALUES;
                        }
                    }

                }
            }
            export module infer {
                export class AnalyticKInfer {

                }

                export class GaussianClassificationKInfer {

                    private alpha: number = 0.05;
                    public getAlpha(): number {
                        return this.alpha;
                    }

                    public setAlpha(alpha: number): void {
                        this.alpha = alpha;
                    }

                }

                export interface KInfer extends org.kevoree.modeling.KObject {

                    train(trainingSet: any[][], expectedResultSet: any[], callback: (p : java.lang.Throwable) => void): void;

                    infer(features: any[]): any;

                    accuracy(testSet: any[][], expectedResultSet: any[]): any;

                    clear(): void;

                }

                export class KInferState {

                    public save(): string {
                        throw "Abstract method";
                    }

                    public load(payload: string): void {
                        throw "Abstract method";
                    }

                    public isDirty(): boolean {
                        throw "Abstract method";
                    }

                    public cloneState(): org.kevoree.modeling.infer.KInferState {
                        throw "Abstract method";
                    }

                }

                export class LinearRegressionKInfer {

                    private alpha: number = 0.0001;
                    private iterations: number = 100;
                    public getAlpha(): number {
                        return this.alpha;
                    }

                    public setAlpha(alpha: number): void {
                        this.alpha = alpha;
                    }

                    public getIterations(): number {
                        return this.iterations;
                    }

                    public setIterations(iterations: number): void {
                        this.iterations = iterations;
                    }

                    private calculate(weights: number[], features: number[]): number {
                        var result: number = 0;
                        for (var i: number = 0; i < features.length; i++) {
                            result += weights[i] * features[i];
                        }
                        result += weights[features.length];
                        return result;
                    }

                }

                export class PerceptronClassificationKInfer {

                    private alpha: number = 0.001;
                    private iterations: number = 100;
                    public getAlpha(): number {
                        return this.alpha;
                    }

                    public setAlpha(alpha: number): void {
                        this.alpha = alpha;
                    }

                    public getIterations(): number {
                        return this.iterations;
                    }

                    public setIterations(iterations: number): void {
                        this.iterations = iterations;
                    }

                    private calculate(weights: number[], features: number[]): number {
                        var res: number = 0;
                        for (var i: number = 0; i < features.length; i++) {
                            res = res + weights[i] * (features[i]);
                        }
                        res = res + weights[features.length];
                        if (res >= 0) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }

                }

                export class PolynomialOfflineKInfer {

                    public maxDegree: number = 20;
                    public toleratedErr: number = 0.01;
                    public getToleratedErr(): number {
                        return this.toleratedErr;
                    }

                    public setToleratedErr(toleratedErr: number): void {
                        this.toleratedErr = toleratedErr;
                    }

                    public getMaxDegree(): number {
                        return this.maxDegree;
                    }

                    public setMaxDegree(maxDegree: number): void {
                        this.maxDegree = maxDegree;
                    }

                }

                export class PolynomialOnlineKInfer {

                    public maxDegree: number = 20;
                    public toleratedErr: number = 0.01;
                    public getToleratedErr(): number {
                        return this.toleratedErr;
                    }

                    public setToleratedErr(toleratedErr: number): void {
                        this.toleratedErr = toleratedErr;
                    }

                    public getMaxDegree(): number {
                        return this.maxDegree;
                    }

                    public setMaxDegree(maxDegree: number): void {
                        this.maxDegree = maxDegree;
                    }

                    private calculateLong(time: number, weights: number[], timeOrigin: number, unit: number): number {
                        var t: number = (<number>(time - timeOrigin)) / unit;
                        return this.calculate(weights, t);
                    }

                    private calculate(weights: number[], t: number): number {
                        var result: number = 0;
                        var power: number = 1;
                        for (var j: number = 0; j < weights.length; j++) {
                            result += weights[j] * power;
                            power = power * t;
                        }
                        return result;
                    }

                }

                export class WinnowClassificationKInfer {

                    private alpha: number = 2;
                    private beta: number = 2;
                    public getAlpha(): number {
                        return this.alpha;
                    }

                    public setAlpha(alpha: number): void {
                        this.alpha = alpha;
                    }

                    public getBeta(): number {
                        return this.beta;
                    }

                    public setBeta(beta: number): void {
                        this.beta = beta;
                    }

                    private calculate(weights: number[], features: number[]): number {
                        var result: number = 0;
                        for (var i: number = 0; i < features.length; i++) {
                            result += weights[i] * features[i];
                        }
                        if (result >= features.length) {
                            return 1.0;
                        } else {
                            return 0.0;
                        }
                    }

                }

                export module states {
                    export class AnalyticKInferState extends org.kevoree.modeling.infer.KInferState {

                        private _isDirty: boolean = false;
                        private sumSquares: number = 0;
                        private sum: number = 0;
                        private nb: number = 0;
                        private min: number;
                        private max: number;
                        public getSumSquares(): number {
                            return this.sumSquares;
                        }

                        public setSumSquares(sumSquares: number): void {
                            this.sumSquares = sumSquares;
                        }

                        public getMin(): number {
                            return this.min;
                        }

                        public setMin(min: number): void {
                            this._isDirty = true;
                            this.min = min;
                        }

                        public getMax(): number {
                            return this.max;
                        }

                        public setMax(max: number): void {
                            this._isDirty = true;
                            this.max = max;
                        }

                        public getNb(): number {
                            return this.nb;
                        }

                        public setNb(nb: number): void {
                            this._isDirty = true;
                            this.nb = nb;
                        }

                        public getSum(): number {
                            return this.sum;
                        }

                        public setSum(sum: number): void {
                            this._isDirty = true;
                            this.sum = sum;
                        }

                        public getAverage(): number {
                            if (this.nb != 0) {
                                return this.sum / this.nb;
                            } else {
                                return null;
                            }
                        }

                        public train(value: number): void {
                            if (this.nb == 0) {
                                this.max = value;
                                this.min = value;
                            } else {
                                if (value < this.min) {
                                    this.min = value;
                                }
                                if (value > this.max) {
                                    this.max = value;
                                }
                            }
                            this.sum += value;
                            this.sumSquares += value * value;
                            this.nb++;
                            this._isDirty = true;
                        }

                        public getVariance(): number {
                            if (this.nb != 0) {
                                var avg: number = this.sum / this.nb;
                                var newvar: number = this.sumSquares / this.nb - avg * avg;
                                return newvar;
                            } else {
                                return null;
                            }
                        }

                        public clear(): void {
                            this.nb = 0;
                            this.sum = 0;
                            this.sumSquares = 0;
                            this._isDirty = true;
                        }

                        public save(): string {
                            return this.sum + "/" + this.nb + "/" + this.min + "/" + this.max + "/" + this.sumSquares;
                        }

                        public load(payload: string): void {
                            try {
                                var previousState: string[] = payload.split("/");
                                this.sum = java.lang.Double.parseDouble(previousState[0]);
                                this.nb = java.lang.Integer.parseInt(previousState[1]);
                                this.min = java.lang.Double.parseDouble(previousState[2]);
                                this.max = java.lang.Double.parseDouble(previousState[3]);
                                this.sumSquares = java.lang.Double.parseDouble(previousState[4]);
                            } catch ($ex$) {
                                if ($ex$ instanceof java.lang.Exception) {
                                    var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                    this.sum = 0;
                                    this.nb = 0;
                                } else {
                                    throw $ex$;
                                }
                            }
                            this._isDirty = false;
                        }

                        public isDirty(): boolean {
                            return this._isDirty;
                        }

                        public cloneState(): org.kevoree.modeling.infer.KInferState {
                            var cloned: org.kevoree.modeling.infer.states.AnalyticKInferState = new org.kevoree.modeling.infer.states.AnalyticKInferState();
                            cloned.setSumSquares(this.getSumSquares());
                            cloned.setNb(this.getNb());
                            cloned.setSum(this.getSum());
                            cloned.setMax(this.getMax());
                            cloned.setMin(this.getMin());
                            return cloned;
                        }

                    }

                    export class BayesianClassificationState extends org.kevoree.modeling.infer.KInferState {

                        private states: org.kevoree.modeling.infer.states.Bayesian.BayesianSubstate[][];
                        private classStats: org.kevoree.modeling.infer.states.Bayesian.EnumSubstate;
                        private numOfFeatures: number;
                        private numOfClasses: number;
                        private static stateSep: string = "/";
                        private static interStateSep: string = "|";
                        public initialize(metaFeatures: any[], MetaClassification: any): void {
                            this.numOfFeatures = metaFeatures.length;
                            this.numOfClasses = 0;
                            this.states = new Array(new Array());
                            this.classStats = new org.kevoree.modeling.infer.states.Bayesian.EnumSubstate();
                            this.classStats.initialize(this.numOfClasses);
                            for (var i: number = 0; i < this.numOfFeatures; i++) {
                            }
                        }

                        public predict(features: any[]): number {
                            var temp: number;
                            var prediction: number = -1;
                            var max: number = 0;
                            for (var i: number = 0; i < this.numOfClasses; i++) {
                                temp = this.classStats.calculateProbability(i);
                                for (var j: number = 0; j < this.numOfFeatures; j++) {
                                    temp = temp * this.states[i][j].calculateProbability(features[j]);
                                }
                                if (temp >= max) {
                                    max = temp;
                                    prediction = i;
                                }
                            }
                            return prediction;
                        }

                        public train(features: any[], classNum: number): void {
                            for (var i: number = 0; i < this.numOfFeatures; i++) {
                                this.states[classNum][i].train(features[i]);
                                this.states[this.numOfClasses][i].train(features[i]);
                            }
                            this.classStats.train(classNum);
                        }

                        public save(): string {
                            var sb: java.lang.StringBuilder = new java.lang.StringBuilder();
                            sb.append(this.numOfClasses + BayesianClassificationState.interStateSep);
                            sb.append(this.numOfFeatures + BayesianClassificationState.interStateSep);
                            for (var i: number = 0; i < this.numOfClasses + 1; i++) {
                                for (var j: number = 0; j < this.numOfFeatures; j++) {
                                    sb.append(this.states[i][j].save(BayesianClassificationState.stateSep));
                                    sb.append(BayesianClassificationState.interStateSep);
                                }
                            }
                            sb.append(this.classStats.save(BayesianClassificationState.stateSep));
                            return sb.toString();
                        }

                        public load(payload: string): void {
                            var st: string[] = payload.split(BayesianClassificationState.interStateSep);
                            this.numOfClasses = java.lang.Integer.parseInt(st[0]);
                            this.numOfFeatures = java.lang.Integer.parseInt(st[1]);
                            this.states = new Array(new Array());
                            var counter: number = 2;
                            for (var i: number = 0; i < this.numOfClasses + 1; i++) {
                                for (var j: number = 0; j < this.numOfFeatures; j++) {
                                    var s: string = st[counter].split(BayesianClassificationState.stateSep)[0];
                                    if (s.equals("EnumSubstate")) {
                                        this.states[i][j] = new org.kevoree.modeling.infer.states.Bayesian.EnumSubstate();
                                    } else {
                                        if (s.equals("GaussianSubState")) {
                                            this.states[i][j] = new org.kevoree.modeling.infer.states.Bayesian.GaussianSubState();
                                        }
                                    }
                                    s = st[counter].substring(s.length + 1);
                                    this.states[i][j].load(s, BayesianClassificationState.stateSep);
                                    counter++;
                                }
                            }
                            var s: string = st[counter].split(BayesianClassificationState.stateSep)[0];
                            s = st[counter].substring(s.length + 1);
                            this.classStats = new org.kevoree.modeling.infer.states.Bayesian.EnumSubstate();
                            this.classStats.load(s, BayesianClassificationState.stateSep);
                        }

                        public isDirty(): boolean {
                            return false;
                        }

                        public cloneState(): org.kevoree.modeling.infer.KInferState {
                            return null;
                        }

                    }

                    export class DoubleArrayKInferState extends org.kevoree.modeling.infer.KInferState {

                        private _isDirty: boolean = false;
                        private weights: number[];
                        public save(): string {
                            var s: string = "";
                            var sb: java.lang.StringBuilder = new java.lang.StringBuilder();
                            if (this.weights != null) {
                                for (var i: number = 0; i < this.weights.length; i++) {
                                    sb.append(this.weights[i] + "/");
                                }
                                s = sb.toString();
                            }
                            return s;
                        }

                        public load(payload: string): void {
                            try {
                                var previousState: string[] = payload.split("/");
                                if (previousState.length > 0) {
                                    this.weights = new Array();
                                    for (var i: number = 0; i < previousState.length; i++) {
                                        this.weights[i] = java.lang.Double.parseDouble(previousState[i]);
                                    }
                                }
                            } catch ($ex$) {
                                if ($ex$ instanceof java.lang.Exception) {
                                    var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                } else {
                                    throw $ex$;
                                }
                            }
                            this._isDirty = false;
                        }

                        public isDirty(): boolean {
                            return this._isDirty;
                        }

                        public set_isDirty(value: boolean): void {
                            this._isDirty = value;
                        }

                        public cloneState(): org.kevoree.modeling.infer.KInferState {
                            var cloned: org.kevoree.modeling.infer.states.DoubleArrayKInferState = new org.kevoree.modeling.infer.states.DoubleArrayKInferState();
                            var clonearray: number[] = new Array();
                            for (var i: number = 0; i < this.weights.length; i++) {
                                clonearray[i] = this.weights[i];
                            }
                            cloned.setWeights(clonearray);
                            return cloned;
                        }

                        public getWeights(): number[] {
                            return this.weights;
                        }

                        public setWeights(weights: number[]): void {
                            this.weights = weights;
                            this._isDirty = true;
                        }

                    }

                    export class GaussianArrayKInferState extends org.kevoree.modeling.infer.KInferState {

                        private _isDirty: boolean = false;
                        private sumSquares: number[] = null;
                        private sum: number[] = null;
                        private epsilon: number = 0;
                        private nb: number = 0;
                        public getSumSquares(): number[] {
                            return this.sumSquares;
                        }

                        public setSumSquares(sumSquares: number[]): void {
                            this.sumSquares = sumSquares;
                        }

                        public getNb(): number {
                            return this.nb;
                        }

                        public setNb(nb: number): void {
                            this._isDirty = true;
                            this.nb = nb;
                        }

                        public getSum(): number[] {
                            return this.sum;
                        }

                        public setSum(sum: number[]): void {
                            this._isDirty = true;
                            this.sum = sum;
                        }

                        public calculateProbability(features: number[]): number {
                            var size: number = this.sum.length;
                            var avg: number[] = new Array();
                            var variances: number[] = new Array();
                            var p: number = 1;
                            for (var i: number = 0; i < size; i++) {
                                avg[i] = this.sum[i] / this.nb;
                                variances[i] = this.sumSquares[i] / this.nb - avg[i] * avg[i];
                                p = p * (1 / Math.sqrt(2 * Math.PI * variances[i])) * Math.exp(-((features[i] - avg[i]) * (features[i] - avg[i])) / (2 * variances[i]));
                            }
                            return p;
                        }

                        public infer(features: number[]): boolean {
                            return (this.calculateProbability(features) <= this.epsilon);
                        }

                        public getAverage(): number[] {
                            if (this.nb != 0) {
                                var size: number = this.sum.length;
                                var avg: number[] = new Array();
                                for (var i: number = 0; i < size; i++) {
                                    avg[i] = this.sum[i] / this.nb;
                                }
                                return avg;
                            } else {
                                return null;
                            }
                        }

                        public train(features: number[], result: boolean, alpha: number): void {
                            var size: number = features.length;
                            if (this.nb == 0) {
                                this.sumSquares = new Array();
                                this.sum = new Array();
                            }
                            for (var i: number = 0; i < size; i++) {
                                this.sum[i] += features[i];
                                this.sumSquares[i] += features[i] * features[i];
                            }
                            this.nb++;
                            var proba: number = this.calculateProbability(features);
                            var diff: number = proba - this.epsilon;
                            if ((proba < this.epsilon && result == false) || (proba > this.epsilon && result == true)) {
                                this.epsilon = this.epsilon + alpha * diff;
                            }
                            this._isDirty = true;
                        }

                        public getVariance(): number[] {
                            if (this.nb != 0) {
                                var size: number = this.sum.length;
                                var avg: number[] = new Array();
                                var newvar: number[] = new Array();
                                for (var i: number = 0; i < size; i++) {
                                    avg[i] = this.sum[i] / this.nb;
                                    newvar[i] = this.sumSquares[i] / this.nb - avg[i] * avg[i];
                                }
                                return newvar;
                            } else {
                                return null;
                            }
                        }

                        public clear(): void {
                            this.nb = 0;
                            this.sum = null;
                            this.sumSquares = null;
                            this._isDirty = true;
                        }

                        public save(): string {
                            var sb: java.lang.StringBuilder = new java.lang.StringBuilder();
                            sb.append(this.nb + "/");
                            sb.append(this.epsilon + "/");
                            var size: number = this.sumSquares.length;
                            for (var i: number = 0; i < size; i++) {
                                sb.append(this.sum[i] + "/");
                            }
                            for (var i: number = 0; i < size; i++) {
                                sb.append(this.sumSquares[i] + "/");
                            }
                            return sb.toString();
                        }

                        public load(payload: string): void {
                            try {
                                var previousState: string[] = payload.split("/");
                                this.nb = java.lang.Integer.parseInt(previousState[0]);
                                this.epsilon = java.lang.Double.parseDouble(previousState[1]);
                                var size: number = (previousState.length - 2) / 2;
                                this.sum = new Array();
                                this.sumSquares = new Array();
                                for (var i: number = 0; i < size; i++) {
                                    this.sum[i] = java.lang.Double.parseDouble(previousState[i + 2]);
                                }
                                for (var i: number = 0; i < size; i++) {
                                    this.sumSquares[i] = java.lang.Double.parseDouble(previousState[i + 2 + size]);
                                }
                            } catch ($ex$) {
                                if ($ex$ instanceof java.lang.Exception) {
                                    var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                    this.sum = null;
                                    this.sumSquares = null;
                                    this.nb = 0;
                                } else {
                                    throw $ex$;
                                }
                            }
                            this._isDirty = false;
                        }

                        public isDirty(): boolean {
                            return this._isDirty;
                        }

                        public cloneState(): org.kevoree.modeling.infer.KInferState {
                            var cloned: org.kevoree.modeling.infer.states.GaussianArrayKInferState = new org.kevoree.modeling.infer.states.GaussianArrayKInferState();
                            cloned.setNb(this.getNb());
                            if (this.nb != 0) {
                                var newSum: number[] = new Array();
                                var newSumSquares: number[] = new Array();
                                for (var i: number = 0; i < this.sum.length; i++) {
                                    newSum[i] = this.sum[i];
                                    newSumSquares[i] = this.sumSquares[i];
                                }
                                cloned.setSum(newSum);
                                cloned.setSumSquares(newSumSquares);
                            }
                            return cloned;
                        }

                        public getEpsilon(): number {
                            return this.epsilon;
                        }

                    }

                    export class PolynomialKInferState extends org.kevoree.modeling.infer.KInferState {

                        private _isDirty: boolean = false;
                        private timeOrigin: number;
                        private unit: number;
                        private weights: number[];
                        public getTimeOrigin(): number {
                            return this.timeOrigin;
                        }

                        public setTimeOrigin(timeOrigin: number): void {
                            this.timeOrigin = timeOrigin;
                        }

                        public is_isDirty(): boolean {
                            return this._isDirty;
                        }

                        public getUnit(): number {
                            return this.unit;
                        }

                        public setUnit(unit: number): void {
                            this.unit = unit;
                        }

                        public static maxError(coef: number[], normalizedTimes: number[], results: number[]): number {
                            var maxErr: number = 0;
                            var temp: number = 0;
                            for (var i: number = 0; i < normalizedTimes.length; i++) {
                                var val: number = org.kevoree.modeling.infer.states.PolynomialKInferState.internal_extrapolate(normalizedTimes[i], coef);
                                temp = Math.abs(val - results[i]);
                                if (temp > maxErr) {
                                    maxErr = temp;
                                }
                            }
                            return maxErr;
                        }

                        private static internal_extrapolate(normalizedTime: number, coef: number[]): number {
                            var result: number = 0;
                            var power: number = 1;
                            for (var j: number = 0; j < coef.length; j++) {
                                result += coef[j] * power;
                                power = power * normalizedTime;
                            }
                            return result;
                        }

                        public save(): string {
                            var s: string = "";
                            var sb: java.lang.StringBuilder = new java.lang.StringBuilder();
                            sb.append(this.timeOrigin + "/");
                            sb.append(this.unit + "/");
                            if (this.weights != null) {
                                for (var i: number = 0; i < this.weights.length; i++) {
                                    sb.append(this.weights[i] + "/");
                                }
                                s = sb.toString();
                            }
                            return s;
                        }

                        public load(payload: string): void {
                            try {
                                var previousState: string[] = payload.split("/");
                                if (previousState.length > 0) {
                                    this.timeOrigin = java.lang.Long.parseLong(previousState[0]);
                                    this.unit = java.lang.Long.parseLong(previousState[1]);
                                    var size: number = previousState.length - 2;
                                    this.weights = new Array();
                                    for (var i: number = 0; i < size; i++) {
                                        this.weights[i] = java.lang.Double.parseDouble(previousState[i - 2]);
                                    }
                                }
                            } catch ($ex$) {
                                if ($ex$ instanceof java.lang.Exception) {
                                    var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                } else {
                                    throw $ex$;
                                }
                            }
                            this._isDirty = false;
                        }

                        public isDirty(): boolean {
                            return this._isDirty;
                        }

                        public set_isDirty(value: boolean): void {
                            this._isDirty = value;
                        }

                        public cloneState(): org.kevoree.modeling.infer.KInferState {
                            var cloned: org.kevoree.modeling.infer.states.PolynomialKInferState = new org.kevoree.modeling.infer.states.PolynomialKInferState();
                            var clonearray: number[] = new Array();
                            for (var i: number = 0; i < this.weights.length; i++) {
                                clonearray[i] = this.weights[i];
                            }
                            cloned.setWeights(clonearray);
                            cloned.setTimeOrigin(this.getTimeOrigin());
                            cloned.setUnit(this.getUnit());
                            return cloned;
                        }

                        public getWeights(): number[] {
                            return this.weights;
                        }

                        public setWeights(weights: number[]): void {
                            this.weights = weights;
                            this._isDirty = true;
                        }

                        public infer(time: number): any {
                            var t: number = (<number>(time - this.timeOrigin)) / this.unit;
                            return org.kevoree.modeling.infer.states.PolynomialKInferState.internal_extrapolate(t, this.weights);
                        }

                    }

                    export module Bayesian {
                        export class BayesianSubstate {

                            public calculateProbability(feature: any): number {
                                throw "Abstract method";
                            }

                            public train(feature: any): void {
                                throw "Abstract method";
                            }

                            public save(separator: string): string {
                                throw "Abstract method";
                            }

                            public load(payload: string, separator: string): void {
                                throw "Abstract method";
                            }

                            public cloneState(): org.kevoree.modeling.infer.states.Bayesian.BayesianSubstate {
                                throw "Abstract method";
                            }

                        }

                        export class EnumSubstate extends org.kevoree.modeling.infer.states.Bayesian.BayesianSubstate {

                            private counter: number[];
                            private total: number = 0;
                            public getCounter(): number[] {
                                return this.counter;
                            }

                            public setCounter(counter: number[]): void {
                                this.counter = counter;
                            }

                            public getTotal(): number {
                                return this.total;
                            }

                            public setTotal(total: number): void {
                                this.total = total;
                            }

                            public initialize(number: number): void {
                                this.counter = new Array();
                            }

                            public calculateProbability(feature: any): number {
                                var res: number = <number>feature;
                                var p: number = this.counter[res];
                                if (this.total != 0) {
                                    return p / this.total;
                                } else {
                                    return 0;
                                }
                            }

                            public train(feature: any): void {
                                var res: number = <number>feature;
                                this.counter[res]++;
                                this.total++;
                            }

                            public save(separator: string): string {
                                if (this.counter == null || this.counter.length == 0) {
                                    return "EnumSubstate" + separator;
                                }
                                var sb: java.lang.StringBuilder = new java.lang.StringBuilder();
                                sb.append("EnumSubstate" + separator);
                                for (var i: number = 0; i < this.counter.length; i++) {
                                    sb.append(this.counter[i] + separator);
                                }
                                return sb.toString();
                            }

                            public load(payload: string, separator: string): void {
                                var res: string[] = payload.split(separator);
                                this.counter = new Array();
                                this.total = 0;
                                for (var i: number = 0; i < res.length; i++) {
                                    this.counter[i] = java.lang.Integer.parseInt(res[i]);
                                    this.total += this.counter[i];
                                }
                            }

                            public cloneState(): org.kevoree.modeling.infer.states.Bayesian.BayesianSubstate {
                                var cloned: org.kevoree.modeling.infer.states.Bayesian.EnumSubstate = new org.kevoree.modeling.infer.states.Bayesian.EnumSubstate();
                                var newCounter: number[] = new Array();
                                for (var i: number = 0; i < this.counter.length; i++) {
                                    newCounter[i] = this.counter[i];
                                }
                                cloned.setCounter(newCounter);
                                cloned.setTotal(this.total);
                                return cloned;
                            }

                        }

                        export class GaussianSubState extends org.kevoree.modeling.infer.states.Bayesian.BayesianSubstate {

                            private sumSquares: number = 0;
                            private sum: number = 0;
                            private nb: number = 0;
                            public getSumSquares(): number {
                                return this.sumSquares;
                            }

                            public setSumSquares(sumSquares: number): void {
                                this.sumSquares = sumSquares;
                            }

                            public getNb(): number {
                                return this.nb;
                            }

                            public setNb(nb: number): void {
                                this.nb = nb;
                            }

                            public getSum(): number {
                                return this.sum;
                            }

                            public setSum(sum: number): void {
                                this.sum = sum;
                            }

                            public calculateProbability(feature: any): number {
                                var fet: number = <number>feature;
                                var avg: number = this.sum / this.nb;
                                var variances: number = this.sumSquares / this.nb - avg * avg;
                                return (1 / Math.sqrt(2 * Math.PI * variances)) * Math.exp(-((fet - avg) * (fet - avg)) / (2 * variances));
                            }

                            public getAverage(): number {
                                if (this.nb != 0) {
                                    var avg: number = this.sum / this.nb;
                                    return avg;
                                } else {
                                    return null;
                                }
                            }

                            public train(feature: any): void {
                                var fet: number = <number>feature;
                                this.sum += fet;
                                this.sumSquares += fet * fet;
                                this.nb++;
                            }

                            public getVariance(): number {
                                if (this.nb != 0) {
                                    var avg: number = this.sum / this.nb;
                                    var newvar: number = this.sumSquares / this.nb - avg * avg;
                                    return newvar;
                                } else {
                                    return null;
                                }
                            }

                            public clear(): void {
                                this.nb = 0;
                                this.sum = 0;
                                this.sumSquares = 0;
                            }

                            public save(separator: string): string {
                                var sb: java.lang.StringBuilder = new java.lang.StringBuilder();
                                sb.append("GaussianSubState" + separator);
                                sb.append(this.nb + separator);
                                sb.append(this.sum + separator);
                                sb.append(this.sumSquares);
                                return sb.toString();
                            }

                            public load(payload: string, separator: string): void {
                                try {
                                    var previousState: string[] = payload.split(separator);
                                    this.nb = java.lang.Integer.parseInt(previousState[0]);
                                    this.sum = java.lang.Double.parseDouble(previousState[1]);
                                    this.sumSquares = java.lang.Double.parseDouble(previousState[2]);
                                } catch ($ex$) {
                                    if ($ex$ instanceof java.lang.Exception) {
                                        var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                        this.sum = 0;
                                        this.sumSquares = 0;
                                        this.nb = 0;
                                    } else {
                                        throw $ex$;
                                    }
                                }
                            }

                            public cloneState(): org.kevoree.modeling.infer.states.Bayesian.BayesianSubstate {
                                var cloned: org.kevoree.modeling.infer.states.Bayesian.GaussianSubState = new org.kevoree.modeling.infer.states.Bayesian.GaussianSubState();
                                cloned.setNb(this.getNb());
                                cloned.setSum(this.getSum());
                                cloned.setSumSquares(this.getSumSquares());
                                return cloned;
                            }

                        }

                    }
                }
            }
            export module memory {
                export interface KMemoryElement {

                    isDirty(): boolean;

                    setClean(metaModel: org.kevoree.modeling.meta.KMetaModel): void;

                    setDirty(): void;

                    serialize(metaModel: org.kevoree.modeling.meta.KMetaModel): string;

                    init(payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void;

                    counter(): number;

                    inc(): void;

                    dec(): void;

                    free(metaModel: org.kevoree.modeling.meta.KMetaModel): void;

                }

                export interface KMemoryFactory {

                    newCacheSegment(): org.kevoree.modeling.memory.struct.segment.KMemorySegment;

                    newLongTree(): org.kevoree.modeling.memory.struct.tree.KLongTree;

                    newLongLongTree(): org.kevoree.modeling.memory.struct.tree.KLongLongTree;

                    newUniverseMap(initSize: number, className: string): org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;

                    newFromKey(universe: number, time: number, uuid: number): org.kevoree.modeling.memory.KMemoryElement;

                }

                export module cache {
                    export interface KCache {

                        get(universe: number, time: number, obj: number): org.kevoree.modeling.memory.KMemoryElement;

                        put(universe: number, time: number, obj: number, payload: org.kevoree.modeling.memory.KMemoryElement): void;

                        dirties(): org.kevoree.modeling.memory.cache.impl.KCacheDirty[];

                        clear(metaModel: org.kevoree.modeling.meta.KMetaModel): void;

                        clean(metaModel: org.kevoree.modeling.meta.KMetaModel): void;

                        monitor(origin: org.kevoree.modeling.KObject): void;

                        size(): number;

                    }

                    export module impl {
                        export class HashMemoryCache implements org.kevoree.modeling.memory.cache.KCache {

                            private elementData: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry[];
                            private elementCount: number;
                            private elementDataSize: number;
                            private loadFactor: number;
                            private initalCapacity: number;
                            private threshold: number;
                            public get(universe: number, time: number, obj: number): org.kevoree.modeling.memory.KMemoryElement {
                                if (this.elementDataSize == 0) {
                                    return null;
                                }
                                var index: number = ((<number>(universe ^ time ^ obj)) & 0x7FFFFFFF) % this.elementDataSize;
                                var m: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry = this.elementData[index];
                                while (m != null){
                                    if (m.universe == universe && m.time == time && m.obj == obj) {
                                        return m.value;
                                    }
                                    m = m.next;
                                }
                                return null;
                            }

                            public put(universe: number, time: number, obj: number, payload: org.kevoree.modeling.memory.KMemoryElement): void {
                                var entry: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry = null;
                                var hash: number = <number>(universe ^ time ^ obj);
                                var index: number = (hash & 0x7FFFFFFF) % this.elementDataSize;
                                if (this.elementDataSize != 0) {
                                    var m: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry = this.elementData[index];
                                    while (m != null){
                                        if (m.universe == universe && m.time == time && m.obj == obj) {
                                            entry = m;
                                            break;
                                        }
                                        m = m.next;
                                    }
                                }
                                if (entry == null) {
                                    entry = this.complex_insert(index, hash, universe, time, obj);
                                }
                                entry.value = payload;
                            }

                            private complex_insert(previousIndex: number, hash: number, universe: number, time: number, obj: number): org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry {
                                var index: number = previousIndex;
                                if (++this.elementCount > this.threshold) {
                                    var length: number = (this.elementDataSize == 0 ? 1 : this.elementDataSize << 1);
                                    var newData: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry[] = new Array();
                                    for (var i: number = 0; i < this.elementDataSize; i++) {
                                        var entry: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry = this.elementData[i];
                                        while (entry != null){
                                            index = (<number>(entry.universe ^ entry.time ^ entry.obj) & 0x7FFFFFFF) % length;
                                            var next: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry = entry.next;
                                            entry.next = newData[index];
                                            newData[index] = entry;
                                            entry = next;
                                        }
                                    }
                                    this.elementData = newData;
                                    this.elementDataSize = length;
                                    this.threshold = <number>(this.elementDataSize * this.loadFactor);
                                    index = (hash & 0x7FFFFFFF) % this.elementDataSize;
                                }
                                var entry: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry = new org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry();
                                entry.universe = universe;
                                entry.time = time;
                                entry.obj = obj;
                                entry.next = this.elementData[index];
                                this.elementData[index] = entry;
                                return entry;
                            }

                            public dirties(): org.kevoree.modeling.memory.cache.impl.KCacheDirty[] {
                                var nbDirties: number = 0;
                                for (var i: number = 0; i < this.elementData.length; i++) {
                                    if (this.elementData[i] != null) {
                                        var current: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry = this.elementData[i];
                                        if (this.elementData[i].value.isDirty()) {
                                            nbDirties++;
                                        }
                                        while (current.next != null){
                                            current = current.next;
                                            if (current.value.isDirty()) {
                                                nbDirties++;
                                            }
                                        }
                                    }
                                }
                                var collectedDirties: org.kevoree.modeling.memory.cache.impl.KCacheDirty[] = new Array();
                                var dirtySize: number = nbDirties;
                                nbDirties = 0;
                                for (var i: number = 0; i < this.elementData.length; i++) {
                                    if (nbDirties < dirtySize) {
                                        if (this.elementData[i] != null) {
                                            var current: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry = this.elementData[i];
                                            if (this.elementData[i].value.isDirty()) {
                                                var dirty: org.kevoree.modeling.memory.cache.impl.KCacheDirty = new org.kevoree.modeling.memory.cache.impl.KCacheDirty(new org.kevoree.modeling.KContentKey(current.universe, current.time, current.obj), this.elementData[i].value);
                                                collectedDirties[nbDirties] = dirty;
                                                nbDirties++;
                                            }
                                            while (current.next != null){
                                                current = current.next;
                                                if (current.value.isDirty()) {
                                                    var dirty: org.kevoree.modeling.memory.cache.impl.KCacheDirty = new org.kevoree.modeling.memory.cache.impl.KCacheDirty(new org.kevoree.modeling.KContentKey(current.universe, current.time, current.obj), current.value);
                                                    collectedDirties[nbDirties] = dirty;
                                                    nbDirties++;
                                                }
                                            }
                                        }
                                    }
                                }
                                return collectedDirties;
                            }

                            public clean(metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                            }

                            public monitor(origin: org.kevoree.modeling.KObject): void {
                            }

                            public size(): number {
                                return this.elementCount;
                            }

                            private remove(universe: number, time: number, obj: number, p_metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                                var hash: number = <number>(universe ^ time ^ obj);
                                var index: number = (hash & 0x7FFFFFFF) % this.elementDataSize;
                                if (this.elementDataSize != 0) {
                                    var previous: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry = null;
                                    var m: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry = this.elementData[index];
                                    while (m != null){
                                        if (m.universe == universe && m.time == time && m.obj == obj) {
                                            this.elementCount--;
                                            try {
                                                m.value.free(p_metaModel);
                                            } catch ($ex$) {
                                                if ($ex$ instanceof java.lang.Exception) {
                                                    var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                    e.printStackTrace();
                                                } else {
                                                    throw $ex$;
                                                }
                                            }
                                            if (previous == null) {
                                                this.elementData[index] = m.next;
                                            } else {
                                                previous.next = m.next;
                                            }
                                        }
                                        previous = m;
                                        m = m.next;
                                    }
                                }
                            }

                            constructor() {
                                this.initalCapacity = org.kevoree.modeling.KConfig.CACHE_INIT_SIZE;
                                this.loadFactor = org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR;
                                this.elementCount = 0;
                                this.elementData = new Array();
                                this.elementDataSize = this.initalCapacity;
                                this.threshold = <number>(this.elementDataSize * this.loadFactor);
                            }

                            public clear(metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                                for (var i: number = 0; i < this.elementData.length; i++) {
                                    var e: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry = this.elementData[i];
                                    while (e != null){
                                        e.value.free(metaModel);
                                        e = e.next;
                                    }
                                }
                                if (this.elementCount > 0) {
                                    this.elementCount = 0;
                                    this.elementData = new Array();
                                    this.elementDataSize = this.initalCapacity;
                                }
                            }

                        }

                        export module HashMemoryCache { 
                            export class Entry {

                                public next: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry;
                                public universe: number;
                                public time: number;
                                public obj: number;
                                public value: org.kevoree.modeling.memory.KMemoryElement;
                            }


                        }
                        export class KCacheDirty {

                            public key: org.kevoree.modeling.KContentKey;
                            public object: org.kevoree.modeling.memory.KMemoryElement;
                            constructor(key: org.kevoree.modeling.KContentKey, object: org.kevoree.modeling.memory.KMemoryElement) {
                                this.key = key;
                                this.object = object;
                            }

                        }

                    }
                }
                export module manager {
                    export class AccessMode {

                        public static RESOLVE: AccessMode = new AccessMode();
                        public static NEW: AccessMode = new AccessMode();
                        public static DELETE: AccessMode = new AccessMode();
                        public equals(other: any): boolean {
                            return this == other;
                        }
                        public static _AccessModeVALUES : AccessMode[] = [
                            AccessMode.RESOLVE
                            ,AccessMode.NEW
                            ,AccessMode.DELETE
                        ];
                        public static values():AccessMode[]{
                            return AccessMode._AccessModeVALUES;
                        }
                    }

                    export interface KMemoryManager {

                        cdn(): org.kevoree.modeling.cdn.KContentDeliveryDriver;

                        model(): org.kevoree.modeling.KModel<any>;

                        cache(): org.kevoree.modeling.memory.cache.KCache;

                        lookup(universe: number, time: number, uuid: number, callback: (p : org.kevoree.modeling.KObject) => void): void;

                        lookupAllobjects(universe: number, time: number, uuid: number[], callback: (p : org.kevoree.modeling.KObject[]) => void): void;

                        lookupAlltimes(universe: number, time: number[], uuid: number, callback: (p : org.kevoree.modeling.KObject[]) => void): void;

                        segment(universe: number, time: number, uuid: number, accessMode: org.kevoree.modeling.memory.manager.AccessMode, metaClass: org.kevoree.modeling.meta.KMetaClass, resolutionTrace: org.kevoree.modeling.memory.manager.KMemorySegmentResolutionTrace): org.kevoree.modeling.memory.struct.segment.KMemorySegment;

                        save(callback: (p : java.lang.Throwable) => void): void;

                        discard(universe: org.kevoree.modeling.KUniverse<any, any, any>, callback: (p : java.lang.Throwable) => void): void;

                        delete(universe: org.kevoree.modeling.KUniverse<any, any, any>, callback: (p : java.lang.Throwable) => void): void;

                        initKObject(obj: org.kevoree.modeling.KObject): void;

                        initUniverse(universe: org.kevoree.modeling.KUniverse<any, any, any>, parent: org.kevoree.modeling.KUniverse<any, any, any>): void;

                        nextUniverseKey(): number;

                        nextObjectKey(): number;

                        nextModelKey(): number;

                        nextGroupKey(): number;

                        getRoot(universe: number, time: number, callback: (p : org.kevoree.modeling.KObject) => void): void;

                        setRoot(newRoot: org.kevoree.modeling.KObject, callback: (p : java.lang.Throwable) => void): void;

                        setContentDeliveryDriver(driver: org.kevoree.modeling.cdn.KContentDeliveryDriver): void;

                        setScheduler(scheduler: org.kevoree.modeling.scheduler.KScheduler): void;

                        operationManager(): org.kevoree.modeling.operation.KOperationManager;

                        connect(callback: (p : java.lang.Throwable) => void): void;

                        close(callback: (p : java.lang.Throwable) => void): void;

                        parentUniverseKey(currentUniverseKey: number): number;

                        descendantsUniverseKeys(currentUniverseKey: number): number[];

                        reload(keys: org.kevoree.modeling.KContentKey[], callback: (p : java.lang.Throwable) => void): void;

                        cleanCache(): void;

                        setFactory(factory: org.kevoree.modeling.memory.KMemoryFactory): void;

                    }

                    export interface KMemorySegmentResolutionTrace {

                        getUniverse(): number;

                        setUniverse(universe: number): void;

                        getTime(): number;

                        setTime(time: number): void;

                        getUniverseTree(): org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;

                        setUniverseOrder(orderMap: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap): void;

                        getTimeTree(): org.kevoree.modeling.memory.struct.tree.KLongTree;

                        setTimeTree(tree: org.kevoree.modeling.memory.struct.tree.KLongTree): void;

                        getSegment(): org.kevoree.modeling.memory.struct.segment.KMemorySegment;

                        setSegment(tree: org.kevoree.modeling.memory.struct.segment.KMemorySegment): void;

                    }

                    export module impl {
                        export class HeapMemoryManager implements org.kevoree.modeling.memory.manager.KMemoryManager {

                            private static OUT_OF_CACHE_MESSAGE: string = "KMF Error: your object is out of cache, you probably kept an old reference. Please reload it with a lookup";
                            private static UNIVERSE_NOT_CONNECTED_ERROR: string = "Please connect your model prior to create a universe or an object";
                            private _db: org.kevoree.modeling.cdn.KContentDeliveryDriver;
                            private _operationManager: org.kevoree.modeling.operation.KOperationManager;
                            private _scheduler: org.kevoree.modeling.scheduler.KScheduler;
                            private _model: org.kevoree.modeling.KModel<any>;
                            private _factory: org.kevoree.modeling.memory.KMemoryFactory;
                            private _objectKeyCalculator: org.kevoree.modeling.memory.manager.impl.KeyCalculator = null;
                            private _universeKeyCalculator: org.kevoree.modeling.memory.manager.impl.KeyCalculator = null;
                            private _modelKeyCalculator: org.kevoree.modeling.memory.manager.impl.KeyCalculator;
                            private _groupKeyCalculator: org.kevoree.modeling.memory.manager.impl.KeyCalculator;
                            private isConnected: boolean = false;
                            private _cache: org.kevoree.modeling.memory.cache.KCache;
                            private static UNIVERSE_INDEX: number = 0;
                            private static OBJ_INDEX: number = 1;
                            private static GLO_TREE_INDEX: number = 2;
                            private static zeroPrefix: number = 0;
                            constructor(model: org.kevoree.modeling.KModel<any>) {
                                this._cache = new org.kevoree.modeling.memory.cache.impl.HashMemoryCache();
                                this._modelKeyCalculator = new org.kevoree.modeling.memory.manager.impl.KeyCalculator(HeapMemoryManager.zeroPrefix, 0);
                                this._groupKeyCalculator = new org.kevoree.modeling.memory.manager.impl.KeyCalculator(HeapMemoryManager.zeroPrefix, 0);
                                this._db = new org.kevoree.modeling.cdn.impl.MemoryContentDeliveryDriver();
                                this._db.setManager(this);
                                this._operationManager = new org.kevoree.modeling.operation.impl.HashOperationManager(this);
                                this._scheduler = new org.kevoree.modeling.scheduler.impl.DirectScheduler();
                                this._model = model;
                                this._factory = new org.kevoree.modeling.memory.struct.HeapMemoryFactory();
                            }

                            public cache(): org.kevoree.modeling.memory.cache.KCache {
                                return this._cache;
                            }

                            public model(): org.kevoree.modeling.KModel<any> {
                                return this._model;
                            }

                            public close(callback: (p : java.lang.Throwable) => void): void {
                                this.isConnected = false;
                                if (this._db != null) {
                                    this._db.close(callback);
                                } else {
                                    callback(null);
                                }
                            }

                            public nextUniverseKey(): number {
                                if (this._universeKeyCalculator == null) {
                                    throw new java.lang.RuntimeException(HeapMemoryManager.UNIVERSE_NOT_CONNECTED_ERROR);
                                }
                                var nextGeneratedKey: number = this._universeKeyCalculator.nextKey();
                                if (nextGeneratedKey == org.kevoree.modeling.KConfig.NULL_LONG || nextGeneratedKey == org.kevoree.modeling.KConfig.END_OF_TIME) {
                                    nextGeneratedKey = this._universeKeyCalculator.nextKey();
                                }
                                return nextGeneratedKey;
                            }

                            public nextObjectKey(): number {
                                if (this._objectKeyCalculator == null) {
                                    throw new java.lang.RuntimeException(HeapMemoryManager.UNIVERSE_NOT_CONNECTED_ERROR);
                                }
                                var nextGeneratedKey: number = this._objectKeyCalculator.nextKey();
                                if (nextGeneratedKey == org.kevoree.modeling.KConfig.NULL_LONG || nextGeneratedKey == org.kevoree.modeling.KConfig.END_OF_TIME) {
                                    nextGeneratedKey = this._objectKeyCalculator.nextKey();
                                }
                                return nextGeneratedKey;
                            }

                            public nextModelKey(): number {
                                return this._modelKeyCalculator.nextKey();
                            }

                            public nextGroupKey(): number {
                                return this._groupKeyCalculator.nextKey();
                            }

                            public globalUniverseOrder(): org.kevoree.modeling.memory.struct.map.KUniverseOrderMap {
                                return <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>this._cache.get(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG);
                            }

                            public initUniverse(p_universe: org.kevoree.modeling.KUniverse<any, any, any>, p_parent: org.kevoree.modeling.KUniverse<any, any, any>): void {
                                var cached: org.kevoree.modeling.memory.struct.map.KLongLongMap = this.globalUniverseOrder();
                                if (cached != null && !cached.contains(p_universe.key())) {
                                    if (p_parent == null) {
                                        cached.put(p_universe.key(), p_universe.key());
                                    } else {
                                        cached.put(p_universe.key(), p_parent.key());
                                    }
                                }
                            }

                            public parentUniverseKey(currentUniverseKey: number): number {
                                var cached: org.kevoree.modeling.memory.struct.map.KLongLongMap = this.globalUniverseOrder();
                                if (cached != null) {
                                    return cached.get(currentUniverseKey);
                                } else {
                                    return org.kevoree.modeling.KConfig.NULL_LONG;
                                }
                            }

                            public descendantsUniverseKeys(currentUniverseKey: number): number[] {
                                var cached: org.kevoree.modeling.memory.struct.map.KLongLongMap = this.globalUniverseOrder();
                                if (cached != null) {
                                    var temp: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                    cached.each( (key : number, value : number) => {
                                        if (value == currentUniverseKey && key != currentUniverseKey) {
                                            temp.put(key, value);
                                        }
                                    });
                                    var result: number[] = new Array();
                                    var insertIndex: number[] = [0];
                                    temp.each( (key : number, value : number) => {
                                        result[insertIndex[0]] = key;
                                        insertIndex[0]++;
                                    });
                                    return result;
                                } else {
                                    return new Array();
                                }
                            }

                            public save(callback: (p : java.lang.Throwable) => void): void {
                                var dirtiesEntries: org.kevoree.modeling.memory.cache.impl.KCacheDirty[] = this._cache.dirties();
                                var request: org.kevoree.modeling.cdn.impl.ContentPutRequest = new org.kevoree.modeling.cdn.impl.ContentPutRequest(dirtiesEntries.length + 2);
                                var notificationMessages: org.kevoree.modeling.message.impl.Events = new org.kevoree.modeling.message.impl.Events(dirtiesEntries.length);
                                for (var i: number = 0; i < dirtiesEntries.length; i++) {
                                    var cachedObject: org.kevoree.modeling.memory.KMemoryElement = dirtiesEntries[i].object;
                                    var meta: number[];
                                    if (dirtiesEntries[i].object instanceof org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment) {
                                        var segment: org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment = <org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment>dirtiesEntries[i].object;
                                        meta = segment.modifiedIndexes(this._model.metaModel().metaClasses()[segment.metaClassIndex()]);
                                    } else {
                                        meta = null;
                                    }
                                    notificationMessages.setEvent(i, dirtiesEntries[i].key, meta);
                                    request.put(dirtiesEntries[i].key, cachedObject.serialize(this._model.metaModel()));
                                    cachedObject.setClean(this._model.metaModel());
                                }
                                request.put(org.kevoree.modeling.KContentKey.createLastObjectIndexFromPrefix(this._objectKeyCalculator.prefix()), "" + this._objectKeyCalculator.lastComputedIndex());
                                request.put(org.kevoree.modeling.KContentKey.createLastUniverseIndexFromPrefix(this._universeKeyCalculator.prefix()), "" + this._universeKeyCalculator.lastComputedIndex());
                                this._db.put(request,  (throwable : java.lang.Throwable) => {
                                    if (throwable == null) {
                                        this._db.send(notificationMessages);
                                    }
                                    if (callback != null) {
                                        callback(throwable);
                                    }
                                });
                            }

                            public initKObject(obj: org.kevoree.modeling.KObject): void {
                                var cacheEntry: org.kevoree.modeling.memory.struct.segment.KMemorySegment = this._factory.newCacheSegment();
                                cacheEntry.initMetaClass(obj.metaClass());
                                cacheEntry.setDirty();
                                cacheEntry.inc();
                                var timeTree: org.kevoree.modeling.memory.struct.tree.KLongTree = this._factory.newLongTree();
                                timeTree.inc();
                                timeTree.insert(obj.now());
                                var universeTree: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap = this._factory.newUniverseMap(0, obj.metaClass().metaName());
                                universeTree.inc();
                                universeTree.put(obj.universe(), obj.now());
                                this._cache.put(obj.universe(), org.kevoree.modeling.KConfig.NULL_LONG, obj.uuid(), timeTree);
                                this._cache.put(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, obj.uuid(), universeTree);
                                this._cache.put(obj.universe(), obj.now(), obj.uuid(), cacheEntry);
                            }

                            public connect(connectCallback: (p : java.lang.Throwable) => void): void {
                                if (this.isConnected) {
                                    if (connectCallback != null) {
                                        connectCallback(null);
                                    }
                                }
                                if (this._db == null) {
                                    if (connectCallback != null) {
                                        connectCallback(new java.lang.Exception("Please attach a KDataBase AND a KBroker first !"));
                                    }
                                } else {
                                    this._db.connect( (throwable : java.lang.Throwable) => {
                                        if (throwable == null) {
                                            this._db.atomicGetIncrement(org.kevoree.modeling.KContentKey.createLastPrefix(),  (newPrefix : number) => {
                                                var connectionElemKeys: org.kevoree.modeling.KContentKey[] = new Array();
                                                connectionElemKeys[HeapMemoryManager.UNIVERSE_INDEX] = org.kevoree.modeling.KContentKey.createLastUniverseIndexFromPrefix(newPrefix);
                                                connectionElemKeys[HeapMemoryManager.OBJ_INDEX] = org.kevoree.modeling.KContentKey.createLastObjectIndexFromPrefix(newPrefix);
                                                connectionElemKeys[HeapMemoryManager.GLO_TREE_INDEX] = org.kevoree.modeling.KContentKey.createGlobalUniverseTree();
                                                var finalNewPrefix: number = newPrefix;
                                                this._db.get(connectionElemKeys,  (strings : string[]) => {
                                                    if (strings.length == 3) {
                                                        var detected: java.lang.Exception = null;
                                                        try {
                                                            var uniIndexPayload: string = strings[HeapMemoryManager.UNIVERSE_INDEX];
                                                            if (uniIndexPayload == null || uniIndexPayload.equals("")) {
                                                                uniIndexPayload = "0";
                                                            }
                                                            var objIndexPayload: string = strings[HeapMemoryManager.OBJ_INDEX];
                                                            if (objIndexPayload == null || objIndexPayload.equals("")) {
                                                                objIndexPayload = "0";
                                                            }
                                                            var globalUniverseTreePayload: string = strings[HeapMemoryManager.GLO_TREE_INDEX];
                                                            var globalUniverseTree: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
                                                            if (globalUniverseTreePayload != null) {
                                                                globalUniverseTree = this._factory.newUniverseMap(0, null);
                                                                try {
                                                                    globalUniverseTree.init(globalUniverseTreePayload, this.model().metaModel());
                                                                } catch ($ex$) {
                                                                    if ($ex$ instanceof java.lang.Exception) {
                                                                        var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                                        e.printStackTrace();
                                                                    } else {
                                                                        throw $ex$;
                                                                    }
                                                                }
                                                            } else {
                                                                globalUniverseTree = this._factory.newUniverseMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, null);
                                                            }
                                                            this._cache.put(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, globalUniverseTree);
                                                            var newUniIndex: number = java.lang.Long.parseLong(uniIndexPayload);
                                                            var newObjIndex: number = java.lang.Long.parseLong(objIndexPayload);
                                                            this._universeKeyCalculator = new org.kevoree.modeling.memory.manager.impl.KeyCalculator(finalNewPrefix, newUniIndex);
                                                            this._objectKeyCalculator = new org.kevoree.modeling.memory.manager.impl.KeyCalculator(finalNewPrefix, newObjIndex);
                                                            this.isConnected = true;
                                                        } catch ($ex$) {
                                                            if ($ex$ instanceof java.lang.Exception) {
                                                                var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                                detected = e;
                                                            } else {
                                                                throw $ex$;
                                                            }
                                                        }
                                                        if (connectCallback != null) {
                                                            connectCallback(detected);
                                                        }
                                                    } else {
                                                        if (connectCallback != null) {
                                                            connectCallback(new java.lang.Exception("Error while connecting the KDataStore..."));
                                                        }
                                                    }
                                                });
                                            });
                                        } else {
                                            if (connectCallback != null) {
                                                connectCallback(throwable);
                                            }
                                        }
                                    });
                                }
                            }

                            public segment(universe: number, time: number, uuid: number, accessMode: org.kevoree.modeling.memory.manager.AccessMode, metaClass: org.kevoree.modeling.meta.KMetaClass, resolutionTrace: org.kevoree.modeling.memory.manager.KMemorySegmentResolutionTrace): org.kevoree.modeling.memory.struct.segment.KMemorySegment {
                                var currentEntry: org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment = <org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment>this._cache.get(universe, time, uuid);
                                if (currentEntry != null) {
                                    if (resolutionTrace != null) {
                                        resolutionTrace.setSegment(currentEntry);
                                        resolutionTrace.setUniverse(universe);
                                        resolutionTrace.setTime(time);
                                        resolutionTrace.setUniverseOrder(<org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>this._cache.get(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, uuid));
                                        resolutionTrace.setTimeTree(<org.kevoree.modeling.memory.struct.tree.KLongTree>this._cache.get(universe, org.kevoree.modeling.KConfig.NULL_LONG, uuid));
                                    }
                                    return currentEntry;
                                }
                                var objectUniverseTree: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap = <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>this._cache.get(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, uuid);
                                var resolvedUniverse: number = org.kevoree.modeling.memory.manager.impl.ResolutionHelper.resolve_universe(this.globalUniverseOrder(), objectUniverseTree, time, universe);
                                var timeTree: org.kevoree.modeling.memory.struct.tree.KLongTree = <org.kevoree.modeling.memory.struct.tree.KLongTree>this._cache.get(resolvedUniverse, org.kevoree.modeling.KConfig.NULL_LONG, uuid);
                                if (timeTree == null) {
                                    throw new java.lang.RuntimeException(HeapMemoryManager.OUT_OF_CACHE_MESSAGE + " : TimeTree not found for " + org.kevoree.modeling.KContentKey.createTimeTree(resolvedUniverse, uuid) + " from " + universe + "/" + resolvedUniverse);
                                }
                                var resolvedTime: number = timeTree.previousOrEqual(time);
                                if (resolutionTrace != null) {
                                    resolutionTrace.setUniverse(resolvedUniverse);
                                    resolutionTrace.setTime(resolvedTime);
                                    resolutionTrace.setUniverseOrder(objectUniverseTree);
                                    resolutionTrace.setTimeTree(timeTree);
                                }
                                if (resolvedTime != org.kevoree.modeling.KConfig.NULL_LONG) {
                                    var needTimeCopy: boolean = accessMode.equals(org.kevoree.modeling.memory.manager.AccessMode.NEW) && (resolvedTime != time);
                                    var needUniverseCopy: boolean = accessMode.equals(org.kevoree.modeling.memory.manager.AccessMode.NEW) && (resolvedUniverse != universe);
                                    var entry: org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment = <org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment>this._cache.get(resolvedUniverse, resolvedTime, uuid);
                                    if (entry == null) {
                                        return null;
                                    }
                                    if (accessMode.equals(org.kevoree.modeling.memory.manager.AccessMode.DELETE)) {
                                        timeTree.delete(time);
                                        if (resolutionTrace != null) {
                                            resolutionTrace.setSegment(entry);
                                        }
                                        return entry;
                                    }
                                    if (!needTimeCopy && !needUniverseCopy) {
                                        if (accessMode.equals(org.kevoree.modeling.memory.manager.AccessMode.NEW)) {
                                            entry.setDirty();
                                        }
                                        if (resolutionTrace != null) {
                                            resolutionTrace.setSegment(entry);
                                        }
                                        return entry;
                                    } else {
                                        var clonedEntry: org.kevoree.modeling.memory.struct.segment.KMemorySegment = entry.clone(metaClass);
                                        this._cache.put(universe, time, uuid, clonedEntry);
                                        if (!needUniverseCopy) {
                                            timeTree.insert(time);
                                        } else {
                                            var newTemporalTree: org.kevoree.modeling.memory.struct.tree.KLongTree = this._factory.newLongTree();
                                            newTemporalTree.insert(time);
                                            newTemporalTree.inc();
                                            timeTree.dec();
                                            this._cache.put(universe, org.kevoree.modeling.KConfig.NULL_LONG, uuid, newTemporalTree);
                                            objectUniverseTree.put(universe, time);
                                        }
                                        entry.dec();
                                        clonedEntry.inc();
                                        if (resolutionTrace != null) {
                                            resolutionTrace.setSegment(clonedEntry);
                                        }
                                        return clonedEntry;
                                    }
                                } else {
                                    System.err.println(HeapMemoryManager.OUT_OF_CACHE_MESSAGE + " Time not resolved " + time);
                                    return null;
                                }
                            }

                            public discard(p_universe: org.kevoree.modeling.KUniverse<any, any, any>, callback: (p : java.lang.Throwable) => void): void {
                                this._cache.clear(this._model.metaModel());
                                var globalUniverseTree: org.kevoree.modeling.KContentKey[] = new Array();
                                globalUniverseTree[0] = org.kevoree.modeling.KContentKey.createGlobalUniverseTree();
                                this.reload(globalUniverseTree,  (throwable : java.lang.Throwable) => {
                                    callback(throwable);
                                });
                            }

                            public delete(p_universe: org.kevoree.modeling.KUniverse<any, any, any>, callback: (p : java.lang.Throwable) => void): void {
                                throw new java.lang.RuntimeException("Not implemented yet !");
                            }

                            public lookup(universe: number, time: number, uuid: number, callback: (p : org.kevoree.modeling.KObject) => void): void {
                                var keys: number[] = new Array();
                                keys[0] = uuid;
                                this.lookupAllobjects(universe, time, keys,  (kObjects : org.kevoree.modeling.KObject[]) => {
                                    if (kObjects.length == 1) {
                                        if (callback != null) {
                                            callback(kObjects[0]);
                                        }
                                    } else {
                                        if (callback != null) {
                                            callback(null);
                                        }
                                    }
                                });
                            }

                            public lookupAllobjects(universe: number, time: number, uuids: number[], callback: (p : org.kevoree.modeling.KObject[]) => void): void {
                                this._scheduler.dispatch(new org.kevoree.modeling.memory.manager.impl.LookupAllRunnable(universe, time, uuids, callback, this));
                            }

                            public lookupAlltimes(universe: number, time: number[], uuid: number, callback: (p : org.kevoree.modeling.KObject[]) => void): void {
                                throw new java.lang.RuntimeException("Not Implemented Yet !");
                            }

                            public cdn(): org.kevoree.modeling.cdn.KContentDeliveryDriver {
                                return this._db;
                            }

                            public setContentDeliveryDriver(p_dataBase: org.kevoree.modeling.cdn.KContentDeliveryDriver): void {
                                this._db = p_dataBase;
                                p_dataBase.setManager(this);
                            }

                            public setScheduler(p_scheduler: org.kevoree.modeling.scheduler.KScheduler): void {
                                if (p_scheduler != null) {
                                    this._scheduler = p_scheduler;
                                }
                            }

                            public operationManager(): org.kevoree.modeling.operation.KOperationManager {
                                return this._operationManager;
                            }

                            public getRoot(universe: number, time: number, callback: (p : org.kevoree.modeling.KObject) => void): void {
                                this.bumpKeyToCache(org.kevoree.modeling.KContentKey.createRootUniverseTree(),  (rootGlobalUniverseIndex : org.kevoree.modeling.memory.KMemoryElement) => {
                                    if (rootGlobalUniverseIndex == null) {
                                        callback(null);
                                    } else {
                                        var closestUniverse: number = org.kevoree.modeling.memory.manager.impl.ResolutionHelper.resolve_universe(this.globalUniverseOrder(), <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>rootGlobalUniverseIndex, time, universe);
                                        var universeTreeRootKey: org.kevoree.modeling.KContentKey = org.kevoree.modeling.KContentKey.createRootTimeTree(closestUniverse);
                                        this.bumpKeyToCache(universeTreeRootKey,  (universeTree : org.kevoree.modeling.memory.KMemoryElement) => {
                                            if (universeTree == null) {
                                                callback(null);
                                            } else {
                                                var resolvedVal: number = (<org.kevoree.modeling.memory.struct.tree.impl.LongLongTree>universeTree).previousOrEqualValue(time);
                                                if (resolvedVal == org.kevoree.modeling.KConfig.NULL_LONG) {
                                                    callback(null);
                                                } else {
                                                    this.lookup(universe, time, resolvedVal, callback);
                                                }
                                            }
                                        });
                                    }
                                });
                            }

                            public setRoot(newRoot: org.kevoree.modeling.KObject, callback: (p : java.lang.Throwable) => void): void {
                                this.bumpKeyToCache(org.kevoree.modeling.KContentKey.createRootUniverseTree(),  (globalRootTree : org.kevoree.modeling.memory.KMemoryElement) => {
                                    var cleanedTree: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap = <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>globalRootTree;
                                    if (cleanedTree == null) {
                                        cleanedTree = this._factory.newUniverseMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, null);
                                        this._cache.put(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.END_OF_TIME, cleanedTree);
                                    }
                                    var closestUniverse: number = org.kevoree.modeling.memory.manager.impl.ResolutionHelper.resolve_universe(this.globalUniverseOrder(), cleanedTree, newRoot.now(), newRoot.universe());
                                    cleanedTree.put(newRoot.universe(), newRoot.now());
                                    if (closestUniverse != newRoot.universe()) {
                                        var newTimeTree: org.kevoree.modeling.memory.struct.tree.KLongLongTree = this._factory.newLongLongTree();
                                        newTimeTree.insert(newRoot.now(), newRoot.uuid());
                                        var universeTreeRootKey: org.kevoree.modeling.KContentKey = org.kevoree.modeling.KContentKey.createRootTimeTree(newRoot.universe());
                                        this._cache.put(universeTreeRootKey.universe, universeTreeRootKey.time, universeTreeRootKey.obj, <org.kevoree.modeling.memory.KMemoryElement>newTimeTree);
                                        if (callback != null) {
                                            callback(null);
                                        }
                                    } else {
                                        var universeTreeRootKey: org.kevoree.modeling.KContentKey = org.kevoree.modeling.KContentKey.createRootTimeTree(closestUniverse);
                                        this.bumpKeyToCache(universeTreeRootKey,  (resolvedRootTimeTree : org.kevoree.modeling.memory.KMemoryElement) => {
                                            var initializedTree: org.kevoree.modeling.memory.struct.tree.KLongLongTree = <org.kevoree.modeling.memory.struct.tree.KLongLongTree>resolvedRootTimeTree;
                                            if (initializedTree == null) {
                                                initializedTree = this._factory.newLongLongTree();
                                                this._cache.put(universeTreeRootKey.universe, universeTreeRootKey.time, universeTreeRootKey.obj, <org.kevoree.modeling.memory.KMemoryElement>initializedTree);
                                            }
                                            initializedTree.insert(newRoot.now(), newRoot.uuid());
                                            if (callback != null) {
                                                callback(null);
                                            }
                                        });
                                    }
                                });
                            }

                            public reload(keys: org.kevoree.modeling.KContentKey[], callback: (p : java.lang.Throwable) => void): void {
                                var toReload: java.util.List<org.kevoree.modeling.KContentKey> = new java.util.ArrayList<org.kevoree.modeling.KContentKey>();
                                for (var i: number = 0; i < keys.length; i++) {
                                    var cached: org.kevoree.modeling.memory.KMemoryElement = this._cache.get(keys[i].universe, keys[i].time, keys[i].obj);
                                    if (cached != null && !cached.isDirty()) {
                                        toReload.add(keys[i]);
                                    }
                                }
                                var toReload_flat: org.kevoree.modeling.KContentKey[] = toReload.toArray(new Array());
                                this._db.get(toReload_flat,  (strings : string[]) => {
                                    for (var i: number = 0; i < strings.length; i++) {
                                        if (strings[i] != null) {
                                            var correspondingKey: org.kevoree.modeling.KContentKey = toReload_flat[i];
                                            var cachedObj: org.kevoree.modeling.memory.KMemoryElement = this._cache.get(correspondingKey.universe, correspondingKey.time, correspondingKey.obj);
                                            if (cachedObj != null && !cachedObj.isDirty()) {
                                                cachedObj = this.internal_unserialize(correspondingKey, strings[i]);
                                                if (cachedObj != null) {
                                                    this._cache.put(correspondingKey.universe, correspondingKey.time, correspondingKey.obj, cachedObj);
                                                }
                                            }
                                        }
                                    }
                                    if (callback != null) {
                                        callback(null);
                                    }
                                });
                            }

                            public cleanCache(): void {
                                if (this._cache != null) {
                                    this._cache.clean(this._model.metaModel());
                                }
                            }

                            public setFactory(p_factory: org.kevoree.modeling.memory.KMemoryFactory): void {
                                this._factory = p_factory;
                            }

                            public bumpKeyToCache(contentKey: org.kevoree.modeling.KContentKey, callback: (p : org.kevoree.modeling.memory.KMemoryElement) => void): void {
                                var cached: org.kevoree.modeling.memory.KMemoryElement = this._cache.get(contentKey.universe, contentKey.time, contentKey.obj);
                                if (cached != null) {
                                    callback(cached);
                                } else {
                                    var keys: org.kevoree.modeling.KContentKey[] = new Array();
                                    keys[0] = contentKey;
                                    this._db.get(keys,  (strings : string[]) => {
                                        if (strings[0] != null) {
                                            var newObject: org.kevoree.modeling.memory.KMemoryElement = this.internal_unserialize(contentKey, strings[0]);
                                            if (newObject != null) {
                                                this._cache.put(contentKey.universe, contentKey.time, contentKey.obj, newObject);
                                            }
                                            callback(newObject);
                                        } else {
                                            callback(null);
                                        }
                                    });
                                }
                            }

                            public bumpKeysToCache(contentKeys: org.kevoree.modeling.KContentKey[], callback: (p : org.kevoree.modeling.memory.KMemoryElement[]) => void): void {
                                var toLoadIndexes: boolean[] = null;
                                var nbElem: number = 0;
                                var result: org.kevoree.modeling.memory.KMemoryElement[] = new Array();
                                for (var i: number = 0; i < contentKeys.length; i++) {
                                    if (contentKeys[i] != null) {
                                        result[i] = this._cache.get(contentKeys[i].universe, contentKeys[i].time, contentKeys[i].obj);
                                        if (result[i] == null) {
                                            if (toLoadIndexes == null) {
                                                toLoadIndexes = new Array();
                                            }
                                            toLoadIndexes[i] = true;
                                            nbElem++;
                                        }
                                    }
                                }
                                if (toLoadIndexes == null) {
                                    callback(result);
                                } else {
                                    var toLoadDbKeys: org.kevoree.modeling.KContentKey[] = new Array();
                                    var originIndexes: number[] = new Array();
                                    var toLoadIndex: number = 0;
                                    for (var i: number = 0; i < contentKeys.length; i++) {
                                        if (toLoadIndexes[i]) {
                                            toLoadDbKeys[toLoadIndex] = contentKeys[i];
                                            originIndexes[toLoadIndex] = i;
                                            toLoadIndex++;
                                        }
                                    }
                                    this._db.get(toLoadDbKeys,  (payloads : string[]) => {
                                        for (var i: number = 0; i < payloads.length; i++) {
                                            if (payloads[i] != null) {
                                                var newObjKey: org.kevoree.modeling.KContentKey = toLoadDbKeys[i];
                                                var newObject: org.kevoree.modeling.memory.KMemoryElement = this.internal_unserialize(newObjKey, payloads[i]);
                                                if (newObject != null) {
                                                    this._cache.put(newObjKey.universe, newObjKey.time, newObjKey.obj, newObject);
                                                    var originIndex: number = originIndexes[i];
                                                    result[originIndex] = newObject;
                                                }
                                            }
                                        }
                                        callback(result);
                                    });
                                }
                            }

                            private internal_unserialize(key: org.kevoree.modeling.KContentKey, payload: string): org.kevoree.modeling.memory.KMemoryElement {
                                var newElement: org.kevoree.modeling.memory.KMemoryElement = this._factory.newFromKey(key.universe, key.time, key.obj);
                                try {
                                    if (key.universe != org.kevoree.modeling.KConfig.NULL_LONG && key.time != org.kevoree.modeling.KConfig.NULL_LONG && key.obj != org.kevoree.modeling.KConfig.NULL_LONG) {
                                        var alreadyLoadedOrder: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap = <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>this._cache.get(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, key.obj);
                                        if (alreadyLoadedOrder != null) {
                                            (<org.kevoree.modeling.memory.struct.segment.KMemorySegment>newElement).initMetaClass(this._model.metaModel().metaClassByName(alreadyLoadedOrder.metaClassName()));
                                        }
                                    }
                                    newElement.init(payload, this.model().metaModel());
                                    newElement.setClean(this.model().metaModel());
                                    return newElement;
                                } catch ($ex$) {
                                    if ($ex$ instanceof java.lang.Exception) {
                                        var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                        e.printStackTrace();
                                        return null;
                                    } else {
                                        throw $ex$;
                                    }
                                }
                            }

                        }

                        export class KeyCalculator {

                             private _prefix: string;
                            private _currentIndex: number;
                            constructor(prefix: number, currentIndex: number) {
                                 this._prefix = "0x" + prefix.toString(org.kevoree.modeling.KConfig.PREFIX_SIZE);
                                 this._currentIndex = currentIndex;
                            }

                            public nextKey(): number {
                                 if (this._currentIndex == org.kevoree.modeling.KConfig.KEY_PREFIX_MASK) {
                                 throw new java.lang.IndexOutOfBoundsException("Object Index could not be created because it exceeded the capacity of the current prefix. Ask for a new prefix.");
                                 }
                                 this._currentIndex++;
                                 var indexHex = this._currentIndex.toString(org.kevoree.modeling.KConfig.PREFIX_SIZE);
                                 var objectKey = parseInt(this._prefix + "000000000".substring(0,9-indexHex.length) + indexHex, org.kevoree.modeling.KConfig.PREFIX_SIZE);
                                 if (objectKey >= org.kevoree.modeling.KConfig.NULL_LONG) {
                                 throw new java.lang.IndexOutOfBoundsException("Object Index exceeds teh maximum JavaScript number capacity. (2^"+org.kevoree.modeling.KConfig.LONG_SIZE+")");
                                 }
                                 return objectKey;
                            }

                            public lastComputedIndex(): number {
                                return this._currentIndex;
                            }

                            public prefix(): number {
                                 return parseInt(this._prefix,org.kevoree.modeling.KConfig.PREFIX_SIZE);
                            }

                        }

                        export class LookupAllRunnable implements java.lang.Runnable {

                            private _universe: number;
                            private _time: number;
                            private _keys: number[];
                            private _callback: (p : org.kevoree.modeling.KObject[]) => void;
                            private _store: org.kevoree.modeling.memory.manager.impl.HeapMemoryManager;
                            constructor(p_universe: number, p_time: number, p_keys: number[], p_callback: (p : org.kevoree.modeling.KObject[]) => void, p_store: org.kevoree.modeling.memory.manager.impl.HeapMemoryManager) {
                                this._universe = p_universe;
                                this._time = p_time;
                                this._keys = p_keys;
                                this._callback = p_callback;
                                this._store = p_store;
                            }

                            public run(): void {
                                var tempKeys: org.kevoree.modeling.KContentKey[] = new Array();
                                for (var i: number = 0; i < this._keys.length; i++) {
                                    if (this._keys[i] != org.kevoree.modeling.KConfig.NULL_LONG) {
                                        tempKeys[i] = org.kevoree.modeling.KContentKey.createUniverseTree(this._keys[i]);
                                    }
                                }
                                this._store.bumpKeysToCache(tempKeys,  (universeIndexes : org.kevoree.modeling.memory.KMemoryElement[]) => {
                                    for (var i: number = 0; i < this._keys.length; i++) {
                                        var toLoadKey: org.kevoree.modeling.KContentKey = null;
                                        if (universeIndexes[i] != null) {
                                            var closestUniverse: number = org.kevoree.modeling.memory.manager.impl.ResolutionHelper.resolve_universe(this._store.globalUniverseOrder(), <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>universeIndexes[i], this._time, this._universe);
                                            toLoadKey = org.kevoree.modeling.KContentKey.createTimeTree(closestUniverse, this._keys[i]);
                                        }
                                        tempKeys[i] = toLoadKey;
                                    }
                                    this._store.bumpKeysToCache(tempKeys,  (timeIndexes : org.kevoree.modeling.memory.KMemoryElement[]) => {
                                        for (var i: number = 0; i < this._keys.length; i++) {
                                            var resolvedContentKey: org.kevoree.modeling.KContentKey = null;
                                            if (timeIndexes[i] != null) {
                                                var cachedIndexTree: org.kevoree.modeling.memory.struct.tree.KLongTree = <org.kevoree.modeling.memory.struct.tree.KLongTree>timeIndexes[i];
                                                var resolvedNode: number = cachedIndexTree.previousOrEqual(this._time);
                                                if (resolvedNode != org.kevoree.modeling.KConfig.NULL_LONG) {
                                                    resolvedContentKey = org.kevoree.modeling.KContentKey.createObject(tempKeys[i].universe, resolvedNode, this._keys[i]);
                                                }
                                            }
                                            tempKeys[i] = resolvedContentKey;
                                        }
                                        this._store.bumpKeysToCache(tempKeys,  (cachedObjects : org.kevoree.modeling.memory.KMemoryElement[]) => {
                                            var proxies: org.kevoree.modeling.KObject[] = new Array();
                                            for (var i: number = 0; i < this._keys.length; i++) {
                                                if (cachedObjects[i] != null && cachedObjects[i] instanceof org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment) {
                                                    proxies[i] = (<org.kevoree.modeling.abs.AbstractKModel<any>>this._store.model()).createProxy(this._universe, this._time, this._keys[i], this._store.model().metaModel().metaClasses()[(<org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment>cachedObjects[i]).metaClassIndex()]);
                                                    if (proxies[i] != null) {
                                                        var cachedIndexTree: org.kevoree.modeling.memory.struct.tree.KLongTree = <org.kevoree.modeling.memory.struct.tree.KLongTree>timeIndexes[i];
                                                        cachedIndexTree.inc();
                                                        var universeTree: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap = <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>universeIndexes[i];
                                                        universeTree.inc();
                                                        cachedObjects[i].inc();
                                                    }
                                                }
                                            }
                                            this._callback(proxies);
                                        });
                                    });
                                });
                            }

                        }

                        export class MemorySegmentResolutionTrace implements org.kevoree.modeling.memory.manager.KMemorySegmentResolutionTrace {

                            private _universe: number;
                            private _time: number;
                            private _universeOrder: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
                            private _timeTree: org.kevoree.modeling.memory.struct.tree.KLongTree;
                            private _segment: org.kevoree.modeling.memory.struct.segment.KMemorySegment;
                            public getUniverse(): number {
                                return this._universe;
                            }

                            public setUniverse(p_universe: number): void {
                                this._universe = p_universe;
                            }

                            public getTime(): number {
                                return this._time;
                            }

                            public setTime(p_time: number): void {
                                this._time = p_time;
                            }

                            public getUniverseTree(): org.kevoree.modeling.memory.struct.map.KUniverseOrderMap {
                                return this._universeOrder;
                            }

                            public setUniverseOrder(p_u_tree: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap): void {
                                this._universeOrder = p_u_tree;
                            }

                            public getTimeTree(): org.kevoree.modeling.memory.struct.tree.KLongTree {
                                return this._timeTree;
                            }

                            public setTimeTree(p_t_tree: org.kevoree.modeling.memory.struct.tree.KLongTree): void {
                                this._timeTree = p_t_tree;
                            }

                            public getSegment(): org.kevoree.modeling.memory.struct.segment.KMemorySegment {
                                return this._segment;
                            }

                            public setSegment(p_segment: org.kevoree.modeling.memory.struct.segment.KMemorySegment): void {
                                this._segment = p_segment;
                            }

                        }

                        export class ResolutionHelper {

                            public static resolve_trees(universe: number, time: number, uuid: number, cache: org.kevoree.modeling.memory.cache.KCache): org.kevoree.modeling.memory.manager.impl.MemorySegmentResolutionTrace {
                                var result: org.kevoree.modeling.memory.manager.impl.MemorySegmentResolutionTrace = new org.kevoree.modeling.memory.manager.impl.MemorySegmentResolutionTrace();
                                var objectUniverseTree: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap = <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>cache.get(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, uuid);
                                var globalUniverseOrder: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap = <org.kevoree.modeling.memory.struct.map.KUniverseOrderMap>cache.get(org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG, org.kevoree.modeling.KConfig.NULL_LONG);
                                result.setUniverseOrder(objectUniverseTree);
                                var resolvedUniverse: number = org.kevoree.modeling.memory.manager.impl.ResolutionHelper.resolve_universe(globalUniverseOrder, objectUniverseTree, time, universe);
                                result.setUniverse(resolvedUniverse);
                                var timeTree: org.kevoree.modeling.memory.struct.tree.KLongTree = <org.kevoree.modeling.memory.struct.tree.KLongTree>cache.get(resolvedUniverse, org.kevoree.modeling.KConfig.NULL_LONG, uuid);
                                if (timeTree != null) {
                                    result.setTimeTree(timeTree);
                                    var resolvedTime: number = timeTree.previousOrEqual(time);
                                    result.setTime(resolvedTime);
                                    result.setSegment(<org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment>cache.get(resolvedUniverse, resolvedTime, uuid));
                                }
                                return result;
                            }

                            public static resolve_universe(globalTree: org.kevoree.modeling.memory.struct.map.KLongLongMap, objUniverseTree: org.kevoree.modeling.memory.struct.map.KLongLongMap, timeToResolve: number, originUniverseId: number): number {
                                if (globalTree == null || objUniverseTree == null) {
                                    return originUniverseId;
                                }
                                var currentUniverse: number = originUniverseId;
                                var previousUniverse: number = org.kevoree.modeling.KConfig.NULL_LONG;
                                var divergenceTime: number = objUniverseTree.get(currentUniverse);
                                while (currentUniverse != previousUniverse){
                                    if (divergenceTime != org.kevoree.modeling.KConfig.NULL_LONG && divergenceTime <= timeToResolve) {
                                        return currentUniverse;
                                    }
                                    previousUniverse = currentUniverse;
                                    currentUniverse = globalTree.get(currentUniverse);
                                    divergenceTime = objUniverseTree.get(currentUniverse);
                                }
                                return originUniverseId;
                            }

                            public static universeSelectByRange(globalTree: org.kevoree.modeling.memory.struct.map.KLongLongMap, objUniverseTree: org.kevoree.modeling.memory.struct.map.KLongLongMap, rangeMin: number, rangeMax: number, originUniverseId: number): number[] {
                                var collected: org.kevoree.modeling.memory.struct.map.KLongLongMap = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                var currentUniverse: number = originUniverseId;
                                var previousUniverse: number = org.kevoree.modeling.KConfig.NULL_LONG;
                                var divergenceTime: number = objUniverseTree.get(currentUniverse);
                                while (currentUniverse != previousUniverse){
                                    if (divergenceTime != org.kevoree.modeling.KConfig.NULL_LONG) {
                                        if (divergenceTime <= rangeMin) {
                                            collected.put(collected.size(), currentUniverse);
                                            break;
                                        } else {
                                            if (divergenceTime <= rangeMax) {
                                                collected.put(collected.size(), currentUniverse);
                                            }
                                        }
                                    }
                                    previousUniverse = currentUniverse;
                                    currentUniverse = globalTree.get(currentUniverse);
                                    divergenceTime = objUniverseTree.get(currentUniverse);
                                }
                                var trimmed: number[] = new Array();
                                for (var i: number = 0; i < collected.size(); i++) {
                                    trimmed[<number>i] = collected.get(i);
                                }
                                return trimmed;
                            }

                        }

                    }
                }
                export module struct {
                    export class HeapMemoryFactory implements org.kevoree.modeling.memory.KMemoryFactory {

                        public newCacheSegment(): org.kevoree.modeling.memory.struct.segment.KMemorySegment {
                            return new org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment();
                        }

                        public newLongTree(): org.kevoree.modeling.memory.struct.tree.KLongTree {
                            return new org.kevoree.modeling.memory.struct.tree.impl.LongTree();
                        }

                        public newLongLongTree(): org.kevoree.modeling.memory.struct.tree.KLongLongTree {
                            return new org.kevoree.modeling.memory.struct.tree.impl.LongLongTree();
                        }

                        public newUniverseMap(initSize: number, p_className: string): org.kevoree.modeling.memory.struct.map.KUniverseOrderMap {
                            return new org.kevoree.modeling.memory.struct.map.impl.ArrayUniverseOrderMap(initSize, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR, p_className);
                        }

                        public newFromKey(universe: number, time: number, uuid: number): org.kevoree.modeling.memory.KMemoryElement {
                            var result: org.kevoree.modeling.memory.KMemoryElement;
                            var isUniverseNotNull: boolean = universe != org.kevoree.modeling.KConfig.NULL_LONG;
                            if (org.kevoree.modeling.KConfig.END_OF_TIME == uuid) {
                                if (isUniverseNotNull) {
                                    result = this.newLongLongTree();
                                } else {
                                    result = this.newUniverseMap(0, null);
                                }
                            } else {
                                var isTimeNotNull: boolean = time != org.kevoree.modeling.KConfig.NULL_LONG;
                                var isObjNotNull: boolean = uuid != org.kevoree.modeling.KConfig.NULL_LONG;
                                if (isUniverseNotNull && isTimeNotNull && isObjNotNull) {
                                    result = this.newCacheSegment();
                                } else {
                                    if (isUniverseNotNull && !isTimeNotNull && isObjNotNull) {
                                        result = this.newLongTree();
                                    } else {
                                        result = this.newUniverseMap(0, null);
                                    }
                                }
                            }
                            return result;
                        }

                    }

                    export module map {
                        export interface KIntMap<V> {

                            contains(key: number): boolean;

                            get(key: number): V;

                            put(key: number, value: V): void;

                            each(callback: (p : number, p1 : V) => void): void;

                        }

                        export interface KIntMapCallBack<V> {

                            on(key: number, value: V): void;

                        }

                        export interface KLongLongMap {

                            contains(key: number): boolean;

                            get(key: number): number;

                            put(key: number, value: number): void;

                            each(callback: (p : number, p1 : number) => void): void;

                            size(): number;

                            clear(): void;

                        }

                        export interface KLongLongMapCallBack<V> {

                            on(key: number, value: number): void;

                        }

                        export interface KLongMap<V> {

                            contains(key: number): boolean;

                            get(key: number): V;

                            put(key: number, value: V): void;

                            each(callback: (p : number, p1 : V) => void): void;

                            size(): number;

                            clear(): void;

                        }

                        export interface KLongMapCallBack<V> {

                            on(key: number, value: V): void;

                        }

                        export interface KStringMap<V> {

                            contains(key: string): boolean;

                            get(key: string): V;

                            put(key: string, value: V): void;

                            each(callback: (p : string, p1 : V) => void): void;

                            size(): number;

                            clear(): void;

                            remove(key: string): void;

                        }

                        export interface KStringMapCallBack<V> {

                            on(key: string, value: V): void;

                        }

                        export interface KUniverseOrderMap extends org.kevoree.modeling.memory.struct.map.KLongLongMap, org.kevoree.modeling.memory.KMemoryElement {

                            metaClassName(): string;

                        }

                        export module impl {
                            export class ArrayIntMap<V> implements org.kevoree.modeling.memory.struct.map.KIntMap<any> {

                                 constructor(initalCapacity: number, loadFactor : number) { }
                                 public clear():void { for(var p in this){if(this.hasOwnProperty(p)){delete this[p];}} }
                                 public get(key:number):V { return this[key]; }
                                 public put(key:number, pval : V):V { var previousVal = this[key];this[key] = pval;return previousVal;}
                                 public contains(key:number):boolean { return this.hasOwnProperty(<any>key);}
                                 public remove(key:number):V { var tmp = this[key]; delete this[key]; return tmp; }
                                 public size():number { return Object.keys(this).length; }
                                 public each(callback: (p : number, p1 : V) => void): void { for(var p in this){ if(this.hasOwnProperty(p)){ callback(<number>p,this[p]); } } }
                            }

                            export class ArrayLongLongMap implements org.kevoree.modeling.memory.struct.map.KLongLongMap {

                                 private _isDirty = false;
                                 constructor(initalCapacity: number, loadFactor : number) { }
                                 public clear():void { for(var p in this){ this._isDirty=true;if(this.hasOwnProperty(p) && p.indexOf('_') != 0){ delete this[p];}} }
                                 public get(key:number):number { return this[key]; }
                                 public put(key:number, pval : number):void { this._isDirty=true; this[key] = pval;}
                                 public contains(key:number):boolean { return this.hasOwnProperty(<any>key);}
                                 public remove(key:number):number { var tmp = this[key]; delete this[key]; return tmp; }
                                 public size():number { return Object.keys(this).length-1; }
                                 public each(callback: (p : number, p1 : number) => void): void { for(var p in this){ if(this.hasOwnProperty(p) && p.indexOf('_') != 0){ callback(<number>p,this[p]); } } }
                                 public isDirty():boolean { return this._isDirty; }
                                 public setClean(mm):void { this._isDirty = false; }
                                 public setDirty():void { this._isDirty = true; }
                            }

                            export class ArrayLongMap<V> implements org.kevoree.modeling.memory.struct.map.KLongMap<any> {

                                 constructor(initalCapacity: number, loadFactor : number) { }
                                 public clear():void { for(var p in this){if(this.hasOwnProperty(p)){delete this[p];} } }
                                 public get(key:number):V { return this[key]; }
                                 public put(key:number, pval : V):V { var previousVal = this[key];this[key] = pval;return previousVal;}
                                 public contains(key:number):boolean { return this.hasOwnProperty(<any>key);}
                                 public remove(key:number):V { var tmp = this[key]; delete this[key]; return tmp; }
                                 public size():number { return Object.keys(this).length; }
                                 public each(callback: (p : number, p1 : V) => void): void { for(var p in this){ if(this.hasOwnProperty(p)){ callback(<number>p,this[p]); } } }
                            }

                            export class ArrayStringMap<V> implements org.kevoree.modeling.memory.struct.map.KStringMap<any> {

                                 constructor(initalCapacity: number, loadFactor : number) { }
                                 public clear():void { for(var p in this){ if(this.hasOwnProperty(p)){ delete this[p];} } }
                                 public get(key:string):V { return this[key]; }
                                 public put(key:string, pval : V):V { var previousVal = this[key];this[key] = pval;return previousVal;}
                                 public contains(key:string):boolean { return this.hasOwnProperty(key);}
                                 public remove(key:string):V { var tmp = this[key]; delete this[key]; return tmp; }
                                 public size():number { return Object.keys(this).length; }
                                 public each(callback: (p : string, p1 : V) => void): void { for(var p in this){ if(this.hasOwnProperty(p)){ callback(<string>p,this[p]); } } }
                            }

                            export class ArrayUniverseOrderMap extends org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap implements org.kevoree.modeling.memory.struct.map.KUniverseOrderMap {

                                 private _counter = 0;
                                 private _className : string;
                                 constructor(initalCapacity: number, loadFactor : number, p_className : string) { super(initalCapacity,loadFactor);this._className = p_className; }
                                 public metaClassName(){ return this._className; }
                                 public counter():number { return this._counter; }
                                 public inc():void { this._counter++; }
                                 public dec():void { this._counter--; }
                                 public free():void {  }
                                 public size():number { return Object.keys(this).length-3; }
                                 public serialize(m): string {
                                 var buffer = "";
                                 if(this._className != null){ buffer = buffer + this._className + ','; }
                                 buffer = buffer + this.size() + JSON.stringify(this, function (k, v) {if(k[0]!='_'){return v;}else{undefined}});
                                 return buffer;
                                 }
                                 public init(payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                                 if (payload == null || payload.length == 0) { return; }
                                 var initPos = 0; var cursor = 0;
                                 while (cursor < payload.length && payload.charAt(cursor) != ',' && payload.charAt(cursor) != '{') { cursor++; }
                                 if (payload.charAt(cursor) == ',') { this._className = payload.substring(initPos, cursor);cursor++;initPos = cursor;}
                                 while (cursor < payload.length && payload.charAt(cursor) != '{') { cursor++; }
                                 var newParsedElem = JSON.parse(payload.substring(cursor));
                                 for(var el in newParsedElem){ this[el] = newParsedElem[el]; }
                                 }
                            }

                        }
                    }
                    export module segment {
                        export interface KMemorySegment extends org.kevoree.modeling.memory.KMemoryElement {

                            clone(metaClass: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.memory.struct.segment.KMemorySegment;

                            set(index: number, content: any, metaClass: org.kevoree.modeling.meta.KMetaClass): void;

                            get(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): any;

                            getRefSize(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;

                            getRefElem(index: number, refIndex: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;

                            getRef(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number[];

                            addRef(index: number, newRef: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean;

                            removeRef(index: number, previousRef: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean;

                            getInfer(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number[];

                            getInferSize(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;

                            getInferElem(index: number, arrayIndex: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;

                            setInferElem(index: number, arrayIndex: number, valueToInsert: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void;

                            extendInfer(index: number, newSize: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void;

                            modifiedIndexes(metaClass: org.kevoree.modeling.meta.KMetaClass): number[];

                            initMetaClass(metaClass: org.kevoree.modeling.meta.KMetaClass): void;

                            metaClassIndex(): number;

                        }

                        export module impl {
                            export class HeapMemorySegment implements org.kevoree.modeling.memory.struct.segment.KMemorySegment {

                                private raw: any[];
                                private _counter: number = 0;
                                private _metaClassIndex: number = -1;
                                private _modifiedIndexes: boolean[] = null;
                                private _dirty: boolean = false;
                                public initMetaClass(p_metaClass: org.kevoree.modeling.meta.KMetaClass): void {
                                    this.raw = new Array();
                                    this._metaClassIndex = p_metaClass.index();
                                }

                                public metaClassIndex(): number {
                                    return this._metaClassIndex;
                                }

                                public isDirty(): boolean {
                                    return this._dirty;
                                }

                                public serialize(metaModel: org.kevoree.modeling.meta.KMetaModel): string {
                                     var builder = {};
                                     var metaClass = metaModel.metaClass(this._metaClassIndex);
                                     var metaElements = metaClass.metaElements();
                                     for (var i = 0; i < this.raw.length; i++) {
                                     if(this.raw[i] != undefined && this.raw[i] != null){ builder[metaElements[i].metaName()] = this.raw[i]; }
                                     }
                                     return JSON.stringify(builder);
                                }

                                public modifiedIndexes(p_metaClass: org.kevoree.modeling.meta.KMetaClass): number[] {
                                    if (this._modifiedIndexes == null) {
                                        return new Array();
                                    } else {
                                        var nbModified: number = 0;
                                        for (var i: number = 0; i < this._modifiedIndexes.length; i++) {
                                            if (this._modifiedIndexes[i]) {
                                                nbModified = nbModified + 1;
                                            }
                                        }
                                        var result: number[] = new Array();
                                        var inserted: number = 0;
                                        for (var i: number = 0; i < this._modifiedIndexes.length; i++) {
                                            if (this._modifiedIndexes[i]) {
                                                result[inserted] = i;
                                                inserted = inserted + 1;
                                            }
                                        }
                                        return result;
                                    }
                                }

                                public setClean(metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                                    this._dirty = false;
                                    this._modifiedIndexes = null;
                                }

                                public setDirty(): void {
                                    this._dirty = true;
                                }

                                public init(payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                                     var rawElem = JSON.parse(payload);
                                     var metaClass = metaModel.metaClass(this._metaClassIndex);
                                     this.raw = [];
                                     for (var key in rawElem) {
                                     if("@class" != key){
                                     var elem = metaClass.metaByName(key);
                                     if(elem != null && elem != undefined){ this.raw[elem.index()] = rawElem[key]; }
                                     }
                                     }
                                }

                                public counter(): number {
                                    return this._counter;
                                }

                                public inc(): void {
                                    this._counter++;
                                }

                                public dec(): void {
                                    this._counter--;
                                }

                                public free(metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                                    this.raw = null;
                                }

                                public get(index: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass): any {
                                    if (this.raw != null) {
                                        return this.raw[index];
                                    } else {
                                        return null;
                                    }
                                }

                                public getRefSize(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number {
                                    var existing: number[] = <number[]>this.raw[index];
                                    if (existing != null) {
                                        return existing.length;
                                    }
                                    return 0;
                                }

                                public getRefElem(index: number, refIndex: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number {
                                    var existing: number[] = <number[]>this.raw[index];
                                    if (existing != null) {
                                        return existing[refIndex];
                                    } else {
                                        return org.kevoree.modeling.KConfig.NULL_LONG;
                                    }
                                }

                                public getRef(index: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass): number[] {
                                    if (this.raw != null) {
                                        var previousObj: any = this.raw[index];
                                        if (previousObj != null) {
                                            try {
                                                return <number[]>previousObj;
                                            } catch ($ex$) {
                                                if ($ex$ instanceof java.lang.Exception) {
                                                    var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                    e.printStackTrace();
                                                    this.raw[index] = null;
                                                    return null;
                                                } else {
                                                    throw $ex$;
                                                }
                                            }
                                        } else {
                                            return null;
                                        }
                                    } else {
                                        return null;
                                    }
                                }

                                public addRef(index: number, newRef: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean {
                                    if (this.raw != null) {
                                        var previous: number[] = <number[]>this.raw[index];
                                        if (previous == null) {
                                            previous = new Array();
                                            previous[0] = newRef;
                                        } else {
                                            for (var i: number = 0; i < previous.length; i++) {
                                                if (previous[i] == newRef) {
                                                    return false;
                                                }
                                            }
                                            var incArray: number[] = new Array();
                                            System.arraycopy(previous, 0, incArray, 0, previous.length);
                                            incArray[previous.length] = newRef;
                                            previous = incArray;
                                        }
                                        this.raw[index] = previous;
                                        if (this._modifiedIndexes == null) {
                                            this._modifiedIndexes = new Array();
                                        }
                                        this._modifiedIndexes[index] = true;
                                        this._dirty = true;
                                        return true;
                                    }
                                    return false;
                                }

                                public removeRef(index: number, refToRemove: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean {
                                    if (this.raw != null) {
                                        var previous: number[] = <number[]>this.raw[index];
                                        if (previous != null) {
                                            var indexToRemove: number = -1;
                                            for (var i: number = 0; i < previous.length; i++) {
                                                if (previous[i] == refToRemove) {
                                                    indexToRemove = i;
                                                    break;
                                                }
                                            }
                                            if (indexToRemove != -1) {
                                                if ((previous.length - 1) == 0) {
                                                    this.raw[index] = null;
                                                } else {
                                                    var newArray: number[] = new Array();
                                                    System.arraycopy(previous, 0, newArray, 0, indexToRemove);
                                                    System.arraycopy(previous, indexToRemove + 1, newArray, indexToRemove, previous.length - indexToRemove - 1);
                                                    this.raw[index] = newArray;
                                                }
                                                if (this._modifiedIndexes == null) {
                                                    this._modifiedIndexes = new Array();
                                                }
                                                this._modifiedIndexes[index] = true;
                                                this._dirty = true;
                                            }
                                        }
                                    }
                                    return false;
                                }

                                public getInfer(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number[] {
                                    if (this.raw != null) {
                                        var previousObj: any = this.raw[index];
                                        if (previousObj != null) {
                                            try {
                                                return <number[]>previousObj;
                                            } catch ($ex$) {
                                                if ($ex$ instanceof java.lang.Exception) {
                                                    var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                    e.printStackTrace();
                                                    this.raw[index] = null;
                                                    return null;
                                                } else {
                                                    throw $ex$;
                                                }
                                            }
                                        } else {
                                            return null;
                                        }
                                    } else {
                                        return null;
                                    }
                                }

                                public getInferSize(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number {
                                    var previousObj: any = this.raw[index];
                                    if (previousObj != null) {
                                        return (<number[]>previousObj).length;
                                    }
                                    return 0;
                                }

                                public getInferElem(index: number, arrayIndex: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number {
                                     var res = this.raw[index];
                                     if(res != null && res != undefined){ return res[arrayIndex]; }
                                     return 0;
                                }

                                public setInferElem(index: number, arrayIndex: number, valueToInsert: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void {
                                     var res = this.raw[index];
                                     if(res != null && res != undefined){ res[arrayIndex] = valueToInsert; }
                                     this._dirty = true;
                                }

                                public extendInfer(index: number, newSize: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void {
                                    if (this.raw != null) {
                                        var previous: number[] = <number[]>this.raw[index];
                                        if (previous == null) {
                                            previous = new Array();
                                        } else {
                                            var incArray: number[] = new Array();
                                            System.arraycopy(previous, 0, incArray, 0, previous.length);
                                            previous = incArray;
                                        }
                                        this.raw[index] = previous;
                                        if (this._modifiedIndexes == null) {
                                            this._modifiedIndexes = new Array();
                                        }
                                        this._modifiedIndexes[index] = true;
                                        this._dirty = true;
                                    }
                                }

                                public set(index: number, content: any, p_metaClass: org.kevoree.modeling.meta.KMetaClass): void {
                                    this.raw[index] = content;
                                    this._dirty = true;
                                    if (this._modifiedIndexes == null) {
                                        this._modifiedIndexes = new Array();
                                    }
                                    this._modifiedIndexes[index] = true;
                                }

                                public clone(p_metaClass: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.memory.struct.segment.KMemorySegment {
                                    if (this.raw == null) {
                                        return new org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment();
                                    } else {
                                        var cloned: any[] = new Array();
                                        System.arraycopy(this.raw, 0, cloned, 0, this.raw.length);
                                        var clonedEntry: org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment = new org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment();
                                        clonedEntry._dirty = true;
                                        clonedEntry.raw = cloned;
                                        clonedEntry._metaClassIndex = this._metaClassIndex;
                                        return clonedEntry;
                                    }
                                }

                            }

                        }
                    }
                    export module tree {
                        export interface KLongLongTree extends org.kevoree.modeling.memory.struct.tree.KTree {

                            insert(key: number, value: number): void;

                            previousOrEqualValue(key: number): number;

                            lookupValue(key: number): number;

                        }

                        export interface KLongTree extends org.kevoree.modeling.memory.struct.tree.KTree {

                            insert(key: number): void;

                            previousOrEqual(key: number): number;

                            lookup(key: number): number;

                            range(startKey: number, endKey: number, walker: (p : number) => void): void;

                            delete(key: number): void;

                        }

                        export interface KTree extends org.kevoree.modeling.memory.KMemoryElement {

                            size(): number;

                        }

                        export interface KTreeWalker {

                            elem(t: number): void;

                        }

                        export module impl {
                            export class LongLongTree implements org.kevoree.modeling.memory.KMemoryElement, org.kevoree.modeling.memory.struct.tree.KLongLongTree {

                                private root: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = null;
                                private _size: number = 0;
                                public _dirty: boolean = false;
                                private _counter: number = 0;
                                private _previousOrEqualsCacheValues: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode[] = null;
                                private _previousOrEqualsNextCacheElem: number;
                                private _lookupCacheValues: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode[] = null;
                                private _lookupNextCacheElem: number;
                                public size(): number {
                                    return this._size;
                                }

                                public counter(): number {
                                    return this._counter;
                                }

                                public inc(): void {
                                    this._counter++;
                                }

                                public dec(): void {
                                    this._counter--;
                                }

                                public free(metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                                }

                                public toString(): string {
                                    return this.serialize(null);
                                }

                                public isDirty(): boolean {
                                    return this._dirty;
                                }

                                public setDirty(): void {
                                    this._dirty = true;
                                }

                                public serialize(metaModel: org.kevoree.modeling.meta.KMetaModel): string {
                                    var builder: java.lang.StringBuilder = new java.lang.StringBuilder();
                                    builder.append(this._size);
                                    if (this.root != null) {
                                        this.root.serialize(builder);
                                    }
                                    return builder.toString();
                                }

                                constructor() {
                                    this._lookupCacheValues = new Array();
                                    this._previousOrEqualsCacheValues = new Array();
                                    this._previousOrEqualsNextCacheElem = 0;
                                    this._lookupNextCacheElem = 0;
                                }

                                private tryPreviousOrEqualsCache(key: number): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    if (this._previousOrEqualsCacheValues != null) {
                                        for (var i: number = 0; i < this._previousOrEqualsNextCacheElem; i++) {
                                            if (this._previousOrEqualsCacheValues[i] != null && key == this._previousOrEqualsCacheValues[i].key) {
                                                return this._previousOrEqualsCacheValues[i];
                                            }
                                        }
                                    }
                                    return null;
                                }

                                private tryLookupCache(key: number): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    if (this._lookupCacheValues != null) {
                                        for (var i: number = 0; i < this._lookupNextCacheElem; i++) {
                                            if (this._lookupCacheValues[i] != null && key == this._lookupCacheValues[i].key) {
                                                return this._lookupCacheValues[i];
                                            }
                                        }
                                    }
                                    return null;
                                }

                                private resetCache(): void {
                                    this._previousOrEqualsNextCacheElem = 0;
                                    this._lookupNextCacheElem = 0;
                                }

                                private putInPreviousOrEqualsCache(resolved: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    if (this._previousOrEqualsNextCacheElem == org.kevoree.modeling.KConfig.TREE_CACHE_SIZE) {
                                        this._previousOrEqualsNextCacheElem = 0;
                                    }
                                    this._previousOrEqualsCacheValues[this._previousOrEqualsNextCacheElem] = resolved;
                                    this._previousOrEqualsNextCacheElem++;
                                }

                                private putInLookupCache(resolved: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    if (this._lookupNextCacheElem == org.kevoree.modeling.KConfig.TREE_CACHE_SIZE) {
                                        this._lookupNextCacheElem = 0;
                                    }
                                    this._lookupCacheValues[this._lookupNextCacheElem] = resolved;
                                    this._lookupNextCacheElem++;
                                }

                                public setClean(metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                                    this._dirty = false;
                                }

                                public init(payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                                    if (payload == null || payload.length == 0) {
                                        return;
                                    }
                                    var i: number = 0;
                                    var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
                                    var ch: string = payload.charAt(i);
                                    while (i < payload.length && ch != '|'){
                                        buffer.append(ch);
                                        i = i + 1;
                                        ch = payload.charAt(i);
                                    }
                                    this._size = java.lang.Integer.parseInt(buffer.toString());
                                    var ctx: org.kevoree.modeling.memory.struct.tree.impl.TreeReaderContext = new org.kevoree.modeling.memory.struct.tree.impl.TreeReaderContext();
                                    ctx.index = i;
                                    ctx.payload = payload;
                                    ctx.buffer = new Array();
                                    this.root = org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode.unserialize(ctx);
                                    this.resetCache();
                                }

                                public lookupValue(key: number): number {
                                    var result: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this.internal_lookup(key);
                                    if (result != null) {
                                        return result.value;
                                    } else {
                                        return org.kevoree.modeling.KConfig.NULL_LONG;
                                    }
                                }

                                private internal_lookup(key: number): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    var n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this.tryLookupCache(key);
                                    if (n != null) {
                                        return n;
                                    }
                                    n = this.root;
                                    if (n == null) {
                                        return null;
                                    }
                                    while (n != null){
                                        if (key == n.key) {
                                            this.putInLookupCache(n);
                                            return n;
                                        } else {
                                            if (key < n.key) {
                                                n = n.getLeft();
                                            } else {
                                                n = n.getRight();
                                            }
                                        }
                                    }
                                    this.putInLookupCache(null);
                                    return n;
                                }

                                public previousOrEqualValue(key: number): number {
                                    var result: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this.internal_previousOrEqual(key);
                                    if (result != null) {
                                        return result.value;
                                    } else {
                                        return org.kevoree.modeling.KConfig.NULL_LONG;
                                    }
                                }

                                private internal_previousOrEqual(key: number): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this.tryPreviousOrEqualsCache(key);
                                    if (p != null) {
                                        return p;
                                    }
                                    p = this.root;
                                    if (p == null) {
                                        return null;
                                    }
                                    while (p != null){
                                        if (key == p.key) {
                                            this.putInPreviousOrEqualsCache(p);
                                            return p;
                                        }
                                        if (key > p.key) {
                                            if (p.getRight() != null) {
                                                p = p.getRight();
                                            } else {
                                                this.putInPreviousOrEqualsCache(p);
                                                return p;
                                            }
                                        } else {
                                            if (p.getLeft() != null) {
                                                p = p.getLeft();
                                            } else {
                                                var parent: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = p.getParent();
                                                var ch: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = p;
                                                while (parent != null && ch == parent.getLeft()){
                                                    ch = parent;
                                                    parent = parent.getParent();
                                                }
                                                this.putInPreviousOrEqualsCache(parent);
                                                return parent;
                                            }
                                        }
                                    }
                                    return null;
                                }

                                public nextOrEqual(key: number): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this.root;
                                    if (p == null) {
                                        return null;
                                    }
                                    while (p != null){
                                        if (key == p.key) {
                                            return p;
                                        }
                                        if (key < p.key) {
                                            if (p.getLeft() != null) {
                                                p = p.getLeft();
                                            } else {
                                                return p;
                                            }
                                        } else {
                                            if (p.getRight() != null) {
                                                p = p.getRight();
                                            } else {
                                                var parent: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = p.getParent();
                                                var ch: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = p;
                                                while (parent != null && ch == parent.getRight()){
                                                    ch = parent;
                                                    parent = parent.getParent();
                                                }
                                                return parent;
                                            }
                                        }
                                    }
                                    return null;
                                }

                                public previous(key: number): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this.root;
                                    if (p == null) {
                                        return null;
                                    }
                                    while (p != null){
                                        if (key < p.key) {
                                            if (p.getLeft() != null) {
                                                p = p.getLeft();
                                            } else {
                                                return p.previous();
                                            }
                                        } else {
                                            if (key > p.key) {
                                                if (p.getRight() != null) {
                                                    p = p.getRight();
                                                } else {
                                                    return p;
                                                }
                                            } else {
                                                return p.previous();
                                            }
                                        }
                                    }
                                    return null;
                                }

                                public next(key: number): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this.root;
                                    if (p == null) {
                                        return null;
                                    }
                                    while (p != null){
                                        if (key < p.key) {
                                            if (p.getLeft() != null) {
                                                p = p.getLeft();
                                            } else {
                                                return p;
                                            }
                                        } else {
                                            if (key > p.key) {
                                                if (p.getRight() != null) {
                                                    p = p.getRight();
                                                } else {
                                                    return p.next();
                                                }
                                            } else {
                                                return p.next();
                                            }
                                        }
                                    }
                                    return null;
                                }

                                public first(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this.root;
                                    if (p == null) {
                                        return null;
                                    }
                                    while (p != null){
                                        if (p.getLeft() != null) {
                                            p = p.getLeft();
                                        } else {
                                            return p;
                                        }
                                    }
                                    return null;
                                }

                                public last(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this.root;
                                    if (p == null) {
                                        return null;
                                    }
                                    while (p != null){
                                        if (p.getRight() != null) {
                                            p = p.getRight();
                                        } else {
                                            return p;
                                        }
                                    }
                                    return null;
                                }

                                private rotateLeft(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    var r: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = n.getRight();
                                    this.replaceNode(n, r);
                                    n.setRight(r.getLeft());
                                    if (r.getLeft() != null) {
                                        r.getLeft().setParent(n);
                                    }
                                    r.setLeft(n);
                                    n.setParent(r);
                                }

                                private rotateRight(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    var l: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = n.getLeft();
                                    this.replaceNode(n, l);
                                    n.setLeft(l.getRight());
                                    if (l.getRight() != null) {
                                        l.getRight().setParent(n);
                                    }
                                    l.setRight(n);
                                    n.setParent(l);
                                }

                                private replaceNode(oldn: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode, newn: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    if (oldn.getParent() == null) {
                                        this.root = newn;
                                    } else {
                                        if (oldn == oldn.getParent().getLeft()) {
                                            oldn.getParent().setLeft(newn);
                                        } else {
                                            oldn.getParent().setRight(newn);
                                        }
                                    }
                                    if (newn != null) {
                                        newn.setParent(oldn.getParent());
                                    }
                                }

                                public insert(key: number, value: number): void {
                                    this.resetCache();
                                    this._dirty = true;
                                    var insertedNode: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = new org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode(key, value, false, null, null);
                                    if (this.root == null) {
                                        this._size++;
                                        this.root = insertedNode;
                                    } else {
                                        var n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this.root;
                                        while (true){
                                            if (key == n.key) {
                                                n.value = value;
                                                return;
                                            } else {
                                                if (key < n.key) {
                                                    if (n.getLeft() == null) {
                                                        n.setLeft(insertedNode);
                                                        this._size++;
                                                        break;
                                                    } else {
                                                        n = n.getLeft();
                                                    }
                                                } else {
                                                    if (n.getRight() == null) {
                                                        n.setRight(insertedNode);
                                                        this._size++;
                                                        break;
                                                    } else {
                                                        n = n.getRight();
                                                    }
                                                }
                                            }
                                        }
                                        insertedNode.setParent(n);
                                    }
                                    this.insertCase1(insertedNode);
                                }

                                private insertCase1(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    if (n.getParent() == null) {
                                        n.color = true;
                                    } else {
                                        this.insertCase2(n);
                                    }
                                }

                                private insertCase2(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    if (this.nodeColor(n.getParent()) == true) {
                                        return;
                                    } else {
                                        this.insertCase3(n);
                                    }
                                }

                                private insertCase3(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    if (this.nodeColor(n.uncle()) == false) {
                                        n.getParent().color = true;
                                        n.uncle().color = true;
                                        n.grandparent().color = false;
                                        this.insertCase1(n.grandparent());
                                    } else {
                                        this.insertCase4(n);
                                    }
                                }

                                private insertCase4(n_n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    var n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = n_n;
                                    if (n == n.getParent().getRight() && n.getParent() == n.grandparent().getLeft()) {
                                        this.rotateLeft(n.getParent());
                                        n = n.getLeft();
                                    } else {
                                        if (n == n.getParent().getLeft() && n.getParent() == n.grandparent().getRight()) {
                                            this.rotateRight(n.getParent());
                                            n = n.getRight();
                                        }
                                    }
                                    this.insertCase5(n);
                                }

                                private insertCase5(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    n.getParent().color = true;
                                    n.grandparent().color = false;
                                    if (n == n.getParent().getLeft() && n.getParent() == n.grandparent().getLeft()) {
                                        this.rotateRight(n.grandparent());
                                    } else {
                                        this.rotateLeft(n.grandparent());
                                    }
                                }

                                public delete(key: number): void {
                                    var n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this.internal_lookup(key);
                                    if (n == null) {
                                        return;
                                    } else {
                                        this._size--;
                                        if (n.getLeft() != null && n.getRight() != null) {
                                            var pred: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = n.getLeft();
                                            while (pred.getRight() != null){
                                                pred = pred.getRight();
                                            }
                                            n.key = pred.key;
                                            n.value = pred.value;
                                            n = pred;
                                        }
                                        var child: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                        if (n.getRight() == null) {
                                            child = n.getLeft();
                                        } else {
                                            child = n.getRight();
                                        }
                                        if (this.nodeColor(n) == true) {
                                            n.color = this.nodeColor(child);
                                            this.deleteCase1(n);
                                        }
                                        this.replaceNode(n, child);
                                    }
                                }

                                private deleteCase1(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    if (n.getParent() == null) {
                                        return;
                                    } else {
                                        this.deleteCase2(n);
                                    }
                                }

                                private deleteCase2(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    if (this.nodeColor(n.sibling()) == false) {
                                        n.getParent().color = false;
                                        n.sibling().color = true;
                                        if (n == n.getParent().getLeft()) {
                                            this.rotateLeft(n.getParent());
                                        } else {
                                            this.rotateRight(n.getParent());
                                        }
                                    }
                                    this.deleteCase3(n);
                                }

                                private deleteCase3(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    if (this.nodeColor(n.getParent()) == true && this.nodeColor(n.sibling()) == true && this.nodeColor(n.sibling().getLeft()) == true && this.nodeColor(n.sibling().getRight()) == true) {
                                        n.sibling().color = false;
                                        this.deleteCase1(n.getParent());
                                    } else {
                                        this.deleteCase4(n);
                                    }
                                }

                                private deleteCase4(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    if (this.nodeColor(n.getParent()) == false && this.nodeColor(n.sibling()) == true && this.nodeColor(n.sibling().getLeft()) == true && this.nodeColor(n.sibling().getRight()) == true) {
                                        n.sibling().color = false;
                                        n.getParent().color = true;
                                    } else {
                                        this.deleteCase5(n);
                                    }
                                }

                                private deleteCase5(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    if (n == n.getParent().getLeft() && this.nodeColor(n.sibling()) == true && this.nodeColor(n.sibling().getLeft()) == false && this.nodeColor(n.sibling().getRight()) == true) {
                                        n.sibling().color = false;
                                        n.sibling().getLeft().color = true;
                                        this.rotateRight(n.sibling());
                                    } else {
                                        if (n == n.getParent().getRight() && this.nodeColor(n.sibling()) == true && this.nodeColor(n.sibling().getRight()) == false && this.nodeColor(n.sibling().getLeft()) == true) {
                                            n.sibling().color = false;
                                            n.sibling().getRight().color = true;
                                            this.rotateLeft(n.sibling());
                                        }
                                    }
                                    this.deleteCase6(n);
                                }

                                private deleteCase6(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    n.sibling().color = this.nodeColor(n.getParent());
                                    n.getParent().color = true;
                                    if (n == n.getParent().getLeft()) {
                                        n.sibling().getRight().color = true;
                                        this.rotateLeft(n.getParent());
                                    } else {
                                        n.sibling().getLeft().color = true;
                                        this.rotateRight(n.getParent());
                                    }
                                }

                                private nodeColor(n: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): boolean {
                                    if (n == null) {
                                        return true;
                                    } else {
                                        return n.color;
                                    }
                                }

                            }

                            export class LongTree implements org.kevoree.modeling.memory.KMemoryElement, org.kevoree.modeling.memory.struct.tree.KLongTree {

                                private _size: number = 0;
                                private root: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = null;
                                private _previousOrEqualsCacheValues: org.kevoree.modeling.memory.struct.tree.impl.TreeNode[] = null;
                                private _nextCacheElem: number;
                                private _counter: number = 0;
                                private _dirty: boolean = false;
                                constructor() {
                                    this._previousOrEqualsCacheValues = new Array();
                                    this._nextCacheElem = 0;
                                }

                                public size(): number {
                                    return this._size;
                                }

                                public counter(): number {
                                    return this._counter;
                                }

                                public inc(): void {
                                    this._counter++;
                                }

                                public dec(): void {
                                    this._counter--;
                                }

                                public free(metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                                }

                                private tryPreviousOrEqualsCache(key: number): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    if (this._previousOrEqualsCacheValues != null) {
                                        for (var i: number = 0; i < this._nextCacheElem; i++) {
                                            if (this._previousOrEqualsCacheValues[i] != null && this._previousOrEqualsCacheValues[i].key == key) {
                                                return this._previousOrEqualsCacheValues[i];
                                            }
                                        }
                                        return null;
                                    } else {
                                        return null;
                                    }
                                }

                                private resetCache(): void {
                                    this._nextCacheElem = 0;
                                }

                                private putInPreviousOrEqualsCache(resolved: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    if (this._nextCacheElem == org.kevoree.modeling.KConfig.TREE_CACHE_SIZE) {
                                        this._nextCacheElem = 0;
                                    }
                                    this._previousOrEqualsCacheValues[this._nextCacheElem] = resolved;
                                    this._nextCacheElem++;
                                }

                                public isDirty(): boolean {
                                    return this._dirty;
                                }

                                public setClean(metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                                    this._dirty = false;
                                }

                                public setDirty(): void {
                                    this._dirty = true;
                                }

                                public serialize(metaModel: org.kevoree.modeling.meta.KMetaModel): string {
                                    var builder: java.lang.StringBuilder = new java.lang.StringBuilder();
                                    builder.append(this._size);
                                    if (this.root != null) {
                                        this.root.serialize(builder);
                                    }
                                    return builder.toString();
                                }

                                public toString(): string {
                                    return this.serialize(null);
                                }

                                public init(payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void {
                                    if (payload == null || payload.length == 0) {
                                        return;
                                    }
                                    var i: number = 0;
                                    var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
                                    var ch: string = payload.charAt(i);
                                    while (i < payload.length && ch != '|'){
                                        buffer.append(ch);
                                        i = i + 1;
                                        ch = payload.charAt(i);
                                    }
                                    this._size = java.lang.Integer.parseInt(buffer.toString());
                                    var ctx: org.kevoree.modeling.memory.struct.tree.impl.TreeReaderContext = new org.kevoree.modeling.memory.struct.tree.impl.TreeReaderContext();
                                    ctx.index = i;
                                    ctx.payload = payload;
                                    this.root = org.kevoree.modeling.memory.struct.tree.impl.TreeNode.unserialize(ctx);
                                    this.resetCache();
                                }

                                public previousOrEqual(key: number): number {
                                    var resolvedNode: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this.internal_previousOrEqual(key);
                                    if (resolvedNode != null) {
                                        return resolvedNode.key;
                                    }
                                    return org.kevoree.modeling.KConfig.NULL_LONG;
                                }

                                public internal_previousOrEqual(key: number): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    var cachedVal: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this.tryPreviousOrEqualsCache(key);
                                    if (cachedVal != null) {
                                        return cachedVal;
                                    }
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this.root;
                                    if (p == null) {
                                        return null;
                                    }
                                    while (p != null){
                                        if (key == p.key) {
                                            this.putInPreviousOrEqualsCache(p);
                                            return p;
                                        }
                                        if (key > p.key) {
                                            if (p.getRight() != null) {
                                                p = p.getRight();
                                            } else {
                                                this.putInPreviousOrEqualsCache(p);
                                                return p;
                                            }
                                        } else {
                                            if (p.getLeft() != null) {
                                                p = p.getLeft();
                                            } else {
                                                var parent: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = p.getParent();
                                                var ch: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = p;
                                                while (parent != null && ch == parent.getLeft()){
                                                    ch = parent;
                                                    parent = parent.getParent();
                                                }
                                                this.putInPreviousOrEqualsCache(parent);
                                                return parent;
                                            }
                                        }
                                    }
                                    return null;
                                }

                                public nextOrEqual(key: number): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this.root;
                                    if (p == null) {
                                        return null;
                                    }
                                    while (p != null){
                                        if (key == p.key) {
                                            return p;
                                        }
                                        if (key < p.key) {
                                            if (p.getLeft() != null) {
                                                p = p.getLeft();
                                            } else {
                                                return p;
                                            }
                                        } else {
                                            if (p.getRight() != null) {
                                                p = p.getRight();
                                            } else {
                                                var parent: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = p.getParent();
                                                var ch: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = p;
                                                while (parent != null && ch == parent.getRight()){
                                                    ch = parent;
                                                    parent = parent.getParent();
                                                }
                                                return parent;
                                            }
                                        }
                                    }
                                    return null;
                                }

                                public previous(key: number): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this.root;
                                    if (p == null) {
                                        return null;
                                    }
                                    while (p != null){
                                        if (key < p.key) {
                                            if (p.getLeft() != null) {
                                                p = p.getLeft();
                                            } else {
                                                return p.previous();
                                            }
                                        } else {
                                            if (key > p.key) {
                                                if (p.getRight() != null) {
                                                    p = p.getRight();
                                                } else {
                                                    return p;
                                                }
                                            } else {
                                                return p.previous();
                                            }
                                        }
                                    }
                                    return null;
                                }

                                public next(key: number): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this.root;
                                    if (p == null) {
                                        return null;
                                    }
                                    while (p != null){
                                        if (key < p.key) {
                                            if (p.getLeft() != null) {
                                                p = p.getLeft();
                                            } else {
                                                return p;
                                            }
                                        } else {
                                            if (key > p.key) {
                                                if (p.getRight() != null) {
                                                    p = p.getRight();
                                                } else {
                                                    return p.next();
                                                }
                                            } else {
                                                return p.next();
                                            }
                                        }
                                    }
                                    return null;
                                }

                                public first(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this.root;
                                    if (p == null) {
                                        return null;
                                    }
                                    while (p != null){
                                        if (p.getLeft() != null) {
                                            p = p.getLeft();
                                        } else {
                                            return p;
                                        }
                                    }
                                    return null;
                                }

                                public last(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this.root;
                                    if (p == null) {
                                        return null;
                                    }
                                    while (p != null){
                                        if (p.getRight() != null) {
                                            p = p.getRight();
                                        } else {
                                            return p;
                                        }
                                    }
                                    return null;
                                }

                                public lookup(key: number): number {
                                    var n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this.root;
                                    if (n == null) {
                                        return org.kevoree.modeling.KConfig.NULL_LONG;
                                    }
                                    while (n != null){
                                        if (key == n.key) {
                                            return n.key;
                                        } else {
                                            if (key < n.key) {
                                                n = n.getLeft();
                                            } else {
                                                n = n.getRight();
                                            }
                                        }
                                    }
                                    return org.kevoree.modeling.KConfig.NULL_LONG;
                                }

                                public range(start: number, end: number, walker: (p : number) => void): void {
                                    var it: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this.internal_previousOrEqual(end);
                                    while (it != null && it.key >= start){
                                        walker(it.key);
                                        it = it.previous();
                                    }
                                }

                                private rotateLeft(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    var r: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = n.getRight();
                                    this.replaceNode(n, r);
                                    n.setRight(r.getLeft());
                                    if (r.getLeft() != null) {
                                        r.getLeft().setParent(n);
                                    }
                                    r.setLeft(n);
                                    n.setParent(r);
                                }

                                private rotateRight(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    var l: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = n.getLeft();
                                    this.replaceNode(n, l);
                                    n.setLeft(l.getRight());
                                    if (l.getRight() != null) {
                                        l.getRight().setParent(n);
                                    }
                                    l.setRight(n);
                                    n.setParent(l);
                                }

                                private replaceNode(oldn: org.kevoree.modeling.memory.struct.tree.impl.TreeNode, newn: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    if (oldn.getParent() == null) {
                                        this.root = newn;
                                    } else {
                                        if (oldn == oldn.getParent().getLeft()) {
                                            oldn.getParent().setLeft(newn);
                                        } else {
                                            oldn.getParent().setRight(newn);
                                        }
                                    }
                                    if (newn != null) {
                                        newn.setParent(oldn.getParent());
                                    }
                                }

                                public insert(key: number): void {
                                    this._dirty = true;
                                    var insertedNode: org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                    if (this.root == null) {
                                        this._size++;
                                        insertedNode = new org.kevoree.modeling.memory.struct.tree.impl.TreeNode(key, false, null, null);
                                        this.root = insertedNode;
                                    } else {
                                        var n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this.root;
                                        while (true){
                                            if (key == n.key) {
                                                this.putInPreviousOrEqualsCache(n);
                                                return;
                                            } else {
                                                if (key < n.key) {
                                                    if (n.getLeft() == null) {
                                                        insertedNode = new org.kevoree.modeling.memory.struct.tree.impl.TreeNode(key, false, null, null);
                                                        n.setLeft(insertedNode);
                                                        this._size++;
                                                        break;
                                                    } else {
                                                        n = n.getLeft();
                                                    }
                                                } else {
                                                    if (n.getRight() == null) {
                                                        insertedNode = new org.kevoree.modeling.memory.struct.tree.impl.TreeNode(key, false, null, null);
                                                        n.setRight(insertedNode);
                                                        this._size++;
                                                        break;
                                                    } else {
                                                        n = n.getRight();
                                                    }
                                                }
                                            }
                                        }
                                        insertedNode.setParent(n);
                                    }
                                    this.insertCase1(insertedNode);
                                    this.putInPreviousOrEqualsCache(insertedNode);
                                }

                                private insertCase1(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    if (n.getParent() == null) {
                                        n.color = true;
                                    } else {
                                        this.insertCase2(n);
                                    }
                                }

                                private insertCase2(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    if (this.nodeColor(n.getParent()) == true) {
                                        return;
                                    } else {
                                        this.insertCase3(n);
                                    }
                                }

                                private insertCase3(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    if (this.nodeColor(n.uncle()) == false) {
                                        n.getParent().color = true;
                                        n.uncle().color = true;
                                        n.grandparent().color = false;
                                        this.insertCase1(n.grandparent());
                                    } else {
                                        this.insertCase4(n);
                                    }
                                }

                                private insertCase4(n_n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    var n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = n_n;
                                    if (n == n.getParent().getRight() && n.getParent() == n.grandparent().getLeft()) {
                                        this.rotateLeft(n.getParent());
                                        n = n.getLeft();
                                    } else {
                                        if (n == n.getParent().getLeft() && n.getParent() == n.grandparent().getRight()) {
                                            this.rotateRight(n.getParent());
                                            n = n.getRight();
                                        }
                                    }
                                    this.insertCase5(n);
                                }

                                private insertCase5(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    n.getParent().color = true;
                                    n.grandparent().color = false;
                                    if (n == n.getParent().getLeft() && n.getParent() == n.grandparent().getLeft()) {
                                        this.rotateRight(n.grandparent());
                                    } else {
                                        this.rotateLeft(n.grandparent());
                                    }
                                }

                                public delete(key: number): void {
                                    var n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = null;
                                    var nn: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this.root;
                                    while (nn != null){
                                        if (key == nn.key) {
                                            n = nn;
                                        } else {
                                            if (key < nn.key) {
                                                nn = nn.getLeft();
                                            } else {
                                                nn = nn.getRight();
                                            }
                                        }
                                    }
                                    if (n == null) {
                                        return;
                                    } else {
                                        this._size--;
                                        if (n.getLeft() != null && n.getRight() != null) {
                                            var pred: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = n.getLeft();
                                            while (pred.getRight() != null){
                                                pred = pred.getRight();
                                            }
                                            n.key = pred.key;
                                            n = pred;
                                        }
                                        var child: org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                        if (n.getRight() == null) {
                                            child = n.getLeft();
                                        } else {
                                            child = n.getRight();
                                        }
                                        if (this.nodeColor(n) == true) {
                                            n.color = this.nodeColor(child);
                                            this.deleteCase1(n);
                                        }
                                        this.replaceNode(n, child);
                                    }
                                }

                                private deleteCase1(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    if (n.getParent() == null) {
                                        return;
                                    } else {
                                        this.deleteCase2(n);
                                    }
                                }

                                private deleteCase2(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    if (this.nodeColor(n.sibling()) == false) {
                                        n.getParent().color = false;
                                        n.sibling().color = true;
                                        if (n == n.getParent().getLeft()) {
                                            this.rotateLeft(n.getParent());
                                        } else {
                                            this.rotateRight(n.getParent());
                                        }
                                    }
                                    this.deleteCase3(n);
                                }

                                private deleteCase3(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    if (this.nodeColor(n.getParent()) == true && this.nodeColor(n.sibling()) == true && this.nodeColor(n.sibling().getLeft()) == true && this.nodeColor(n.sibling().getRight()) == true) {
                                        n.sibling().color = false;
                                        this.deleteCase1(n.getParent());
                                    } else {
                                        this.deleteCase4(n);
                                    }
                                }

                                private deleteCase4(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    if (this.nodeColor(n.getParent()) == false && this.nodeColor(n.sibling()) == true && this.nodeColor(n.sibling().getLeft()) == true && this.nodeColor(n.sibling().getRight()) == true) {
                                        n.sibling().color = false;
                                        n.getParent().color = true;
                                    } else {
                                        this.deleteCase5(n);
                                    }
                                }

                                private deleteCase5(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    if (n == n.getParent().getLeft() && this.nodeColor(n.sibling()) == true && this.nodeColor(n.sibling().getLeft()) == false && this.nodeColor(n.sibling().getRight()) == true) {
                                        n.sibling().color = false;
                                        n.sibling().getLeft().color = true;
                                        this.rotateRight(n.sibling());
                                    } else {
                                        if (n == n.getParent().getRight() && this.nodeColor(n.sibling()) == true && this.nodeColor(n.sibling().getRight()) == false && this.nodeColor(n.sibling().getLeft()) == true) {
                                            n.sibling().color = false;
                                            n.sibling().getRight().color = true;
                                            this.rotateLeft(n.sibling());
                                        }
                                    }
                                    this.deleteCase6(n);
                                }

                                private deleteCase6(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    n.sibling().color = this.nodeColor(n.getParent());
                                    n.getParent().color = true;
                                    if (n == n.getParent().getLeft()) {
                                        n.sibling().getRight().color = true;
                                        this.rotateLeft(n.getParent());
                                    } else {
                                        n.sibling().getLeft().color = true;
                                        this.rotateRight(n.getParent());
                                    }
                                }

                                private nodeColor(n: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): boolean {
                                    if (n == null) {
                                        return true;
                                    } else {
                                        return n.color;
                                    }
                                }

                            }

                            export class LongTreeNode {

                                public static BLACK: string = '0';
                                public static RED: string = '2';
                                public key: number;
                                public value: number;
                                public color: boolean;
                                private left: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                private right: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                private parent: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = null;
                                constructor(key: number, value: number, color: boolean, left: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode, right: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode) {
                                    this.key = key;
                                    this.value = value;
                                    this.color = color;
                                    this.left = left;
                                    this.right = right;
                                    if (left != null) {
                                        left.parent = this;
                                    }
                                    if (right != null) {
                                        right.parent = this;
                                    }
                                    this.parent = null;
                                }

                                public grandparent(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    if (this.parent != null) {
                                        return this.parent.parent;
                                    } else {
                                        return null;
                                    }
                                }

                                public sibling(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    if (this.parent == null) {
                                        return null;
                                    } else {
                                        if (this == this.parent.left) {
                                            return this.parent.right;
                                        } else {
                                            return this.parent.left;
                                        }
                                    }
                                }

                                public uncle(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    if (this.parent != null) {
                                        return this.parent.sibling();
                                    } else {
                                        return null;
                                    }
                                }

                                public getLeft(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    return this.left;
                                }

                                public setLeft(left: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    this.left = left;
                                }

                                public getRight(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    return this.right;
                                }

                                public setRight(right: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    this.right = right;
                                }

                                public getParent(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    return this.parent;
                                }

                                public setParent(parent: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void {
                                    this.parent = parent;
                                }

                                public serialize(builder: java.lang.StringBuilder): void {
                                    builder.append("|");
                                    if (this.color == true) {
                                        builder.append(LongTreeNode.BLACK);
                                    } else {
                                        builder.append(LongTreeNode.RED);
                                    }
                                    builder.append(this.key);
                                    builder.append("@");
                                    builder.append(this.value);
                                    if (this.left == null && this.right == null) {
                                        builder.append("%");
                                    } else {
                                        if (this.left != null) {
                                            this.left.serialize(builder);
                                        } else {
                                            builder.append("#");
                                        }
                                        if (this.right != null) {
                                            this.right.serialize(builder);
                                        } else {
                                            builder.append("#");
                                        }
                                    }
                                }

                                public next(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this;
                                    if (p.right != null) {
                                        p = p.right;
                                        while (p.left != null){
                                            p = p.left;
                                        }
                                        return p;
                                    } else {
                                        if (p.parent != null) {
                                            if (p == p.parent.left) {
                                                return p.parent;
                                            } else {
                                                while (p.parent != null && p == p.parent.right){
                                                    p = p.parent;
                                                }
                                                return p.parent;
                                            }
                                        } else {
                                            return null;
                                        }
                                    }
                                }

                                public previous(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = this;
                                    if (p.left != null) {
                                        p = p.left;
                                        while (p.right != null){
                                            p = p.right;
                                        }
                                        return p;
                                    } else {
                                        if (p.parent != null) {
                                            if (p == p.parent.right) {
                                                return p.parent;
                                            } else {
                                                while (p.parent != null && p == p.parent.left){
                                                    p = p.parent;
                                                }
                                                return p.parent;
                                            }
                                        } else {
                                            return null;
                                        }
                                    }
                                }

                                public static unserialize(ctx: org.kevoree.modeling.memory.struct.tree.impl.TreeReaderContext): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    return org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode.internal_unserialize(true, ctx);
                                }

                                public static internal_unserialize(rightBranch: boolean, ctx: org.kevoree.modeling.memory.struct.tree.impl.TreeReaderContext): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode {
                                    if (ctx.index >= ctx.payload.length) {
                                        return null;
                                    }
                                    var ch: string = ctx.payload.charAt(ctx.index);
                                    if (ch == '%') {
                                        if (rightBranch) {
                                            ctx.index = ctx.index + 1;
                                        }
                                        return null;
                                    }
                                    if (ch == '#') {
                                        ctx.index = ctx.index + 1;
                                        return null;
                                    }
                                    if (ch != '|') {
                                        throw new java.lang.Exception("Error while loading BTree");
                                    }
                                    ctx.index = ctx.index + 1;
                                    ch = ctx.payload.charAt(ctx.index);
                                    var colorLoaded: boolean = true;
                                    if (ch == LongTreeNode.RED) {
                                        colorLoaded = false;
                                    }
                                    ctx.index = ctx.index + 1;
                                    ch = ctx.payload.charAt(ctx.index);
                                    var i: number = 0;
                                    while (ctx.index + 1 < ctx.payload.length && ch != '|' && ch != '#' && ch != '%' && ch != '@'){
                                        ctx.buffer[i] = ch;
                                        i++;
                                        ctx.index = ctx.index + 1;
                                        ch = ctx.payload.charAt(ctx.index);
                                    }
                                    if (ch != '|' && ch != '#' && ch != '%' && ch != '@') {
                                        ctx.buffer[i] = ch;
                                        i++;
                                    }
                                    var key: number = java.lang.Long.parseLong(StringUtils.copyValueOf(ctx.buffer, 0, i));
                                    i = 0;
                                    ctx.index = ctx.index + 1;
                                    ch = ctx.payload.charAt(ctx.index);
                                    while (ctx.index + 1 < ctx.payload.length && ch != '|' && ch != '#' && ch != '%' && ch != '@'){
                                        ctx.buffer[i] = ch;
                                        i++;
                                        ctx.index = ctx.index + 1;
                                        ch = ctx.payload.charAt(ctx.index);
                                    }
                                    if (ch != '|' && ch != '#' && ch != '%' && ch != '@') {
                                        ctx.buffer[i] = ch;
                                        i++;
                                    }
                                    var value: number = java.lang.Long.parseLong(StringUtils.copyValueOf(ctx.buffer, 0, i));
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = new org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode(key, value, colorLoaded, null, null);
                                    var left: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode.internal_unserialize(false, ctx);
                                    if (left != null) {
                                        left.setParent(p);
                                    }
                                    var right: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode = org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode.internal_unserialize(true, ctx);
                                    if (right != null) {
                                        right.setParent(p);
                                    }
                                    p.setLeft(left);
                                    p.setRight(right);
                                    return p;
                                }

                            }

                            export class TreeNode {

                                public static BLACK: string = '0';
                                public static RED: string = '1';
                                public key: number;
                                public color: boolean;
                                private left: org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                private right: org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                private parent: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = null;
                                constructor(key: number, color: boolean, left: org.kevoree.modeling.memory.struct.tree.impl.TreeNode, right: org.kevoree.modeling.memory.struct.tree.impl.TreeNode) {
                                    this.key = key;
                                    this.color = color;
                                    this.left = left;
                                    this.right = right;
                                    if (left != null) {
                                        left.parent = this;
                                    }
                                    if (right != null) {
                                        right.parent = this;
                                    }
                                    this.parent = null;
                                }

                                public getKey(): number {
                                    return this.key;
                                }

                                public grandparent(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    if (this.parent != null) {
                                        return this.parent.parent;
                                    } else {
                                        return null;
                                    }
                                }

                                public sibling(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    if (this.parent == null) {
                                        return null;
                                    } else {
                                        if (this == this.parent.left) {
                                            return this.parent.right;
                                        } else {
                                            return this.parent.left;
                                        }
                                    }
                                }

                                public uncle(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    if (this.parent != null) {
                                        return this.parent.sibling();
                                    } else {
                                        return null;
                                    }
                                }

                                public getLeft(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    return this.left;
                                }

                                public setLeft(left: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    this.left = left;
                                }

                                public getRight(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    return this.right;
                                }

                                public setRight(right: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    this.right = right;
                                }

                                public getParent(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    return this.parent;
                                }

                                public setParent(parent: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void {
                                    this.parent = parent;
                                }

                                public serialize(builder: java.lang.StringBuilder): void {
                                    builder.append("|");
                                    if (this.color == true) {
                                        builder.append(TreeNode.BLACK);
                                    } else {
                                        builder.append(TreeNode.RED);
                                    }
                                    builder.append(this.key);
                                    if (this.left == null && this.right == null) {
                                        builder.append("%");
                                    } else {
                                        if (this.left != null) {
                                            this.left.serialize(builder);
                                        } else {
                                            builder.append("#");
                                        }
                                        if (this.right != null) {
                                            this.right.serialize(builder);
                                        } else {
                                            builder.append("#");
                                        }
                                    }
                                }

                                public next(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this;
                                    if (p.right != null) {
                                        p = p.right;
                                        while (p.left != null){
                                            p = p.left;
                                        }
                                        return p;
                                    } else {
                                        if (p.parent != null) {
                                            if (p == p.parent.left) {
                                                return p.parent;
                                            } else {
                                                while (p.parent != null && p == p.parent.right){
                                                    p = p.parent;
                                                }
                                                return p.parent;
                                            }
                                        } else {
                                            return null;
                                        }
                                    }
                                }

                                public previous(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = this;
                                    if (p.left != null) {
                                        p = p.left;
                                        while (p.right != null){
                                            p = p.right;
                                        }
                                        return p;
                                    } else {
                                        if (p.parent != null) {
                                            if (p == p.parent.right) {
                                                return p.parent;
                                            } else {
                                                while (p.parent != null && p == p.parent.left){
                                                    p = p.parent;
                                                }
                                                return p.parent;
                                            }
                                        } else {
                                            return null;
                                        }
                                    }
                                }

                                public static unserialize(ctx: org.kevoree.modeling.memory.struct.tree.impl.TreeReaderContext): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    return org.kevoree.modeling.memory.struct.tree.impl.TreeNode.internal_unserialize(true, ctx);
                                }

                                public static internal_unserialize(rightBranch: boolean, ctx: org.kevoree.modeling.memory.struct.tree.impl.TreeReaderContext): org.kevoree.modeling.memory.struct.tree.impl.TreeNode {
                                    if (ctx.index >= ctx.payload.length) {
                                        return null;
                                    }
                                    var tokenBuild: java.lang.StringBuilder = new java.lang.StringBuilder();
                                    var ch: string = ctx.payload.charAt(ctx.index);
                                    if (ch == '%') {
                                        if (rightBranch) {
                                            ctx.index = ctx.index + 1;
                                        }
                                        return null;
                                    }
                                    if (ch == '#') {
                                        ctx.index = ctx.index + 1;
                                        return null;
                                    }
                                    if (ch != '|') {
                                        throw new java.lang.Exception("Error while loading BTree");
                                    }
                                    ctx.index = ctx.index + 1;
                                    ch = ctx.payload.charAt(ctx.index);
                                    var colorLoaded: boolean;
                                    if (ch == org.kevoree.modeling.memory.struct.tree.impl.TreeNode.BLACK) {
                                        colorLoaded = true;
                                    } else {
                                        colorLoaded = false;
                                    }
                                    ctx.index = ctx.index + 1;
                                    ch = ctx.payload.charAt(ctx.index);
                                    while (ctx.index + 1 < ctx.payload.length && ch != '|' && ch != '#' && ch != '%'){
                                        tokenBuild.append(ch);
                                        ctx.index = ctx.index + 1;
                                        ch = ctx.payload.charAt(ctx.index);
                                    }
                                    if (ch != '|' && ch != '#' && ch != '%') {
                                        tokenBuild.append(ch);
                                    }
                                    var p: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = new org.kevoree.modeling.memory.struct.tree.impl.TreeNode(java.lang.Long.parseLong(tokenBuild.toString()), colorLoaded, null, null);
                                    var left: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = org.kevoree.modeling.memory.struct.tree.impl.TreeNode.internal_unserialize(false, ctx);
                                    if (left != null) {
                                        left.setParent(p);
                                    }
                                    var right: org.kevoree.modeling.memory.struct.tree.impl.TreeNode = org.kevoree.modeling.memory.struct.tree.impl.TreeNode.internal_unserialize(true, ctx);
                                    if (right != null) {
                                        right.setParent(p);
                                    }
                                    p.setLeft(left);
                                    p.setRight(right);
                                    return p;
                                }

                            }

                            export class TreeReaderContext {

                                public payload: string;
                                public index: number;
                                public buffer: string[];
                            }

                        }
                    }
                }
            }
            export module message {
                export interface KMessage {

                    json(): string;

                    type(): number;

                }

                export class KMessageLoader {

                    public static TYPE_NAME: string = "type";
                    public static OPERATION_NAME: string = "op";
                    public static KEY_NAME: string = "key";
                    public static KEYS_NAME: string = "keys";
                    public static ID_NAME: string = "id";
                    public static VALUE_NAME: string = "value";
                    public static VALUES_NAME: string = "values";
                    public static CLASS_IDX_NAME: string = "class";
                    public static PARAMETERS_NAME: string = "params";
                    public static EVENTS_TYPE: number = 0;
                    public static GET_REQ_TYPE: number = 1;
                    public static GET_RES_TYPE: number = 2;
                    public static PUT_REQ_TYPE: number = 3;
                    public static PUT_RES_TYPE: number = 4;
                    public static OPERATION_CALL_TYPE: number = 5;
                    public static OPERATION_RESULT_TYPE: number = 6;
                    public static ATOMIC_GET_INC_REQUEST_TYPE: number = 7;
                    public static ATOMIC_GET_INC_RESULT_TYPE: number = 8;
                    public static load(payload: string): org.kevoree.modeling.message.KMessage {
                        if (payload == null) {
                            return null;
                        }
                        var objectReader: org.kevoree.modeling.format.json.JsonObjectReader = new org.kevoree.modeling.format.json.JsonObjectReader();
                        objectReader.parseObject(payload);
                        try {
                            var parsedType: number = java.lang.Integer.parseInt(objectReader.get(KMessageLoader.TYPE_NAME).toString());
                            if (parsedType == KMessageLoader.EVENTS_TYPE) {
                                var eventsMessage: org.kevoree.modeling.message.impl.Events = null;
                                if (objectReader.get(KMessageLoader.KEYS_NAME) != null) {
                                    var objIdsRaw: string[] = objectReader.getAsStringArray(KMessageLoader.KEYS_NAME);
                                    eventsMessage = new org.kevoree.modeling.message.impl.Events(objIdsRaw.length);
                                    var keys: org.kevoree.modeling.KContentKey[] = new Array();
                                    for (var i: number = 0; i < objIdsRaw.length; i++) {
                                        try {
                                            keys[i] = org.kevoree.modeling.KContentKey.create(objIdsRaw[i]);
                                        } catch ($ex$) {
                                            if ($ex$ instanceof java.lang.Exception) {
                                                var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                e.printStackTrace();
                                            } else {
                                                throw $ex$;
                                            }
                                        }
                                    }
                                    eventsMessage._objIds = keys;
                                    if (objectReader.get(KMessageLoader.VALUES_NAME) != null) {
                                        var metaInt: string[] = objectReader.getAsStringArray(KMessageLoader.VALUES_NAME);
                                        var metaIndexes: number[][] = new Array();
                                        for (var i: number = 0; i < metaInt.length; i++) {
                                            try {
                                                if (metaInt[i] != null) {
                                                    var splitted: string[] = metaInt[i].split("%");
                                                    var newMeta: number[] = new Array();
                                                    for (var h: number = 0; h < splitted.length; h++) {
                                                        if (splitted[h] != null && !splitted[h].isEmpty()) {
                                                            newMeta[h] = java.lang.Integer.parseInt(splitted[h]);
                                                        }
                                                    }
                                                    metaIndexes[i] = newMeta;
                                                }
                                            } catch ($ex$) {
                                                if ($ex$ instanceof java.lang.Exception) {
                                                    var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                    e.printStackTrace();
                                                } else {
                                                    throw $ex$;
                                                }
                                            }
                                        }
                                        eventsMessage._metaindexes = metaIndexes;
                                    }
                                }
                                return eventsMessage;
                            } else {
                                if (parsedType == KMessageLoader.GET_REQ_TYPE) {
                                    var getKeysRequest: org.kevoree.modeling.message.impl.GetRequest = new org.kevoree.modeling.message.impl.GetRequest();
                                    if (objectReader.get(KMessageLoader.ID_NAME) != null) {
                                        getKeysRequest.id = java.lang.Long.parseLong(objectReader.get(KMessageLoader.ID_NAME).toString());
                                    }
                                    if (objectReader.get(KMessageLoader.KEYS_NAME) != null) {
                                        var metaInt: string[] = objectReader.getAsStringArray(KMessageLoader.KEYS_NAME);
                                        var keys: org.kevoree.modeling.KContentKey[] = new Array();
                                        for (var i: number = 0; i < metaInt.length; i++) {
                                            keys[i] = org.kevoree.modeling.KContentKey.create(metaInt[i]);
                                        }
                                        getKeysRequest.keys = keys;
                                    }
                                    return getKeysRequest;
                                } else {
                                    if (parsedType == KMessageLoader.GET_RES_TYPE) {
                                        var getResult: org.kevoree.modeling.message.impl.GetResult = new org.kevoree.modeling.message.impl.GetResult();
                                        if (objectReader.get(KMessageLoader.ID_NAME) != null) {
                                            getResult.id = java.lang.Long.parseLong(objectReader.get(KMessageLoader.ID_NAME).toString());
                                        }
                                        if (objectReader.get(KMessageLoader.VALUES_NAME) != null) {
                                            var metaInt: string[] = objectReader.getAsStringArray(KMessageLoader.VALUES_NAME);
                                            var values: string[] = new Array();
                                            for (var i: number = 0; i < metaInt.length; i++) {
                                                values[i] = org.kevoree.modeling.format.json.JsonString.unescape(metaInt[i]);
                                            }
                                            getResult.values = values;
                                        }
                                        return getResult;
                                    } else {
                                        if (parsedType == KMessageLoader.PUT_REQ_TYPE) {
                                            var putRequest: org.kevoree.modeling.message.impl.PutRequest = new org.kevoree.modeling.message.impl.PutRequest();
                                            if (objectReader.get(KMessageLoader.ID_NAME) != null) {
                                                putRequest.id = java.lang.Long.parseLong(objectReader.get(KMessageLoader.ID_NAME).toString());
                                            }
                                            var toFlatKeys: string[] = null;
                                            var toFlatValues: string[] = null;
                                            if (objectReader.get(KMessageLoader.KEYS_NAME) != null) {
                                                toFlatKeys = objectReader.getAsStringArray(KMessageLoader.KEYS_NAME);
                                            }
                                            if (objectReader.get(KMessageLoader.VALUES_NAME) != null) {
                                                toFlatValues = objectReader.getAsStringArray(KMessageLoader.VALUES_NAME);
                                            }
                                            if (toFlatKeys != null && toFlatValues != null && toFlatKeys.length == toFlatValues.length) {
                                                if (putRequest.request == null) {
                                                    putRequest.request = new org.kevoree.modeling.cdn.impl.ContentPutRequest(toFlatKeys.length);
                                                }
                                                for (var i: number = 0; i < toFlatKeys.length; i++) {
                                                    putRequest.request.put(org.kevoree.modeling.KContentKey.create(toFlatKeys[i]), org.kevoree.modeling.format.json.JsonString.unescape(toFlatValues[i]));
                                                }
                                            }
                                            return putRequest;
                                        } else {
                                            if (parsedType == KMessageLoader.PUT_RES_TYPE) {
                                                var putResult: org.kevoree.modeling.message.impl.PutResult = new org.kevoree.modeling.message.impl.PutResult();
                                                if (objectReader.get(KMessageLoader.ID_NAME) != null) {
                                                    putResult.id = java.lang.Long.parseLong(objectReader.get(KMessageLoader.ID_NAME).toString());
                                                }
                                                return putResult;
                                            } else {
                                                if (parsedType == KMessageLoader.OPERATION_CALL_TYPE) {
                                                    var callMessage: org.kevoree.modeling.message.impl.OperationCallMessage = new org.kevoree.modeling.message.impl.OperationCallMessage();
                                                    if (objectReader.get(KMessageLoader.ID_NAME) != null) {
                                                        callMessage.id = java.lang.Long.parseLong(objectReader.get(KMessageLoader.ID_NAME).toString());
                                                    }
                                                    if (objectReader.get(KMessageLoader.KEY_NAME) != null) {
                                                        callMessage.key = org.kevoree.modeling.KContentKey.create(objectReader.get(KMessageLoader.KEY_NAME).toString());
                                                    }
                                                    if (objectReader.get(KMessageLoader.CLASS_IDX_NAME) != null) {
                                                        callMessage.classIndex = java.lang.Integer.parseInt(objectReader.get(KMessageLoader.CLASS_IDX_NAME).toString());
                                                    }
                                                    if (objectReader.get(KMessageLoader.OPERATION_NAME) != null) {
                                                        callMessage.opIndex = java.lang.Integer.parseInt(objectReader.get(KMessageLoader.OPERATION_NAME).toString());
                                                    }
                                                    if (objectReader.get(KMessageLoader.PARAMETERS_NAME) != null) {
                                                        var params: string[] = objectReader.getAsStringArray(KMessageLoader.PARAMETERS_NAME);
                                                        var toFlat: string[] = new Array();
                                                        for (var i: number = 0; i < params.length; i++) {
                                                            toFlat[i] = org.kevoree.modeling.format.json.JsonString.unescape(params[i]);
                                                        }
                                                        callMessage.params = toFlat;
                                                    }
                                                    return callMessage;
                                                } else {
                                                    if (parsedType == KMessageLoader.OPERATION_RESULT_TYPE) {
                                                        var resultMessage: org.kevoree.modeling.message.impl.OperationResultMessage = new org.kevoree.modeling.message.impl.OperationResultMessage();
                                                        if (objectReader.get(KMessageLoader.ID_NAME) != null) {
                                                            resultMessage.id = java.lang.Long.parseLong(objectReader.get(KMessageLoader.ID_NAME).toString());
                                                        }
                                                        if (objectReader.get(KMessageLoader.KEY_NAME) != null) {
                                                            resultMessage.key = org.kevoree.modeling.KContentKey.create(objectReader.get(KMessageLoader.KEY_NAME).toString());
                                                        }
                                                        if (objectReader.get(KMessageLoader.VALUE_NAME) != null) {
                                                            resultMessage.value = objectReader.get(KMessageLoader.VALUE_NAME).toString();
                                                        }
                                                        return resultMessage;
                                                    } else {
                                                        if (parsedType == KMessageLoader.ATOMIC_GET_INC_REQUEST_TYPE) {
                                                            var atomicGetMessage: org.kevoree.modeling.message.impl.AtomicGetIncrementRequest = new org.kevoree.modeling.message.impl.AtomicGetIncrementRequest();
                                                            if (objectReader.get(KMessageLoader.ID_NAME) != null) {
                                                                atomicGetMessage.id = java.lang.Long.parseLong(objectReader.get(KMessageLoader.ID_NAME).toString());
                                                            }
                                                            if (objectReader.get(KMessageLoader.KEY_NAME) != null) {
                                                                atomicGetMessage.key = org.kevoree.modeling.KContentKey.create(objectReader.get(KMessageLoader.KEY_NAME).toString());
                                                            }
                                                            return atomicGetMessage;
                                                        } else {
                                                            if (parsedType == KMessageLoader.ATOMIC_GET_INC_RESULT_TYPE) {
                                                                var atomicGetResultMessage: org.kevoree.modeling.message.impl.AtomicGetIncrementResult = new org.kevoree.modeling.message.impl.AtomicGetIncrementResult();
                                                                if (objectReader.get(KMessageLoader.ID_NAME) != null) {
                                                                    atomicGetResultMessage.id = java.lang.Long.parseLong(objectReader.get(KMessageLoader.ID_NAME).toString());
                                                                }
                                                                if (objectReader.get(KMessageLoader.VALUE_NAME) != null) {
                                                                    try {
                                                                        atomicGetResultMessage.value = java.lang.Short.parseShort(objectReader.get(KMessageLoader.VALUE_NAME).toString());
                                                                    } catch ($ex$) {
                                                                        if ($ex$ instanceof java.lang.Exception) {
                                                                            var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                                            e.printStackTrace();
                                                                        } else {
                                                                            throw $ex$;
                                                                        }
                                                                    }
                                                                }
                                                                return atomicGetResultMessage;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            return null;
                        } catch ($ex$) {
                            if ($ex$ instanceof java.lang.Exception) {
                                var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                e.printStackTrace();
                                return null;
                            } else {
                                throw $ex$;
                            }
                        }
                    }

                }

                export module impl {
                    export class AtomicGetIncrementRequest implements org.kevoree.modeling.message.KMessage {

                        public id: number;
                        public key: org.kevoree.modeling.KContentKey;
                        public json(): string {
                            var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonStart(buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printType(buffer, this.type());
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.id, org.kevoree.modeling.message.KMessageLoader.ID_NAME, buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.key, org.kevoree.modeling.message.KMessageLoader.KEY_NAME, buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonEnd(buffer);
                            return buffer.toString();
                        }

                        public type(): number {
                            return org.kevoree.modeling.message.KMessageLoader.ATOMIC_GET_INC_REQUEST_TYPE;
                        }

                    }

                    export class AtomicGetIncrementResult implements org.kevoree.modeling.message.KMessage {

                        public id: number;
                        public value: number;
                        public json(): string {
                            var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonStart(buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printType(buffer, this.type());
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.id, org.kevoree.modeling.message.KMessageLoader.ID_NAME, buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.value, org.kevoree.modeling.message.KMessageLoader.VALUE_NAME, buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonEnd(buffer);
                            return buffer.toString();
                        }

                        public type(): number {
                            return org.kevoree.modeling.message.KMessageLoader.ATOMIC_GET_INC_RESULT_TYPE;
                        }

                    }

                    export class Events implements org.kevoree.modeling.message.KMessage {

                        public _objIds: org.kevoree.modeling.KContentKey[];
                        public _metaindexes: number[][];
                        private _size: number;
                        public allKeys(): org.kevoree.modeling.KContentKey[] {
                            return this._objIds;
                        }

                        constructor(nbObject: number) {
                            this._objIds = new Array();
                            this._metaindexes = new Array();
                            this._size = nbObject;
                        }

                        public json(): string {
                            var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonStart(buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printType(buffer, this.type());
                            buffer.append(",");
                            buffer.append("\"");
                            buffer.append(org.kevoree.modeling.message.KMessageLoader.KEYS_NAME).append("\":[");
                            for (var i: number = 0; i < this._objIds.length; i++) {
                                if (i != 0) {
                                    buffer.append(",");
                                }
                                buffer.append("\"");
                                buffer.append(this._objIds[i]);
                                buffer.append("\"");
                            }
                            buffer.append("]\n");
                            if (this._metaindexes != null) {
                                buffer.append(",");
                                buffer.append("\"");
                                buffer.append(org.kevoree.modeling.message.KMessageLoader.VALUES_NAME).append("\":[");
                                for (var i: number = 0; i < this._metaindexes.length; i++) {
                                    if (i != 0) {
                                        buffer.append(",");
                                    }
                                    buffer.append("\"");
                                    var metaModified: number[] = this._metaindexes[i];
                                    if (metaModified != null) {
                                        for (var j: number = 0; j < metaModified.length; j++) {
                                            if (j != 0) {
                                                buffer.append("%");
                                            }
                                            buffer.append(metaModified[j]);
                                        }
                                    }
                                    buffer.append("\"");
                                }
                                buffer.append("]\n");
                            }
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonEnd(buffer);
                            return buffer.toString();
                        }

                        public type(): number {
                            return org.kevoree.modeling.message.KMessageLoader.EVENTS_TYPE;
                        }

                        public size(): number {
                            return this._size;
                        }

                        public setEvent(index: number, p_objId: org.kevoree.modeling.KContentKey, p_metaIndexes: number[]): void {
                            this._objIds[index] = p_objId;
                            this._metaindexes[index] = p_metaIndexes;
                        }

                        public getKey(index: number): org.kevoree.modeling.KContentKey {
                            return this._objIds[index];
                        }

                        public getIndexes(index: number): number[] {
                            return this._metaindexes[index];
                        }

                    }

                    export class GetRequest implements org.kevoree.modeling.message.KMessage {

                        public id: number;
                        public keys: org.kevoree.modeling.KContentKey[];
                        public json(): string {
                            var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonStart(buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printType(buffer, this.type());
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.id, org.kevoree.modeling.message.KMessageLoader.ID_NAME, buffer);
                            if (this.keys != null) {
                                buffer.append(",");
                                buffer.append("\"");
                                buffer.append(org.kevoree.modeling.message.KMessageLoader.KEYS_NAME).append("\":[");
                                for (var i: number = 0; i < this.keys.length; i++) {
                                    if (i != 0) {
                                        buffer.append(",");
                                    }
                                    buffer.append("\"");
                                    buffer.append(this.keys[i].toString());
                                    buffer.append("\"");
                                }
                                buffer.append("]\n");
                            }
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonEnd(buffer);
                            return buffer.toString();
                        }

                        public type(): number {
                            return org.kevoree.modeling.message.KMessageLoader.GET_REQ_TYPE;
                        }

                    }

                    export class GetResult implements org.kevoree.modeling.message.KMessage {

                        public id: number;
                        public values: string[];
                        public json(): string {
                            var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonStart(buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printType(buffer, this.type());
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.id, org.kevoree.modeling.message.KMessageLoader.ID_NAME, buffer);
                            if (this.values != null) {
                                buffer.append(",");
                                buffer.append("\"");
                                buffer.append(org.kevoree.modeling.message.KMessageLoader.VALUES_NAME).append("\":[");
                                for (var i: number = 0; i < this.values.length; i++) {
                                    if (i != 0) {
                                        buffer.append(",");
                                    }
                                    buffer.append("\"");
                                    buffer.append(org.kevoree.modeling.format.json.JsonString.encode(this.values[i]));
                                    buffer.append("\"");
                                }
                                buffer.append("]\n");
                            }
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonEnd(buffer);
                            return buffer.toString();
                        }

                        public type(): number {
                            return org.kevoree.modeling.message.KMessageLoader.GET_RES_TYPE;
                        }

                    }

                    export class MessageHelper {

                        public static printJsonStart(builder: java.lang.StringBuilder): void {
                            builder.append("{\n");
                        }

                        public static printJsonEnd(builder: java.lang.StringBuilder): void {
                            builder.append("}\n");
                        }

                        public static printType(builder: java.lang.StringBuilder, type: number): void {
                            builder.append("\"");
                            builder.append(org.kevoree.modeling.message.KMessageLoader.TYPE_NAME);
                            builder.append("\":\"");
                            builder.append(type);
                            builder.append("\"\n");
                        }

                        public static printElem(elem: any, name: string, builder: java.lang.StringBuilder): void {
                            if (elem != null) {
                                builder.append(",");
                                builder.append("\"");
                                builder.append(name);
                                builder.append("\":\"");
                                builder.append(elem.toString());
                                builder.append("\"\n");
                            }
                        }

                    }

                    export class OperationCallMessage implements org.kevoree.modeling.message.KMessage {

                        public id: number;
                        public classIndex: number;
                        public opIndex: number;
                        public params: string[];
                        public key: org.kevoree.modeling.KContentKey;
                        public json(): string {
                            var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonStart(buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printType(buffer, this.type());
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.id, org.kevoree.modeling.message.KMessageLoader.ID_NAME, buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.key, org.kevoree.modeling.message.KMessageLoader.KEY_NAME, buffer);
                            buffer.append(",\"").append(org.kevoree.modeling.message.KMessageLoader.CLASS_IDX_NAME).append("\":\"").append(this.classIndex).append("\"");
                            buffer.append(",\"").append(org.kevoree.modeling.message.KMessageLoader.OPERATION_NAME).append("\":\"").append(this.opIndex).append("\"");
                            if (this.params != null) {
                                buffer.append(",\"");
                                buffer.append(org.kevoree.modeling.message.KMessageLoader.PARAMETERS_NAME).append("\":[");
                                for (var i: number = 0; i < this.params.length; i++) {
                                    if (i != 0) {
                                        buffer.append(",");
                                    }
                                    buffer.append("\"");
                                    buffer.append(org.kevoree.modeling.format.json.JsonString.encode(this.params[i]));
                                    buffer.append("\"");
                                }
                                buffer.append("]\n");
                            }
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonEnd(buffer);
                            return buffer.toString();
                        }

                        public type(): number {
                            return org.kevoree.modeling.message.KMessageLoader.OPERATION_CALL_TYPE;
                        }

                    }

                    export class OperationResultMessage implements org.kevoree.modeling.message.KMessage {

                        public id: number;
                        public value: string;
                        public key: org.kevoree.modeling.KContentKey;
                        public json(): string {
                            var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonStart(buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printType(buffer, this.type());
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.id, org.kevoree.modeling.message.KMessageLoader.ID_NAME, buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.key, org.kevoree.modeling.message.KMessageLoader.KEY_NAME, buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.value, org.kevoree.modeling.message.KMessageLoader.VALUE_NAME, buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonEnd(buffer);
                            return buffer.toString();
                        }

                        public type(): number {
                            return org.kevoree.modeling.message.KMessageLoader.OPERATION_RESULT_TYPE;
                        }

                    }

                    export class PutRequest implements org.kevoree.modeling.message.KMessage {

                        public request: org.kevoree.modeling.cdn.KContentPutRequest;
                        public id: number;
                        public json(): string {
                            var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonStart(buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printType(buffer, this.type());
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.id, org.kevoree.modeling.message.KMessageLoader.ID_NAME, buffer);
                            if (this.request != null) {
                                buffer.append(",\"");
                                buffer.append(org.kevoree.modeling.message.KMessageLoader.KEYS_NAME).append("\":[");
                                for (var i: number = 0; i < this.request.size(); i++) {
                                    if (i != 0) {
                                        buffer.append(",");
                                    }
                                    buffer.append("\"");
                                    buffer.append(this.request.getKey(i));
                                    buffer.append("\"");
                                }
                                buffer.append("]\n");
                                buffer.append(",\"");
                                buffer.append(org.kevoree.modeling.message.KMessageLoader.VALUES_NAME).append("\":[");
                                for (var i: number = 0; i < this.request.size(); i++) {
                                    if (i != 0) {
                                        buffer.append(",");
                                    }
                                    buffer.append("\"");
                                    buffer.append(org.kevoree.modeling.format.json.JsonString.encode(this.request.getContent(i)));
                                    buffer.append("\"");
                                }
                                buffer.append("]\n");
                            }
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonEnd(buffer);
                            return buffer.toString();
                        }

                        public type(): number {
                            return org.kevoree.modeling.message.KMessageLoader.PUT_REQ_TYPE;
                        }

                    }

                    export class PutResult implements org.kevoree.modeling.message.KMessage {

                        public id: number;
                        public json(): string {
                            var buffer: java.lang.StringBuilder = new java.lang.StringBuilder();
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonStart(buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printType(buffer, this.type());
                            org.kevoree.modeling.message.impl.MessageHelper.printElem(this.id, org.kevoree.modeling.message.KMessageLoader.ID_NAME, buffer);
                            org.kevoree.modeling.message.impl.MessageHelper.printJsonEnd(buffer);
                            return buffer.toString();
                        }

                        public type(): number {
                            return org.kevoree.modeling.message.KMessageLoader.PUT_RES_TYPE;
                        }

                    }

                }
            }
            export module meta {
                export interface KMeta {

                    index(): number;

                    metaName(): string;

                    metaType(): org.kevoree.modeling.meta.MetaType;

                }

                export interface KMetaAttribute extends org.kevoree.modeling.meta.KMeta {

                    key(): boolean;

                    attributeType(): org.kevoree.modeling.KType;

                    strategy(): org.kevoree.modeling.extrapolation.Extrapolation;

                    precision(): number;

                    setExtrapolation(extrapolation: org.kevoree.modeling.extrapolation.Extrapolation): void;

                    setPrecision(precision: number): void;

                }

                export interface KMetaClass extends org.kevoree.modeling.meta.KMeta {

                    metaElements(): org.kevoree.modeling.meta.KMeta[];

                    meta(index: number): org.kevoree.modeling.meta.KMeta;

                    metaByName(name: string): org.kevoree.modeling.meta.KMeta;

                    attribute(name: string): org.kevoree.modeling.meta.KMetaAttribute;

                    reference(name: string): org.kevoree.modeling.meta.KMetaReference;

                    operation(name: string): org.kevoree.modeling.meta.KMetaOperation;

                    addAttribute(attributeName: string, p_type: org.kevoree.modeling.KType): org.kevoree.modeling.meta.KMetaAttribute;

                    addReference(referenceName: string, metaClass: org.kevoree.modeling.meta.KMetaClass, oppositeName: string, toMany: boolean): org.kevoree.modeling.meta.KMetaReference;

                    addOperation(operationName: string): org.kevoree.modeling.meta.KMetaOperation;

                }

                export interface KMetaModel extends org.kevoree.modeling.meta.KMeta {

                    metaClasses(): org.kevoree.modeling.meta.KMetaClass[];

                    metaClassByName(name: string): org.kevoree.modeling.meta.KMetaClass;

                    metaClass(index: number): org.kevoree.modeling.meta.KMetaClass;

                    addMetaClass(metaClassName: string): org.kevoree.modeling.meta.KMetaClass;

                    model(): org.kevoree.modeling.KModel<any>;

                }

                export interface KMetaOperation extends org.kevoree.modeling.meta.KMeta {

                    origin(): org.kevoree.modeling.meta.KMeta;

                }

                export interface KMetaReference extends org.kevoree.modeling.meta.KMeta {

                    visible(): boolean;

                    single(): boolean;

                    type(): org.kevoree.modeling.meta.KMetaClass;

                    opposite(): org.kevoree.modeling.meta.KMetaReference;

                    origin(): org.kevoree.modeling.meta.KMetaClass;

                }

                export class KPrimitiveTypes {

                    public static STRING: org.kevoree.modeling.KType = new org.kevoree.modeling.abs.AbstractDataType("STRING", false);
                    public static LONG: org.kevoree.modeling.KType = new org.kevoree.modeling.abs.AbstractDataType("LONG", false);
                    public static INT: org.kevoree.modeling.KType = new org.kevoree.modeling.abs.AbstractDataType("INT", false);
                    public static BOOL: org.kevoree.modeling.KType = new org.kevoree.modeling.abs.AbstractDataType("BOOL", false);
                    public static SHORT: org.kevoree.modeling.KType = new org.kevoree.modeling.abs.AbstractDataType("SHORT", false);
                    public static DOUBLE: org.kevoree.modeling.KType = new org.kevoree.modeling.abs.AbstractDataType("DOUBLE", false);
                    public static FLOAT: org.kevoree.modeling.KType = new org.kevoree.modeling.abs.AbstractDataType("FLOAT", false);
                    public static CONTINUOUS: org.kevoree.modeling.KType = new org.kevoree.modeling.abs.AbstractDataType("CONTINUOUS", false);
                }

                export class MetaType {

                    public static ATTRIBUTE: MetaType = new MetaType();
                    public static REFERENCE: MetaType = new MetaType();
                    public static OPERATION: MetaType = new MetaType();
                    public static CLASS: MetaType = new MetaType();
                    public static MODEL: MetaType = new MetaType();
                    public equals(other: any): boolean {
                        return this == other;
                    }
                    public static _MetaTypeVALUES : MetaType[] = [
                        MetaType.ATTRIBUTE
                        ,MetaType.REFERENCE
                        ,MetaType.OPERATION
                        ,MetaType.CLASS
                        ,MetaType.MODEL
                    ];
                    public static values():MetaType[]{
                        return MetaType._MetaTypeVALUES;
                    }
                }

                export module impl {
                    export class GenericModel extends org.kevoree.modeling.abs.AbstractKModel<any> {

                        private _p_metaModel: org.kevoree.modeling.meta.KMetaModel;
                        constructor(mm: org.kevoree.modeling.meta.KMetaModel) {
                            super();
                            this._p_metaModel = mm;
                        }

                        public metaModel(): org.kevoree.modeling.meta.KMetaModel {
                            return this._p_metaModel;
                        }

                        public internalCreateUniverse(universe: number): org.kevoree.modeling.KUniverse<any, any, any> {
                            return new org.kevoree.modeling.meta.impl.GenericUniverse(universe, this._manager);
                        }

                        public internalCreateObject(universe: number, time: number, uuid: number, clazz: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.KObject {
                            return new org.kevoree.modeling.meta.impl.GenericObject(universe, time, uuid, clazz, this._manager);
                        }

                    }

                    export class GenericObject extends org.kevoree.modeling.abs.AbstractKObject {

                        constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
                            super(p_universe, p_time, p_uuid, p_metaClass, p_manager);
                        }

                    }

                    export class GenericUniverse extends org.kevoree.modeling.abs.AbstractKUniverse<any, any, any> {

                        constructor(p_key: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
                            super(p_key, p_manager);
                        }

                        public internal_create(timePoint: number): org.kevoree.modeling.KView {
                            return new org.kevoree.modeling.meta.impl.GenericView(this._universe, timePoint, this._manager);
                        }

                    }

                    export class GenericView extends org.kevoree.modeling.abs.AbstractKView {

                        constructor(p_universe: number, _time: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
                            super(p_universe, _time, p_manager);
                        }

                    }

                    export class MetaAttribute implements org.kevoree.modeling.meta.KMetaAttribute {

                        private _name: string;
                        private _index: number;
                        public _precision: number;
                        private _key: boolean;
                        private _metaType: org.kevoree.modeling.KType;
                        private _extrapolation: org.kevoree.modeling.extrapolation.Extrapolation;
                        public attributeType(): org.kevoree.modeling.KType {
                            return this._metaType;
                        }

                        public index(): number {
                            return this._index;
                        }

                        public metaName(): string {
                            return this._name;
                        }

                        public metaType(): org.kevoree.modeling.meta.MetaType {
                            return org.kevoree.modeling.meta.MetaType.ATTRIBUTE;
                        }

                        public precision(): number {
                            return this._precision;
                        }

                        public key(): boolean {
                            return this._key;
                        }

                        public strategy(): org.kevoree.modeling.extrapolation.Extrapolation {
                            return this._extrapolation;
                        }

                        public setExtrapolation(extrapolation: org.kevoree.modeling.extrapolation.Extrapolation): void {
                            this._extrapolation = extrapolation;
                        }

                        public setPrecision(p_precision: number): void {
                            this._precision = p_precision;
                        }

                        constructor(p_name: string, p_index: number, p_precision: number, p_key: boolean, p_metaType: org.kevoree.modeling.KType, p_extrapolation: org.kevoree.modeling.extrapolation.Extrapolation) {
                            this._name = p_name;
                            this._index = p_index;
                            this._precision = p_precision;
                            this._key = p_key;
                            this._metaType = p_metaType;
                            this._extrapolation = p_extrapolation;
                            if (this._extrapolation == null) {
                                this._extrapolation = org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation.instance();
                            }
                        }

                    }

                    export class MetaClass implements org.kevoree.modeling.meta.KMetaClass {

                        private _name: string;
                        private _index: number;
                        private _meta: org.kevoree.modeling.meta.KMeta[];
                        private _indexes: org.kevoree.modeling.memory.struct.map.KStringMap<any> = null;
                        constructor(p_name: string, p_index: number) {
                            this._name = p_name;
                            this._index = p_index;
                            this._meta = new Array();
                            this._indexes = new org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                        }

                        public init(p_metaElements: org.kevoree.modeling.meta.KMeta[]): void {
                            this._indexes.clear();
                            this._meta = p_metaElements;
                            for (var i: number = 0; i < this._meta.length; i++) {
                                this._indexes.put(p_metaElements[i].metaName(), p_metaElements[i].index());
                            }
                        }

                        public metaByName(name: string): org.kevoree.modeling.meta.KMeta {
                            if (this._indexes != null) {
                                var resolvedIndex: number = this._indexes.get(name);
                                if (resolvedIndex != null) {
                                    return this._meta[resolvedIndex];
                                }
                            }
                            return null;
                        }

                        public attribute(name: string): org.kevoree.modeling.meta.KMetaAttribute {
                            var resolved: org.kevoree.modeling.meta.KMeta = this.metaByName(name);
                            if (resolved != null && resolved instanceof org.kevoree.modeling.meta.impl.MetaAttribute) {
                                return <org.kevoree.modeling.meta.KMetaAttribute>resolved;
                            }
                            return null;
                        }

                        public reference(name: string): org.kevoree.modeling.meta.KMetaReference {
                            var resolved: org.kevoree.modeling.meta.KMeta = this.metaByName(name);
                            if (resolved != null && resolved instanceof org.kevoree.modeling.meta.impl.MetaReference) {
                                return <org.kevoree.modeling.meta.KMetaReference>resolved;
                            }
                            return null;
                        }

                        public operation(name: string): org.kevoree.modeling.meta.KMetaOperation {
                            var resolved: org.kevoree.modeling.meta.KMeta = this.metaByName(name);
                            if (resolved != null && resolved instanceof org.kevoree.modeling.meta.impl.MetaOperation) {
                                return <org.kevoree.modeling.meta.KMetaOperation>resolved;
                            }
                            return null;
                        }

                        public metaElements(): org.kevoree.modeling.meta.KMeta[] {
                            return this._meta;
                        }

                        public index(): number {
                            return this._index;
                        }

                        public metaName(): string {
                            return this._name;
                        }

                        public metaType(): org.kevoree.modeling.meta.MetaType {
                            return org.kevoree.modeling.meta.MetaType.CLASS;
                        }

                        public meta(index: number): org.kevoree.modeling.meta.KMeta {
                            if (index >= 0 && index < this._meta.length) {
                                return this._meta[index];
                            } else {
                                return null;
                            }
                        }

                        public addAttribute(attributeName: string, p_type: org.kevoree.modeling.KType): org.kevoree.modeling.meta.KMetaAttribute {
                            var precisionCleaned: number = -1;
                            var extrapolation: org.kevoree.modeling.extrapolation.Extrapolation;
                            if (p_type == org.kevoree.modeling.meta.KPrimitiveTypes.CONTINUOUS) {
                                extrapolation = org.kevoree.modeling.extrapolation.impl.PolynomialExtrapolation.instance();
                                precisionCleaned = 0.1;
                            } else {
                                extrapolation = org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation.instance();
                            }
                            var tempAttribute: org.kevoree.modeling.meta.KMetaAttribute = new org.kevoree.modeling.meta.impl.MetaAttribute(attributeName, this._meta.length, precisionCleaned, false, p_type, extrapolation);
                            this.internal_add_meta(tempAttribute);
                            return tempAttribute;
                        }

                        public addReference(referenceName: string, p_metaClass: org.kevoree.modeling.meta.KMetaClass, oppositeName: string, toMany: boolean): org.kevoree.modeling.meta.KMetaReference {
                            var tempOrigin: org.kevoree.modeling.meta.KMetaClass = this;
                            var opName: string = oppositeName;
                            if (opName == null) {
                                opName = "op_" + referenceName;
                                (<org.kevoree.modeling.meta.impl.MetaClass>p_metaClass).getOrCreate(opName, referenceName, this, false, false);
                            } else {
                                (<org.kevoree.modeling.meta.impl.MetaClass>p_metaClass).getOrCreate(opName, referenceName, this, true, false);
                            }
                            var tempReference: org.kevoree.modeling.meta.impl.MetaReference = new org.kevoree.modeling.meta.impl.MetaReference(referenceName, this._meta.length, false, !toMany,  () => {
                                return p_metaClass;
                            }, opName,  () => {
                                return tempOrigin;
                            });
                            this.internal_add_meta(tempReference);
                            return tempReference;
                        }

                        private getOrCreate(p_name: string, p_oppositeName: string, p_oppositeClass: org.kevoree.modeling.meta.KMetaClass, p_visible: boolean, p_single: boolean): org.kevoree.modeling.meta.KMetaReference {
                            var previous: org.kevoree.modeling.meta.KMetaReference = this.reference(p_name);
                            if (previous != null) {
                                return previous;
                            }
                            var tempOrigin: org.kevoree.modeling.meta.KMetaClass = this;
                            var tempReference: org.kevoree.modeling.meta.KMetaReference = new org.kevoree.modeling.meta.impl.MetaReference(p_name, this._meta.length, p_visible, p_single,  () => {
                                return p_oppositeClass;
                            }, p_oppositeName,  () => {
                                return tempOrigin;
                            });
                            this.internal_add_meta(tempReference);
                            return tempReference;
                        }

                        public addOperation(operationName: string): org.kevoree.modeling.meta.KMetaOperation {
                            var tempOrigin: org.kevoree.modeling.meta.KMetaClass = this;
                            var tempOperation: org.kevoree.modeling.meta.impl.MetaOperation = new org.kevoree.modeling.meta.impl.MetaOperation(operationName, this._meta.length + 1,  () => {
                                return tempOrigin;
                            });
                            this.internal_add_meta(tempOperation);
                            return tempOperation;
                        }

                        private internal_add_meta(p_new_meta: org.kevoree.modeling.meta.KMeta): void {
                             this._meta[p_new_meta.index()] = p_new_meta;
                             this._indexes.put(p_new_meta.metaName(), p_new_meta.index());
                        }

                    }

                    export class MetaModel implements org.kevoree.modeling.meta.KMetaModel {

                        private _name: string;
                        private _index: number;
                        private _metaClasses: org.kevoree.modeling.meta.KMetaClass[];
                        private _metaClasses_indexes: org.kevoree.modeling.memory.struct.map.KStringMap<any> = null;
                        public index(): number {
                            return this._index;
                        }

                        public metaName(): string {
                            return this._name;
                        }

                        public metaType(): org.kevoree.modeling.meta.MetaType {
                            return org.kevoree.modeling.meta.MetaType.MODEL;
                        }

                        constructor(p_name: string) {
                            this._name = p_name;
                            this._index = 0;
                            this._metaClasses_indexes = new org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                        }

                        public init(p_metaClasses: org.kevoree.modeling.meta.KMetaClass[]): void {
                            this._metaClasses_indexes.clear();
                            this._metaClasses = p_metaClasses;
                            for (var i: number = 0; i < this._metaClasses.length; i++) {
                                this._metaClasses_indexes.put(p_metaClasses[i].metaName(), p_metaClasses[i].index());
                            }
                        }

                        public metaClasses(): org.kevoree.modeling.meta.KMetaClass[] {
                            return this._metaClasses;
                        }

                        public metaClassByName(name: string): org.kevoree.modeling.meta.KMetaClass {
                            if (this._metaClasses_indexes == null) {
                                return null;
                            }
                            var resolved: number = this._metaClasses_indexes.get(name);
                            if (resolved == null) {
                                return null;
                            } else {
                                return this._metaClasses[resolved];
                            }
                        }

                        public metaClass(index: number): org.kevoree.modeling.meta.KMetaClass {
                            if (index >= 0 && index < this._metaClasses.length) {
                                return this._metaClasses[index];
                            }
                            return null;
                        }

                        public addMetaClass(metaClassName: string): org.kevoree.modeling.meta.KMetaClass {
                            if (this._metaClasses_indexes.contains(metaClassName)) {
                                return this.metaClassByName(metaClassName);
                            } else {
                                if (this._metaClasses == null) {
                                    this._metaClasses = new Array();
                                    this._metaClasses[0] = new org.kevoree.modeling.meta.impl.MetaClass(metaClassName, 0);
                                    this._metaClasses_indexes.put(metaClassName, this._metaClasses[0].index());
                                    return this._metaClasses[0];
                                } else {
                                    var newMetaClass: org.kevoree.modeling.meta.KMetaClass = new org.kevoree.modeling.meta.impl.MetaClass(metaClassName, this._metaClasses.length);
                                    this.interal_add_meta_class(newMetaClass);
                                    return newMetaClass;
                                }
                            }
                        }

                        private interal_add_meta_class(p_newMetaClass: org.kevoree.modeling.meta.KMetaClass): void {
                             this._metaClasses[p_newMetaClass.index()] = p_newMetaClass;
                             this._metaClasses_indexes.put(p_newMetaClass.metaName(), p_newMetaClass.index());
                        }

                        public model(): org.kevoree.modeling.KModel<any> {
                            return new org.kevoree.modeling.meta.impl.GenericModel(this);
                        }

                    }

                    export class MetaOperation implements org.kevoree.modeling.meta.KMetaOperation {

                        private _name: string;
                        private _index: number;
                        private _lazyMetaClass: () => org.kevoree.modeling.meta.KMeta;
                        public index(): number {
                            return this._index;
                        }

                        public metaName(): string {
                            return this._name;
                        }

                        public metaType(): org.kevoree.modeling.meta.MetaType {
                            return org.kevoree.modeling.meta.MetaType.OPERATION;
                        }

                        constructor(p_name: string, p_index: number, p_lazyMetaClass: () => org.kevoree.modeling.meta.KMeta) {
                            this._name = p_name;
                            this._index = p_index;
                            this._lazyMetaClass = p_lazyMetaClass;
                        }

                        public origin(): org.kevoree.modeling.meta.KMetaClass {
                            if (this._lazyMetaClass != null) {
                                return <org.kevoree.modeling.meta.KMetaClass>this._lazyMetaClass();
                            }
                            return null;
                        }

                    }

                    export class MetaReference implements org.kevoree.modeling.meta.KMetaReference {

                        private _name: string;
                        private _index: number;
                        private _visible: boolean;
                        private _single: boolean;
                        private _lazyMetaType: () => org.kevoree.modeling.meta.KMeta;
                        private _op_name: string;
                        private _lazyMetaOrigin: () => org.kevoree.modeling.meta.KMeta;
                        public single(): boolean {
                            return this._single;
                        }

                        public type(): org.kevoree.modeling.meta.KMetaClass {
                            if (this._lazyMetaType != null) {
                                return <org.kevoree.modeling.meta.KMetaClass>this._lazyMetaType();
                            } else {
                                return null;
                            }
                        }

                        public opposite(): org.kevoree.modeling.meta.KMetaReference {
                            if (this._op_name != null) {
                                return this.type().reference(this._op_name);
                            }
                            return null;
                        }

                        public origin(): org.kevoree.modeling.meta.KMetaClass {
                            if (this._lazyMetaOrigin != null) {
                                return <org.kevoree.modeling.meta.KMetaClass>this._lazyMetaOrigin();
                            }
                            return null;
                        }

                        public index(): number {
                            return this._index;
                        }

                        public metaName(): string {
                            return this._name;
                        }

                        public metaType(): org.kevoree.modeling.meta.MetaType {
                            return org.kevoree.modeling.meta.MetaType.REFERENCE;
                        }

                        public visible(): boolean {
                            return this._visible;
                        }

                        constructor(p_name: string, p_index: number, p_visible: boolean, p_single: boolean, p_lazyMetaType: () => org.kevoree.modeling.meta.KMeta, op_name: string, p_lazyMetaOrigin: () => org.kevoree.modeling.meta.KMeta) {
                            this._name = p_name;
                            this._index = p_index;
                            this._visible = p_visible;
                            this._single = p_single;
                            this._lazyMetaType = p_lazyMetaType;
                            this._op_name = op_name;
                            this._lazyMetaOrigin = p_lazyMetaOrigin;
                        }

                    }

                }
            }
            export module operation {
                export interface KOperation {

                    on(source: org.kevoree.modeling.KObject, params: any[], result: (p : any) => void): void;

                }

                export interface KOperationManager {

                    registerOperation(operation: org.kevoree.modeling.meta.KMetaOperation, callback: (p : org.kevoree.modeling.KObject, p1 : any[], p2 : (p : any) => void) => void, target: org.kevoree.modeling.KObject): void;

                    call(source: org.kevoree.modeling.KObject, operation: org.kevoree.modeling.meta.KMetaOperation, param: any[], callback: (p : any) => void): void;

                    operationEventReceived(operationEvent: org.kevoree.modeling.message.KMessage): void;

                }

                export module impl {
                    export class HashOperationManager implements org.kevoree.modeling.operation.KOperationManager {

                        private staticOperations: org.kevoree.modeling.memory.struct.map.KIntMap<any>;
                        private instanceOperations: org.kevoree.modeling.memory.struct.map.KLongMap<any>;
                        private remoteCallCallbacks: org.kevoree.modeling.memory.struct.map.KLongMap<any> = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                        private _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                        private _callbackId: number = 0;
                        constructor(p_manager: org.kevoree.modeling.memory.manager.KMemoryManager) {
                            this.staticOperations = new org.kevoree.modeling.memory.struct.map.impl.ArrayIntMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                            this.instanceOperations = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                            this._manager = p_manager;
                        }

                        public registerOperation(operation: org.kevoree.modeling.meta.KMetaOperation, callback: (p : org.kevoree.modeling.KObject, p1 : any[], p2 : (p : any) => void) => void, target: org.kevoree.modeling.KObject): void {
                            if (target == null) {
                                var clazzOperations: org.kevoree.modeling.memory.struct.map.KIntMap<any> = this.staticOperations.get(operation.origin().index());
                                if (clazzOperations == null) {
                                    clazzOperations = new org.kevoree.modeling.memory.struct.map.impl.ArrayIntMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                    this.staticOperations.put(operation.origin().index(), clazzOperations);
                                }
                                clazzOperations.put(operation.index(), callback);
                            } else {
                                var objectOperations: org.kevoree.modeling.memory.struct.map.KIntMap<any> = this.instanceOperations.get(target.uuid());
                                if (objectOperations == null) {
                                    objectOperations = new org.kevoree.modeling.memory.struct.map.impl.ArrayIntMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                    this.instanceOperations.put(target.uuid(), objectOperations);
                                }
                                objectOperations.put(operation.index(), callback);
                            }
                        }

                        private searchOperation(source: number, clazz: number, operation: number): (p : org.kevoree.modeling.KObject, p1 : any[], p2 : (p : any) => void) => void {
                            var objectOperations: org.kevoree.modeling.memory.struct.map.KIntMap<any> = this.instanceOperations.get(source);
                            if (objectOperations != null) {
                                return objectOperations.get(operation);
                            }
                            var clazzOperations: org.kevoree.modeling.memory.struct.map.KIntMap<any> = this.staticOperations.get(clazz);
                            if (clazzOperations != null) {
                                return clazzOperations.get(operation);
                            }
                            return null;
                        }

                        public call(source: org.kevoree.modeling.KObject, operation: org.kevoree.modeling.meta.KMetaOperation, param: any[], callback: (p : any) => void): void {
                            var operationCore: (p : org.kevoree.modeling.KObject, p1 : any[], p2 : (p : any) => void) => void = this.searchOperation(source.uuid(), operation.origin().index(), operation.index());
                            if (operationCore != null) {
                                operationCore(source, param, callback);
                            } else {
                                this.sendToRemote(source, operation, param, callback);
                            }
                        }

                        private sendToRemote(source: org.kevoree.modeling.KObject, operation: org.kevoree.modeling.meta.KMetaOperation, param: any[], callback: (p : any) => void): void {
                            var stringParams: string[] = new Array();
                            for (var i: number = 0; i < param.length; i++) {
                                stringParams[i] = param[i].toString();
                            }
                            var contentKey: org.kevoree.modeling.KContentKey = new org.kevoree.modeling.KContentKey(source.universe(), source.now(), source.uuid());
                            var operationCall: org.kevoree.modeling.message.impl.OperationCallMessage = new org.kevoree.modeling.message.impl.OperationCallMessage();
                            operationCall.id = this.nextKey();
                            operationCall.key = contentKey;
                            operationCall.classIndex = source.metaClass().index();
                            operationCall.opIndex = operation.index();
                            operationCall.params = stringParams;
                            this.remoteCallCallbacks.put(operationCall.id, callback);
                            this._manager.cdn().send(operationCall);
                        }

                        public nextKey(): number {
                            if (this._callbackId == org.kevoree.modeling.KConfig.CALLBACK_HISTORY) {
                                this._callbackId = 0;
                            } else {
                                this._callbackId++;
                            }
                            return this._callbackId;
                        }

                        public operationEventReceived(operationEvent: org.kevoree.modeling.message.KMessage): void {
                            if (operationEvent.type() == org.kevoree.modeling.message.KMessageLoader.OPERATION_RESULT_TYPE) {
                                var operationResult: org.kevoree.modeling.message.impl.OperationResultMessage = <org.kevoree.modeling.message.impl.OperationResultMessage>operationEvent;
                                var cb: (p : any) => void = this.remoteCallCallbacks.get(operationResult.id);
                                if (cb != null) {
                                    cb(operationResult.value);
                                }
                            } else {
                                if (operationEvent.type() == org.kevoree.modeling.message.KMessageLoader.OPERATION_CALL_TYPE) {
                                    var operationCall: org.kevoree.modeling.message.impl.OperationCallMessage = <org.kevoree.modeling.message.impl.OperationCallMessage>operationEvent;
                                    var sourceKey: org.kevoree.modeling.KContentKey = operationCall.key;
                                    var operationCore: (p : org.kevoree.modeling.KObject, p1 : any[], p2 : (p : any) => void) => void = this.searchOperation(sourceKey.obj, operationCall.classIndex, operationCall.opIndex);
                                    if (operationCore != null) {
                                        var view: org.kevoree.modeling.KView = this._manager.model().universe(sourceKey.universe).time(sourceKey.time);
                                        view.lookup(sourceKey.obj,  (kObject : org.kevoree.modeling.KObject) => {
                                            if (kObject != null) {
                                                operationCore(kObject, operationCall.params,  (o : any) => {
                                                    var operationResultMessage: org.kevoree.modeling.message.impl.OperationResultMessage = new org.kevoree.modeling.message.impl.OperationResultMessage();
                                                    operationResultMessage.key = operationCall.key;
                                                    operationResultMessage.id = operationCall.id;
                                                    operationResultMessage.value = o.toString();
                                                    this._manager.cdn().send(operationResultMessage);
                                                });
                                            }
                                        });
                                    }
                                } else {
                                    System.err.println("BAD ROUTING !");
                                }
                            }
                        }

                    }

                }
            }
            export module scheduler {
                export interface KScheduler {

                    dispatch(runnable: java.lang.Runnable): void;

                    stop(): void;

                }

                export module impl {
                    export class DirectScheduler implements org.kevoree.modeling.scheduler.KScheduler {

                        public dispatch(runnable: java.lang.Runnable): void {
                            runnable.run();
                        }

                        public stop(): void {
                        }

                    }

                    export class ExecutorServiceScheduler implements org.kevoree.modeling.scheduler.KScheduler {

                        public dispatch(p_runnable: java.lang.Runnable): void {
                             p_runnable.run();
                        }

                        public stop(): void {
                        }

                    }

                }
            }
            export module traversal {
                export interface KTraversal {

                    traverse(metaReference: org.kevoree.modeling.meta.KMetaReference): org.kevoree.modeling.traversal.KTraversal;

                    traverseQuery(metaReferenceQuery: string): org.kevoree.modeling.traversal.KTraversal;

                    attributeQuery(attributeQuery: string): org.kevoree.modeling.traversal.KTraversal;

                    withAttribute(attribute: org.kevoree.modeling.meta.KMetaAttribute, expectedValue: any): org.kevoree.modeling.traversal.KTraversal;

                    withoutAttribute(attribute: org.kevoree.modeling.meta.KMetaAttribute, expectedValue: any): org.kevoree.modeling.traversal.KTraversal;

                    filter(filter: (p : org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;

                    then(cb: (p : org.kevoree.modeling.KObject[]) => void): void;

                    map(attribute: org.kevoree.modeling.meta.KMetaAttribute, cb: (p : any[]) => void): void;

                    collect(metaReference: org.kevoree.modeling.meta.KMetaReference, continueCondition: (p : org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;

                }

                export interface KTraversalAction {

                    chain(next: org.kevoree.modeling.traversal.KTraversalAction): void;

                    execute(inputs: org.kevoree.modeling.KObject[]): void;

                }

                export interface KTraversalFilter {

                    filter(obj: org.kevoree.modeling.KObject): boolean;

                }

                export module impl {
                    export class Traversal implements org.kevoree.modeling.traversal.KTraversal {

                        private static TERMINATED_MESSAGE: string = "Promise is terminated by the call of done method, please create another promise";
                        private _initObjs: org.kevoree.modeling.KObject[];
                        private _initAction: org.kevoree.modeling.traversal.KTraversalAction;
                        private _lastAction: org.kevoree.modeling.traversal.KTraversalAction;
                        private _terminated: boolean = false;
                        constructor(p_root: org.kevoree.modeling.KObject) {
                            this._initObjs = new Array();
                            this._initObjs[0] = p_root;
                        }

                        private internal_chain_action(p_action: org.kevoree.modeling.traversal.KTraversalAction): org.kevoree.modeling.traversal.KTraversal {
                            if (this._terminated) {
                                throw new java.lang.RuntimeException(Traversal.TERMINATED_MESSAGE);
                            }
                            if (this._initAction == null) {
                                this._initAction = p_action;
                            }
                            if (this._lastAction != null) {
                                this._lastAction.chain(p_action);
                            }
                            this._lastAction = p_action;
                            return this;
                        }

                        public traverse(p_metaReference: org.kevoree.modeling.meta.KMetaReference): org.kevoree.modeling.traversal.KTraversal {
                            return this.internal_chain_action(new org.kevoree.modeling.traversal.impl.actions.TraverseAction(p_metaReference));
                        }

                        public traverseQuery(p_metaReferenceQuery: string): org.kevoree.modeling.traversal.KTraversal {
                            return this.internal_chain_action(new org.kevoree.modeling.traversal.impl.actions.TraverseQueryAction(p_metaReferenceQuery));
                        }

                        public withAttribute(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_expectedValue: any): org.kevoree.modeling.traversal.KTraversal {
                            return this.internal_chain_action(new org.kevoree.modeling.traversal.impl.actions.FilterAttributeAction(p_attribute, p_expectedValue));
                        }

                        public withoutAttribute(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_expectedValue: any): org.kevoree.modeling.traversal.KTraversal {
                            return this.internal_chain_action(new org.kevoree.modeling.traversal.impl.actions.FilterNotAttributeAction(p_attribute, p_expectedValue));
                        }

                        public attributeQuery(p_attributeQuery: string): org.kevoree.modeling.traversal.KTraversal {
                            return this.internal_chain_action(new org.kevoree.modeling.traversal.impl.actions.FilterAttributeQueryAction(p_attributeQuery));
                        }

                        public filter(p_filter: (p : org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal {
                            return this.internal_chain_action(new org.kevoree.modeling.traversal.impl.actions.FilterAction(p_filter));
                        }

                        public collect(metaReference: org.kevoree.modeling.meta.KMetaReference, continueCondition: (p : org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal {
                            return this.internal_chain_action(new org.kevoree.modeling.traversal.impl.actions.DeepCollectAction(metaReference, continueCondition));
                        }

                        public then(cb: (p : org.kevoree.modeling.KObject[]) => void): void {
                            this.internal_chain_action(new org.kevoree.modeling.traversal.impl.actions.FinalAction(cb));
                            this._terminated = true;
                            this._initAction.execute(this._initObjs);
                        }

                        public map(attribute: org.kevoree.modeling.meta.KMetaAttribute, cb: (p : any[]) => void): void {
                            this.internal_chain_action(new org.kevoree.modeling.traversal.impl.actions.MapAction(attribute, cb));
                            this._terminated = true;
                            this._initAction.execute(this._initObjs);
                        }

                    }

                    export module actions {
                        export class DeepCollectAction implements org.kevoree.modeling.traversal.KTraversalAction {

                            private _next: org.kevoree.modeling.traversal.KTraversalAction;
                            private _reference: org.kevoree.modeling.meta.KMetaReference;
                            private _continueCondition: (p : org.kevoree.modeling.KObject) => boolean;
                            private _alreadyPassed: org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any> = null;
                            private _finalElements: org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any> = null;
                            constructor(p_reference: org.kevoree.modeling.meta.KMetaReference, p_continueCondition: (p : org.kevoree.modeling.KObject) => boolean) {
                                this._reference = p_reference;
                                this._continueCondition = p_continueCondition;
                            }

                            public chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void {
                                this._next = p_next;
                            }

                            public execute(p_inputs: org.kevoree.modeling.KObject[]): void {
                                if (p_inputs == null || p_inputs.length == 0) {
                                    this._next.execute(p_inputs);
                                    return;
                                } else {
                                    this._alreadyPassed = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                    this._finalElements = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                    var filtered_inputs: org.kevoree.modeling.KObject[] = new Array();
                                    for (var i: number = 0; i < p_inputs.length; i++) {
                                        if (this._continueCondition == null || this._continueCondition(p_inputs[i])) {
                                            filtered_inputs[i] = p_inputs[i];
                                            this._alreadyPassed.put(p_inputs[i].uuid(), p_inputs[i]);
                                        }
                                    }
                                    var iterationCallbacks: {(p : org.kevoree.modeling.KObject[]) : void;}[] = new Array();
                                    iterationCallbacks[0] =  (traversed : org.kevoree.modeling.KObject[]) => {
                                        var filtered_inputs2: org.kevoree.modeling.KObject[] = new Array();
                                        var nbSize: number = 0;
                                        for (var i: number = 0; i < traversed.length; i++) {
                                            if ((this._continueCondition == null || this._continueCondition(traversed[i])) && !this._alreadyPassed.contains(traversed[i].uuid())) {
                                                filtered_inputs2[i] = traversed[i];
                                                this._alreadyPassed.put(traversed[i].uuid(), traversed[i]);
                                                this._finalElements.put(traversed[i].uuid(), traversed[i]);
                                                nbSize++;
                                            }
                                        }
                                        if (nbSize > 0) {
                                            this.executeStep(filtered_inputs2, iterationCallbacks[0]);
                                        } else {
                                            var trimmed: org.kevoree.modeling.KObject[] = new Array();
                                            var nbInserted: number[] = [0];
                                            this._finalElements.each( (key : number, value : org.kevoree.modeling.KObject) => {
                                                trimmed[nbInserted[0]] = value;
                                                nbInserted[0]++;
                                            });
                                            this._next.execute(trimmed);
                                        }
                                    };
                                    this.executeStep(filtered_inputs, iterationCallbacks[0]);
                                }
                            }

                            private executeStep(p_inputStep: org.kevoree.modeling.KObject[], private_callback: (p : org.kevoree.modeling.KObject[]) => void): void {
                                var currentObject: org.kevoree.modeling.abs.AbstractKObject = null;
                                var nextIds: org.kevoree.modeling.memory.struct.map.KLongLongMap = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                for (var i: number = 0; i < p_inputStep.length; i++) {
                                    if (p_inputStep[i] != null) {
                                        try {
                                            var loopObj: org.kevoree.modeling.abs.AbstractKObject = <org.kevoree.modeling.abs.AbstractKObject>p_inputStep[i];
                                            currentObject = loopObj;
                                            var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = loopObj._manager.segment(loopObj.universe(), loopObj.now(), loopObj.uuid(), org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, loopObj.metaClass(), null);
                                            if (raw != null) {
                                                if (this._reference == null) {
                                                    var metaElements: org.kevoree.modeling.meta.KMeta[] = loopObj.metaClass().metaElements();
                                                    for (var j: number = 0; j < metaElements.length; j++) {
                                                        if (metaElements[j] instanceof org.kevoree.modeling.meta.impl.MetaReference) {
                                                            var resolved: number[] = raw.getRef(metaElements[j].index(), loopObj.metaClass());
                                                            if (resolved != null) {
                                                                for (var k: number = 0; k < resolved.length; k++) {
                                                                    nextIds.put(resolved[k], resolved[k]);
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    var translatedRef: org.kevoree.modeling.meta.KMetaReference = loopObj.internal_transpose_ref(this._reference);
                                                    if (translatedRef != null) {
                                                        var resolved: number[] = raw.getRef(translatedRef.index(), loopObj.metaClass());
                                                        if (resolved != null) {
                                                            for (var j: number = 0; j < resolved.length; j++) {
                                                                nextIds.put(resolved[j], resolved[j]);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } catch ($ex$) {
                                            if ($ex$ instanceof java.lang.Exception) {
                                                var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                e.printStackTrace();
                                            } else {
                                                throw $ex$;
                                            }
                                        }
                                    }
                                }
                                var trimmed: number[] = new Array();
                                var inserted: number[] = [0];
                                nextIds.each( (key : number, value : number) => {
                                    trimmed[inserted[0]] = key;
                                    inserted[0]++;
                                });
                                currentObject._manager.lookupAllobjects(currentObject.universe(), currentObject.now(), trimmed,  (kObjects : org.kevoree.modeling.KObject[]) => {
                                    private_callback(kObjects);
                                });
                            }

                        }

                        export class FilterAction implements org.kevoree.modeling.traversal.KTraversalAction {

                            private _next: org.kevoree.modeling.traversal.KTraversalAction;
                            private _filter: (p : org.kevoree.modeling.KObject) => boolean;
                            constructor(p_filter: (p : org.kevoree.modeling.KObject) => boolean) {
                                this._filter = p_filter;
                            }

                            public chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void {
                                this._next = p_next;
                            }

                            public execute(p_inputs: org.kevoree.modeling.KObject[]): void {
                                var selectedIndex: boolean[] = new Array();
                                var selected: number = 0;
                                for (var i: number = 0; i < p_inputs.length; i++) {
                                    try {
                                        if (this._filter(p_inputs[i])) {
                                            selectedIndex[i] = true;
                                            selected++;
                                        }
                                    } catch ($ex$) {
                                        if ($ex$ instanceof java.lang.Exception) {
                                            var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                            e.printStackTrace();
                                        } else {
                                            throw $ex$;
                                        }
                                    }
                                }
                                var nextStepElement: org.kevoree.modeling.KObject[] = new Array();
                                var inserted: number = 0;
                                for (var i: number = 0; i < p_inputs.length; i++) {
                                    if (selectedIndex[i]) {
                                        nextStepElement[inserted] = p_inputs[i];
                                        inserted++;
                                    }
                                }
                                this._next.execute(nextStepElement);
                            }

                        }

                        export class FilterAttributeAction implements org.kevoree.modeling.traversal.KTraversalAction {

                            private _next: org.kevoree.modeling.traversal.KTraversalAction;
                            private _attribute: org.kevoree.modeling.meta.KMetaAttribute;
                            private _expectedValue: any;
                            constructor(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_expectedValue: any) {
                                this._attribute = p_attribute;
                                this._expectedValue = p_expectedValue;
                            }

                            public chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void {
                                this._next = p_next;
                            }

                            public execute(p_inputs: org.kevoree.modeling.KObject[]): void {
                                if (p_inputs == null || p_inputs.length == 0) {
                                    this._next.execute(p_inputs);
                                    return;
                                } else {
                                    var selectedIndexes: boolean[] = new Array();
                                    var nbSelected: number = 0;
                                    for (var i: number = 0; i < p_inputs.length; i++) {
                                        try {
                                            var loopObj: org.kevoree.modeling.abs.AbstractKObject = <org.kevoree.modeling.abs.AbstractKObject>p_inputs[i];
                                            var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = (loopObj)._manager.segment(loopObj.universe(), loopObj.now(), loopObj.uuid(), org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, loopObj.metaClass(), null);
                                            if (raw != null) {
                                                if (this._attribute == null) {
                                                    if (this._expectedValue == null) {
                                                        selectedIndexes[i] = true;
                                                        nbSelected++;
                                                    } else {
                                                        var addToNext: boolean = false;
                                                        var metaElements: org.kevoree.modeling.meta.KMeta[] = loopObj.metaClass().metaElements();
                                                        for (var j: number = 0; j < metaElements.length; j++) {
                                                            if (metaElements[j] instanceof org.kevoree.modeling.meta.impl.MetaAttribute) {
                                                                var resolved: any = raw.get(metaElements[j].index(), loopObj.metaClass());
                                                                if (resolved == null) {
                                                                    if (this._expectedValue.toString().equals("*")) {
                                                                        addToNext = true;
                                                                    }
                                                                } else {
                                                                    if (resolved.equals(this._expectedValue)) {
                                                                        addToNext = true;
                                                                    } else {
                                                                        if (resolved.toString().matches(this._expectedValue.toString().replace("*", ".*"))) {
                                                                            addToNext = true;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        if (addToNext) {
                                                            selectedIndexes[i] = true;
                                                            nbSelected++;
                                                        }
                                                    }
                                                } else {
                                                    var translatedAtt: org.kevoree.modeling.meta.KMetaAttribute = loopObj.internal_transpose_att(this._attribute);
                                                    if (translatedAtt != null) {
                                                        var resolved: any = raw.get(translatedAtt.index(), loopObj.metaClass());
                                                        if (this._expectedValue == null) {
                                                            if (resolved == null) {
                                                                selectedIndexes[i] = true;
                                                                nbSelected++;
                                                            }
                                                        } else {
                                                            if (resolved == null) {
                                                                if (this._expectedValue.toString().equals("*")) {
                                                                    selectedIndexes[i] = true;
                                                                    nbSelected++;
                                                                }
                                                            } else {
                                                                if (resolved.equals(this._expectedValue)) {
                                                                    selectedIndexes[i] = true;
                                                                    nbSelected++;
                                                                } else {
                                                                    if (resolved.toString().matches(this._expectedValue.toString().replace("*", ".*"))) {
                                                                        selectedIndexes[i] = true;
                                                                        nbSelected++;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                System.err.println("WARN: Empty KObject " + loopObj.uuid());
                                            }
                                        } catch ($ex$) {
                                            if ($ex$ instanceof java.lang.Exception) {
                                                var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                e.printStackTrace();
                                            } else {
                                                throw $ex$;
                                            }
                                        }
                                    }
                                    var nextStepElement: org.kevoree.modeling.KObject[] = new Array();
                                    var inserted: number = 0;
                                    for (var i: number = 0; i < p_inputs.length; i++) {
                                        if (selectedIndexes[i]) {
                                            nextStepElement[inserted] = p_inputs[i];
                                            inserted++;
                                        }
                                    }
                                    this._next.execute(nextStepElement);
                                }
                            }

                        }

                        export class FilterAttributeQueryAction implements org.kevoree.modeling.traversal.KTraversalAction {

                            private _next: org.kevoree.modeling.traversal.KTraversalAction;
                            private _attributeQuery: string;
                            constructor(p_attributeQuery: string) {
                                this._attributeQuery = p_attributeQuery;
                            }

                            public chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void {
                                this._next = p_next;
                            }

                            public execute(p_inputs: org.kevoree.modeling.KObject[]): void {
                                if (p_inputs == null || p_inputs.length == 0) {
                                    this._next.execute(p_inputs);
                                    return;
                                } else {
                                    var selectedIndexes: boolean[] = new Array();
                                    var nbSelected: number = 0;
                                    for (var i: number = 0; i < p_inputs.length; i++) {
                                        try {
                                            var loopObj: org.kevoree.modeling.abs.AbstractKObject = <org.kevoree.modeling.abs.AbstractKObject>p_inputs[i];
                                            if (this._attributeQuery == null) {
                                                selectedIndexes[i] = true;
                                                nbSelected++;
                                            } else {
                                                var metaElements: org.kevoree.modeling.meta.KMeta[] = loopObj.metaClass().metaElements();
                                                var params: org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any> = this.buildParams(this._attributeQuery);
                                                var selectedForNext: boolean[] = [true];
                                                params.each( (key : string, param : org.kevoree.modeling.traversal.impl.selector.QueryParam) => {
                                                    for (var j: number = 0; j < metaElements.length; j++) {
                                                        if (metaElements[j] instanceof org.kevoree.modeling.meta.impl.MetaAttribute) {
                                                            var metaAttribute: org.kevoree.modeling.meta.KMetaAttribute = <org.kevoree.modeling.meta.KMetaAttribute>metaElements[j];
                                                            if (metaAttribute.metaName().matches("^" + param.name() + "$")) {
                                                                var o_raw: any = loopObj.get(metaAttribute);
                                                                if (o_raw != null) {
                                                                    if (param.value().equals("null")) {
                                                                        if (!param.isNegative()) {
                                                                            selectedForNext[0] = false;
                                                                        }
                                                                    } else {
                                                                        if (o_raw.toString().matches("^" + param.value() + "$")) {
                                                                            if (param.isNegative()) {
                                                                                selectedForNext[0] = false;
                                                                            }
                                                                        } else {
                                                                            if (!param.isNegative()) {
                                                                                selectedForNext[0] = false;
                                                                            }
                                                                        }
                                                                    }
                                                                } else {
                                                                    if (param.value().equals("null") || param.value().equals("*")) {
                                                                        if (param.isNegative()) {
                                                                            selectedForNext[0] = false;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                });
                                                if (selectedForNext[0]) {
                                                    selectedIndexes[i] = true;
                                                    nbSelected++;
                                                }
                                            }
                                        } catch ($ex$) {
                                            if ($ex$ instanceof java.lang.Exception) {
                                                var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                e.printStackTrace();
                                            } else {
                                                throw $ex$;
                                            }
                                        }
                                    }
                                    var nextStepElement: org.kevoree.modeling.KObject[] = new Array();
                                    var inserted: number = 0;
                                    for (var i: number = 0; i < p_inputs.length; i++) {
                                        if (selectedIndexes[i]) {
                                            nextStepElement[inserted] = p_inputs[i];
                                            inserted++;
                                        }
                                    }
                                    this._next.execute(nextStepElement);
                                }
                            }

                            private buildParams(p_paramString: string): org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any> {
                                var params: org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any> = new org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                var iParam: number = 0;
                                var lastStart: number = iParam;
                                while (iParam < p_paramString.length){
                                    if (p_paramString.charAt(iParam) == ',') {
                                        var p: string = p_paramString.substring(lastStart, iParam).trim();
                                        if (!p.equals("") && !p.equals("*")) {
                                            if (p.endsWith("=")) {
                                                p = p + "*";
                                            }
                                            var pArray: string[] = p.split("=");
                                            var pObject: org.kevoree.modeling.traversal.impl.selector.QueryParam;
                                            if (pArray.length > 1) {
                                                var paramKey: string = pArray[0].trim();
                                                var negative: boolean = paramKey.endsWith("!");
                                                pObject = new org.kevoree.modeling.traversal.impl.selector.QueryParam(paramKey.replace("!", "").replace("*", ".*"), pArray[1].trim().replace("*", ".*"), negative);
                                                params.put(pObject.name(), pObject);
                                            }
                                        }
                                        lastStart = iParam + 1;
                                    }
                                    iParam = iParam + 1;
                                }
                                var lastParam: string = p_paramString.substring(lastStart, iParam).trim();
                                if (!lastParam.equals("") && !lastParam.equals("*")) {
                                    if (lastParam.endsWith("=")) {
                                        lastParam = lastParam + "*";
                                    }
                                    var pArray: string[] = lastParam.split("=");
                                    var pObject: org.kevoree.modeling.traversal.impl.selector.QueryParam;
                                    if (pArray.length > 1) {
                                        var paramKey: string = pArray[0].trim();
                                        var negative: boolean = paramKey.endsWith("!");
                                        pObject = new org.kevoree.modeling.traversal.impl.selector.QueryParam(paramKey.replace("!", "").replace("*", ".*"), pArray[1].trim().replace("*", ".*"), negative);
                                        params.put(pObject.name(), pObject);
                                    }
                                }
                                return params;
                            }

                        }

                        export class FilterNotAttributeAction implements org.kevoree.modeling.traversal.KTraversalAction {

                            private _next: org.kevoree.modeling.traversal.KTraversalAction;
                            private _attribute: org.kevoree.modeling.meta.KMetaAttribute;
                            private _expectedValue: any;
                            constructor(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_expectedValue: any) {
                                this._attribute = p_attribute;
                                this._expectedValue = p_expectedValue;
                            }

                            public chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void {
                                this._next = p_next;
                            }

                            public execute(p_inputs: org.kevoree.modeling.KObject[]): void {
                                if (p_inputs == null || p_inputs.length == 0) {
                                    this._next.execute(p_inputs);
                                } else {
                                    var selectedIndexes: boolean[] = new Array();
                                    var nbSelected: number = 0;
                                    for (var i: number = 0; i < p_inputs.length; i++) {
                                        try {
                                            var loopObj: org.kevoree.modeling.abs.AbstractKObject = <org.kevoree.modeling.abs.AbstractKObject>p_inputs[i];
                                            var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = loopObj._manager.segment(loopObj.universe(), loopObj.now(), loopObj.uuid(), org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, loopObj.metaClass(), null);
                                            if (raw != null) {
                                                if (this._attribute == null) {
                                                    if (this._expectedValue == null) {
                                                        selectedIndexes[i] = true;
                                                        nbSelected++;
                                                    } else {
                                                        var addToNext: boolean = true;
                                                        var metaElements: org.kevoree.modeling.meta.KMeta[] = loopObj.metaClass().metaElements();
                                                        for (var j: number = 0; j < metaElements.length; j++) {
                                                            if (metaElements[j] instanceof org.kevoree.modeling.meta.impl.MetaAttribute) {
                                                                var ref: org.kevoree.modeling.meta.KMetaAttribute = <org.kevoree.modeling.meta.KMetaAttribute>metaElements[j];
                                                                var resolved: any = raw.get(ref.index(), loopObj.metaClass());
                                                                if (resolved == null) {
                                                                    if (this._expectedValue.toString().equals("*")) {
                                                                        addToNext = false;
                                                                    }
                                                                } else {
                                                                    if (resolved.equals(this._expectedValue)) {
                                                                        addToNext = false;
                                                                    } else {
                                                                        if (resolved.toString().matches(this._expectedValue.toString().replace("*", ".*"))) {
                                                                            addToNext = false;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        if (addToNext) {
                                                            selectedIndexes[i] = true;
                                                            nbSelected++;
                                                        }
                                                    }
                                                } else {
                                                    var translatedAtt: org.kevoree.modeling.meta.KMetaAttribute = loopObj.internal_transpose_att(this._attribute);
                                                    if (translatedAtt != null) {
                                                        var resolved: any = raw.get(translatedAtt.index(), loopObj.metaClass());
                                                        if (this._expectedValue == null) {
                                                            if (resolved != null) {
                                                                selectedIndexes[i] = true;
                                                                nbSelected++;
                                                            }
                                                        } else {
                                                            if (resolved == null) {
                                                                if (!this._expectedValue.toString().equals("*")) {
                                                                    selectedIndexes[i] = true;
                                                                    nbSelected++;
                                                                }
                                                            } else {
                                                                if (resolved.equals(this._expectedValue)) {
                                                                } else {
                                                                    if (resolved.toString().matches(this._expectedValue.toString().replace("*", ".*"))) {
                                                                    } else {
                                                                        selectedIndexes[i] = true;
                                                                        nbSelected++;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                System.err.println("WARN: Empty KObject " + loopObj.uuid());
                                            }
                                        } catch ($ex$) {
                                            if ($ex$ instanceof java.lang.Exception) {
                                                var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                e.printStackTrace();
                                            } else {
                                                throw $ex$;
                                            }
                                        }
                                    }
                                    var nextStepElement: org.kevoree.modeling.KObject[] = new Array();
                                    var inserted: number = 0;
                                    for (var i: number = 0; i < p_inputs.length; i++) {
                                        if (selectedIndexes[i]) {
                                            nextStepElement[inserted] = p_inputs[i];
                                            inserted++;
                                        }
                                    }
                                    this._next.execute(nextStepElement);
                                }
                            }

                        }

                        export class FinalAction implements org.kevoree.modeling.traversal.KTraversalAction {

                            private _finalCallback: (p : org.kevoree.modeling.KObject[]) => void;
                            constructor(p_callback: (p : org.kevoree.modeling.KObject[]) => void) {
                                this._finalCallback = p_callback;
                            }

                            public chain(next: org.kevoree.modeling.traversal.KTraversalAction): void {
                            }

                            public execute(inputs: org.kevoree.modeling.KObject[]): void {
                                this._finalCallback(inputs);
                            }

                        }

                        export class MapAction implements org.kevoree.modeling.traversal.KTraversalAction {

                            private _finalCallback: (p : any[]) => void;
                            private _attribute: org.kevoree.modeling.meta.KMetaAttribute;
                            constructor(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_callback: (p : any[]) => void) {
                                this._finalCallback = p_callback;
                                this._attribute = p_attribute;
                            }

                            public chain(next: org.kevoree.modeling.traversal.KTraversalAction): void {
                            }

                            public execute(inputs: org.kevoree.modeling.KObject[]): void {
                                var selected: any[] = new Array();
                                var nbElem: number = 0;
                                for (var i: number = 0; i < inputs.length; i++) {
                                    if (inputs[i] != null) {
                                        var resolved: any = inputs[i].get(this._attribute);
                                        if (resolved != null) {
                                            selected[i] = resolved;
                                            nbElem++;
                                        }
                                    }
                                }
                                var trimmed: any[] = new Array();
                                var nbInserted: number = 0;
                                for (var i: number = 0; i < inputs.length; i++) {
                                    if (selected[i] != null) {
                                        trimmed[nbInserted] = selected[i];
                                        nbInserted++;
                                    }
                                }
                                this._finalCallback(trimmed);
                            }

                        }

                        export class RemoveDuplicateAction implements org.kevoree.modeling.traversal.KTraversalAction {

                            private _next: org.kevoree.modeling.traversal.KTraversalAction;
                            public chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void {
                                this._next = p_next;
                            }

                            public execute(p_inputs: org.kevoree.modeling.KObject[]): void {
                                var elems: org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any> = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>(p_inputs.length, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                for (var i: number = 0; i < p_inputs.length; i++) {
                                    elems.put(p_inputs[i].uuid(), p_inputs[i]);
                                }
                                var trimmed: org.kevoree.modeling.KObject[] = new Array();
                                var nbInserted: number[] = [0];
                                elems.each( (key : number, value : org.kevoree.modeling.KObject) => {
                                    trimmed[nbInserted[0]] = value;
                                    nbInserted[0]++;
                                });
                                this._next.execute(trimmed);
                            }

                        }

                        export class TraverseAction implements org.kevoree.modeling.traversal.KTraversalAction {

                            private _next: org.kevoree.modeling.traversal.KTraversalAction;
                            private _reference: org.kevoree.modeling.meta.KMetaReference;
                            constructor(p_reference: org.kevoree.modeling.meta.KMetaReference) {
                                this._reference = p_reference;
                            }

                            public chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void {
                                this._next = p_next;
                            }

                            public execute(p_inputs: org.kevoree.modeling.KObject[]): void {
                                if (p_inputs == null || p_inputs.length == 0) {
                                    this._next.execute(p_inputs);
                                    return;
                                } else {
                                    var currentObject: org.kevoree.modeling.abs.AbstractKObject = <org.kevoree.modeling.abs.AbstractKObject>p_inputs[0];
                                    var nextIds: org.kevoree.modeling.memory.struct.map.KLongLongMap = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                    for (var i: number = 0; i < p_inputs.length; i++) {
                                        try {
                                            var loopObj: org.kevoree.modeling.abs.AbstractKObject = <org.kevoree.modeling.abs.AbstractKObject>p_inputs[i];
                                            var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = currentObject._manager.segment(loopObj.universe(), loopObj.now(), loopObj.uuid(), org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, loopObj.metaClass(), null);
                                            if (raw != null) {
                                                if (this._reference == null) {
                                                    var metaElements: org.kevoree.modeling.meta.KMeta[] = loopObj.metaClass().metaElements();
                                                    for (var j: number = 0; j < metaElements.length; j++) {
                                                        if (metaElements[j] instanceof org.kevoree.modeling.meta.impl.MetaReference) {
                                                            var ref: org.kevoree.modeling.meta.KMetaReference = <org.kevoree.modeling.meta.KMetaReference>metaElements[j];
                                                            var resolved: number[] = raw.getRef(ref.index(), currentObject.metaClass());
                                                            if (resolved != null) {
                                                                for (var k: number = 0; k < resolved.length; k++) {
                                                                    nextIds.put(resolved[k], resolved[k]);
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    var translatedRef: org.kevoree.modeling.meta.KMetaReference = loopObj.internal_transpose_ref(this._reference);
                                                    if (translatedRef != null) {
                                                        var resolved: number[] = raw.getRef(translatedRef.index(), currentObject.metaClass());
                                                        if (resolved != null) {
                                                            for (var j: number = 0; j < resolved.length; j++) {
                                                                nextIds.put(resolved[j], resolved[j]);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } catch ($ex$) {
                                            if ($ex$ instanceof java.lang.Exception) {
                                                var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                e.printStackTrace();
                                            } else {
                                                throw $ex$;
                                            }
                                        }
                                    }
                                    var trimmed: number[] = new Array();
                                    var inserted: number[] = [0];
                                    nextIds.each( (key : number, value : number) => {
                                        trimmed[inserted[0]] = key;
                                        inserted[0]++;
                                    });
                                    currentObject._manager.lookupAllobjects(currentObject.universe(), currentObject.now(), trimmed,  (kObjects : org.kevoree.modeling.KObject[]) => {
                                        this._next.execute(kObjects);
                                    });
                                }
                            }

                        }

                        export class TraverseQueryAction implements org.kevoree.modeling.traversal.KTraversalAction {

                            private SEP: string = ",";
                            private _next: org.kevoree.modeling.traversal.KTraversalAction;
                            private _referenceQuery: string;
                            constructor(p_referenceQuery: string) {
                                this._referenceQuery = p_referenceQuery;
                            }

                            public chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void {
                                this._next = p_next;
                            }

                            public execute(p_inputs: org.kevoree.modeling.KObject[]): void {
                                if (p_inputs == null || p_inputs.length == 0) {
                                    this._next.execute(p_inputs);
                                    return;
                                } else {
                                    var currentFirstObject: org.kevoree.modeling.abs.AbstractKObject = <org.kevoree.modeling.abs.AbstractKObject>p_inputs[0];
                                    var nextIds: org.kevoree.modeling.memory.struct.map.KLongLongMap = new org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap(org.kevoree.modeling.KConfig.CACHE_INIT_SIZE, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
                                    for (var i: number = 0; i < p_inputs.length; i++) {
                                        try {
                                            var loopObj: org.kevoree.modeling.abs.AbstractKObject = <org.kevoree.modeling.abs.AbstractKObject>p_inputs[i];
                                            var raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment = loopObj._manager.segment(loopObj.universe(), loopObj.now(), loopObj.uuid(), org.kevoree.modeling.memory.manager.AccessMode.RESOLVE, loopObj.metaClass(), null);
                                            var metaElements: org.kevoree.modeling.meta.KMeta[] = loopObj.metaClass().metaElements();
                                            if (raw != null) {
                                                if (this._referenceQuery == null) {
                                                    for (var j: number = 0; j < metaElements.length; j++) {
                                                        if (metaElements[j] instanceof org.kevoree.modeling.meta.impl.MetaReference) {
                                                            var resolved: number[] = raw.getRef(metaElements[j].index(), loopObj.metaClass());
                                                            if (resolved != null) {
                                                                for (var k: number = 0; k < resolved.length; k++) {
                                                                    var idResolved: number = resolved[k];
                                                                    nextIds.put(idResolved, idResolved);
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    var queries: string[] = this._referenceQuery.split(this.SEP);
                                                    for (var k: number = 0; k < queries.length; k++) {
                                                        queries[k] = queries[k].replace("*", ".*");
                                                    }
                                                    for (var h: number = 0; h < metaElements.length; h++) {
                                                        if (metaElements[h] instanceof org.kevoree.modeling.meta.impl.MetaReference) {
                                                            var metaReference: org.kevoree.modeling.meta.KMetaReference = <org.kevoree.modeling.meta.KMetaReference>metaElements[h];
                                                            var selected: boolean = false;
                                                            for (var k: number = 0; k < queries.length; k++) {
                                                                if (queries[k] != null && queries[k].startsWith("#")) {
                                                                    if (metaReference.opposite().metaName().matches(queries[k].substring(1))) {
                                                                        selected = true;
                                                                        break;
                                                                    }
                                                                } else {
                                                                    if (metaReference.metaName().matches("^" + queries[k] + "$")) {
                                                                        selected = true;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            if (selected) {
                                                                var resolved: number[] = raw.getRef(metaElements[h].index(), loopObj.metaClass());
                                                                if (resolved != null) {
                                                                    for (var j: number = 0; j < resolved.length; j++) {
                                                                        nextIds.put(resolved[j], resolved[j]);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } catch ($ex$) {
                                            if ($ex$ instanceof java.lang.Exception) {
                                                var e: java.lang.Exception = <java.lang.Exception>$ex$;
                                                e.printStackTrace();
                                            } else {
                                                throw $ex$;
                                            }
                                        }
                                    }
                                    var trimmed: number[] = new Array();
                                    var inserted: number[] = [0];
                                    nextIds.each( (key : number, value : number) => {
                                        trimmed[inserted[0]] = key;
                                        inserted[0]++;
                                    });
                                    currentFirstObject._manager.lookupAllobjects(currentFirstObject.universe(), currentFirstObject.now(), trimmed,  (kObjects : org.kevoree.modeling.KObject[]) => {
                                        this._next.execute(kObjects);
                                    });
                                }
                            }

                        }

                    }
                    export module selector {
                        export class Query {

                            public static OPEN_BRACKET: string = '[';
                            public static CLOSE_BRACKET: string = ']';
                            public static QUERY_SEP: string = '/';
                            public relationName: string;
                            public params: string;
                            constructor(relationName: string, params: string) {
                                this.relationName = relationName;
                                this.params = params;
                            }

                            public toString(): string {
                                return "KQuery{" + "relationName='" + this.relationName + '\'' + ", params='" + this.params + '\'' + '}';
                            }

                            public static buildChain(query: string): java.util.List<org.kevoree.modeling.traversal.impl.selector.Query> {
                                var result: java.util.List<org.kevoree.modeling.traversal.impl.selector.Query> = new java.util.ArrayList<org.kevoree.modeling.traversal.impl.selector.Query>();
                                if (query == null || query.length == 0) {
                                    return null;
                                }
                                var i: number = 0;
                                var escaped: boolean = false;
                                var previousKQueryStart: number = 0;
                                var previousKQueryNameEnd: number = -1;
                                var previousKQueryAttributesEnd: number = -1;
                                var previousKQueryAttributesStart: number = 0;
                                while (i < query.length){
                                    var notLastElem: boolean = (i + 1) != query.length;
                                    if (escaped && notLastElem) {
                                        escaped = false;
                                    } else {
                                        var currentChar: string = query.charAt(i);
                                        if (currentChar == Query.CLOSE_BRACKET && notLastElem) {
                                            previousKQueryAttributesEnd = i;
                                        } else {
                                            if (currentChar == '\\' && notLastElem) {
                                                escaped = true;
                                            } else {
                                                if (currentChar == Query.OPEN_BRACKET && notLastElem) {
                                                    previousKQueryNameEnd = i;
                                                    previousKQueryAttributesStart = i + 1;
                                                } else {
                                                    if (currentChar == Query.QUERY_SEP || !notLastElem) {
                                                        var relationName: string;
                                                        var atts: string = null;
                                                        if (previousKQueryNameEnd == -1) {
                                                            if (notLastElem) {
                                                                previousKQueryNameEnd = i;
                                                            } else {
                                                                previousKQueryNameEnd = i + 1;
                                                            }
                                                        } else {
                                                            if (previousKQueryAttributesStart != -1) {
                                                                if (previousKQueryAttributesEnd == -1) {
                                                                    if (notLastElem || currentChar == Query.QUERY_SEP || currentChar == Query.CLOSE_BRACKET) {
                                                                        previousKQueryAttributesEnd = i;
                                                                    } else {
                                                                        previousKQueryAttributesEnd = i + 1;
                                                                    }
                                                                }
                                                                atts = query.substring(previousKQueryAttributesStart, previousKQueryAttributesEnd);
                                                                if (atts.length == 0) {
                                                                    atts = null;
                                                                }
                                                            }
                                                        }
                                                        relationName = query.substring(previousKQueryStart, previousKQueryNameEnd);
                                                        var additionalQuery: org.kevoree.modeling.traversal.impl.selector.Query = new org.kevoree.modeling.traversal.impl.selector.Query(relationName, atts);
                                                        result.add(additionalQuery);
                                                        previousKQueryStart = i + 1;
                                                        previousKQueryNameEnd = -1;
                                                        previousKQueryAttributesEnd = -1;
                                                        previousKQueryAttributesStart = -1;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    i = i + 1;
                                }
                                return result;
                            }

                        }

                        export class QueryParam {

                            private _name: string;
                            private _value: string;
                            private _negative: boolean;
                            constructor(p_name: string, p_value: string, p_negative: boolean) {
                                this._name = p_name;
                                this._value = p_value;
                                this._negative = p_negative;
                            }

                            public name(): string {
                                return this._name;
                            }

                            public value(): string {
                                return this._value;
                            }

                            public isNegative(): boolean {
                                return this._negative;
                            }

                        }

                        export class Selector {

                            public static select(root: org.kevoree.modeling.KObject, query: string, callback: (p : org.kevoree.modeling.KObject[]) => void): void {
                                if (callback == null) {
                                    return;
                                }
                                var current: org.kevoree.modeling.traversal.KTraversal = null;
                                var extracted: java.util.List<org.kevoree.modeling.traversal.impl.selector.Query> = org.kevoree.modeling.traversal.impl.selector.Query.buildChain(query);
                                if (extracted != null) {
                                    for (var i: number = 0; i < extracted.size(); i++) {
                                        if (current == null) {
                                            current = root.traversal().traverseQuery(extracted.get(i).relationName);
                                        } else {
                                            current = current.traverseQuery(extracted.get(i).relationName);
                                        }
                                        current = current.attributeQuery(extracted.get(i).params);
                                    }
                                }
                                if (current != null) {
                                    current.then(callback);
                                } else {
                                    callback(new Array());
                                }
                            }

                        }

                    }
                }
                export module visitor {
                    export interface KModelAttributeVisitor {

                        visit(metaAttribute: org.kevoree.modeling.meta.KMetaAttribute, value: any): void;

                    }

                    export interface KModelVisitor {

                        visit(elem: org.kevoree.modeling.KObject): org.kevoree.modeling.traversal.visitor.KVisitResult;

                    }

                    export class KVisitResult {

                        public static CONTINUE: KVisitResult = new KVisitResult();
                        public static SKIP: KVisitResult = new KVisitResult();
                        public static STOP: KVisitResult = new KVisitResult();
                        public equals(other: any): boolean {
                            return this == other;
                        }
                        public static _KVisitResultVALUES : KVisitResult[] = [
                            KVisitResult.CONTINUE
                            ,KVisitResult.SKIP
                            ,KVisitResult.STOP
                        ];
                        public static values():KVisitResult[]{
                            return KVisitResult._KVisitResultVALUES;
                        }
                    }

                }
            }
            export module util {
                export class Checker {

                    public static isDefined(param: any): boolean {
                         return param != undefined && param != null;
                    }

                }

            }
        }
    }
}
