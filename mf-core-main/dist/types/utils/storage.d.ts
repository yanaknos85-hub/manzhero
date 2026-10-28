export declare enum TokenStorage {
    token = "token",
    refreshToken = "refreshToken",
    sudirLogoutTweet = "sudirLogout",
    sudirReAuthAmt = "sudirReAuthAmt"
}
export declare const getToken: () => string;
export declare const setToken: (token: string) => void;
export declare const removeToken: () => void;
export declare const getRefreshToken: () => string;
export declare const setRefreshToken: (token: string) => void;
export declare const removeRefreshToken: () => void;
export declare const setSudirLogoutTweet: () => void;
export declare const getSudirLogoutTweet: () => string;
export declare const removeSudirLogoutTweet: () => void;
export declare const setSudirReAuthAmt: (amt: number) => void;
export declare const getSudirReAuthAmt: () => number;
export declare const removeSudirReAuthAmt: () => void;
export declare const getExpireToken: () => string;
export declare const clearTokenCookie: () => void;
export declare const clear: () => void;
