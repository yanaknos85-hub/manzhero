import * as t from 'io-ts';
export function ioTypeFromEnum(enumName, theEnum) {
    var isEnumValue = function (input) { return Object.values(theEnum).includes(input); };
    return new t.Type(enumName, isEnumValue, function (input, context) { return (isEnumValue(input) ? t.success(input) : t.failure(input, context)); }, t.identity);
}
