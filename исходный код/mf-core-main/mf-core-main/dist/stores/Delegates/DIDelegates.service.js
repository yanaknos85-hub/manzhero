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
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import axios from 'axios';
import { inject, injectable } from 'inversify';
import { ADD_DELEGATE, DELETE_DELEGATE, GET_CANDIDATES_IN_DELEGATES_PARAMS, GET_DELEGATES, GET_SELF_CONDIDATES_TO_DELEGATES } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
var DIDelegatesService = /** @class */ (function () {
    function DIDelegatesService() {
    }
    DIDelegatesService.prototype.getCandidatesToDelegates = function (args) {
        var depId = args.depId, orgId = args.orgId, supId = args.supId, transType = args.transType, date = args.date;
        return this.http
            .get(GET_CANDIDATES_IN_DELEGATES_PARAMS, {
            params: { date: date },
            urlParams: {
                orgId: orgId,
                depId: depId,
                supId: supId,
                transType: transType,
            },
        })
            .then(this.process.getResponseData);
    };
    DIDelegatesService.prototype.getSelfCandidatesToDelegates = function (transportType, date) {
        var _this = this;
        return this.http
            .get(GET_SELF_CONDIDATES_TO_DELEGATES, {
            params: { date: date },
            urlParams: {
                transportType: transportType,
            },
        })
            .then(function (data) { return _this.process.getResponseData(data).content; });
    };
    DIDelegatesService.prototype.getDelegates = function (args) {
        var depId = args.depId, orgId = args.orgId, supId = args.supId, date = args.date;
        return this.http
            .get(GET_DELEGATES, {
            params: { date: date },
            urlParams: {
                orgId: orgId,
                depId: depId,
                supId: supId,
            },
        })
            .then(this.process.getResponseData);
    };
    DIDelegatesService.prototype.addDelegate = function (args, delegate) {
        var depId = args.depId, orgId = args.orgId;
        return this.http
            .post(ADD_DELEGATE, __assign({}, delegate), { urlParams: { orgId: orgId, depId: depId } })
            .then(this.process.getResponseData);
    };
    DIDelegatesService.prototype.deleteDelegate = function (args) {
        var depId = args.depId, orgId = args.orgId, delegateId = args.delegateId;
        return this.http
            .delete(DELETE_DELEGATE, {
            urlParams: {
                orgId: orgId,
                depId: depId,
                delId: delegateId,
            },
        })
            .then(this.process.getResponseStatus);
    };
    DIDelegatesService.prototype.searchSelfDelegateCandidates = function (transportType, params, cancelerSetter) {
        var _this = this;
        return this.http
            .get(GET_SELF_CONDIDATES_TO_DELEGATES, {
            params: params,
            urlParams: { transportType: transportType },
            cancelToken: cancelerSetter && new axios.CancelToken(cancelerSetter),
        })
            .then(function (data) { return _this.process.getResponseData(data.data.content); });
    };
    __decorate([
        inject(TYPES.IHttpService),
        __metadata("design:type", Object)
    ], DIDelegatesService.prototype, "http", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DIDelegatesService.prototype, "process", void 0);
    DIDelegatesService = __decorate([
        injectable()
    ], DIDelegatesService);
    return DIDelegatesService;
}());
export { DIDelegatesService };
