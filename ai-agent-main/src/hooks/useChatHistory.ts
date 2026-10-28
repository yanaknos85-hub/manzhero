import { useCallback, useState } from 'react';

import { EvaluationTypes } from '../constants/constants';
import { ChatHistory } from '../types/types';
import { UUID } from '../utils/io-ts';
import uuid from '../utils/uuid';

const useChatHistory = () => {
  const [chatHistory, setChatHistory] = useState<ChatHistory[]>([]);

  const onAddHistory = useCallback((question: string): UUID => {
    const id = uuid();

    setChatHistory(state => [
      ...state,
      {
        id,
        question,
        userEvaluation: EvaluationTypes.NOT_EVALUATED,
      },
    ]);

    return id;
  }, []);

  const onEditHistory = useCallback((id: UUID, values: Partial<ChatHistory>) => {
    setChatHistory(state => state.map(history => (history.id === id ? { ...history, ...values } : history)));
  }, []);

  const onResetChat = useCallback(() => {
    setChatHistory([]);
  }, []);

  return {
    chatHistory,
    onAddHistory,
    onEditHistory,
    onResetChat,
  };
};

export default useChatHistory;
