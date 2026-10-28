import * as t from 'io-ts';
export * from './wrapped';
export declare const optional: <T, U>(x: t.Type<T, U, unknown>) => t.UnionC<[t.Type<T, U, unknown>, t.UndefinedC]>;
export declare function withValidate<C extends t.Any>(codec: C, validate: C['validate'], name?: string): C;
export declare const nullable: <T, U>(x: t.Type<T, U, unknown>) => t.UnionC<[t.Type<T, U, unknown>, t.NullC]>;
export declare function fallback<C extends t.Any>(codec: C, a: t.TypeOf<C>, name?: string): C;
export type NumberStringC = t.Type<number, string, unknown>;
export declare const numberString: NumberStringC;
export type MoneyC = t.Type<number, number, unknown>;
export declare const money: MoneyC;
interface UUIDBrand {
    readonly UUID: unique symbol;
}
export type UUID = t.Branded<string, UUIDBrand>;
export declare const validateUUID: (s: string) => s is UUID;
export declare const uuid: t.BrandC<t.StringC, UUIDBrand>;
type Literals<Variants extends [string, string, ...string[]]> = {
    [K in keyof Variants]: Variants[K] extends string ? t.LiteralC<Variants[K]> : never;
};
export declare const oneOf: <T extends string, Variants extends [T, T, ...T[]]>(variants: Variants) => t.UnionC<Literals<Variants>>;
