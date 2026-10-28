import { Agent } from '../api/ai-assistant/ai-assistant.type';
declare const useAgent: (agents: Agent[]) => {
    activeAgent: ({
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
    }) | undefined;
    onActiveAgent: (agentId: string) => void;
};
export default useAgent;
