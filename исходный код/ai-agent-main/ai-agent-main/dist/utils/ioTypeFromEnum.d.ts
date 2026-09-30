import * as t from 'io-ts';
export declare function ioTypeFromEnum<EnumType>(enumName: string, theEnum: Record<string, string | number>): t.Type<EnumType>;
