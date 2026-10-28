import React from 'react';
import { AIType } from '../constants/constants';
import { IHttpService, Message } from '../types/types';
import { UUID } from '../utils/io-ts';
export interface AssistantProps {
    agentType: keyof typeof AIType;
    userId: string | UUID;
    httpService: IHttpService;
    onMessage: (type: Message, description: string) => void;
}
export declare const AssistantContext: React.Context<AssistantProps>;
export declare const useAssistantContext: () => AssistantProps;
