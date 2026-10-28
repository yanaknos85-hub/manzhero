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

export const AssistantContext = React.createContext({} as AssistantProps);

export const useAssistantContext = () => {
  const context = React.useContext(AssistantContext);

  if (context === undefined) {
    const error = 'Assistant.Context must be used within a Assistant.Provider';
    throw new Error(error);
  }

  return context;
};
