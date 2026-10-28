import { CustomErrorCode } from '../constants/constants';
export var getErrorCode = function (e) {
    var _a;
    var axiosError = e;
    if (axiosError.isAxiosError) {
        if ((_a = axiosError.response) === null || _a === void 0 ? void 0 : _a.status) {
            return axiosError.response.status;
        }
        if (axiosError.code === 'ECONNABORTED' || axiosError.message.includes('timeout')) {
            return CustomErrorCode.TIMEOUT;
        }
    }
    var error = e;
    if (error.message.includes('Network Error')) {
        return CustomErrorCode.CORS;
    }
    if (error.message.includes('Expecting')) {
        return CustomErrorCode.TYPES;
    }
    if (error.message.includes('Cannot read')) {
        return CustomErrorCode.UNDEFINED_FIELD;
    }
    return CustomErrorCode.UNKNOWN;
};
