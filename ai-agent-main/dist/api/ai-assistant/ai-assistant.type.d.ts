import * as t from 'io-ts';
import { EvaluationTypes } from '../../constants/constants';
/** ИИ ассистент */
export declare const Agent: t.IntersectionC<[t.TypeC<{
    /** Уникальный идентификатор */
    id: t.StringC;
    /** Наименование ии-агента */
    namePrompt: t.StringC;
    /** Уникальный идентификатор языковой модели */
    modelId: t.StringC;
    /** Тип агента по месту применения */
    type: t.Type<any, any, unknown>;
    /** Статус активности агента */
    active: t.BooleanC;
}>, t.PartialC<{
    /** Описание системного промпта */
    prompt: t.StringC;
    /** Контекст промпта, заданный по дефолту */
    defaultContext: t.StringC;
    /** Динамический выбор словаря модели */
    topP: t.NumberC;
    /** Температура выборки */
    temperature: t.NumberC;
    /** Количество ответов модели на один и тот же вопрос */
    nvalue: t.NumberC;
    /** Штраф на повторения слов */
    repetitionPenalty: t.NumberC;
    /** Максимальное количество токенов для генерации ответов */
    maxTokens: t.NumberC;
}>]>;
export type Agent = t.TypeOf<typeof Agent>;
/** Данные для отправки запроса ассистенту  */
export declare const AssistantRequest: t.TypeC<{
    /** Текст вопроса */
    question: t.StringC;
    /** Уникальный идентификатор агента */
    agentId: t.StringC;
}>;
export type AssistantRequest = t.TypeOf<typeof AssistantRequest>;
/** Данные которые возвращает ассистент */
export declare const AssistantResponse: t.TypeC<{
    /** Текст ответа */
    answer: t.StringC;
}>;
export type AssistantResponse = t.TypeOf<typeof AssistantResponse>;
/** Оценка ассистента */
export declare const Assessment: t.IntersectionC<[t.TypeC<{
    /** Текст вопроса */
    question: t.StringC;
    /** Текст ответа */
    answer: t.StringC;
}>, t.PartialC<{
    /** Пользовательская оценка ответа */
    userEvaluation: t.Type<EvaluationTypes, EvaluationTypes, unknown>;
    /** Комментарий к оценке ответа, оставленный пользователем */
    comment: t.StringC;
    /** Оценка Администратором пользовательской оценки */
    adminEvaluation: t.Type<EvaluationTypes, EvaluationTypes, unknown>;
    /** Идентификатор пользователя */
    userId: t.StringC;
}>]>;
export type Assessment = t.TypeOf<typeof Assessment>;
