declare module org {
    module kevoree {
        module modeling {
            class KActionType {
                static CALL: KActionType;
                static CALL_RESPONSE: KActionType;
                static SET: KActionType;
                static ADD: KActionType;
                static REMOVE: KActionType;
                static NEW: KActionType;
                equals(other: any): boolean;
                static _KActionTypeVALUES: KActionType[];
                static values(): KActionType[];
            }
            interface KCallback<A> {
                on(a: A): void;
            }
            class KConfig {
                static TREE_CACHE_SIZE: number;
                static CALLBACK_HISTORY: number;
                static LONG_SIZE: number;
                static PREFIX_SIZE: number;
                static BEGINNING_OF_TIME: number;
                static END_OF_TIME: number;
                static NULL_LONG: number;
                static KEY_PREFIX_MASK: number;
                static KEY_SEP: string;
                static KEY_SIZE: number;
                static CACHE_INIT_SIZE: number;
                static CACHE_LOAD_FACTOR: number;
            }
            class KContentKey {
                universe: number;
                time: number;
                obj: number;
                constructor(p_universeID: number, p_timeID: number, p_objID: number);
                static createUniverseTree(p_objectID: number): org.kevoree.modeling.KContentKey;
                static createTimeTree(p_universeID: number, p_objectID: number): org.kevoree.modeling.KContentKey;
                static createObject(p_universeID: number, p_quantaID: number, p_objectID: number): org.kevoree.modeling.KContentKey;
                static createGlobalUniverseTree(): org.kevoree.modeling.KContentKey;
                static createRootUniverseTree(): org.kevoree.modeling.KContentKey;
                static createRootTimeTree(universeID: number): org.kevoree.modeling.KContentKey;
                static createLastPrefix(): org.kevoree.modeling.KContentKey;
                static createLastObjectIndexFromPrefix(prefix: number): org.kevoree.modeling.KContentKey;
                static createLastUniverseIndexFromPrefix(prefix: number): org.kevoree.modeling.KContentKey;
                static create(payload: string): org.kevoree.modeling.KContentKey;
                toString(): string;
                equals(param: any): boolean;
            }
            interface KModel<A extends org.kevoree.modeling.KUniverse<any, any, any>> {
                key(): number;
                newUniverse(): A;
                universe(key: number): A;
                manager(): org.kevoree.modeling.memory.manager.KMemoryManager;
                setContentDeliveryDriver(dataBase: org.kevoree.modeling.cdn.KContentDeliveryDriver): org.kevoree.modeling.KModel<any>;
                setScheduler(scheduler: org.kevoree.modeling.scheduler.KScheduler): org.kevoree.modeling.KModel<any>;
                setOperation(metaOperation: org.kevoree.modeling.meta.KMetaOperation, operation: (p: org.kevoree.modeling.KObject, p1: any[], p2: (p: any) => void) => void): void;
                setInstanceOperation(metaOperation: org.kevoree.modeling.meta.KMetaOperation, target: org.kevoree.modeling.KObject, operation: (p: org.kevoree.modeling.KObject, p1: any[], p2: (p: any) => void) => void): void;
                metaModel(): org.kevoree.modeling.meta.KMetaModel;
                defer(): org.kevoree.modeling.defer.KDefer;
                save(cb: (p: any) => void): void;
                discard(cb: (p: any) => void): void;
                connect(cb: (p: any) => void): void;
                close(cb: (p: any) => void): void;
                clearListenerGroup(groupID: number): void;
                nextGroup(): number;
                createByName(metaClassName: string, universe: number, time: number): org.kevoree.modeling.KObject;
                create(clazz: org.kevoree.modeling.meta.KMetaClass, universe: number, time: number): org.kevoree.modeling.KObject;
            }
            interface KObject {
                universe(): number;
                now(): number;
                uuid(): number;
                delete(cb: (p: any) => void): void;
                select(query: string, cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                listen(groupId: number, listener: (p: org.kevoree.modeling.KObject, p1: org.kevoree.modeling.meta.KMeta[]) => void): void;
                visitAttributes(visitor: (p: org.kevoree.modeling.meta.KMetaAttribute, p1: any) => void): void;
                visit(visitor: (p: org.kevoree.modeling.KObject) => org.kevoree.modeling.traversal.visitor.KVisitResult, cb: (p: any) => void): void;
                timeWalker(): org.kevoree.modeling.KTimeWalker;
                metaClass(): org.kevoree.modeling.meta.KMetaClass;
                mutate(actionType: org.kevoree.modeling.KActionType, metaReference: org.kevoree.modeling.meta.KMetaReference, param: org.kevoree.modeling.KObject): void;
                ref(metaReference: org.kevoree.modeling.meta.KMetaReference, cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                traversal(): org.kevoree.modeling.traversal.KTraversal;
                get(attribute: org.kevoree.modeling.meta.KMetaAttribute): any;
                getByName(atributeName: string): any;
                set(attribute: org.kevoree.modeling.meta.KMetaAttribute, payload: any): void;
                setByName(atributeName: string, payload: any): void;
                toJSON(): string;
                equals(other: any): boolean;
                jump(time: number, callback: (p: org.kevoree.modeling.KObject) => void): void;
                referencesWith(o: org.kevoree.modeling.KObject): org.kevoree.modeling.meta.KMetaReference[];
                call(operation: org.kevoree.modeling.meta.KMetaOperation, params: any[], cb: (p: any) => void): void;
                manager(): org.kevoree.modeling.memory.manager.KMemoryManager;
            }
            interface KTimeWalker {
                allTimes(cb: (p: number[]) => void): void;
                timesBefore(endOfSearch: number, cb: (p: number[]) => void): void;
                timesAfter(beginningOfSearch: number, cb: (p: number[]) => void): void;
                timesBetween(beginningOfSearch: number, endOfSearch: number, cb: (p: number[]) => void): void;
            }
            interface KType {
                name(): string;
                isEnum(): boolean;
                save(src: any): string;
                load(payload: string): any;
            }
            interface KUniverse<A extends org.kevoree.modeling.KView, B extends org.kevoree.modeling.KUniverse<any, any, any>, C extends org.kevoree.modeling.KModel<any>> {
                key(): number;
                time(timePoint: number): A;
                model(): C;
                equals(other: any): boolean;
                diverge(): B;
                origin(): B;
                descendants(): java.util.List<B>;
                delete(cb: (p: any) => void): void;
                lookupAllTimes(uuid: number, times: number[], cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                listenAll(groupId: number, objects: number[], multiListener: (p: org.kevoree.modeling.KObject[]) => void): void;
            }
            interface KView {
                createByName(metaClassName: string): org.kevoree.modeling.KObject;
                create(clazz: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.KObject;
                select(query: string, cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                lookup(key: number, cb: (p: org.kevoree.modeling.KObject) => void): void;
                lookupAll(keys: number[], cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                universe(): number;
                now(): number;
                json(): org.kevoree.modeling.format.KModelFormat;
                xmi(): org.kevoree.modeling.format.KModelFormat;
                equals(other: any): boolean;
                setRoot(elem: org.kevoree.modeling.KObject, cb: (p: any) => void): void;
                getRoot(cb: (p: org.kevoree.modeling.KObject) => void): void;
            }
            module abs {
                class AbstractDataType implements org.kevoree.modeling.KType {
                    private _name;
                    private _isEnum;
                    constructor(p_name: string, p_isEnum: boolean);
                    name(): string;
                    isEnum(): boolean;
                    save(src: any): string;
                    load(payload: string): any;
                }
                class AbstractKModel<A extends org.kevoree.modeling.KUniverse<any, any, any>> implements org.kevoree.modeling.KModel<any> {
                    _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                    private _key;
                    constructor();
                    metaModel(): org.kevoree.modeling.meta.KMetaModel;
                    connect(cb: (p: any) => void): void;
                    close(cb: (p: any) => void): void;
                    manager(): org.kevoree.modeling.memory.manager.KMemoryManager;
                    newUniverse(): A;
                    internalCreateUniverse(universe: number): A;
                    internalCreateObject(universe: number, time: number, uuid: number, clazz: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.KObject;
                    createProxy(universe: number, time: number, uuid: number, clazz: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.KObject;
                    universe(key: number): A;
                    save(cb: (p: any) => void): void;
                    discard(cb: (p: any) => void): void;
                    setContentDeliveryDriver(p_driver: org.kevoree.modeling.cdn.KContentDeliveryDriver): org.kevoree.modeling.KModel<any>;
                    setScheduler(p_scheduler: org.kevoree.modeling.scheduler.KScheduler): org.kevoree.modeling.KModel<any>;
                    setOperation(metaOperation: org.kevoree.modeling.meta.KMetaOperation, operation: (p: org.kevoree.modeling.KObject, p1: any[], p2: (p: any) => void) => void): void;
                    setInstanceOperation(metaOperation: org.kevoree.modeling.meta.KMetaOperation, target: org.kevoree.modeling.KObject, operation: (p: org.kevoree.modeling.KObject, p1: any[], p2: (p: any) => void) => void): void;
                    defer(): org.kevoree.modeling.defer.KDefer;
                    key(): number;
                    clearListenerGroup(groupID: number): void;
                    nextGroup(): number;
                    create(clazz: org.kevoree.modeling.meta.KMetaClass, universe: number, time: number): org.kevoree.modeling.KObject;
                    createByName(metaClassName: string, universe: number, time: number): org.kevoree.modeling.KObject;
                }
                class AbstractKObject implements org.kevoree.modeling.KObject {
                    _uuid: number;
                    _time: number;
                    _universe: number;
                    private _metaClass;
                    _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                    private static OUT_OF_CACHE_MSG;
                    constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    uuid(): number;
                    metaClass(): org.kevoree.modeling.meta.KMetaClass;
                    now(): number;
                    universe(): number;
                    timeWalker(): org.kevoree.modeling.KTimeWalker;
                    delete(cb: (p: any) => void): void;
                    select(query: string, cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                    listen(groupId: number, listener: (p: org.kevoree.modeling.KObject, p1: org.kevoree.modeling.meta.KMeta[]) => void): void;
                    get(p_attribute: org.kevoree.modeling.meta.KMetaAttribute): any;
                    getByName(atributeName: string): any;
                    set(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, payload: any): void;
                    setByName(atributeName: string, payload: any): void;
                    mutate(actionType: org.kevoree.modeling.KActionType, metaReference: org.kevoree.modeling.meta.KMetaReference, param: org.kevoree.modeling.KObject): void;
                    internal_mutate(actionType: org.kevoree.modeling.KActionType, metaReferenceP: org.kevoree.modeling.meta.KMetaReference, param: org.kevoree.modeling.KObject, setOpposite: boolean): void;
                    size(p_metaReference: org.kevoree.modeling.meta.KMetaReference): number;
                    ref(p_metaReference: org.kevoree.modeling.meta.KMetaReference, cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                    visitAttributes(visitor: (p: org.kevoree.modeling.meta.KMetaAttribute, p1: any) => void): void;
                    visit(p_visitor: (p: org.kevoree.modeling.KObject) => org.kevoree.modeling.traversal.visitor.KVisitResult, cb: (p: any) => void): void;
                    private internal_visit(visitor, end, visited, traversed);
                    toJSON(): string;
                    toString(): string;
                    equals(obj: any): boolean;
                    hashCode(): number;
                    jump(p_time: number, p_callback: (p: org.kevoree.modeling.KObject) => void): void;
                    internal_transpose_ref(p: org.kevoree.modeling.meta.KMetaReference): org.kevoree.modeling.meta.KMetaReference;
                    internal_transpose_att(p: org.kevoree.modeling.meta.KMetaAttribute): org.kevoree.modeling.meta.KMetaAttribute;
                    internal_transpose_op(p: org.kevoree.modeling.meta.KMetaOperation): org.kevoree.modeling.meta.KMetaOperation;
                    traversal(): org.kevoree.modeling.traversal.KTraversal;
                    referencesWith(o: org.kevoree.modeling.KObject): org.kevoree.modeling.meta.KMetaReference[];
                    call(p_operation: org.kevoree.modeling.meta.KMetaOperation, p_params: any[], cb: (p: any) => void): void;
                    manager(): org.kevoree.modeling.memory.manager.KMemoryManager;
                }
                class AbstractKObjectInfer extends org.kevoree.modeling.abs.AbstractKObject implements org.kevoree.modeling.infer.KInfer {
                    constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    readOnlyState(): org.kevoree.modeling.infer.KInferState;
                    modifyState(): org.kevoree.modeling.infer.KInferState;
                    private internal_load(raw);
                    train(trainingSet: any[][], expectedResultSet: any[], callback: (p: java.lang.Throwable) => void): void;
                    infer(features: any[]): any;
                    accuracy(testSet: any[][], expectedResultSet: any[]): any;
                    clear(): void;
                    createEmptyState(): org.kevoree.modeling.infer.KInferState;
                }
                class AbstractKUniverse<A extends org.kevoree.modeling.KView, B extends org.kevoree.modeling.KUniverse<any, any, any>, C extends org.kevoree.modeling.KModel<any>> implements org.kevoree.modeling.KUniverse<any, any, any> {
                    _universe: number;
                    _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                    constructor(p_key: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    key(): number;
                    model(): C;
                    delete(cb: (p: any) => void): void;
                    time(timePoint: number): A;
                    internal_create(timePoint: number): A;
                    equals(obj: any): boolean;
                    origin(): B;
                    diverge(): B;
                    descendants(): java.util.List<B>;
                    lookupAllTimes(uuid: number, times: number[], cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                    listenAll(groupId: number, objects: number[], multiListener: (p: org.kevoree.modeling.KObject[]) => void): void;
                }
                class AbstractKView implements org.kevoree.modeling.KView {
                    _time: number;
                    _universe: number;
                    _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                    constructor(p_universe: number, _time: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    now(): number;
                    universe(): number;
                    setRoot(elem: org.kevoree.modeling.KObject, cb: (p: any) => void): void;
                    getRoot(cb: (p: any) => void): void;
                    select(query: string, cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                    lookup(kid: number, cb: (p: org.kevoree.modeling.KObject) => void): void;
                    lookupAll(keys: number[], cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                    create(clazz: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.KObject;
                    createByName(metaClassName: string): org.kevoree.modeling.KObject;
                    json(): org.kevoree.modeling.format.KModelFormat;
                    xmi(): org.kevoree.modeling.format.KModelFormat;
                    equals(obj: any): boolean;
                }
                class AbstractTimeWalker implements org.kevoree.modeling.KTimeWalker {
                    private _origin;
                    constructor(p_origin: org.kevoree.modeling.abs.AbstractKObject);
                    private internal_times(start, end, cb);
                    allTimes(cb: (p: number[]) => void): void;
                    timesBefore(endOfSearch: number, cb: (p: number[]) => void): void;
                    timesAfter(beginningOfSearch: number, cb: (p: number[]) => void): void;
                    timesBetween(beginningOfSearch: number, endOfSearch: number, cb: (p: number[]) => void): void;
                }
                interface KLazyResolver {
                    meta(): org.kevoree.modeling.meta.KMeta;
                }
            }
            module cdn {
                interface KContentDeliveryDriver {
                    get(keys: org.kevoree.modeling.KContentKey[], callback: (p: string[]) => void): void;
                    atomicGetIncrement(key: org.kevoree.modeling.KContentKey, cb: (p: number) => void): void;
                    put(request: org.kevoree.modeling.cdn.KContentPutRequest, error: (p: java.lang.Throwable) => void): void;
                    remove(keys: string[], error: (p: java.lang.Throwable) => void): void;
                    connect(callback: (p: java.lang.Throwable) => void): void;
                    close(callback: (p: java.lang.Throwable) => void): void;
                    registerListener(groupId: number, origin: org.kevoree.modeling.KObject, listener: (p: org.kevoree.modeling.KObject, p1: org.kevoree.modeling.meta.KMeta[]) => void): void;
                    registerMultiListener(groupId: number, origin: org.kevoree.modeling.KUniverse<any, any, any>, objects: number[], listener: (p: org.kevoree.modeling.KObject[]) => void): void;
                    unregisterGroup(groupId: number): void;
                    send(msgs: org.kevoree.modeling.message.KMessage): void;
                    setManager(manager: org.kevoree.modeling.memory.manager.KMemoryManager): void;
                }
                interface KContentPutRequest {
                    put(p_key: org.kevoree.modeling.KContentKey, p_payload: string): void;
                    getKey(index: number): org.kevoree.modeling.KContentKey;
                    getContent(index: number): string;
                    size(): number;
                }
                module impl {
                    class ContentPutRequest implements org.kevoree.modeling.cdn.KContentPutRequest {
                        private _content;
                        private static KEY_INDEX;
                        private static CONTENT_INDEX;
                        private static SIZE_INDEX;
                        private _size;
                        constructor(requestSize: number);
                        put(p_key: org.kevoree.modeling.KContentKey, p_payload: string): void;
                        getKey(index: number): org.kevoree.modeling.KContentKey;
                        getContent(index: number): string;
                        size(): number;
                    }
                    class MemoryContentDeliveryDriver implements org.kevoree.modeling.cdn.KContentDeliveryDriver {
                        private backend;
                        private _localEventListeners;
                        static DEBUG: boolean;
                        atomicGetIncrement(key: org.kevoree.modeling.KContentKey, cb: (p: number) => void): void;
                        get(keys: org.kevoree.modeling.KContentKey[], callback: (p: string[]) => void): void;
                        put(p_request: org.kevoree.modeling.cdn.KContentPutRequest, p_callback: (p: java.lang.Throwable) => void): void;
                        remove(keys: string[], callback: (p: java.lang.Throwable) => void): void;
                        connect(callback: (p: java.lang.Throwable) => void): void;
                        close(callback: (p: java.lang.Throwable) => void): void;
                        registerListener(groupId: number, p_origin: org.kevoree.modeling.KObject, p_listener: (p: org.kevoree.modeling.KObject, p1: org.kevoree.modeling.meta.KMeta[]) => void): void;
                        unregisterGroup(groupId: number): void;
                        registerMultiListener(groupId: number, origin: org.kevoree.modeling.KUniverse<any, any, any>, objects: number[], listener: (p: org.kevoree.modeling.KObject[]) => void): void;
                        send(msgs: org.kevoree.modeling.message.KMessage): void;
                        setManager(manager: org.kevoree.modeling.memory.manager.KMemoryManager): void;
                    }
                }
            }
            module defer {
                interface KDefer {
                    wait(resultName: string): (p: any) => void;
                    waitDefer(previous: org.kevoree.modeling.defer.KDefer): org.kevoree.modeling.defer.KDefer;
                    isDone(): boolean;
                    getResult(resultName: string): any;
                    then(cb: (p: any) => void): void;
                    next(): org.kevoree.modeling.defer.KDefer;
                }
                module impl {
                    class Defer implements org.kevoree.modeling.defer.KDefer {
                        private _isDone;
                        _isReady: boolean;
                        private _nbRecResult;
                        private _nbExpectedResult;
                        private _nextTasks;
                        private _results;
                        private _thenCB;
                        constructor();
                        setDoneOrRegister(next: org.kevoree.modeling.defer.KDefer): boolean;
                        equals(obj: any): boolean;
                        private informParentEnd(end);
                        waitDefer(p_previous: org.kevoree.modeling.defer.KDefer): org.kevoree.modeling.defer.KDefer;
                        next(): org.kevoree.modeling.defer.KDefer;
                        wait(resultName: string): (p: any) => void;
                        isDone(): boolean;
                        getResult(resultName: string): any;
                        then(cb: (p: any) => void): void;
                    }
                }
            }
            module event {
                interface KEventListener {
                    on(src: org.kevoree.modeling.KObject, modifications: org.kevoree.modeling.meta.KMeta[]): void;
                }
                interface KEventMultiListener {
                    on(objects: org.kevoree.modeling.KObject[]): void;
                }
                module impl {
                    class LocalEventListeners {
                        private _manager;
                        private _internalListenerKeyGen;
                        private _simpleListener;
                        private _multiListener;
                        private _listener2Object;
                        private _listener2Objects;
                        private _obj2Listener;
                        private _group2Listener;
                        constructor();
                        registerListener(groupId: number, origin: org.kevoree.modeling.KObject, listener: (p: org.kevoree.modeling.KObject, p1: org.kevoree.modeling.meta.KMeta[]) => void): void;
                        registerListenerAll(groupId: number, universe: number, objects: number[], listener: (p: org.kevoree.modeling.KObject[]) => void): void;
                        unregister(groupId: number): void;
                        clear(): void;
                        setManager(manager: org.kevoree.modeling.memory.manager.KMemoryManager): void;
                        dispatch(param: org.kevoree.modeling.message.KMessage): void;
                    }
                }
            }
            module extrapolation {
                interface Extrapolation {
                    extrapolate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute): any;
                    mutate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute, payload: any): void;
                    save(cache: any, attribute: org.kevoree.modeling.meta.KMetaAttribute): string;
                    load(payload: string, attribute: org.kevoree.modeling.meta.KMetaAttribute, now: number): any;
                }
                module impl {
                    class DiscreteExtrapolation implements org.kevoree.modeling.extrapolation.Extrapolation {
                        private static INSTANCE;
                        static instance(): org.kevoree.modeling.extrapolation.Extrapolation;
                        extrapolate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute): any;
                        mutate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute, payload: any): void;
                        save(cache: any, attribute: org.kevoree.modeling.meta.KMetaAttribute): string;
                        load(payload: string, attribute: org.kevoree.modeling.meta.KMetaAttribute, now: number): any;
                    }
                    class PolynomialExtrapolation implements org.kevoree.modeling.extrapolation.Extrapolation {
                        private static _maxDegree;
                        private static DEGREE;
                        private static NUMSAMPLES;
                        private static STEP;
                        private static LASTTIME;
                        private static WEIGHTS;
                        private static INSTANCE;
                        extrapolate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute): any;
                        private extrapolateValue(encodedPolynomial, time, timeOrigin);
                        private maxErr(precision, degree);
                        insert(time: number, value: number, timeOrigin: number, raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment, index: number, precision: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean;
                        private tempError(computedWeights, times, values);
                        private test_extrapolate(time, weights);
                        private internal_extrapolate(t, encodedPolynomial);
                        private initial_feed(time, value, raw, index, metaClass);
                        mutate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute, payload: any): void;
                        save(cache: any, attribute: org.kevoree.modeling.meta.KMetaAttribute): string;
                        load(payload: string, attribute: org.kevoree.modeling.meta.KMetaAttribute, now: number): any;
                        private castNumber(payload);
                        static instance(): org.kevoree.modeling.extrapolation.Extrapolation;
                    }
                    module maths {
                        class AdjLinearSolverQr {
                            numRows: number;
                            numCols: number;
                            private decomposer;
                            maxRows: number;
                            maxCols: number;
                            Q: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            R: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            private Y;
                            private Z;
                            setA(A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): boolean;
                            private solveU(U, b, n);
                            solve(B: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, X: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void;
                            constructor();
                            setMaxSize(maxRows: number, maxCols: number): void;
                        }
                        class DenseMatrix64F {
                            numRows: number;
                            numCols: number;
                            data: number[];
                            static MULT_COLUMN_SWITCH: number;
                            static multTransA_smallMV(A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, B: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, C: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void;
                            static multTransA_reorderMV(A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, B: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, C: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void;
                            static multTransA_reorderMM(a: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, b: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, c: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void;
                            static multTransA_smallMM(a: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, b: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, c: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void;
                            static multTransA(a: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, b: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, c: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void;
                            static setIdentity(mat: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void;
                            static widentity(width: number): org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            static identity(numRows: number, numCols: number): org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            static fill(a: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, value: number): void;
                            get(index: number): number;
                            set(index: number, val: number): number;
                            plus(index: number, val: number): number;
                            constructor(numRows: number, numCols: number);
                            reshape(numRows: number, numCols: number, saveValues: boolean): void;
                            cset(row: number, col: number, value: number): void;
                            unsafe_get(row: number, col: number): number;
                            getNumElements(): number;
                        }
                        class PolynomialFitEjml {
                            A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            coef: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            y: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            solver: org.kevoree.modeling.extrapolation.impl.maths.AdjLinearSolverQr;
                            constructor(degree: number);
                            getCoef(): number[];
                            fit(samplePoints: number[], observations: number[]): void;
                        }
                        class QRDecompositionHouseholderColumn_D64 {
                            dataQR: number[][];
                            v: number[];
                            numCols: number;
                            numRows: number;
                            minLength: number;
                            gammas: number[];
                            gamma: number;
                            tau: number;
                            error: boolean;
                            setExpectedMaxSize(numRows: number, numCols: number): void;
                            getQ(Q: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, compact: boolean): org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            getR(R: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, compact: boolean): org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F;
                            decompose(A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): boolean;
                            convertToColumnMajor(A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F): void;
                            householder(j: number): void;
                            updateA(w: number): void;
                            static findMax(u: number[], startU: number, length: number): number;
                            static divideElements(j: number, numRows: number, u: number[], u_0: number): void;
                            static computeTauAndDivide(j: number, numRows: number, u: number[], max: number): number;
                            static rank1UpdateMultR(A: org.kevoree.modeling.extrapolation.impl.maths.DenseMatrix64F, u: number[], gamma: number, colA0: number, w0: number, w1: number, _temp: number[]): void;
                        }
                    }
                }
            }
            module format {
                interface KModelFormat {
                    save(model: org.kevoree.modeling.KObject, cb: (p: string) => void): void;
                    saveRoot(cb: (p: string) => void): void;
                    load(payload: string, cb: (p: any) => void): void;
                }
                module json {
                    class JsonFormat implements org.kevoree.modeling.format.KModelFormat {
                        static KEY_META: string;
                        static KEY_UUID: string;
                        static KEY_ROOT: string;
                        private _manager;
                        private _universe;
                        private _time;
                        private static NULL_PARAM_MSG;
                        constructor(p_universe: number, p_time: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                        save(model: org.kevoree.modeling.KObject, cb: (p: string) => void): void;
                        saveRoot(cb: (p: string) => void): void;
                        load(payload: string, cb: (p: any) => void): void;
                    }
                    class JsonModelLoader {
                        static load(manager: org.kevoree.modeling.memory.manager.KMemoryManager, universe: number, time: number, payload: string, callback: (p: java.lang.Throwable) => void): void;
                        private static loadObj(p_param, manager, universe, time, p_mappedKeys, p_rootElem);
                        private static transposeArr(plainRawSet, p_mappedKeys);
                    }
                    class JsonModelSerializer {
                        static serialize(model: org.kevoree.modeling.KObject, callback: (p: string) => void): void;
                        static printJSON(elem: org.kevoree.modeling.KObject, builder: java.lang.StringBuilder, isRoot: boolean): void;
                    }
                    class JsonObjectReader {
                        private readObject;
                        parseObject(payload: string): void;
                        get(name: string): any;
                        getAsStringArray(name: string): string[];
                        keys(): string[];
                    }
                    class JsonString {
                        private static ESCAPE_CHAR;
                        static encodeBuffer(buffer: java.lang.StringBuilder, chain: string): void;
                        static encode(p_chain: string): string;
                        static unescape(p_src: string): string;
                    }
                }
                module xmi {
                    class SerializationContext {
                        ignoreGeneratedID: boolean;
                        model: org.kevoree.modeling.KObject;
                        finishCallback: (p: string) => void;
                        printer: java.lang.StringBuilder;
                        attributesVisitor: (p: org.kevoree.modeling.meta.KMetaAttribute, p1: any) => void;
                        addressTable: org.kevoree.modeling.memory.struct.map.impl.ArrayLongHashMap<any>;
                        elementsCount: org.kevoree.modeling.memory.struct.map.impl.ArrayStringHashMap<any>;
                        packageList: java.util.ArrayList<string>;
                    }
                    class XMILoadingContext {
                        xmiReader: org.kevoree.modeling.format.xmi.XmlParser;
                        loadedRoots: org.kevoree.modeling.KObject;
                        resolvers: java.util.ArrayList<org.kevoree.modeling.format.xmi.XMIResolveCommand>;
                        map: org.kevoree.modeling.memory.struct.map.impl.ArrayStringHashMap<any>;
                        elementsCount: org.kevoree.modeling.memory.struct.map.impl.ArrayStringHashMap<any>;
                        successCallback: (p: java.lang.Throwable) => void;
                    }
                    class XMIModelLoader {
                        static LOADER_XMI_LOCAL_NAME: string;
                        static LOADER_XMI_XSI: string;
                        static LOADER_XMI_NS_URI: string;
                        static unescapeXml(src: string): string;
                        static load(manager: org.kevoree.modeling.memory.manager.KMemoryManager, universe: number, time: number, str: string, callback: (p: java.lang.Throwable) => void): void;
                        private static deserialize(manager, universe, time, context);
                        private static callFactory(manager, universe, time, ctx, objectType);
                        private static loadObject(manager, universe, time, ctx, xmiAddress, objectType);
                    }
                    class XMIModelSerializer {
                        static save(model: org.kevoree.modeling.KObject, callback: (p: string) => void): void;
                    }
                    class XMIResolveCommand {
                        private context;
                        private target;
                        private mutatorType;
                        private refName;
                        private ref;
                        constructor(context: org.kevoree.modeling.format.xmi.XMILoadingContext, target: org.kevoree.modeling.KObject, mutatorType: org.kevoree.modeling.KActionType, refName: string, ref: string);
                        run(): void;
                    }
                    class XmiFormat implements org.kevoree.modeling.format.KModelFormat {
                        private _manager;
                        private _universe;
                        private _time;
                        constructor(p_universe: number, p_time: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                        save(model: org.kevoree.modeling.KObject, cb: (p: string) => void): void;
                        saveRoot(cb: (p: string) => void): void;
                        load(payload: string, cb: (p: any) => void): void;
                    }
                    class XmlParser {
                        private payload;
                        private current;
                        private currentChar;
                        private tagName;
                        private tagPrefix;
                        private attributePrefix;
                        private readSingleton;
                        private attributesNames;
                        private attributesPrefixes;
                        private attributesValues;
                        private attributeName;
                        private attributeValue;
                        constructor(str: string);
                        getTagPrefix(): string;
                        hasNext(): boolean;
                        getLocalName(): string;
                        getAttributeCount(): number;
                        getAttributeLocalName(i: number): string;
                        getAttributePrefix(i: number): string;
                        getAttributeValue(i: number): string;
                        private readChar();
                        next(): org.kevoree.modeling.format.xmi.XmlToken;
                        private read_lessThan();
                        private read_upperThan();
                        private read_xmlHeader();
                        private read_closingTag();
                        private read_openTag();
                        private read_tagName();
                        private read_attributes();
                    }
                    class XmlToken {
                        static XML_HEADER: XmlToken;
                        static END_DOCUMENT: XmlToken;
                        static START_TAG: XmlToken;
                        static END_TAG: XmlToken;
                        static COMMENT: XmlToken;
                        static SINGLETON_TAG: XmlToken;
                        equals(other: any): boolean;
                        static _XmlTokenVALUES: XmlToken[];
                        static values(): XmlToken[];
                    }
                }
            }
            module infer {
                class AnalyticKInfer extends org.kevoree.modeling.abs.AbstractKObjectInfer {
                    constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    train(trainingSet: any[][], expectedResultSet: any[], callback: (p: java.lang.Throwable) => void): void;
                    infer(features: any[]): any;
                    accuracy(testSet: any[][], expectedResultSet: any[]): any;
                    clear(): void;
                    createEmptyState(): org.kevoree.modeling.infer.KInferState;
                }
                class GaussianClassificationKInfer extends org.kevoree.modeling.abs.AbstractKObjectInfer {
                    private alpha;
                    constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    getAlpha(): number;
                    setAlpha(alpha: number): void;
                    train(trainingSet: any[][], expectedResultSet: any[], callback: (p: java.lang.Throwable) => void): void;
                    infer(features: any[]): any;
                    accuracy(testSet: any[][], expectedResultSet: any[]): any;
                    clear(): void;
                    createEmptyState(): org.kevoree.modeling.infer.KInferState;
                }
                interface KInfer extends org.kevoree.modeling.KObject {
                    train(trainingSet: any[][], expectedResultSet: any[], callback: (p: java.lang.Throwable) => void): void;
                    infer(features: any[]): any;
                    accuracy(testSet: any[][], expectedResultSet: any[]): any;
                    clear(): void;
                }
                class KInferState {
                    save(): string;
                    load(payload: string): void;
                    isDirty(): boolean;
                    cloneState(): org.kevoree.modeling.infer.KInferState;
                }
                class LinearRegressionKInfer extends org.kevoree.modeling.abs.AbstractKObjectInfer {
                    private alpha;
                    private iterations;
                    constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    getAlpha(): number;
                    setAlpha(alpha: number): void;
                    getIterations(): number;
                    setIterations(iterations: number): void;
                    private calculate(weights, features);
                    train(trainingSet: any[][], expectedResultSet: any[], callback: (p: java.lang.Throwable) => void): void;
                    infer(features: any[]): any;
                    accuracy(testSet: any[][], expectedResultSet: any[]): any;
                    clear(): void;
                    createEmptyState(): org.kevoree.modeling.infer.KInferState;
                }
                class PerceptronClassificationKInfer extends org.kevoree.modeling.abs.AbstractKObjectInfer {
                    private alpha;
                    private iterations;
                    constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    getAlpha(): number;
                    setAlpha(alpha: number): void;
                    getIterations(): number;
                    setIterations(iterations: number): void;
                    private calculate(weights, features);
                    train(trainingSet: any[][], expectedResultSet: any[], callback: (p: java.lang.Throwable) => void): void;
                    infer(features: any[]): any;
                    accuracy(testSet: any[][], expectedResultSet: any[]): any;
                    clear(): void;
                    createEmptyState(): org.kevoree.modeling.infer.KInferState;
                }
                class PolynomialOfflineKInfer extends org.kevoree.modeling.abs.AbstractKObjectInfer {
                    maxDegree: number;
                    toleratedErr: number;
                    constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    getToleratedErr(): number;
                    setToleratedErr(toleratedErr: number): void;
                    getMaxDegree(): number;
                    setMaxDegree(maxDegree: number): void;
                    private calculateLong(time, weights, timeOrigin, unit);
                    private calculate(weights, t);
                    train(trainingSet: any[][], expectedResultSet: any[], callback: (p: java.lang.Throwable) => void): void;
                    infer(features: any[]): any;
                    accuracy(testSet: any[][], expectedResultSet: any[]): any;
                    clear(): void;
                    createEmptyState(): org.kevoree.modeling.infer.KInferState;
                }
                class PolynomialOnlineKInfer extends org.kevoree.modeling.abs.AbstractKObjectInfer {
                    maxDegree: number;
                    toleratedErr: number;
                    constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    getToleratedErr(): number;
                    setToleratedErr(toleratedErr: number): void;
                    getMaxDegree(): number;
                    setMaxDegree(maxDegree: number): void;
                    private calculateLong(time, weights, timeOrigin, unit);
                    private calculate(weights, t);
                    train(trainingSet: any[][], expectedResultSet: any[], callback: (p: java.lang.Throwable) => void): void;
                    infer(features: any[]): any;
                    accuracy(testSet: any[][], expectedResultSet: any[]): any;
                    clear(): void;
                    createEmptyState(): org.kevoree.modeling.infer.KInferState;
                }
                class WinnowClassificationKInfer extends org.kevoree.modeling.abs.AbstractKObjectInfer {
                    private alpha;
                    private beta;
                    constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    getAlpha(): number;
                    setAlpha(alpha: number): void;
                    getBeta(): number;
                    setBeta(beta: number): void;
                    private calculate(weights, features);
                    train(trainingSet: any[][], expectedResultSet: any[], callback: (p: java.lang.Throwable) => void): void;
                    infer(features: any[]): any;
                    accuracy(testSet: any[][], expectedResultSet: any[]): any;
                    clear(): void;
                    createEmptyState(): org.kevoree.modeling.infer.KInferState;
                }
                module states {
                    class AnalyticKInferState extends org.kevoree.modeling.infer.KInferState {
                        private _isDirty;
                        private sumSquares;
                        private sum;
                        private nb;
                        private min;
                        private max;
                        getSumSquares(): number;
                        setSumSquares(sumSquares: number): void;
                        getMin(): number;
                        setMin(min: number): void;
                        getMax(): number;
                        setMax(max: number): void;
                        getNb(): number;
                        setNb(nb: number): void;
                        getSum(): number;
                        setSum(sum: number): void;
                        getAverage(): number;
                        train(value: number): void;
                        getVariance(): number;
                        clear(): void;
                        save(): string;
                        load(payload: string): void;
                        isDirty(): boolean;
                        cloneState(): org.kevoree.modeling.infer.KInferState;
                    }
                    class BayesianClassificationState extends org.kevoree.modeling.infer.KInferState {
                        private states;
                        private classStats;
                        private numOfFeatures;
                        private numOfClasses;
                        private static stateSep;
                        private static interStateSep;
                        initialize(metaFeatures: any[], MetaClassification: any): void;
                        predict(features: any[]): number;
                        train(features: any[], classNum: number): void;
                        save(): string;
                        load(payload: string): void;
                        isDirty(): boolean;
                        cloneState(): org.kevoree.modeling.infer.KInferState;
                    }
                    class DoubleArrayKInferState extends org.kevoree.modeling.infer.KInferState {
                        private _isDirty;
                        private weights;
                        save(): string;
                        load(payload: string): void;
                        isDirty(): boolean;
                        set_isDirty(value: boolean): void;
                        cloneState(): org.kevoree.modeling.infer.KInferState;
                        getWeights(): number[];
                        setWeights(weights: number[]): void;
                    }
                    class GaussianArrayKInferState extends org.kevoree.modeling.infer.KInferState {
                        private _isDirty;
                        private sumSquares;
                        private sum;
                        private epsilon;
                        private nb;
                        getSumSquares(): number[];
                        setSumSquares(sumSquares: number[]): void;
                        getNb(): number;
                        setNb(nb: number): void;
                        getSum(): number[];
                        setSum(sum: number[]): void;
                        calculateProbability(features: number[]): number;
                        infer(features: number[]): boolean;
                        getAverage(): number[];
                        train(features: number[], result: boolean, alpha: number): void;
                        getVariance(): number[];
                        clear(): void;
                        save(): string;
                        load(payload: string): void;
                        isDirty(): boolean;
                        cloneState(): org.kevoree.modeling.infer.KInferState;
                        getEpsilon(): number;
                    }
                    class PolynomialKInferState extends org.kevoree.modeling.infer.KInferState {
                        private _isDirty;
                        private timeOrigin;
                        private unit;
                        private weights;
                        getTimeOrigin(): number;
                        setTimeOrigin(timeOrigin: number): void;
                        is_isDirty(): boolean;
                        getUnit(): number;
                        setUnit(unit: number): void;
                        static maxError(coef: number[], normalizedTimes: number[], results: number[]): number;
                        private static internal_extrapolate(normalizedTime, coef);
                        save(): string;
                        load(payload: string): void;
                        isDirty(): boolean;
                        set_isDirty(value: boolean): void;
                        cloneState(): org.kevoree.modeling.infer.KInferState;
                        getWeights(): number[];
                        setWeights(weights: number[]): void;
                        infer(time: number): any;
                    }
                    module Bayesian {
                        class BayesianSubstate {
                            calculateProbability(feature: any): number;
                            train(feature: any): void;
                            save(separator: string): string;
                            load(payload: string, separator: string): void;
                            cloneState(): org.kevoree.modeling.infer.states.Bayesian.BayesianSubstate;
                        }
                        class EnumSubstate extends org.kevoree.modeling.infer.states.Bayesian.BayesianSubstate {
                            private counter;
                            private total;
                            getCounter(): number[];
                            setCounter(counter: number[]): void;
                            getTotal(): number;
                            setTotal(total: number): void;
                            initialize(number: number): void;
                            calculateProbability(feature: any): number;
                            train(feature: any): void;
                            save(separator: string): string;
                            load(payload: string, separator: string): void;
                            cloneState(): org.kevoree.modeling.infer.states.Bayesian.BayesianSubstate;
                        }
                        class GaussianSubState extends org.kevoree.modeling.infer.states.Bayesian.BayesianSubstate {
                            private sumSquares;
                            private sum;
                            private nb;
                            getSumSquares(): number;
                            setSumSquares(sumSquares: number): void;
                            getNb(): number;
                            setNb(nb: number): void;
                            getSum(): number;
                            setSum(sum: number): void;
                            calculateProbability(feature: any): number;
                            getAverage(): number;
                            train(feature: any): void;
                            getVariance(): number;
                            clear(): void;
                            save(separator: string): string;
                            load(payload: string, separator: string): void;
                            cloneState(): org.kevoree.modeling.infer.states.Bayesian.BayesianSubstate;
                        }
                    }
                }
            }
            module memory {
                interface KMemoryElement {
                    isDirty(): boolean;
                    serialize(metaModel: org.kevoree.modeling.meta.KMetaModel): string;
                    setClean(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                    setDirty(): void;
                    unserialize(key: org.kevoree.modeling.KContentKey, payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                    counter(): number;
                    inc(): void;
                    dec(): void;
                    free(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                }
                interface KMemoryFactory {
                    newCacheSegment(originTime: number): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
                    newLongTree(): org.kevoree.modeling.memory.struct.tree.KLongTree;
                    newLongLongTree(): org.kevoree.modeling.memory.struct.tree.KLongLongTree;
                }
                module cache {
                    interface KCache {
                        get(universe: number, time: number, obj: number): org.kevoree.modeling.memory.KMemoryElement;
                        put(universe: number, time: number, obj: number, payload: org.kevoree.modeling.memory.KMemoryElement): void;
                        dirties(): org.kevoree.modeling.memory.cache.impl.KCacheDirty[];
                        clear(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                        clean(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                        monitor(origin: org.kevoree.modeling.KObject): void;
                        size(): number;
                    }
                    module impl {
                        class HashMemoryCache implements org.kevoree.modeling.memory.cache.KCache {
                            private elementData;
                            private elementCount;
                            private elementDataSize;
                            private loadFactor;
                            private initalCapacity;
                            private threshold;
                            get(universe: number, time: number, obj: number): org.kevoree.modeling.memory.KMemoryElement;
                            put(universe: number, time: number, obj: number, payload: org.kevoree.modeling.memory.KMemoryElement): void;
                            private complex_insert(previousIndex, hash, universe, time, obj);
                            dirties(): org.kevoree.modeling.memory.cache.impl.KCacheDirty[];
                            clean(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                            monitor(origin: org.kevoree.modeling.KObject): void;
                            size(): number;
                            private remove(universe, time, obj, p_metaModel);
                            constructor();
                            clear(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                        }
                        module HashMemoryCache {
                            class Entry {
                                next: org.kevoree.modeling.memory.cache.impl.HashMemoryCache.Entry;
                                universe: number;
                                time: number;
                                obj: number;
                                value: org.kevoree.modeling.memory.KMemoryElement;
                            }
                        }
                        class KCacheDirty {
                            key: org.kevoree.modeling.KContentKey;
                            object: org.kevoree.modeling.memory.KMemoryElement;
                            constructor(key: org.kevoree.modeling.KContentKey, object: org.kevoree.modeling.memory.KMemoryElement);
                        }
                    }
                }
                module manager {
                    class AccessMode {
                        static RESOLVE: AccessMode;
                        static NEW: AccessMode;
                        static DELETE: AccessMode;
                        equals(other: any): boolean;
                        static _AccessModeVALUES: AccessMode[];
                        static values(): AccessMode[];
                    }
                    interface KMemoryManager {
                        cdn(): org.kevoree.modeling.cdn.KContentDeliveryDriver;
                        model(): org.kevoree.modeling.KModel<any>;
                        cache(): org.kevoree.modeling.memory.cache.KCache;
                        lookup(universe: number, time: number, uuid: number, callback: (p: org.kevoree.modeling.KObject) => void): void;
                        lookupAllobjects(universe: number, time: number, uuid: number[], callback: (p: org.kevoree.modeling.KObject[]) => void): void;
                        lookupAlltimes(universe: number, time: number[], uuid: number, callback: (p: org.kevoree.modeling.KObject[]) => void): void;
                        segment(universe: number, time: number, uuid: number, accessMode: org.kevoree.modeling.memory.manager.AccessMode, metaClass: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
                        save(callback: (p: java.lang.Throwable) => void): void;
                        discard(universe: org.kevoree.modeling.KUniverse<any, any, any>, callback: (p: java.lang.Throwable) => void): void;
                        delete(universe: org.kevoree.modeling.KUniverse<any, any, any>, callback: (p: java.lang.Throwable) => void): void;
                        initKObject(obj: org.kevoree.modeling.KObject): void;
                        initUniverse(universe: org.kevoree.modeling.KUniverse<any, any, any>, parent: org.kevoree.modeling.KUniverse<any, any, any>): void;
                        nextUniverseKey(): number;
                        nextObjectKey(): number;
                        nextModelKey(): number;
                        nextGroupKey(): number;
                        getRoot(universe: number, time: number, callback: (p: org.kevoree.modeling.KObject) => void): void;
                        setRoot(newRoot: org.kevoree.modeling.KObject, callback: (p: java.lang.Throwable) => void): void;
                        setContentDeliveryDriver(driver: org.kevoree.modeling.cdn.KContentDeliveryDriver): void;
                        setScheduler(scheduler: org.kevoree.modeling.scheduler.KScheduler): void;
                        operationManager(): org.kevoree.modeling.operation.KOperationManager;
                        connect(callback: (p: java.lang.Throwable) => void): void;
                        close(callback: (p: java.lang.Throwable) => void): void;
                        parentUniverseKey(currentUniverseKey: number): number;
                        descendantsUniverseKeys(currentUniverseKey: number): number[];
                        reload(keys: org.kevoree.modeling.KContentKey[], callback: (p: java.lang.Throwable) => void): void;
                        cleanCache(): void;
                        setFactory(factory: org.kevoree.modeling.memory.KMemoryFactory): void;
                    }
                    module impl {
                        class HeapMemoryManager implements org.kevoree.modeling.memory.manager.KMemoryManager {
                            private static OUT_OF_CACHE_MESSAGE;
                            private static UNIVERSE_NOT_CONNECTED_ERROR;
                            private _db;
                            private _operationManager;
                            private _scheduler;
                            private _model;
                            private _factory;
                            private _objectKeyCalculator;
                            private _universeKeyCalculator;
                            private _modelKeyCalculator;
                            private _groupKeyCalculator;
                            private isConnected;
                            private UNIVERSE_INDEX;
                            private OBJ_INDEX;
                            private GLO_TREE_INDEX;
                            private _cache;
                            private static zeroPrefix;
                            constructor(model: org.kevoree.modeling.KModel<any>);
                            cache(): org.kevoree.modeling.memory.cache.KCache;
                            model(): org.kevoree.modeling.KModel<any>;
                            close(callback: (p: java.lang.Throwable) => void): void;
                            nextUniverseKey(): number;
                            nextObjectKey(): number;
                            nextModelKey(): number;
                            nextGroupKey(): number;
                            globalUniverseOrder(): org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongHashMap;
                            initUniverse(p_universe: org.kevoree.modeling.KUniverse<any, any, any>, p_parent: org.kevoree.modeling.KUniverse<any, any, any>): void;
                            parentUniverseKey(currentUniverseKey: number): number;
                            descendantsUniverseKeys(currentUniverseKey: number): number[];
                            save(callback: (p: java.lang.Throwable) => void): void;
                            initKObject(obj: org.kevoree.modeling.KObject): void;
                            connect(connectCallback: (p: java.lang.Throwable) => void): void;
                            segment(universe: number, time: number, uuid: number, accessMode: org.kevoree.modeling.memory.manager.AccessMode, metaClass: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
                            discard(p_universe: org.kevoree.modeling.KUniverse<any, any, any>, callback: (p: java.lang.Throwable) => void): void;
                            delete(p_universe: org.kevoree.modeling.KUniverse<any, any, any>, callback: (p: java.lang.Throwable) => void): void;
                            lookup(universe: number, time: number, uuid: number, callback: (p: org.kevoree.modeling.KObject) => void): void;
                            lookupAllobjects(universe: number, time: number, uuids: number[], callback: (p: org.kevoree.modeling.KObject[]) => void): void;
                            lookupAlltimes(universe: number, time: number[], uuid: number, callback: (p: org.kevoree.modeling.KObject[]) => void): void;
                            cdn(): org.kevoree.modeling.cdn.KContentDeliveryDriver;
                            setContentDeliveryDriver(p_dataBase: org.kevoree.modeling.cdn.KContentDeliveryDriver): void;
                            setScheduler(p_scheduler: org.kevoree.modeling.scheduler.KScheduler): void;
                            operationManager(): org.kevoree.modeling.operation.KOperationManager;
                            getRoot(universe: number, time: number, callback: (p: org.kevoree.modeling.KObject) => void): void;
                            setRoot(newRoot: org.kevoree.modeling.KObject, callback: (p: java.lang.Throwable) => void): void;
                            reload(keys: org.kevoree.modeling.KContentKey[], callback: (p: java.lang.Throwable) => void): void;
                            cleanCache(): void;
                            setFactory(p_factory: org.kevoree.modeling.memory.KMemoryFactory): void;
                            bumpKeyToCache(contentKey: org.kevoree.modeling.KContentKey, callback: (p: org.kevoree.modeling.memory.KMemoryElement) => void): void;
                            bumpKeysToCache(contentKeys: org.kevoree.modeling.KContentKey[], callback: (p: org.kevoree.modeling.memory.KMemoryElement[]) => void): void;
                            private internal_unserialize(key, payload);
                        }
                        class JsonRaw {
                            static decode(payload: string, now: number, metaModel: org.kevoree.modeling.meta.KMetaModel, entry: org.kevoree.modeling.memory.struct.segment.impl.HeapMemorySegment): boolean;
                            static encode(raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment, uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, isRoot: boolean): string;
                        }
                        class KeyCalculator {
                            private _prefix;
                            private _currentIndex;
                            constructor(prefix: number, currentIndex: number);
                            nextKey(): number;
                            lastComputedIndex(): number;
                            prefix(): number;
                        }
                        class LookupAllRunnable implements java.lang.Runnable {
                            private _universe;
                            private _time;
                            private _keys;
                            private _callback;
                            private _store;
                            constructor(p_universe: number, p_time: number, p_keys: number[], p_callback: (p: org.kevoree.modeling.KObject[]) => void, p_store: org.kevoree.modeling.memory.manager.impl.HeapMemoryManager);
                            run(): void;
                        }
                        class ResolutionHelper {
                            static resolve_trees(universe: number, time: number, uuid: number, cache: org.kevoree.modeling.memory.cache.KCache): org.kevoree.modeling.memory.manager.impl.ResolutionResult;
                            static resolve_universe(globalTree: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongHashMap, objUniverseTree: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongHashMap, timeToResolve: number, originUniverseId: number): number;
                            static universeSelectByRange(globalTree: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongHashMap, objUniverseTree: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongHashMap, rangeMin: number, rangeMax: number, originUniverseId: number): number[];
                        }
                        class ResolutionResult {
                            universeTree: org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongHashMap;
                            timeTree: org.kevoree.modeling.memory.struct.tree.KLongTree;
                            segment: org.kevoree.modeling.memory.struct.segment.KMemorySegment;
                            universe: number;
                            time: number;
                            uuid: number;
                        }
                    }
                }
                module struct {
                    class HeapMemoryFactory implements org.kevoree.modeling.memory.KMemoryFactory {
                        newCacheSegment(originTime: number): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
                        newLongTree(): org.kevoree.modeling.memory.struct.tree.KLongTree;
                        newLongLongTree(): org.kevoree.modeling.memory.struct.tree.KLongLongTree;
                    }
                    module map {
                        interface KIntHashMap<V> {
                            get(key: number): V;
                            put(key: number, value: V): void;
                            each(callback: (p: number, p1: V) => void): void;
                        }
                        interface KIntHashMapCallBack<V> {
                            on(key: number, value: V): void;
                        }
                        interface KLongHashMap<V> {
                            get(key: number): V;
                            put(key: number, value: V): void;
                            each(callback: (p: number, p1: V) => void): void;
                        }
                        interface KLongHashMapCallBack<V> {
                            on(key: number, value: V): void;
                        }
                        interface KLongLongHashMap {
                            get(key: number): number;
                            put(key: number, value: number): void;
                            each(callback: (p: number, p1: number) => void): void;
                        }
                        interface KLongLongHashMapCallBack<V> {
                            on(key: number, value: number): void;
                        }
                        interface KStringHashMap<V> {
                            get(key: string): V;
                            put(key: string, value: V): void;
                            each(callback: (p: string, p1: V) => void): void;
                        }
                        interface KStringHashMapCallBack<V> {
                            on(key: string, value: V): void;
                        }
                        module impl {
                            class ArrayIntHashMap<V> implements org.kevoree.modeling.memory.struct.map.KIntHashMap<any> {
                                constructor(initalCapacity: number, loadFactor: number);
                                clear(): void;
                                get(key: number): V;
                                put(key: number, pval: V): V;
                                containsKey(key: number): boolean;
                                remove(key: number): V;
                                size(): number;
                                each(callback: (p: number, p1: V) => void): void;
                            }
                            class ArrayLongHashMap<V> implements org.kevoree.modeling.memory.struct.map.KLongHashMap<any> {
                                constructor(initalCapacity: number, loadFactor: number);
                                clear(): void;
                                get(key: number): V;
                                put(key: number, pval: V): V;
                                containsKey(key: number): boolean;
                                remove(key: number): V;
                                size(): number;
                                each(callback: (p: number, p1: V) => void): void;
                            }
                            class ArrayLongLongHashMap implements org.kevoree.modeling.memory.KMemoryElement, org.kevoree.modeling.memory.struct.map.KLongLongHashMap {
                                private _counter;
                                private _isDirty;
                                static ELEMENT_SEP: string;
                                static CHUNK_SEP: string;
                                constructor(initalCapacity: number, loadFactor: number);
                                clear(): void;
                                get(key: number): number;
                                put(key: number, pval: number): number;
                                containsKey(key: number): boolean;
                                remove(key: number): number;
                                size(): number;
                                each(callback: (p: number, p1: number) => void): void;
                                counter(): number;
                                inc(): void;
                                dec(): void;
                                free(): void;
                                isDirty(): boolean;
                                setClean(mm: any): void;
                                setDirty(): void;
                                serialize(m: any): string;
                                unserialize(key: org.kevoree.modeling.KContentKey, payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                            }
                            class ArrayStringHashMap<V> implements org.kevoree.modeling.memory.struct.map.KStringHashMap<any> {
                                constructor(initalCapacity: number, loadFactor: number);
                                clear(): void;
                                get(key: string): V;
                                put(key: string, pval: V): V;
                                containsKey(key: string): boolean;
                                remove(key: string): V;
                                size(): number;
                                each(callback: (p: string, p1: V) => void): void;
                            }
                        }
                    }
                    module segment {
                        interface KMemorySegment extends org.kevoree.modeling.memory.KMemoryElement {
                            clone(newtimeOrigin: number, metaClass: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
                            set(index: number, content: any, metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                            get(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): any;
                            getRef(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number[];
                            addRef(index: number, newRef: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean;
                            removeRef(index: number, previousRef: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean;
                            getInfer(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number[];
                            getInferElem(index: number, arrayIndex: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;
                            setInferElem(index: number, arrayIndex: number, valueToInsert: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                            extendInfer(index: number, newSize: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                            modifiedIndexes(metaClass: org.kevoree.modeling.meta.KMetaClass): number[];
                            init(metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                            metaClassIndex(): number;
                            originTime(): number;
                        }
                        module impl {
                            class HeapMemorySegment implements org.kevoree.modeling.memory.struct.segment.KMemorySegment {
                                private raw;
                                private _counter;
                                private _metaClassIndex;
                                private _modifiedIndexes;
                                private _dirty;
                                private _timeOrigin;
                                constructor(p_timeOrigin: number);
                                init(p_metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                                metaClassIndex(): number;
                                originTime(): number;
                                isDirty(): boolean;
                                serialize(metaModel: org.kevoree.modeling.meta.KMetaModel): string;
                                modifiedIndexes(p_metaClass: org.kevoree.modeling.meta.KMetaClass): number[];
                                setClean(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                setDirty(): void;
                                unserialize(key: org.kevoree.modeling.KContentKey, payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                counter(): number;
                                inc(): void;
                                dec(): void;
                                free(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                get(index: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass): any;
                                getRef(index: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass): number[];
                                addRef(index: number, newRef: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean;
                                removeRef(index: number, newRef: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean;
                                getInfer(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number[];
                                getInferElem(index: number, arrayIndex: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;
                                setInferElem(index: number, arrayIndex: number, valueToInsert: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                                extendInfer(index: number, newSize: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                                set(index: number, content: any, p_metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                                clone(newTimeOrigin: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
                            }
                        }
                    }
                    module tree {
                        interface KLongLongTree extends org.kevoree.modeling.memory.struct.tree.KTree {
                            insert(key: number, value: number): void;
                            previousOrEqualValue(key: number): number;
                            lookupValue(key: number): number;
                        }
                        interface KLongTree extends org.kevoree.modeling.memory.struct.tree.KTree {
                            insert(key: number): void;
                            previousOrEqual(key: number): number;
                            lookup(key: number): number;
                            range(startKey: number, endKey: number, walker: (p: number) => void): void;
                            delete(key: number): void;
                        }
                        interface KTree extends org.kevoree.modeling.memory.KMemoryElement {
                            size(): number;
                        }
                        interface KTreeWalker {
                            elem(t: number): void;
                        }
                        module impl {
                            class LongLongTree implements org.kevoree.modeling.memory.KMemoryElement, org.kevoree.modeling.memory.struct.tree.KLongLongTree {
                                private root;
                                private _size;
                                _dirty: boolean;
                                private _counter;
                                private _previousOrEqualsCacheValues;
                                private _previousOrEqualsNextCacheElem;
                                private _lookupCacheValues;
                                private _lookupNextCacheElem;
                                size(): number;
                                counter(): number;
                                inc(): void;
                                dec(): void;
                                free(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                toString(): string;
                                isDirty(): boolean;
                                setDirty(): void;
                                serialize(metaModel: org.kevoree.modeling.meta.KMetaModel): string;
                                constructor();
                                private tryPreviousOrEqualsCache(key);
                                private tryLookupCache(key);
                                private resetCache();
                                private putInPreviousOrEqualsCache(resolved);
                                private putInLookupCache(resolved);
                                setClean(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                unserialize(key: org.kevoree.modeling.KContentKey, payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                lookupValue(key: number): number;
                                private internal_lookup(key);
                                previousOrEqualValue(key: number): number;
                                private internal_previousOrEqual(key);
                                nextOrEqual(key: number): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                previous(key: number): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                next(key: number): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                first(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                last(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                private rotateLeft(n);
                                private rotateRight(n);
                                private replaceNode(oldn, newn);
                                insert(key: number, value: number): void;
                                private insertCase1(n);
                                private insertCase2(n);
                                private insertCase3(n);
                                private insertCase4(n_n);
                                private insertCase5(n);
                                delete(key: number): void;
                                private deleteCase1(n);
                                private deleteCase2(n);
                                private deleteCase3(n);
                                private deleteCase4(n);
                                private deleteCase5(n);
                                private deleteCase6(n);
                                private nodeColor(n);
                            }
                            class LongTree implements org.kevoree.modeling.memory.KMemoryElement, org.kevoree.modeling.memory.struct.tree.KLongTree {
                                private _size;
                                private root;
                                private _previousOrEqualsCacheValues;
                                private _nextCacheElem;
                                private _counter;
                                private _dirty;
                                constructor();
                                size(): number;
                                counter(): number;
                                inc(): void;
                                dec(): void;
                                free(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                private tryPreviousOrEqualsCache(key);
                                private resetCache();
                                private putInPreviousOrEqualsCache(resolved);
                                isDirty(): boolean;
                                setClean(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                setDirty(): void;
                                serialize(metaModel: org.kevoree.modeling.meta.KMetaModel): string;
                                toString(): string;
                                unserialize(key: org.kevoree.modeling.KContentKey, payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                previousOrEqual(key: number): number;
                                internal_previousOrEqual(key: number): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                nextOrEqual(key: number): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                previous(key: number): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                next(key: number): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                first(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                last(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                lookup(key: number): number;
                                range(start: number, end: number, walker: (p: number) => void): void;
                                private rotateLeft(n);
                                private rotateRight(n);
                                private replaceNode(oldn, newn);
                                insert(key: number): void;
                                private insertCase1(n);
                                private insertCase2(n);
                                private insertCase3(n);
                                private insertCase4(n_n);
                                private insertCase5(n);
                                delete(key: number): void;
                                private deleteCase1(n);
                                private deleteCase2(n);
                                private deleteCase3(n);
                                private deleteCase4(n);
                                private deleteCase5(n);
                                private deleteCase6(n);
                                private nodeColor(n);
                            }
                            class LongTreeNode {
                                static BLACK: string;
                                static RED: string;
                                key: number;
                                value: number;
                                color: boolean;
                                private left;
                                private right;
                                private parent;
                                constructor(key: number, value: number, color: boolean, left: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode, right: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode);
                                grandparent(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                sibling(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                uncle(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                getLeft(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                setLeft(left: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void;
                                getRight(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                setRight(right: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void;
                                getParent(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                setParent(parent: org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode): void;
                                serialize(builder: java.lang.StringBuilder): void;
                                next(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                previous(): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                static unserialize(ctx: org.kevoree.modeling.memory.struct.tree.impl.TreeReaderContext): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                                static internal_unserialize(rightBranch: boolean, ctx: org.kevoree.modeling.memory.struct.tree.impl.TreeReaderContext): org.kevoree.modeling.memory.struct.tree.impl.LongTreeNode;
                            }
                            class TreeNode {
                                static BLACK: string;
                                static RED: string;
                                key: number;
                                color: boolean;
                                private left;
                                private right;
                                private parent;
                                constructor(key: number, color: boolean, left: org.kevoree.modeling.memory.struct.tree.impl.TreeNode, right: org.kevoree.modeling.memory.struct.tree.impl.TreeNode);
                                getKey(): number;
                                grandparent(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                sibling(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                uncle(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                getLeft(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                setLeft(left: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void;
                                getRight(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                setRight(right: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void;
                                getParent(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                setParent(parent: org.kevoree.modeling.memory.struct.tree.impl.TreeNode): void;
                                serialize(builder: java.lang.StringBuilder): void;
                                next(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                previous(): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                static unserialize(ctx: org.kevoree.modeling.memory.struct.tree.impl.TreeReaderContext): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                                static internal_unserialize(rightBranch: boolean, ctx: org.kevoree.modeling.memory.struct.tree.impl.TreeReaderContext): org.kevoree.modeling.memory.struct.tree.impl.TreeNode;
                            }
                            class TreeReaderContext {
                                payload: string;
                                index: number;
                                buffer: string[];
                            }
                        }
                    }
                }
            }
            module message {
                interface KMessage {
                    json(): string;
                    type(): number;
                }
                class KMessageLoader {
                    static TYPE_NAME: string;
                    static OPERATION_NAME: string;
                    static KEY_NAME: string;
                    static KEYS_NAME: string;
                    static ID_NAME: string;
                    static VALUE_NAME: string;
                    static VALUES_NAME: string;
                    static CLASS_IDX_NAME: string;
                    static PARAMETERS_NAME: string;
                    static EVENTS_TYPE: number;
                    static GET_REQ_TYPE: number;
                    static GET_RES_TYPE: number;
                    static PUT_REQ_TYPE: number;
                    static PUT_RES_TYPE: number;
                    static OPERATION_CALL_TYPE: number;
                    static OPERATION_RESULT_TYPE: number;
                    static ATOMIC_GET_INC_REQUEST_TYPE: number;
                    static ATOMIC_GET_INC_RESULT_TYPE: number;
                    static load(payload: string): org.kevoree.modeling.message.KMessage;
                }
                module impl {
                    class AtomicGetIncrementRequest implements org.kevoree.modeling.message.KMessage {
                        id: number;
                        key: org.kevoree.modeling.KContentKey;
                        json(): string;
                        type(): number;
                    }
                    class AtomicGetIncrementResult implements org.kevoree.modeling.message.KMessage {
                        id: number;
                        value: number;
                        json(): string;
                        type(): number;
                    }
                    class Events implements org.kevoree.modeling.message.KMessage {
                        _objIds: org.kevoree.modeling.KContentKey[];
                        _metaindexes: number[][];
                        private _size;
                        allKeys(): org.kevoree.modeling.KContentKey[];
                        constructor(nbObject: number);
                        json(): string;
                        type(): number;
                        size(): number;
                        setEvent(index: number, p_objId: org.kevoree.modeling.KContentKey, p_metaIndexes: number[]): void;
                        getKey(index: number): org.kevoree.modeling.KContentKey;
                        getIndexes(index: number): number[];
                    }
                    class GetRequest implements org.kevoree.modeling.message.KMessage {
                        id: number;
                        keys: org.kevoree.modeling.KContentKey[];
                        json(): string;
                        type(): number;
                    }
                    class GetResult implements org.kevoree.modeling.message.KMessage {
                        id: number;
                        values: string[];
                        json(): string;
                        type(): number;
                    }
                    class MessageHelper {
                        static printJsonStart(builder: java.lang.StringBuilder): void;
                        static printJsonEnd(builder: java.lang.StringBuilder): void;
                        static printType(builder: java.lang.StringBuilder, type: number): void;
                        static printElem(elem: any, name: string, builder: java.lang.StringBuilder): void;
                    }
                    class OperationCallMessage implements org.kevoree.modeling.message.KMessage {
                        id: number;
                        classIndex: number;
                        opIndex: number;
                        params: string[];
                        key: org.kevoree.modeling.KContentKey;
                        json(): string;
                        type(): number;
                    }
                    class OperationResultMessage implements org.kevoree.modeling.message.KMessage {
                        id: number;
                        value: string;
                        key: org.kevoree.modeling.KContentKey;
                        json(): string;
                        type(): number;
                    }
                    class PutRequest implements org.kevoree.modeling.message.KMessage {
                        request: org.kevoree.modeling.cdn.KContentPutRequest;
                        id: number;
                        json(): string;
                        type(): number;
                    }
                    class PutResult implements org.kevoree.modeling.message.KMessage {
                        id: number;
                        json(): string;
                        type(): number;
                    }
                }
            }
            module meta {
                interface KMeta {
                    index(): number;
                    metaName(): string;
                    metaType(): org.kevoree.modeling.meta.MetaType;
                }
                interface KMetaAttribute extends org.kevoree.modeling.meta.KMeta {
                    key(): boolean;
                    attributeType(): org.kevoree.modeling.KType;
                    strategy(): org.kevoree.modeling.extrapolation.Extrapolation;
                    precision(): number;
                    setExtrapolation(extrapolation: org.kevoree.modeling.extrapolation.Extrapolation): void;
                }
                interface KMetaClass extends org.kevoree.modeling.meta.KMeta {
                    metaElements(): org.kevoree.modeling.meta.KMeta[];
                    meta(index: number): org.kevoree.modeling.meta.KMeta;
                    metaByName(name: string): org.kevoree.modeling.meta.KMeta;
                    attribute(name: string): org.kevoree.modeling.meta.KMetaAttribute;
                    reference(name: string): org.kevoree.modeling.meta.KMetaReference;
                    operation(name: string): org.kevoree.modeling.meta.KMetaOperation;
                    addAttribute(attributeName: string, p_type: org.kevoree.modeling.KType, p_precision: number, extrapolation: org.kevoree.modeling.extrapolation.Extrapolation): org.kevoree.modeling.meta.KMetaAttribute;
                    addReference(referenceName: string, metaClass: org.kevoree.modeling.meta.KMetaClass, oppositeName: string, toMany: boolean): org.kevoree.modeling.meta.KMetaReference;
                    addOperation(operationName: string): org.kevoree.modeling.meta.KMetaOperation;
                }
                class KMetaInferClass implements org.kevoree.modeling.meta.KMetaClass {
                    private static _INSTANCE;
                    private _attributes;
                    private _metaReferences;
                    static getInstance(): org.kevoree.modeling.meta.KMetaInferClass;
                    getRaw(): org.kevoree.modeling.meta.KMetaAttribute;
                    getCache(): org.kevoree.modeling.meta.KMetaAttribute;
                    constructor();
                    metaElements(): org.kevoree.modeling.meta.KMeta[];
                    meta(index: number): org.kevoree.modeling.meta.KMeta;
                    metaByName(name: string): org.kevoree.modeling.meta.KMeta;
                    attribute(name: string): org.kevoree.modeling.meta.KMetaAttribute;
                    reference(name: string): org.kevoree.modeling.meta.KMetaReference;
                    operation(name: string): org.kevoree.modeling.meta.KMetaOperation;
                    addAttribute(attributeName: string, p_type: org.kevoree.modeling.KType, p_precision: number, extrapolation: org.kevoree.modeling.extrapolation.Extrapolation): org.kevoree.modeling.meta.KMetaAttribute;
                    addReference(referenceName: string, metaClass: org.kevoree.modeling.meta.KMetaClass, oppositeName: string, toMany: boolean): org.kevoree.modeling.meta.KMetaReference;
                    addOperation(operationName: string): org.kevoree.modeling.meta.KMetaOperation;
                    metaName(): string;
                    metaType(): org.kevoree.modeling.meta.MetaType;
                    index(): number;
                }
                interface KMetaModel extends org.kevoree.modeling.meta.KMeta {
                    metaClasses(): org.kevoree.modeling.meta.KMetaClass[];
                    metaClassByName(name: string): org.kevoree.modeling.meta.KMetaClass;
                    metaClass(index: number): org.kevoree.modeling.meta.KMetaClass;
                    addMetaClass(metaClassName: string): org.kevoree.modeling.meta.KMetaClass;
                    model(): org.kevoree.modeling.KModel<any>;
                }
                interface KMetaOperation extends org.kevoree.modeling.meta.KMeta {
                    origin(): org.kevoree.modeling.meta.KMeta;
                }
                interface KMetaReference extends org.kevoree.modeling.meta.KMeta {
                    visible(): boolean;
                    single(): boolean;
                    type(): org.kevoree.modeling.meta.KMetaClass;
                    opposite(): org.kevoree.modeling.meta.KMetaReference;
                    origin(): org.kevoree.modeling.meta.KMetaClass;
                }
                class KPrimitiveTypes {
                    static STRING: org.kevoree.modeling.KType;
                    static LONG: org.kevoree.modeling.KType;
                    static INT: org.kevoree.modeling.KType;
                    static BOOL: org.kevoree.modeling.KType;
                    static SHORT: org.kevoree.modeling.KType;
                    static DOUBLE: org.kevoree.modeling.KType;
                    static FLOAT: org.kevoree.modeling.KType;
                    static TRANSIENT: org.kevoree.modeling.KType;
                }
                class MetaType {
                    static ATTRIBUTE: MetaType;
                    static REFERENCE: MetaType;
                    static OPERATION: MetaType;
                    static CLASS: MetaType;
                    static MODEL: MetaType;
                    equals(other: any): boolean;
                    static _MetaTypeVALUES: MetaType[];
                    static values(): MetaType[];
                }
                module impl {
                    class GenericModel extends org.kevoree.modeling.abs.AbstractKModel<any> {
                        private _p_metaModel;
                        constructor(mm: org.kevoree.modeling.meta.KMetaModel);
                        metaModel(): org.kevoree.modeling.meta.KMetaModel;
                        internalCreateUniverse(universe: number): org.kevoree.modeling.KUniverse<any, any, any>;
                        internalCreateObject(universe: number, time: number, uuid: number, clazz: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.KObject;
                    }
                    class GenericObject extends org.kevoree.modeling.abs.AbstractKObject {
                        constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    }
                    class GenericUniverse extends org.kevoree.modeling.abs.AbstractKUniverse<any, any, any> {
                        constructor(p_key: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                        internal_create(timePoint: number): org.kevoree.modeling.KView;
                    }
                    class GenericView extends org.kevoree.modeling.abs.AbstractKView {
                        constructor(p_universe: number, _time: number, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    }
                    class MetaAttribute implements org.kevoree.modeling.meta.KMetaAttribute {
                        private _name;
                        private _index;
                        _precision: number;
                        private _key;
                        private _metaType;
                        private _extrapolation;
                        attributeType(): org.kevoree.modeling.KType;
                        index(): number;
                        metaName(): string;
                        metaType(): org.kevoree.modeling.meta.MetaType;
                        precision(): number;
                        key(): boolean;
                        strategy(): org.kevoree.modeling.extrapolation.Extrapolation;
                        setExtrapolation(extrapolation: org.kevoree.modeling.extrapolation.Extrapolation): void;
                        constructor(p_name: string, p_index: number, p_precision: number, p_key: boolean, p_metaType: org.kevoree.modeling.KType, p_extrapolation: org.kevoree.modeling.extrapolation.Extrapolation);
                    }
                    class MetaClass implements org.kevoree.modeling.meta.KMetaClass {
                        private _name;
                        private _index;
                        private _meta;
                        private _indexes;
                        constructor(p_name: string, p_index: number);
                        init(p_metaElements: org.kevoree.modeling.meta.KMeta[]): void;
                        metaByName(name: string): org.kevoree.modeling.meta.KMeta;
                        attribute(name: string): org.kevoree.modeling.meta.KMetaAttribute;
                        reference(name: string): org.kevoree.modeling.meta.KMetaReference;
                        operation(name: string): org.kevoree.modeling.meta.KMetaOperation;
                        metaElements(): org.kevoree.modeling.meta.KMeta[];
                        index(): number;
                        metaName(): string;
                        metaType(): org.kevoree.modeling.meta.MetaType;
                        meta(index: number): org.kevoree.modeling.meta.KMeta;
                        addAttribute(attributeName: string, p_type: org.kevoree.modeling.KType, p_precision: number, extrapolation: org.kevoree.modeling.extrapolation.Extrapolation): org.kevoree.modeling.meta.KMetaAttribute;
                        addReference(referenceName: string, p_metaClass: org.kevoree.modeling.meta.KMetaClass, oppositeName: string, toMany: boolean): org.kevoree.modeling.meta.KMetaReference;
                        private getOrCreate(p_name, p_oppositeName, p_oppositeClass, p_visible, p_single);
                        addOperation(operationName: string): org.kevoree.modeling.meta.KMetaOperation;
                        private internal_add_meta(p_new_meta);
                    }
                    class MetaModel implements org.kevoree.modeling.meta.KMetaModel {
                        private _name;
                        private _index;
                        private _metaClasses;
                        private _metaClasses_indexes;
                        index(): number;
                        metaName(): string;
                        metaType(): org.kevoree.modeling.meta.MetaType;
                        constructor(p_name: string);
                        init(p_metaClasses: org.kevoree.modeling.meta.KMetaClass[]): void;
                        metaClasses(): org.kevoree.modeling.meta.KMetaClass[];
                        metaClassByName(name: string): org.kevoree.modeling.meta.KMetaClass;
                        metaClass(index: number): org.kevoree.modeling.meta.KMetaClass;
                        addMetaClass(metaClassName: string): org.kevoree.modeling.meta.KMetaClass;
                        private interal_add_meta_class(p_newMetaClass);
                        model(): org.kevoree.modeling.KModel<any>;
                    }
                    class MetaOperation implements org.kevoree.modeling.meta.KMetaOperation {
                        private _name;
                        private _index;
                        private _lazyMetaClass;
                        index(): number;
                        metaName(): string;
                        metaType(): org.kevoree.modeling.meta.MetaType;
                        constructor(p_name: string, p_index: number, p_lazyMetaClass: () => org.kevoree.modeling.meta.KMeta);
                        origin(): org.kevoree.modeling.meta.KMetaClass;
                    }
                    class MetaReference implements org.kevoree.modeling.meta.KMetaReference {
                        private _name;
                        private _index;
                        private _visible;
                        private _single;
                        private _lazyMetaType;
                        private _op_name;
                        private _lazyMetaOrigin;
                        single(): boolean;
                        type(): org.kevoree.modeling.meta.KMetaClass;
                        opposite(): org.kevoree.modeling.meta.KMetaReference;
                        origin(): org.kevoree.modeling.meta.KMetaClass;
                        index(): number;
                        metaName(): string;
                        metaType(): org.kevoree.modeling.meta.MetaType;
                        visible(): boolean;
                        constructor(p_name: string, p_index: number, p_visible: boolean, p_single: boolean, p_lazyMetaType: () => org.kevoree.modeling.meta.KMeta, op_name: string, p_lazyMetaOrigin: () => org.kevoree.modeling.meta.KMeta);
                    }
                }
            }
            module operation {
                interface KOperation {
                    on(source: org.kevoree.modeling.KObject, params: any[], result: (p: any) => void): void;
                }
                interface KOperationManager {
                    registerOperation(operation: org.kevoree.modeling.meta.KMetaOperation, callback: (p: org.kevoree.modeling.KObject, p1: any[], p2: (p: any) => void) => void, target: org.kevoree.modeling.KObject): void;
                    call(source: org.kevoree.modeling.KObject, operation: org.kevoree.modeling.meta.KMetaOperation, param: any[], callback: (p: any) => void): void;
                    operationEventReceived(operationEvent: org.kevoree.modeling.message.KMessage): void;
                }
                module impl {
                    class HashOperationManager implements org.kevoree.modeling.operation.KOperationManager {
                        private staticOperations;
                        private instanceOperations;
                        private remoteCallCallbacks;
                        private _manager;
                        private _callbackId;
                        constructor(p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                        registerOperation(operation: org.kevoree.modeling.meta.KMetaOperation, callback: (p: org.kevoree.modeling.KObject, p1: any[], p2: (p: any) => void) => void, target: org.kevoree.modeling.KObject): void;
                        private searchOperation(source, clazz, operation);
                        call(source: org.kevoree.modeling.KObject, operation: org.kevoree.modeling.meta.KMetaOperation, param: any[], callback: (p: any) => void): void;
                        private sendToRemote(source, operation, param, callback);
                        nextKey(): number;
                        operationEventReceived(operationEvent: org.kevoree.modeling.message.KMessage): void;
                    }
                }
            }
            module scheduler {
                interface KScheduler {
                    dispatch(runnable: java.lang.Runnable): void;
                    stop(): void;
                }
                module impl {
                    class DirectScheduler implements org.kevoree.modeling.scheduler.KScheduler {
                        dispatch(runnable: java.lang.Runnable): void;
                        stop(): void;
                    }
                    class ExecutorServiceScheduler implements org.kevoree.modeling.scheduler.KScheduler {
                        dispatch(p_runnable: java.lang.Runnable): void;
                        stop(): void;
                    }
                }
            }
            module traversal {
                interface KTraversal {
                    traverse(metaReference: org.kevoree.modeling.meta.KMetaReference): org.kevoree.modeling.traversal.KTraversal;
                    traverseQuery(metaReferenceQuery: string): org.kevoree.modeling.traversal.KTraversal;
                    attributeQuery(attributeQuery: string): org.kevoree.modeling.traversal.KTraversal;
                    withAttribute(attribute: org.kevoree.modeling.meta.KMetaAttribute, expectedValue: any): org.kevoree.modeling.traversal.KTraversal;
                    withoutAttribute(attribute: org.kevoree.modeling.meta.KMetaAttribute, expectedValue: any): org.kevoree.modeling.traversal.KTraversal;
                    filter(filter: (p: org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;
                    then(cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                    map(attribute: org.kevoree.modeling.meta.KMetaAttribute, cb: (p: any[]) => void): void;
                    collect(metaReference: org.kevoree.modeling.meta.KMetaReference, continueCondition: (p: org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;
                }
                interface KTraversalAction {
                    chain(next: org.kevoree.modeling.traversal.KTraversalAction): void;
                    execute(inputs: org.kevoree.modeling.KObject[]): void;
                }
                interface KTraversalFilter {
                    filter(obj: org.kevoree.modeling.KObject): boolean;
                }
                module impl {
                    class Traversal implements org.kevoree.modeling.traversal.KTraversal {
                        private static TERMINATED_MESSAGE;
                        private _initObjs;
                        private _initAction;
                        private _lastAction;
                        private _terminated;
                        constructor(p_root: org.kevoree.modeling.KObject);
                        private internal_chain_action(p_action);
                        traverse(p_metaReference: org.kevoree.modeling.meta.KMetaReference): org.kevoree.modeling.traversal.KTraversal;
                        traverseQuery(p_metaReferenceQuery: string): org.kevoree.modeling.traversal.KTraversal;
                        withAttribute(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_expectedValue: any): org.kevoree.modeling.traversal.KTraversal;
                        withoutAttribute(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_expectedValue: any): org.kevoree.modeling.traversal.KTraversal;
                        attributeQuery(p_attributeQuery: string): org.kevoree.modeling.traversal.KTraversal;
                        filter(p_filter: (p: org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;
                        collect(metaReference: org.kevoree.modeling.meta.KMetaReference, continueCondition: (p: org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;
                        then(cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                        map(attribute: org.kevoree.modeling.meta.KMetaAttribute, cb: (p: any[]) => void): void;
                    }
                    module actions {
                        class DeepCollectAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            private _reference;
                            private _continueCondition;
                            private _alreadyPassed;
                            private _finalElements;
                            constructor(p_reference: org.kevoree.modeling.meta.KMetaReference, p_continueCondition: (p: org.kevoree.modeling.KObject) => boolean);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(p_inputs: org.kevoree.modeling.KObject[]): void;
                            private executeStep(p_inputStep, private_callback);
                        }
                        class FilterAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            private _filter;
                            constructor(p_filter: (p: org.kevoree.modeling.KObject) => boolean);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(p_inputs: org.kevoree.modeling.KObject[]): void;
                        }
                        class FilterAttributeAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            private _attribute;
                            private _expectedValue;
                            constructor(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_expectedValue: any);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(p_inputs: org.kevoree.modeling.KObject[]): void;
                        }
                        class FilterAttributeQueryAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            private _attributeQuery;
                            constructor(p_attributeQuery: string);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(p_inputs: org.kevoree.modeling.KObject[]): void;
                            private buildParams(p_paramString);
                        }
                        class FilterNotAttributeAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            private _attribute;
                            private _expectedValue;
                            constructor(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_expectedValue: any);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(p_inputs: org.kevoree.modeling.KObject[]): void;
                        }
                        class FinalAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _finalCallback;
                            constructor(p_callback: (p: org.kevoree.modeling.KObject[]) => void);
                            chain(next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(inputs: org.kevoree.modeling.KObject[]): void;
                        }
                        class MapAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _finalCallback;
                            private _attribute;
                            constructor(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_callback: (p: any[]) => void);
                            chain(next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(inputs: org.kevoree.modeling.KObject[]): void;
                        }
                        class RemoveDuplicateAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(p_inputs: org.kevoree.modeling.KObject[]): void;
                        }
                        class TraverseAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            private _reference;
                            constructor(p_reference: org.kevoree.modeling.meta.KMetaReference);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(p_inputs: org.kevoree.modeling.KObject[]): void;
                        }
                        class TraverseQueryAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private SEP;
                            private _next;
                            private _referenceQuery;
                            constructor(p_referenceQuery: string);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(p_inputs: org.kevoree.modeling.KObject[]): void;
                        }
                    }
                    module selector {
                        class Query {
                            static OPEN_BRACKET: string;
                            static CLOSE_BRACKET: string;
                            static QUERY_SEP: string;
                            relationName: string;
                            params: string;
                            constructor(relationName: string, params: string);
                            toString(): string;
                            static buildChain(query: string): java.util.List<org.kevoree.modeling.traversal.impl.selector.Query>;
                        }
                        class QueryParam {
                            private _name;
                            private _value;
                            private _negative;
                            constructor(p_name: string, p_value: string, p_negative: boolean);
                            name(): string;
                            value(): string;
                            isNegative(): boolean;
                        }
                        class Selector {
                            static select(root: org.kevoree.modeling.KObject, query: string, callback: (p: org.kevoree.modeling.KObject[]) => void): void;
                        }
                    }
                }
                module visitor {
                    interface KModelAttributeVisitor {
                        visit(metaAttribute: org.kevoree.modeling.meta.KMetaAttribute, value: any): void;
                    }
                    interface KModelVisitor {
                        visit(elem: org.kevoree.modeling.KObject): org.kevoree.modeling.traversal.visitor.KVisitResult;
                    }
                    class KVisitResult {
                        static CONTINUE: KVisitResult;
                        static SKIP: KVisitResult;
                        static STOP: KVisitResult;
                        equals(other: any): boolean;
                        static _KVisitResultVALUES: KVisitResult[];
                        static values(): KVisitResult[];
                    }
                }
            }
            module util {
                class Checker {
                    static isDefined(param: any): boolean;
                }
            }
        }
    }
}
