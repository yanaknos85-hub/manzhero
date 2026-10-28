import type { History } from 'history';
type NavSudirReAuth = ({ reAuth, fail }: {
    reAuth: string;
    fail: string;
}) => void;
export interface INavigator {
    readonly navRoot: () => void;
    readonly navLogout: () => void;
    readonly navSudirReAuth: NavSudirReAuth;
}
export declare class Navigator implements INavigator {
    private configStore;
    get history(): History | null;
    readonly navRoot: () => void;
    readonly navLogout: () => void;
    readonly navSudirReAuth: NavSudirReAuth;
}
export {};
