import React from 'react';
import * as t from 'io-ts';

declare enum AIType {
    WebCorp = "WEB_CORP",
    Client = "CLIENT",
    Dispatcher = "DISPATCHER",
    Autoservice = "AUTOSERVICE"
}

interface UUIDBrand {
    readonly UUID: unique symbol;
}
type UUID = t.Branded<string, UUIDBrand>;

interface AxiosResponse<T> {
    data: T;
    status: number;
    statusText: string;
    headers: any;
    config: any;
    request?: any;
}
type Message = 'info' | 'success' | 'warning' | 'error';
interface IHttpService {
    get<T>(url: string, params?: Record<string, unknown>): Promise<AxiosResponse<T>>;
    post<T>(url: string, data: Record<string, unknown>, params?: Record<string, unknown>): Promise<AxiosResponse<T>>;
}

interface AssistantProps {
    agentType: keyof typeof AIType;
    userId: string | UUID;
    httpService: IHttpService;
    onMessage: (type: Message, description: string) => void;
}

declare const _default: (props: AssistantProps) => React.JSX.Element;

export { _default as AiAssistant };
