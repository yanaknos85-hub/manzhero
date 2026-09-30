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
import { either as Either } from 'fp-ts';
import * as t from 'io-ts';
export function wrapped(typ, Cls) {
    return new t.Type(Cls.name, function (x) { return x instanceof Cls; }, function (u) { return Either.map(function (x) { return new Cls(x); })(typ.decode(u)); }, function (c) { return (__assign({}, c)); });
}
