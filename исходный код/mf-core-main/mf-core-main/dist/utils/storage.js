export var TokenStorage;
(function (TokenStorage) {
    TokenStorage["token"] = "token";
    TokenStorage["refreshToken"] = "refreshToken";
    TokenStorage["sudirLogoutTweet"] = "sudirLogout";
    TokenStorage["sudirReAuthAmt"] = "sudirReAuthAmt";
})(TokenStorage || (TokenStorage = {}));
var tokenStorage = window.sessionStorage; // изменено с localStorage на sessionStorage по требованию
// безопасников. Необходимо сбрасывать авторизацию после завершения сессии (конкретно закрытия браузера)
var sessStorage = window.sessionStorage;
export var getToken = function () { return tokenStorage.getItem(TokenStorage.token) || ''; };
export var setToken = function (token) {
    tokenStorage.setItem(TokenStorage.token, token);
};
export var removeToken = function () {
    tokenStorage.removeItem(TokenStorage.token);
};
export var getRefreshToken = function () { return tokenStorage.getItem(TokenStorage.refreshToken) || ''; };
export var setRefreshToken = function (token) {
    tokenStorage.setItem(TokenStorage.refreshToken, token);
};
export var removeRefreshToken = function () {
    tokenStorage.removeItem(TokenStorage.refreshToken);
};
export var setSudirLogoutTweet = function () {
    tokenStorage.setItem(TokenStorage.sudirLogoutTweet, 'done');
};
export var getSudirLogoutTweet = function () { return tokenStorage.getItem(TokenStorage.sudirLogoutTweet) || ''; };
export var removeSudirLogoutTweet = function () {
    tokenStorage.removeItem(TokenStorage.sudirLogoutTweet);
};
export var setSudirReAuthAmt = function (amt) {
    sessStorage.setItem(TokenStorage.sudirReAuthAmt, "".concat(amt));
};
export var getSudirReAuthAmt = function () { return +(sessStorage.getItem(TokenStorage.sudirReAuthAmt) || 0); };
export var removeSudirReAuthAmt = function () {
    sessStorage.removeItem(TokenStorage.sudirReAuthAmt);
};
/* Это все временное решение, пока бек не починит обновление/апи на своей стороне */
export var getExpireToken = function () {
    var token = document.cookie.match(new RegExp('(^| )token=([^;]+)'));
    return token ? token[2] : '';
};
export var clearTokenCookie = function () {
    document.cookie = "".concat(TokenStorage.token, "=;expires=").concat(new Date().toUTCString(), ";path=/");
};
export var clear = function () {
    removeSudirLogoutTweet();
    removeToken();
    removeRefreshToken();
    clearTokenCookie();
};
