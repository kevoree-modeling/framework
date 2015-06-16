declare class System {
    static gc(): void;
    static out: {
        println(obj?: any): void;
        print(obj: any): void;
    };
    static err: {
        println(obj?: any): void;
        print(obj: any): void;
    };
    static arraycopy(src: Number[], srcPos: number, dest: Number[], destPos: number, numElements: number): void;
}
interface Number {
    equals: (other: Number) => boolean;
    longValue(): number;
    floatValue(): number;
    intValue(): number;
    shortValue(): number;
}
interface String {
    equals: (other: String) => boolean;
    startsWith: (other: String) => boolean;
    endsWith: (other: String) => boolean;
    matches: (regEx: String) => boolean;
    isEmpty: () => boolean;
    hashCode: () => number;
}
declare class StringUtils {
    static copyValueOf(data: string[], offset: number, count: number): string;
}
interface Boolean {
    equals: (other: String) => boolean;
}
declare module java {
    module lang {
        class Double {
            static parseDouble(val: string): number;
        }
        class Float {
            static parseFloat(val: string): number;
        }
        class Integer {
            static parseInt(val: string): number;
        }
        class Long {
            static parseLong(val: string): number;
        }
        class Boolean {
            static parseBoolean(val: string): boolean;
        }
        class Short {
            static MIN_VALUE: number;
            static MAX_VALUE: number;
            static parseShort(val: string): number;
        }
        class Throwable {
            private message;
            private error;
            constructor(message: string);
            printStackTrace(): void;
        }
        class Exception extends Throwable {
        }
        class RuntimeException extends Exception {
        }
        class IndexOutOfBoundsException extends Exception {
        }
        interface Runnable {
            run(): void;
        }
        class StringBuilder {
            buffer: string;
            length: number;
            append(val: any): StringBuilder;
            toString(): string;
        }
        module ref {
            class WeakReference<A> {
            }
        }
    }
    module util {
        class Random {
            nextInt(max: number): number;
            nextDouble(): number;
        }
        class Arrays {
            static fill(data: Number[], begin: number, nbElem: number, param: number): void;
        }
        class Collections {
            static reverse<A>(p: List<A>): void;
            static sort<A>(p: List<A>): void;
        }
        interface Collection<T> {
            add(val: T): void;
            addAll(vals: Collection<T>): any;
            remove(val: T): any;
            clear(): any;
            isEmpty(): boolean;
            size(): number;
            contains(val: T): boolean;
            toArray(a: Array<T>): T[];
        }
        class XArray {
            constructor();
            pop(): any;
            push(val: any): number;
            splice(newS: any, arrL: any): void;
            length: number;
            indexOf(val: any): number;
            shift(): any;
            sort(): void;
        }
        class List<T> extends XArray implements Collection<T> {
            addAll(vals: Collection<T>): void;
            clear(): void;
            poll(): T;
            remove(val: T): void;
            toArray(a: Array<T>): T[];
            size(): number;
            add(val: T): void;
            get(index: number): T;
            contains(val: T): boolean;
            isEmpty(): boolean;
        }
        class ArrayList<T> extends List<T> {
        }
        class LinkedList<T> extends List<T> {
        }
        class Map<K, V> {
            get(key: K): V;
            put(key: K, value: V): V;
            containsKey(key: K): boolean;
            remove(key: K): V;
            keySet(): Set<K>;
            isEmpty(): boolean;
            values(): Set<V>;
            clear(): void;
        }
        class HashMap<K, V> extends Map<K, V> {
        }
        class Set<T> implements Collection<T> {
            add(val: T): void;
            clear(): void;
            contains(val: T): boolean;
            addAll(vals: Collection<T>): void;
            remove(val: T): void;
            size(): number;
            isEmpty(): boolean;
            toArray(a: Array<T>): T[];
        }
        class HashSet<T> extends Set<T> {
        }
    }
}
declare module org {
    module junit {
        class Assert {
            static assertNotNull(p: any): void;
            static assertNull(p: any): void;
            static assertEquals(p: any, p2: any): void;
            static assertNotEquals(p: any, p2: any): void;
            static assertTrue(b: boolean): void;
            static assertFalse(b: boolean): void;
        }
    }
}
