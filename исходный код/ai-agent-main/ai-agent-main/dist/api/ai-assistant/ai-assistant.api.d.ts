import { AIType } from '../../constants/constants';
import { Assessment, AssistantRequest } from './ai-assistant.type';
/** Получение списка агентов */
export declare const useAgents: () => (type: AIType) => Promise<never[] | ({
    id: string;
    namePrompt: string;
    modelId: string;
    type: any;
    active: boolean;
} & {
    prompt?: string | undefined;
    defaultContext?: string | undefined;
    topP?: number | undefined;
    temperature?: number | undefined;
    nvalue?: number | undefined;
    repetitionPenalty?: number | undefined;
    maxTokens?: number | undefined;
})[]>;
/** Отправка запроса ассистенту */
export declare const useAssistantMessage: () => (data: AssistantRequest) => Promise<{
    answer: string;
}>;
/** Отправить оценку на ответ ассистента */
export declare const useAssessment: () => (data: Assessment) => Promise<void>;
