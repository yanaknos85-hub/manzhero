import { useCallback } from 'react';

import { AIType } from '../../constants/constants';
import { useAssistantContext } from '../../context/Assistant.context';
import { AGENTS, AI_ASSESSMENT, ASSISTANT_MESSAGE } from './ai-assistant.constants';
import {
  Agent, Assessment, AssistantRequest, AssistantResponse
} from './ai-assistant.type';

/** Получение списка агентов */
export const useAgents = () => {
  const { httpService } = useAssistantContext();

  return useCallback(
    (type: AIType) => httpService
      .get<Agent[]>(AGENTS, { params: { type, active: true } })
      .then(({ data }) => data ?? [])
      .catch(() => []),
    [httpService]
  );
};

/** Отправка запроса ассистенту */
export const useAssistantMessage = () => {
  const { httpService, onMessage } = useAssistantContext();

  return useCallback(
    (data: AssistantRequest) => httpService
      .post<AssistantResponse>(ASSISTANT_MESSAGE, data)
      .then(({ data }) => data)
      .catch(() => {
        onMessage('error', 'Не удалось отправить сообщение');
        throw new Error();
      }),
    [httpService, onMessage]
  );
};

/** Отправить оценку на ответ ассистента */
export const useAssessment = () => {
  const { httpService, onMessage } = useAssistantContext();

  return useCallback(
    (data: Assessment) => httpService
      .post(AI_ASSESSMENT, data)
      .then(() => {
        onMessage('success', 'Отзыв отправлен');
      })
      .catch(() => {
        onMessage('error', 'Ошибка при отправке отзыва');
        throw new Error();
      }),
    [httpService, onMessage]
  );
};
