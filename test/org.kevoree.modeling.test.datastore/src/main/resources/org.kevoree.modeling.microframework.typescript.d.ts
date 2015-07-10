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
                lookup(universe: number, time: number, uuid: number, cb: (p: org.kevoree.modeling.KObject) => void): void;
            }
            interface KObject {
                universe(): number;
                now(): number;
                uuid(): number;
                delete(cb: (p: any) => void): void;
                select(query: string, cb: (p: any[]) => void): void;
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
            interface KObjectInfer extends org.kevoree.modeling.KObject {
                train(dependencies: org.kevoree.modeling.KObject[], expectedOutputs: any[], callback: (p: any) => void): void;
                trainAll(trainingSet: org.kevoree.modeling.KObject[][], expectedResultSet: any[][], callback: (p: any) => void): void;
                infer(features: org.kevoree.modeling.KObject[], callback: (p: any[]) => void): void;
                inferAll(features: org.kevoree.modeling.KObject[][], callback: (p: any[][]) => void): void;
                resetLearning(): void;
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
                select(query: string, cb: (p: any[]) => void): void;
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
                    lookup(p_universe: number, p_time: number, p_uuid: number, cb: (p: org.kevoree.modeling.KObject) => void): void;
                }
                class AbstractKObject implements org.kevoree.modeling.KObject {
                    _uuid: number;
                    _time: number;
                    _universe: number;
                    _metaClass: org.kevoree.modeling.meta.KMetaClass;
                    _manager: org.kevoree.modeling.memory.manager.KMemoryManager;
                    private static OUT_OF_CACHE_MSG;
                    constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    uuid(): number;
                    metaClass(): org.kevoree.modeling.meta.KMetaClass;
                    now(): number;
                    universe(): number;
                    timeWalker(): org.kevoree.modeling.KTimeWalker;
                    delete(cb: (p: any) => void): void;
                    select(query: string, cb: (p: any[]) => void): void;
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
                class AbstractKObjectInfer extends org.kevoree.modeling.abs.AbstractKObject implements org.kevoree.modeling.KObjectInfer {
                    constructor(p_universe: number, p_time: number, p_uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, p_manager: org.kevoree.modeling.memory.manager.KMemoryManager);
                    private dependenciesResolver(dependencies);
                    train(dependencies: org.kevoree.modeling.KObject[], expectedOutputs: any[], callback: (p: any) => void): void;
                    trainAll(p_dependencies: org.kevoree.modeling.KObject[][], p_outputs: any[][], callback: (p: any) => void): void;
                    infer(dependencies: org.kevoree.modeling.KObject[], callback: (p: any[]) => void): void;
                    inferAll(features: org.kevoree.modeling.KObject[][], callback: (p: any[][]) => void): void;
                    resetLearning(): void;
                    private internalConvertOutput(output, metaOutput);
                    private internalReverseOutput(inferred, metaOutput);
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
                    select(query: string, cb: (p: any[]) => void): void;
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
                    addMessageInterceptor(interceptor: (p: org.kevoree.modeling.message.KMessage) => boolean): number;
                    removeMessageInterceptor(id: number): void;
                    setManager(manager: org.kevoree.modeling.memory.manager.KMemoryManager): void;
                }
                interface KContentPutRequest {
                    put(p_key: org.kevoree.modeling.KContentKey, p_payload: string): void;
                    getKey(index: number): org.kevoree.modeling.KContentKey;
                    getContent(index: number): string;
                    size(): number;
                }
                interface KMessageInterceptor {
                    on(msg: org.kevoree.modeling.message.KMessage): boolean;
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
                        private additionalInterceptors;
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
                        private randomInterceptorID();
                        addMessageInterceptor(p_interceptor: (p: org.kevoree.modeling.message.KMessage) => boolean): number;
                        removeMessageInterceptor(id: number): void;
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
                }
                module impl {
                    class DiscreteExtrapolation implements org.kevoree.modeling.extrapolation.Extrapolation {
                        private static INSTANCE;
                        static instance(): org.kevoree.modeling.extrapolation.Extrapolation;
                        extrapolate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute): any;
                        mutate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute, payload: any): void;
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
                        private extrapolateValue(segment, meta, index, time, timeOrigin);
                        private maxErr(precision, degree);
                        insert(time: number, value: number, timeOrigin: number, raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment, index: number, precision: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean;
                        private tempError(computedWeights, times, values);
                        private internal_extrapolate(t, raw, index, metaClass);
                        private initial_feed(time, value, raw, index, metaClass);
                        mutate(current: org.kevoree.modeling.KObject, attribute: org.kevoree.modeling.meta.KMetaAttribute, payload: any): void;
                        private castNumber(payload);
                        static instance(): org.kevoree.modeling.extrapolation.Extrapolation;
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
                    class JsonRaw {
                        static encode(raw: org.kevoree.modeling.memory.struct.segment.KMemorySegment, uuid: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass, isRoot: boolean): string;
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
                        addressTable: org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap<any>;
                        elementsCount: org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>;
                        packageList: java.util.ArrayList<string>;
                    }
                    class XMILoadingContext {
                        xmiReader: org.kevoree.modeling.format.xmi.XmlParser;
                        loadedRoots: org.kevoree.modeling.KObject;
                        resolvers: java.util.ArrayList<org.kevoree.modeling.format.xmi.XMIResolveCommand>;
                        map: org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>;
                        elementsCount: org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap<any>;
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
                interface KInferAlg {
                    train(trainingSet: number[][], expectedResultSet: number[][], currentInferObject: org.kevoree.modeling.KObject): void;
                    infer(features: number[][], currentInferObject: org.kevoree.modeling.KObject): number[][];
                }
                module impl {
                    class GaussianClassificationAlg implements org.kevoree.modeling.infer.KInferAlg {
                        private static MIN;
                        private static MAX;
                        private static SUM;
                        private static SUMSQUARE;
                        private static NUMOFFIELDS;
                        private maxOutput;
                        private getIndex(input, output, field, meta);
                        private getCounter(output, meta);
                        getAvg(output: number, state: org.kevoree.modeling.util.maths.structure.impl.Array1D, meta: org.kevoree.modeling.meta.KMetaDependencies): number[];
                        getVariance(output: number, state: org.kevoree.modeling.util.maths.structure.impl.Array1D, avg: number[], meta: org.kevoree.modeling.meta.KMetaDependencies): number[];
                        train(trainingSet: number[][], expectedResultSet: number[][], origin: org.kevoree.modeling.KObject): void;
                        infer(features: number[][], origin: org.kevoree.modeling.KObject): number[][];
                        getProba(features: number[], output: number, state: org.kevoree.modeling.util.maths.structure.impl.Array1D, meta: org.kevoree.modeling.meta.KMetaDependencies): number;
                        getAllProba(features: number[], state: org.kevoree.modeling.util.maths.structure.impl.Array1D, meta: org.kevoree.modeling.meta.KMetaDependencies): number[];
                    }
                    class StatInferAlg implements org.kevoree.modeling.infer.KInferAlg {
                        private static MIN;
                        private static MAX;
                        private static SUM;
                        private static SUMSQuare;
                        private static NUMOFFIELDS;
                        train(trainingSet: number[][], expectedResultSet: number[][], origin: org.kevoree.modeling.KObject): void;
                        infer(features: number[][], origin: org.kevoree.modeling.KObject): number[][];
                        getAvgAll(ks: org.kevoree.modeling.memory.struct.segment.KMemorySegment, meta: org.kevoree.modeling.meta.KMetaDependencies): number[];
                        getMinAll(ks: org.kevoree.modeling.memory.struct.segment.KMemorySegment, meta: org.kevoree.modeling.meta.KMetaDependencies): number[];
                        getMaxAll(ks: org.kevoree.modeling.memory.struct.segment.KMemorySegment, meta: org.kevoree.modeling.meta.KMetaDependencies): number[];
                        getVarianceAll(ks: org.kevoree.modeling.memory.struct.segment.KMemorySegment, meta: org.kevoree.modeling.meta.KMetaDependencies, avgs: number[]): number[];
                        getAvg(featureNum: number, ks: org.kevoree.modeling.memory.struct.segment.KMemorySegment, meta: org.kevoree.modeling.meta.KMetaDependencies): number;
                        getMin(featureNum: number, ks: org.kevoree.modeling.memory.struct.segment.KMemorySegment, meta: org.kevoree.modeling.meta.KMetaDependencies): number;
                        getMax(featureNum: number, ks: org.kevoree.modeling.memory.struct.segment.KMemorySegment, meta: org.kevoree.modeling.meta.KMetaDependencies): number;
                        getVariance(featureNum: number, ks: org.kevoree.modeling.memory.struct.segment.KMemorySegment, meta: org.kevoree.modeling.meta.KMetaDependencies, avg: number): number;
                    }
                }
            }
            module memory {
                interface KMemoryElement {
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
                interface KMemoryFactory {
                    newCacheSegment(): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
                    newLongTree(): org.kevoree.modeling.memory.struct.tree.KLongTree;
                    newLongLongTree(): org.kevoree.modeling.memory.struct.tree.KLongLongTree;
                    newUniverseMap(initSize: number, className: string): org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
                    newFromKey(universe: number, time: number, uuid: number): org.kevoree.modeling.memory.KMemoryElement;
                    newCache(): org.kevoree.modeling.memory.struct.cache.KCache;
                }
                interface KOffHeapMemoryElement extends org.kevoree.modeling.memory.KMemoryElement {
                    getMemoryAddress(): number;
                    setMemoryAddress(address: number): void;
                }
                module manager {
                    interface KMemoryManager {
                        cdn(): org.kevoree.modeling.cdn.KContentDeliveryDriver;
                        model(): org.kevoree.modeling.KModel<any>;
                        cache(): org.kevoree.modeling.memory.struct.cache.KCache;
                        lookup(universe: number, time: number, uuid: number, callback: (p: org.kevoree.modeling.KObject) => void): void;
                        lookupAllobjects(universe: number, time: number, uuid: number[], callback: (p: org.kevoree.modeling.KObject[]) => void): void;
                        lookupAlltimes(universe: number, time: number[], uuid: number, callback: (p: org.kevoree.modeling.KObject[]) => void): void;
                        segment(universe: number, time: number, uuid: number, resolvePreviousSegment: boolean, metaClass: org.kevoree.modeling.meta.KMetaClass, resolutionTrace: org.kevoree.modeling.memory.manager.KMemorySegmentResolutionTrace): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
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
                    interface KMemorySegmentResolutionTrace {
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
                    module impl {
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
                            constructor(p_universe: number, p_time: number, p_keys: number[], p_callback: (p: org.kevoree.modeling.KObject[]) => void, p_store: org.kevoree.modeling.memory.manager.impl.MemoryManager);
                            run(): void;
                        }
                        class MemoryManager implements org.kevoree.modeling.memory.manager.KMemoryManager {
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
                            private _cache;
                            private prefix;
                            private static UNIVERSE_INDEX;
                            private static OBJ_INDEX;
                            private static GLO_TREE_INDEX;
                            private static zeroPrefix;
                            constructor(model: org.kevoree.modeling.KModel<any>);
                            cache(): org.kevoree.modeling.memory.struct.cache.KCache;
                            model(): org.kevoree.modeling.KModel<any>;
                            close(callback: (p: java.lang.Throwable) => void): void;
                            nextUniverseKey(): number;
                            nextObjectKey(): number;
                            nextModelKey(): number;
                            nextGroupKey(): number;
                            globalUniverseOrder(): org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
                            initUniverse(p_universe: org.kevoree.modeling.KUniverse<any, any, any>, p_parent: org.kevoree.modeling.KUniverse<any, any, any>): void;
                            parentUniverseKey(currentUniverseKey: number): number;
                            descendantsUniverseKeys(currentUniverseKey: number): number[];
                            save(callback: (p: java.lang.Throwable) => void): void;
                            initKObject(obj: org.kevoree.modeling.KObject): void;
                            connect(connectCallback: (p: java.lang.Throwable) => void): void;
                            segment(universe: number, time: number, uuid: number, resolvePreviousSegment: boolean, metaClass: org.kevoree.modeling.meta.KMetaClass, resolutionTrace: org.kevoree.modeling.memory.manager.KMemorySegmentResolutionTrace): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
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
                        class MemorySegmentResolutionTrace implements org.kevoree.modeling.memory.manager.KMemorySegmentResolutionTrace {
                            private _universe;
                            private _time;
                            private _universeOrder;
                            private _timeTree;
                            private _segment;
                            getUniverse(): number;
                            setUniverse(p_universe: number): void;
                            getTime(): number;
                            setTime(p_time: number): void;
                            getUniverseTree(): org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
                            setUniverseOrder(p_u_tree: org.kevoree.modeling.memory.struct.map.KUniverseOrderMap): void;
                            getTimeTree(): org.kevoree.modeling.memory.struct.tree.KLongTree;
                            setTimeTree(p_t_tree: org.kevoree.modeling.memory.struct.tree.KLongTree): void;
                            getSegment(): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
                            setSegment(p_segment: org.kevoree.modeling.memory.struct.segment.KMemorySegment): void;
                        }
                        class ResolutionHelper {
                            static resolve_trees(universe: number, time: number, uuid: number, cache: org.kevoree.modeling.memory.struct.cache.KCache): org.kevoree.modeling.memory.manager.impl.MemorySegmentResolutionTrace;
                            static resolve_universe(globalTree: org.kevoree.modeling.memory.struct.map.KLongLongMap, objUniverseTree: org.kevoree.modeling.memory.struct.map.KLongLongMap, timeToResolve: number, originUniverseId: number): number;
                            static universeSelectByRange(globalTree: org.kevoree.modeling.memory.struct.map.KLongLongMap, objUniverseTree: org.kevoree.modeling.memory.struct.map.KLongLongMap, rangeMin: number, rangeMax: number, originUniverseId: number): number[];
                        }
                    }
                }
                module struct {
                    class HeapMemoryFactory implements org.kevoree.modeling.memory.KMemoryFactory {
                        newCacheSegment(): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
                        newLongTree(): org.kevoree.modeling.memory.struct.tree.KLongTree;
                        newLongLongTree(): org.kevoree.modeling.memory.struct.tree.KLongLongTree;
                        newUniverseMap(initSize: number, p_className: string): org.kevoree.modeling.memory.struct.map.KUniverseOrderMap;
                        newFromKey(universe: number, time: number, uuid: number): org.kevoree.modeling.memory.KMemoryElement;
                        newCache(): org.kevoree.modeling.memory.struct.cache.KCache;
                    }
                    module cache {
                        interface KCache {
                            get(universe: number, time: number, obj: number): org.kevoree.modeling.memory.KMemoryElement;
                            put(universe: number, time: number, obj: number, payload: org.kevoree.modeling.memory.KMemoryElement): void;
                            dirties(): org.kevoree.modeling.memory.struct.cache.impl.KCacheDirty[];
                            clear(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                            clean(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                            monitor(origin: org.kevoree.modeling.KObject): void;
                            size(): number;
                        }
                        module impl {
                            class HashMemoryCache implements org.kevoree.modeling.memory.struct.cache.KCache {
                                private elementData;
                                private elementCount;
                                private elementDataSize;
                                private loadFactor;
                                private initalCapacity;
                                private threshold;
                                get(universe: number, time: number, obj: number): org.kevoree.modeling.memory.KMemoryElement;
                                put(universe: number, time: number, obj: number, payload: org.kevoree.modeling.memory.KMemoryElement): void;
                                private complex_insert(previousIndex, hash, universe, time, obj);
                                dirties(): org.kevoree.modeling.memory.struct.cache.impl.KCacheDirty[];
                                clean(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                monitor(origin: org.kevoree.modeling.KObject): void;
                                size(): number;
                                private remove(universe, time, obj, p_metaModel);
                                constructor();
                                clear(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                            }
                            module HashMemoryCache {
                                class Entry {
                                    next: org.kevoree.modeling.memory.struct.cache.impl.HashMemoryCache.Entry;
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
                    module map {
                        interface KIntMap<V> {
                            contains(key: number): boolean;
                            get(key: number): V;
                            put(key: number, value: V): void;
                            each(callback: (p: number, p1: V) => void): void;
                        }
                        interface KIntMapCallBack<V> {
                            on(key: number, value: V): void;
                        }
                        interface KLongLongMap {
                            contains(key: number): boolean;
                            get(key: number): number;
                            put(key: number, value: number): void;
                            each(callback: (p: number, p1: number) => void): void;
                            size(): number;
                            clear(): void;
                        }
                        interface KLongLongMapCallBack<V> {
                            on(key: number, value: number): void;
                        }
                        interface KLongMap<V> {
                            contains(key: number): boolean;
                            get(key: number): V;
                            put(key: number, value: V): void;
                            each(callback: (p: number, p1: V) => void): void;
                            size(): number;
                            clear(): void;
                        }
                        interface KLongMapCallBack<V> {
                            on(key: number, value: V): void;
                        }
                        interface KStringMap<V> {
                            contains(key: string): boolean;
                            get(key: string): V;
                            put(key: string, value: V): void;
                            each(callback: (p: string, p1: V) => void): void;
                            size(): number;
                            clear(): void;
                            remove(key: string): void;
                        }
                        interface KStringMapCallBack<V> {
                            on(key: string, value: V): void;
                        }
                        interface KUniverseOrderMap extends org.kevoree.modeling.memory.struct.map.KLongLongMap, org.kevoree.modeling.memory.KMemoryElement {
                            metaClassName(): string;
                        }
                        module impl {
                            class ArrayIntMap<V> implements org.kevoree.modeling.memory.struct.map.KIntMap<any> {
                                constructor(initalCapacity: number, loadFactor: number);
                                clear(): void;
                                get(key: number): V;
                                put(key: number, pval: V): V;
                                contains(key: number): boolean;
                                remove(key: number): V;
                                size(): number;
                                each(callback: (p: number, p1: V) => void): void;
                            }
                            class ArrayLongLongMap implements org.kevoree.modeling.memory.struct.map.KLongLongMap {
                                private _isDirty;
                                constructor(initalCapacity: number, loadFactor: number);
                                clear(): void;
                                get(key: number): number;
                                put(key: number, pval: number): void;
                                contains(key: number): boolean;
                                remove(key: number): number;
                                size(): number;
                                each(callback: (p: number, p1: number) => void): void;
                                isDirty(): boolean;
                                setClean(mm: any): void;
                                setDirty(): void;
                            }
                            class ArrayLongMap<V> implements org.kevoree.modeling.memory.struct.map.KLongMap<any> {
                                constructor(initalCapacity: number, loadFactor: number);
                                clear(): void;
                                get(key: number): V;
                                put(key: number, pval: V): V;
                                contains(key: number): boolean;
                                remove(key: number): V;
                                size(): number;
                                each(callback: (p: number, p1: V) => void): void;
                            }
                            class ArrayStringMap<V> implements org.kevoree.modeling.memory.struct.map.KStringMap<any> {
                                constructor(initalCapacity: number, loadFactor: number);
                                clear(): void;
                                get(key: string): V;
                                put(key: string, pval: V): V;
                                contains(key: string): boolean;
                                remove(key: string): V;
                                size(): number;
                                each(callback: (p: string, p1: V) => void): void;
                            }
                            class ArrayUniverseOrderMap extends org.kevoree.modeling.memory.struct.map.impl.ArrayLongLongMap implements org.kevoree.modeling.memory.struct.map.KUniverseOrderMap {
                                private _counter;
                                private _className;
                                constructor(initalCapacity: number, loadFactor: number, p_className: string);
                                metaClassName(): string;
                                counter(): number;
                                inc(): void;
                                dec(): void;
                                free(): void;
                                size(): number;
                                serialize(m: any): string;
                                init(payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                            }
                        }
                    }
                    module segment {
                        interface KMemorySegment extends org.kevoree.modeling.memory.KMemoryElement {
                            clone(metaClass: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
                            set(index: number, content: any, metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                            get(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): any;
                            getRefSize(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;
                            getRefElem(index: number, refIndex: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;
                            getRef(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number[];
                            addRef(index: number, newRef: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean;
                            removeRef(index: number, previousRef: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean;
                            clearRef(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                            getInfer(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number[];
                            getInferSize(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;
                            getInferElem(index: number, arrayIndex: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;
                            setInferElem(index: number, arrayIndex: number, valueToInsert: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                            extendInfer(index: number, newSize: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                            modifiedIndexes(metaClass: org.kevoree.modeling.meta.KMetaClass): number[];
                            initMetaClass(metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                            metaClassIndex(): number;
                        }
                        module impl {
                            class HeapMemorySegment implements org.kevoree.modeling.memory.struct.segment.KMemorySegment {
                                private raw;
                                private _counter;
                                private _metaClassIndex;
                                private _modifiedIndexes;
                                private _dirty;
                                initMetaClass(p_metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                                metaClassIndex(): number;
                                isDirty(): boolean;
                                serialize(metaModel: org.kevoree.modeling.meta.KMetaModel): string;
                                modifiedIndexes(p_metaClass: org.kevoree.modeling.meta.KMetaClass): number[];
                                setClean(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                setDirty(): void;
                                init(payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                counter(): number;
                                inc(): void;
                                dec(): void;
                                free(metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                get(index: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass): any;
                                getRefSize(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;
                                getRefElem(index: number, refIndex: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;
                                getRef(index: number, p_metaClass: org.kevoree.modeling.meta.KMetaClass): number[];
                                addRef(index: number, newRef: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean;
                                removeRef(index: number, refToRemove: number, metaClass: org.kevoree.modeling.meta.KMetaClass): boolean;
                                clearRef(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                                getInfer(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number[];
                                getInferSize(index: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;
                                getInferElem(index: number, arrayIndex: number, metaClass: org.kevoree.modeling.meta.KMetaClass): number;
                                setInferElem(index: number, arrayIndex: number, valueToInsert: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                                extendInfer(index: number, newSize: number, metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                                set(index: number, content: any, p_metaClass: org.kevoree.modeling.meta.KMetaClass): void;
                                clone(p_metaClass: org.kevoree.modeling.meta.KMetaClass): org.kevoree.modeling.memory.struct.segment.KMemorySegment;
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
                        }
                        interface KTree extends org.kevoree.modeling.memory.KMemoryElement {
                            size(): number;
                        }
                        interface KTreeWalker {
                            elem(t: number): void;
                        }
                        module impl {
                            class AbstractArrayTree {
                                _root_index: number;
                                _size: number;
                                _threshold: number;
                                _loadFactor: number;
                                _back: number[];
                                private _dirty;
                                private _counter;
                                private static BLACK_LEFT;
                                private static BLACK_RIGHT;
                                private static RED_LEFT;
                                private static RED_RIGHT;
                                constructor();
                                ELEM_SIZE(): number;
                                private allocate(capacity);
                                size(): number;
                                key(p_currentIndex: number): number;
                                setKey(p_currentIndex: number, p_paramIndex: number): void;
                                left(p_currentIndex: number): number;
                                setLeft(p_currentIndex: number, p_paramIndex: number): void;
                                right(p_currentIndex: number): number;
                                setRight(p_currentIndex: number, p_paramIndex: number): void;
                                private parent(p_currentIndex);
                                setParent(p_currentIndex: number, p_paramIndex: number): void;
                                private color(currentIndex);
                                setColor(currentIndex: number, paramIndex: number): void;
                                value(currentIndex: number): number;
                                setValue(currentIndex: number, paramIndex: number): void;
                                grandParent(currentIndex: number): number;
                                sibling(currentIndex: number): number;
                                uncle(currentIndex: number): number;
                                private previous(p_index);
                                lookup(p_key: number): number;
                                range(startKey: number, endKey: number, walker: (p: number) => void): void;
                                internal_previousOrEqual_index(p_key: number): number;
                                private rotateLeft(n);
                                private rotateRight(n);
                                private replaceNode(oldn, newn);
                                insertCase1(n: number): void;
                                private insertCase2(n);
                                private insertCase3(n);
                                private insertCase4(n_n);
                                private insertCase5(n);
                                private nodeColor(n);
                                serialize(metaModel: org.kevoree.modeling.meta.KMetaModel): string;
                                init(payload: string, metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                isDirty(): boolean;
                                setClean(p_metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                                setDirty(): void;
                                counter(): number;
                                inc(): void;
                                dec(): void;
                                free(p_metaModel: org.kevoree.modeling.meta.KMetaModel): void;
                            }
                            class ArrayLongLongTree extends org.kevoree.modeling.memory.struct.tree.impl.AbstractArrayTree implements org.kevoree.modeling.memory.struct.tree.KLongLongTree {
                                private static SIZE_NODE;
                                constructor();
                                ELEM_SIZE(): number;
                                previousOrEqualValue(p_key: number): number;
                                lookupValue(p_key: number): number;
                                insert(p_key: number, p_value: number): void;
                            }
                            class ArrayLongTree extends org.kevoree.modeling.memory.struct.tree.impl.AbstractArrayTree implements org.kevoree.modeling.memory.struct.tree.KLongTree {
                                private static SIZE_NODE;
                                constructor();
                                ELEM_SIZE(): number;
                                previousOrEqual(key: number): number;
                                insert(key: number): void;
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
                    static SENDER: string;
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
                        private _sender;
                        private _size;
                        getSender(): number;
                        allKeys(): org.kevoree.modeling.KContentKey[];
                        constructor(nbObject: number, p_sender: number);
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
                    setPrecision(precision: number): void;
                }
                interface KMetaClass extends org.kevoree.modeling.meta.KMeta {
                    metaElements(): org.kevoree.modeling.meta.KMeta[];
                    meta(index: number): org.kevoree.modeling.meta.KMeta;
                    metaByName(name: string): org.kevoree.modeling.meta.KMeta;
                    attribute(name: string): org.kevoree.modeling.meta.KMetaAttribute;
                    reference(name: string): org.kevoree.modeling.meta.KMetaReference;
                    operation(name: string): org.kevoree.modeling.meta.KMetaOperation;
                    addAttribute(attributeName: string, p_type: org.kevoree.modeling.KType): org.kevoree.modeling.meta.KMetaAttribute;
                    addReference(referenceName: string, metaClass: org.kevoree.modeling.meta.KMetaClass, oppositeName: string, toMany: boolean): org.kevoree.modeling.meta.KMetaReference;
                    addDependency(dependencyName: string, p_metaClass: org.kevoree.modeling.meta.KMetaClass, oppositeName: string): org.kevoree.modeling.meta.KMetaDependency;
                    addInput(name: string, extractor: string): org.kevoree.modeling.meta.KMetaInferInput;
                    addOutput(name: string, metaClass: org.kevoree.modeling.KType): org.kevoree.modeling.meta.KMetaInferOutput;
                    addOperation(operationName: string): org.kevoree.modeling.meta.KMetaOperation;
                    inferAlg(): org.kevoree.modeling.infer.KInferAlg;
                    dependencies(): org.kevoree.modeling.meta.KMetaDependencies;
                    inputs(): org.kevoree.modeling.meta.KMetaInferInput[];
                    outputs(): org.kevoree.modeling.meta.KMetaInferOutput[];
                }
                interface KMetaDependencies extends org.kevoree.modeling.meta.KMeta {
                    origin(): org.kevoree.modeling.meta.KMetaClass;
                    allDependencies(): org.kevoree.modeling.meta.KMetaDependency[];
                    dependencyByName(dependencyName: string): org.kevoree.modeling.meta.KMetaDependency;
                    addDependency(dependencyName: string, type: org.kevoree.modeling.meta.KMetaClass, oppositeName: string): org.kevoree.modeling.meta.KMetaDependency;
                }
                interface KMetaDependency extends org.kevoree.modeling.meta.KMeta {
                    type(): org.kevoree.modeling.meta.KMetaClass;
                    opposite(): org.kevoree.modeling.meta.KMetaDependency;
                    origin(): org.kevoree.modeling.meta.KMetaDependencies;
                }
                interface KMetaInferInput extends org.kevoree.modeling.meta.KMeta {
                    extractorQuery(): string;
                    extractor(): org.kevoree.modeling.traversal.KTraversal;
                }
                interface KMetaInferOutput extends org.kevoree.modeling.meta.KMeta {
                    type(): org.kevoree.modeling.KType;
                }
                interface KMetaModel extends org.kevoree.modeling.meta.KMeta {
                    metaClasses(): org.kevoree.modeling.meta.KMetaClass[];
                    metaClassByName(name: string): org.kevoree.modeling.meta.KMetaClass;
                    metaClass(index: number): org.kevoree.modeling.meta.KMetaClass;
                    addMetaClass(metaClassName: string): org.kevoree.modeling.meta.KMetaClass;
                    addInferMetaClass(metaClassName: string, inferAlg: org.kevoree.modeling.infer.KInferAlg): org.kevoree.modeling.meta.KMetaClass;
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
                    static CONTINUOUS: org.kevoree.modeling.KType;
                }
                class MetaType {
                    static ATTRIBUTE: MetaType;
                    static REFERENCE: MetaType;
                    static DEPENDENCY: MetaType;
                    static DEPENDENCIES: MetaType;
                    static INPUT: MetaType;
                    static OUTPUT: MetaType;
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
                    class GenericObjectInfer extends org.kevoree.modeling.abs.AbstractKObjectInfer {
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
                        setPrecision(p_precision: number): void;
                        constructor(p_name: string, p_index: number, p_precision: number, p_key: boolean, p_metaType: org.kevoree.modeling.KType, p_extrapolation: org.kevoree.modeling.extrapolation.Extrapolation);
                    }
                    class MetaClass implements org.kevoree.modeling.meta.KMetaClass {
                        private _name;
                        private _index;
                        private _meta;
                        private _indexes;
                        private _alg;
                        private _cachedInputs;
                        private _cachedOutputs;
                        constructor(p_name: string, p_index: number, p_alg: org.kevoree.modeling.infer.KInferAlg);
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
                        addAttribute(attributeName: string, p_type: org.kevoree.modeling.KType): org.kevoree.modeling.meta.KMetaAttribute;
                        private internal_addatt(attributeName, p_type);
                        addReference(referenceName: string, p_metaClass: org.kevoree.modeling.meta.KMetaClass, oppositeName: string, toMany: boolean): org.kevoree.modeling.meta.KMetaReference;
                        private internal_addref(referenceName, p_metaClass, oppositeName, toMany);
                        private getOrCreate(p_name, p_oppositeName, p_oppositeClass, p_visible, p_single);
                        addOperation(operationName: string): org.kevoree.modeling.meta.KMetaOperation;
                        inferAlg(): org.kevoree.modeling.infer.KInferAlg;
                        addDependency(dependencyName: string, p_metaClass: org.kevoree.modeling.meta.KMetaClass, oppositeName: string): org.kevoree.modeling.meta.KMetaDependency;
                        addInput(p_name: string, p_extractor: string): org.kevoree.modeling.meta.KMetaInferInput;
                        addOutput(p_name: string, p_type: org.kevoree.modeling.KType): org.kevoree.modeling.meta.KMetaInferOutput;
                        dependencies(): org.kevoree.modeling.meta.KMetaDependencies;
                        inputs(): org.kevoree.modeling.meta.KMetaInferInput[];
                        private cacheInputs();
                        outputs(): org.kevoree.modeling.meta.KMetaInferOutput[];
                        private cacheOuputs();
                        private clearCached();
                        private internal_add_meta(p_new_meta);
                    }
                    class MetaDependencies implements org.kevoree.modeling.meta.KMetaDependencies {
                        private _origin;
                        private _dependencies;
                        static DEPENDENCIES_NAME: string;
                        private _index;
                        private _indexes;
                        constructor(p_index: number, p_origin: org.kevoree.modeling.meta.KMetaClass);
                        origin(): org.kevoree.modeling.meta.KMetaClass;
                        allDependencies(): org.kevoree.modeling.meta.KMetaDependency[];
                        dependencyByName(dependencyName: string): org.kevoree.modeling.meta.KMetaDependency;
                        index(): number;
                        metaName(): string;
                        metaType(): org.kevoree.modeling.meta.MetaType;
                        addDependency(p_dependencyName: string, p_type: org.kevoree.modeling.meta.KMetaClass, op_name: string): org.kevoree.modeling.meta.KMetaDependency;
                        private internal_add_dep(p_new_meta);
                    }
                    class MetaDependency implements org.kevoree.modeling.meta.KMetaDependency {
                        private _name;
                        private _index;
                        private _origin;
                        private _lazyMetaType;
                        private _oppositeName;
                        constructor(p_name: string, p_index: number, p_origin: org.kevoree.modeling.meta.KMetaDependencies, p_lazyMetaType: () => org.kevoree.modeling.meta.KMeta, p_oppositeName: string);
                        type(): org.kevoree.modeling.meta.KMetaClass;
                        opposite(): org.kevoree.modeling.meta.KMetaDependency;
                        origin(): org.kevoree.modeling.meta.KMetaDependencies;
                        index(): number;
                        metaName(): string;
                        metaType(): org.kevoree.modeling.meta.MetaType;
                    }
                    class MetaInferInput implements org.kevoree.modeling.meta.KMetaInferInput {
                        private _name;
                        private _index;
                        private _extractor;
                        private _cachedTraversal;
                        constructor(p_name: string, p_index: number, p_extractor: string);
                        extractorQuery(): string;
                        extractor(): org.kevoree.modeling.traversal.KTraversal;
                        private cacheTraversal();
                        index(): number;
                        metaName(): string;
                        metaType(): org.kevoree.modeling.meta.MetaType;
                    }
                    class MetaInferOutput implements org.kevoree.modeling.meta.KMetaInferOutput {
                        private _name;
                        private _index;
                        private _type;
                        constructor(p_name: string, p_index: number, p_type: org.kevoree.modeling.KType);
                        index(): number;
                        metaName(): string;
                        metaType(): org.kevoree.modeling.meta.MetaType;
                        type(): org.kevoree.modeling.KType;
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
                        addInferMetaClass(metaClassName: string, inferAlg: org.kevoree.modeling.infer.KInferAlg): org.kevoree.modeling.meta.KMetaClass;
                        private internal_addmetaclass(metaClassName, alg);
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
                    eval(expression: string, callback: (p: any[]) => void): void;
                    map(attribute: org.kevoree.modeling.meta.KMetaAttribute, cb: (p: any[]) => void): void;
                    collect(metaReference: org.kevoree.modeling.meta.KMetaReference, continueCondition: (p: org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;
                    traverseTime(timeOffset: number, steps: number, continueCondition: (p: org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;
                    traverseUniverse(universeOffset: number, continueCondition: (p: org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;
                    traverseIndex(indexName: string): org.kevoree.modeling.traversal.KTraversal;
                    exec(origins: org.kevoree.modeling.KObject[], resolver: (p: string) => org.kevoree.modeling.KObject[], callback: (p: any[]) => void): void;
                }
                interface KTraversalAction {
                    chain(next: org.kevoree.modeling.traversal.KTraversalAction): void;
                    execute(context: org.kevoree.modeling.traversal.KTraversalActionContext): void;
                }
                interface KTraversalActionContext {
                    inputObjects(): org.kevoree.modeling.KObject[];
                    setInputObjects(newSet: org.kevoree.modeling.KObject[]): void;
                    indexResolver(): (p: string) => org.kevoree.modeling.KObject[];
                    finalCallback(): (p: any[]) => void;
                }
                interface KTraversalFilter {
                    filter(obj: org.kevoree.modeling.KObject): boolean;
                }
                interface KTraversalIndexResolver {
                    resolve(indexName: string): org.kevoree.modeling.KObject[];
                }
                module impl {
                    class Traversal implements org.kevoree.modeling.traversal.KTraversal {
                        private static TERMINATED_MESSAGE;
                        private _initObjs;
                        private _initAction;
                        private _lastAction;
                        private _terminated;
                        constructor(p_roots: org.kevoree.modeling.KObject[]);
                        private internal_chain_action(p_action);
                        traverse(p_metaReference: org.kevoree.modeling.meta.KMetaReference): org.kevoree.modeling.traversal.KTraversal;
                        traverseQuery(p_metaReferenceQuery: string): org.kevoree.modeling.traversal.KTraversal;
                        withAttribute(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_expectedValue: any): org.kevoree.modeling.traversal.KTraversal;
                        withoutAttribute(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_expectedValue: any): org.kevoree.modeling.traversal.KTraversal;
                        attributeQuery(p_attributeQuery: string): org.kevoree.modeling.traversal.KTraversal;
                        filter(p_filter: (p: org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;
                        collect(metaReference: org.kevoree.modeling.meta.KMetaReference, continueCondition: (p: org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;
                        traverseIndex(p_indexName: string): org.kevoree.modeling.traversal.KTraversal;
                        traverseTime(timeOffset: number, steps: number, continueCondition: (p: org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;
                        traverseUniverse(universeOffset: number, continueCondition: (p: org.kevoree.modeling.KObject) => boolean): org.kevoree.modeling.traversal.KTraversal;
                        then(cb: (p: org.kevoree.modeling.KObject[]) => void): void;
                        eval(p_expression: string, callback: (p: any[]) => void): void;
                        map(attribute: org.kevoree.modeling.meta.KMetaAttribute, cb: (p: any[]) => void): void;
                        exec(origins: org.kevoree.modeling.KObject[], resolver: (p: string) => org.kevoree.modeling.KObject[], callback: (p: any[]) => void): void;
                    }
                    class TraversalContext implements org.kevoree.modeling.traversal.KTraversalActionContext {
                        private _inputs;
                        private _resolver;
                        private _finalCallback;
                        constructor(_inputs: org.kevoree.modeling.KObject[], _resolver: (p: string) => org.kevoree.modeling.KObject[], p_finalCallback: (p: any[]) => void);
                        inputObjects(): org.kevoree.modeling.KObject[];
                        setInputObjects(p_newSet: org.kevoree.modeling.KObject[]): void;
                        indexResolver(): (p: string) => org.kevoree.modeling.KObject[];
                        finalCallback(): (p: any[]) => void;
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
                            execute(context: org.kevoree.modeling.traversal.KTraversalActionContext): void;
                            private executeStep(p_inputStep, private_callback);
                        }
                        class FilterAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            private _filter;
                            constructor(p_filter: (p: org.kevoree.modeling.KObject) => boolean);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(context: org.kevoree.modeling.traversal.KTraversalActionContext): void;
                        }
                        class FilterAttributeAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            private _attribute;
                            private _expectedValue;
                            constructor(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_expectedValue: any);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(context: org.kevoree.modeling.traversal.KTraversalActionContext): void;
                        }
                        class FilterAttributeQueryAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            private _attributeQuery;
                            constructor(p_attributeQuery: string);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(context: org.kevoree.modeling.traversal.KTraversalActionContext): void;
                            private buildParams(p_paramString);
                        }
                        module FilterAttributeQueryAction {
                            class QueryParam {
                                private _name;
                                private _value;
                                private _negative;
                                constructor(p_name: string, p_value: string, p_negative: boolean);
                                name(): string;
                                value(): string;
                                isNegative(): boolean;
                            }
                        }
                        class FilterNotAttributeAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            private _attribute;
                            private _expectedValue;
                            constructor(p_attribute: org.kevoree.modeling.meta.KMetaAttribute, p_expectedValue: any);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(context: org.kevoree.modeling.traversal.KTraversalActionContext): void;
                        }
                        class MapAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _attribute;
                            constructor(p_attribute: org.kevoree.modeling.meta.KMetaAttribute);
                            chain(next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(context: org.kevoree.modeling.traversal.KTraversalActionContext): void;
                        }
                        class MathExpressionAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _expression;
                            private _engine;
                            constructor(p_expression: string);
                            chain(next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(context: org.kevoree.modeling.traversal.KTraversalActionContext): void;
                        }
                        class RemoveDuplicateAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(context: org.kevoree.modeling.traversal.KTraversalActionContext): void;
                        }
                        class TraverseAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            private _reference;
                            constructor(p_reference: org.kevoree.modeling.meta.KMetaReference);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(context: org.kevoree.modeling.traversal.KTraversalActionContext): void;
                        }
                        class TraverseIndexAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private _next;
                            private _indexName;
                            constructor(p_indexName: string);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(context: org.kevoree.modeling.traversal.KTraversalActionContext): void;
                        }
                        class TraverseQueryAction implements org.kevoree.modeling.traversal.KTraversalAction {
                            private SEP;
                            private _next;
                            private _referenceQuery;
                            constructor(p_referenceQuery: string);
                            chain(p_next: org.kevoree.modeling.traversal.KTraversalAction): void;
                            execute(context: org.kevoree.modeling.traversal.KTraversalActionContext): void;
                        }
                    }
                }
                module query {
                    interface KQueryEngine {
                        eval(query: string, origins: org.kevoree.modeling.KObject[], callback: (p: any[]) => void): void;
                        buildTraversal(query: string): org.kevoree.modeling.traversal.KTraversal;
                    }
                    module impl {
                        class QueryEngine implements org.kevoree.modeling.traversal.query.KQueryEngine {
                            private static INSTANCE;
                            static OPEN_BRACKET: string;
                            static CLOSE_BRACKET: string;
                            static PIPE_SEP: string;
                            static getINSTANCE(): org.kevoree.modeling.traversal.query.KQueryEngine;
                            eval(query: string, origins: org.kevoree.modeling.KObject[], callback: (p: any[]) => void): void;
                            buildTraversal(query: string): org.kevoree.modeling.traversal.KTraversal;
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
                module maths {
                    class AdjLinearSolverQr {
                        numRows: number;
                        numCols: number;
                        private decomposer;
                        maxRows: number;
                        maxCols: number;
                        Q: org.kevoree.modeling.util.maths.DenseMatrix64F;
                        R: org.kevoree.modeling.util.maths.DenseMatrix64F;
                        private Y;
                        private Z;
                        setA(A: org.kevoree.modeling.util.maths.DenseMatrix64F): boolean;
                        private solveU(U, b, n);
                        solve(B: org.kevoree.modeling.util.maths.DenseMatrix64F, X: org.kevoree.modeling.util.maths.DenseMatrix64F): void;
                        constructor();
                        setMaxSize(maxRows: number, maxCols: number): void;
                    }
                    class Correlations {
                        static pearson(x: number[], y: number[]): number;
                    }
                    class DenseMatrix64F {
                        numRows: number;
                        numCols: number;
                        data: number[];
                        static MULT_COLUMN_SWITCH: number;
                        static multTransA_smallMV(A: org.kevoree.modeling.util.maths.DenseMatrix64F, B: org.kevoree.modeling.util.maths.DenseMatrix64F, C: org.kevoree.modeling.util.maths.DenseMatrix64F): void;
                        static multTransA_reorderMV(A: org.kevoree.modeling.util.maths.DenseMatrix64F, B: org.kevoree.modeling.util.maths.DenseMatrix64F, C: org.kevoree.modeling.util.maths.DenseMatrix64F): void;
                        static multTransA_reorderMM(a: org.kevoree.modeling.util.maths.DenseMatrix64F, b: org.kevoree.modeling.util.maths.DenseMatrix64F, c: org.kevoree.modeling.util.maths.DenseMatrix64F): void;
                        static multTransA_smallMM(a: org.kevoree.modeling.util.maths.DenseMatrix64F, b: org.kevoree.modeling.util.maths.DenseMatrix64F, c: org.kevoree.modeling.util.maths.DenseMatrix64F): void;
                        static multTransA(a: org.kevoree.modeling.util.maths.DenseMatrix64F, b: org.kevoree.modeling.util.maths.DenseMatrix64F, c: org.kevoree.modeling.util.maths.DenseMatrix64F): void;
                        static setIdentity(mat: org.kevoree.modeling.util.maths.DenseMatrix64F): void;
                        static widentity(width: number): org.kevoree.modeling.util.maths.DenseMatrix64F;
                        static identity(numRows: number, numCols: number): org.kevoree.modeling.util.maths.DenseMatrix64F;
                        static fill(a: org.kevoree.modeling.util.maths.DenseMatrix64F, value: number): void;
                        get(index: number): number;
                        set(index: number, val: number): number;
                        plus(index: number, val: number): number;
                        constructor(numRows: number, numCols: number);
                        reshape(numRows: number, numCols: number, saveValues: boolean): void;
                        cset(row: number, col: number, value: number): void;
                        unsafe_get(row: number, col: number): number;
                        getNumElements(): number;
                    }
                    class Distribution {
                        static inverseNormalCDF(q: number): number;
                        static gaussian(features: number[], means: number[], variances: number[]): number;
                        static parallelGaussian(features: number[], means: number[], variances: number[]): number[];
                        static gaussianOneFeature(feature: number, mean: number, variance: number): number;
                    }
                    class PolynomialFit {
                        A: org.kevoree.modeling.util.maths.DenseMatrix64F;
                        coef: org.kevoree.modeling.util.maths.DenseMatrix64F;
                        y: org.kevoree.modeling.util.maths.DenseMatrix64F;
                        solver: org.kevoree.modeling.util.maths.AdjLinearSolverQr;
                        constructor(degree: number);
                        getCoef(): number[];
                        fit(samplePoints: number[], observations: number[]): void;
                        static extrapolate(time: number, weights: number[]): number;
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
                        getQ(Q: org.kevoree.modeling.util.maths.DenseMatrix64F, compact: boolean): org.kevoree.modeling.util.maths.DenseMatrix64F;
                        getR(R: org.kevoree.modeling.util.maths.DenseMatrix64F, compact: boolean): org.kevoree.modeling.util.maths.DenseMatrix64F;
                        decompose(A: org.kevoree.modeling.util.maths.DenseMatrix64F): boolean;
                        convertToColumnMajor(A: org.kevoree.modeling.util.maths.DenseMatrix64F): void;
                        householder(j: number): void;
                        updateA(w: number): void;
                        static findMax(u: number[], startU: number, length: number): number;
                        static divideElements(j: number, numRows: number, u: number[], u_0: number): void;
                        static computeTauAndDivide(j: number, numRows: number, u: number[], max: number): number;
                        static rank1UpdateMultR(A: org.kevoree.modeling.util.maths.DenseMatrix64F, u: number[], gamma: number, colA0: number, w0: number, w1: number, _temp: number[]): void;
                    }
                    class Ranking {
                        static wilsonRank(positive: number, negative: number, confidence: number): number;
                    }
                    class Statistic {
                        static calcHistogram(data: number[], dataratings: number[], numBins: number): void;
                    }
                    class StringDistance {
                        static levenshtein(s0: string, s1: string): number;
                    }
                    module expression {
                        interface KMathExpressionEngine {
                            eval(p_expression: string): number;
                            setVarResolver(resolver: (p: string) => number): void;
                        }
                        interface KMathVariableResolver {
                            resolve(potentialVarName: string): number;
                        }
                        module impl {
                            class MathEntities {
                                private static INSTANCE;
                                operators: org.kevoree.modeling.memory.struct.map.KStringMap<any>;
                                functions: org.kevoree.modeling.memory.struct.map.KStringMap<any>;
                                static getINSTANCE(): org.kevoree.modeling.util.maths.expression.impl.MathEntities;
                                constructor();
                            }
                            class MathExpressionEngine implements org.kevoree.modeling.util.maths.expression.KMathExpressionEngine {
                                private varResolver;
                                static decimalSeparator: string;
                                static minusSign: string;
                                constructor();
                                static isNumber(st: string): boolean;
                                static isDigit(c: string): boolean;
                                static isLetter(c: string): boolean;
                                static isWhitespace(c: string): boolean;
                                private shuntingYard(expression);
                                eval(p_expression: string): number;
                                setVarResolver(p_resolver: (p: string) => number): void;
                            }
                            class MathExpressionTokenizer {
                                private pos;
                                private input;
                                private previousToken;
                                constructor(input: string);
                                hasNext(): boolean;
                                private peekNextChar();
                                next(): string;
                                getPos(): number;
                            }
                            class MathFunction {
                                private name;
                                private numParams;
                                constructor(name: string, numParams: number);
                                getName(): string;
                                getNumParams(): number;
                                eval(p: number[]): number;
                            }
                            class MathOperation {
                                private oper;
                                private precedence;
                                private leftAssoc;
                                constructor(oper: string, precedence: number, leftAssoc: boolean);
                                getOper(): string;
                                getPrecedence(): number;
                                isLeftAssoc(): boolean;
                                eval(v1: number, v2: number): number;
                            }
                        }
                    }
                    module structure {
                        interface KArray1D {
                            size(): number;
                            get(index: number): number;
                            set(index: number, value: number): number;
                            add(index: number, value: number): number;
                        }
                        interface KArray2D {
                            nbRaws(): number;
                            nbColumns(): number;
                            get(rawIndex: number, columnIndex: number): number;
                            set(rawIndex: number, columnIndex: number, value: number): number;
                            add(rawIndex: number, columnIndex: number, value: number): number;
                        }
                        interface KArray3D {
                            nbRaws(): number;
                            nbColumns(): number;
                            nbDeeps(): number;
                            get(rawIndex: number, columnIndex: number, deepIndex: number): number;
                            set(rawIndex: number, columnIndex: number, deepIndex: number, value: number): number;
                            add(p_rawIndex: number, p_columnIndex: number, p_deepIndex: number, value: number): number;
                        }
                        module impl {
                            class Array1D implements org.kevoree.modeling.util.maths.structure.KArray1D {
                                private _size;
                                private _offset;
                                private _segmentIndex;
                                private _segment;
                                private _metaClass;
                                constructor(p_size: number, p_offset: number, p_segmentIndex: number, p_segment: org.kevoree.modeling.memory.struct.segment.KMemorySegment, p_metaClass: org.kevoree.modeling.meta.KMetaClass);
                                size(): number;
                                get(p_index: number): number;
                                set(p_index: number, p_value: number): number;
                                add(index: number, value: number): number;
                            }
                            class Array2D implements org.kevoree.modeling.util.maths.structure.KArray2D {
                                private _nbRaws;
                                private _nbColumns;
                                private _offset;
                                private _segmentIndex;
                                private _segment;
                                private _metaClass;
                                constructor(p_nbRaws: number, p_nbColumns: number, p_offset: number, p_segmentIndex: number, p_segment: org.kevoree.modeling.memory.struct.segment.KMemorySegment, p_metaClass: org.kevoree.modeling.meta.KMetaClass);
                                nbRaws(): number;
                                nbColumns(): number;
                                get(p_rawIndex: number, p_columnIndex: number): number;
                                set(p_rawIndex: number, p_columnIndex: number, value: number): number;
                                add(rawIndex: number, columnIndex: number, value: number): number;
                            }
                            class Array3D implements org.kevoree.modeling.util.maths.structure.KArray3D {
                                private _nbRaws;
                                private _nbColumns;
                                private _nbDeeps;
                                private _offset;
                                private _segmentIndex;
                                private _segment;
                                private _metaClass;
                                constructor(p_nbRaws: number, p_nbColumns: number, p_nbDeeps: number, p_offset: number, p_segmentIndex: number, p_segment: org.kevoree.modeling.memory.struct.segment.KMemorySegment, p_metaClass: org.kevoree.modeling.meta.KMetaClass);
                                nbRaws(): number;
                                nbColumns(): number;
                                nbDeeps(): number;
                                get(p_rawIndex: number, p_columnIndex: number, p_deepIndex: number): number;
                                set(p_rawIndex: number, p_columnIndex: number, p_deepIndex: number, p_value: number): number;
                                add(p_rawIndex: number, p_columnIndex: number, p_deepIndex: number, value: number): number;
                            }
                        }
                    }
                }
            }
        }
    }
}
