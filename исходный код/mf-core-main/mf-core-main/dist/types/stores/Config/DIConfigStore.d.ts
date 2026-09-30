import { IConfigStore } from './Config.interface';
import type { ENVConfig } from './Config.interface';
export declare class DIConfigStore implements IConfigStore {
    isBasicAuth: boolean;
    isMockedAuth: boolean;
    isMockedApi: boolean;
    isCorp: boolean;
    history: import("history").History<unknown>;
    env: ENVConfig;
    constructor();
    private fillEnvConfig;
    private parseEnv;
    private setDefaultConfig;
    setConfig: (config: Partial<IConfigStore>) => void;
}
