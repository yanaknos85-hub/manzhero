var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
/* eslint-disable no-console */
import React from 'react';
import { message, notification } from 'antd';
import Paragraph from 'antd/lib/typography/Paragraph';
import { inject, injectable } from 'inversify';
import cn from 'classnames';
import { useMenuResizingSpy } from '../../hooks/useMenuResizingsSpy';
import { ReactComponent as CloseIcon } from '../../assets/closeIcon.svg';
import Cat from '../../assets/cat.png';
import { SDO_SUPPORT_PHONE, SUPPORT_PHONE } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import styles from './logger.module.scss';
var nextKey = 0;
var CommonLogger = /** @class */ (function () {
    function CommonLogger() {
        var _this = this;
        this.notificationKeys = [];
        this.toConsole = function (type, value) {
            console[type](value);
        };
        this.toConsoleGroup = function (type, description, title) {
            var args = [];
            for (var _i = 3; _i < arguments.length; _i++) {
                args[_i - 3] = arguments[_i];
            }
            console.group(title);
            console[type](description);
            if (args.length > 0) {
                args.forEach(function (x) { return console.info(x); });
            }
            console.groupEnd();
        };
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        this.toNotify = function (type, description, title, duration, code) {
            var _a;
            if (duration === void 0) { duration = 5; }
            var phone = _this.configStore.env.IS_SDO ? SDO_SUPPORT_PHONE : SUPPORT_PHONE;
            var sdoLimitedAccess = _this.configStore.env.IS_SDO && code === 403;
            var renderNotifyActions = function () { return (React.createElement("div", { className: styles.actions },
                React.createElement("button", { className: styles.closeBtn, onClick: _this.closeAll }, "\u0417\u0430\u043A\u0440\u044B\u0442\u044C \u0432\u0441\u0435..."))); };
            var descriptionNode = function (description) {
                var _a;
                return (React.createElement("div", { className: cn((_a = {}, _a[styles.desc] = type === 'error', _a)) },
                    description,
                    type === 'error' ? (React.createElement(React.Fragment, null,
                        React.createElement("a", { href: "tel:".concat(phone.code), className: styles.phone }, phone.title),
                        React.createElement("footer", { className: styles.footer },
                            React.createElement(Paragraph, { type: "secondary", className: styles.code }, code && "\u041E\u0448\u0438\u0431\u043A\u0430 ".concat(code)),
                            _this.notificationKeys.length > 1 && renderNotifyActions()))) : renderNotifyActions()));
            };
            var logToConsole = description.length > 100;
            if (logToConsole) {
                _this.toConsoleGroup('warn', description, title);
            }
            var key = "".concat(nextKey++);
            _this.notificationKeys.push(key);
            notification[type]({
                key: key,
                message: title,
                description: !sdoLimitedAccess && descriptionNode(logToConsole ? "".concat(description.slice(0, 100), "... look to console") : description),
                duration: duration,
                icon: type === 'error' ? (React.createElement("img", { src: Cat, alt: "\u0421\u0431\u0435\u0440\u043A\u043E\u0442", className: styles.icon })) : undefined,
                className: cn(styles.logger, (_a = {},
                    _a[styles.error] = type === 'error',
                    _a)),
                closeIcon: React.createElement(CloseIcon, null),
            });
        };
        this.toMessage = function (type, description) {
            message.config({ top: 100 });
            message[type](description);
            useMenuResizingSpy();
        };
        this.closeAll = function () {
            var notificationsToClose = __spreadArray([], _this.notificationKeys, true);
            _this.notificationKeys.length = 0;
            var closeLastNotification = function () {
                var key = notificationsToClose.pop();
                if (key) {
                    requestAnimationFrame(function () {
                        notification.close(key);
                        if (notificationsToClose.length) {
                            closeLastNotification();
                        }
                    });
                }
            };
            closeLastNotification();
        };
    }
    CommonLogger.prototype.toError = function (description, title) {
        var args = [];
        for (var _i = 2; _i < arguments.length; _i++) {
            args[_i - 2] = arguments[_i];
        }
        // this.toNotify('error', description, title); // в консоль надо смотреть
        this.toConsoleGroup.apply(this, __spreadArray(['error', description, title], args, false));
    };
    __decorate([
        inject(TYPES.IConfigStore),
        __metadata("design:type", Object)
    ], CommonLogger.prototype, "configStore", void 0);
    CommonLogger = __decorate([
        injectable()
    ], CommonLogger);
    return CommonLogger;
}());
export { CommonLogger };
