import * as t from 'io-ts';
interface UUIDBrand {
    readonly UUID: unique symbol;
}
export type UUID = t.Branded<string, UUIDBrand>;
export declare const validateUUID: (s: string) => s is UUID;
export declare const uuid: t.BrandC<t.StringC, {
    readonly UUID: symbol;
}>;
export {};
