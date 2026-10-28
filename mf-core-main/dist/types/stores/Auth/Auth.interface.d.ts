import { AuthSteps } from '../../constants/constants';
export interface IAuthConfig {
    isBasicAuth?: boolean;
    isMockedAuth?: boolean;
}
export interface IAuthStore {
    isBasicAuth: boolean;
    isMockedAuth: boolean;
    token: string | null;
    isAuthenticated: boolean;
    isAwaiting: boolean;
    transportPassword: boolean;
    step: AuthSteps;
    setRefreshToken: (refreshToken: string) => any;
    setAccessToken: (accessToken: string) => any;
    updateRefreshToken: (refreshToken: string) => Promise<void>;
    updateAccessToken: (accessToken: string) => void;
    login: (login: string, password: string) => void;
    code: (code: string) => void;
    logout: () => void;
    sudir: (url: string) => Promise<void>;
    goToAuthPage?: boolean;
    check: () => void;
}
export interface IAuthService {
    login: (login: string, password: string) => Promise<AuthResponse>;
    code: (code: string, token: string) => Promise<AuthResponse>;
    logout: () => Promise<void>;
    updateRefreshToken: (refreshToken: string) => Promise<AuthResponse>;
}
export interface AuthResponseDefault {
    token: string;
    refreshToken: string;
    transferPassword: false;
}
export interface AuthResponseTransport {
    transferPassword: true;
    token: string;
}
export type AuthResponse = AuthResponseDefault | AuthResponseTransport;
