import * as t from 'io-ts';

import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';
import { AIType, EvaluationTypes } from '../../constants/constants';

/** ИИ ассистент */
export const Agent = t.intersection([
  t.type({
    /** Уникальный идентификатор */
    id: t.string, // tt.uuid,
    /** Наименование ии-агента */
    namePrompt: t.string,
    /** Уникальный идентификатор языковой модели */
    modelId: t.string,
    /** Тип агента по месту применения */
    type: ioTypeFromEnum('AIType', AIType),
    /** Статус активности агента */
    active: t.boolean,
  }),
  t.partial({
    /** Описание системного промпта */
    prompt: t.string,
    /** Контекст промпта, заданный по дефолту */
    defaultContext: t.string,
    /** Динамический выбор словаря модели */
    topP: t.number,
    /** Температура выборки */
    temperature: t.number,
    /** Количество ответов модели на один и тот же вопрос */
    nvalue: t.number,
    /** Штраф на повторения слов */
    repetitionPenalty: t.number,
    /** Максимальное количество токенов для генерации ответов */
    maxTokens: t.number,
  }),
]);

export type Agent = t.TypeOf<typeof Agent>;

/** Данные для отправки запроса ассистенту  */
export const AssistantRequest = t.type({
  /** Текст вопроса */
  question: t.string,
  /** Уникальный идентификатор агента */
  agentId: t.string,
});

export type AssistantRequest = t.TypeOf<typeof AssistantRequest>;

/** Данные которые возвращает ассистент */
export const AssistantResponse = t.type({
  /** Текст ответа */
  answer: t.string,
});

export type AssistantResponse = t.TypeOf<typeof AssistantResponse>;

/** Оценка ассистента */
export const Assessment = t.intersection([
  t.type({
    /** Текст вопроса */
    question: t.string,
    /** Текст ответа */
    answer: t.string,
  }),
  t.partial({
    /** Пользовательская оценка ответа */
    userEvaluation: ioTypeFromEnum<EvaluationTypes>('EvaluationTypes', EvaluationTypes),
    /** Комментарий к оценке ответа, оставленный пользователем */
    comment: t.string,
    /** Оценка Администратором пользовательской оценки */
    adminEvaluation: ioTypeFromEnum<EvaluationTypes>('EvaluationTypes', EvaluationTypes),
    /** Идентификатор пользователя */
    userId: t.string, // tt.uuid,
  }),
]);

export type Assessment = t.TypeOf<typeof Assessment>;
