import { createBrowserHistory } from 'history';
export var useHistory = (function (history) { return function () { return history; }; })(createBrowserHistory());
