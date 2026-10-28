var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
import { either, orElse } from 'fp-ts/lib/Either';
import * as t from 'io-ts';
import * as R from 'ramda';
export * from './wrapped';
// eslint-disable-next-line @stylistic/max-len
export var optional = function (x) { return t.union([x, /* t.null, */ t.undefined]); };
export function withValidate(codec, validate, name) {
    if (name === void 0) { name = codec.name; }
    var r = __assign(__assign({}, codec), { validate: validate, name: name, decode: function (i) { return validate(i, t.getDefaultContext(r)); } });
    return r;
}
export var nullable = function (x) { return t.union([x, t.null]); };
export function fallback(codec, a, name) {
    if (name === void 0) { name = "withFallback(".concat(codec.name, ")"); }
    return withValidate(codec, function (u, c) { return orElse(function () { return t.success(a); })(codec.validate(u, c)); }, name);
}
export var numberString = new t.Type('NumberFromString', t.number.is, function (u, c) { return either.chain(t.string.validate(u, c), function (s) {
    var n = +s;
    return Number.isNaN(n) || s.trim() === '' ? t.failure(u, c) : t.success(n);
}); }, String);
export var money = new t.Type('Money', t.number.is, function (u, c) { return either.chain(t.number.validate(u, c), function (s) { return t.success(s / 100); }); }, function (n) { return Math.floor(n * 100); });
var uuidRe = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
export var validateUUID = function (s) { return uuidRe.test(s); };
export var uuid = t.brand(t.string, validateUUID, 'UUID');
export var oneOf = function (variants
// @ts-ignore
) { return t.union(R.map(t.literal)(variants)); };
