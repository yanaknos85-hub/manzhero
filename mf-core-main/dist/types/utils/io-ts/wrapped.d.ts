import * as t from 'io-ts';
export declare function wrapped<T, C>(typ: t.Type<T>, Cls: new (x: T) => C): t.Type<C, C, unknown>;
