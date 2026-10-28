import { ChatHistory } from '../types/types';
import { UUID } from '../utils/io-ts';
declare const useChatHistory: () => {
    chatHistory: ChatHistory[];
    onAddHistory: (question: string) => UUID;
    onEditHistory: (id: UUID, values: Partial<ChatHistory>) => void;
    onResetChat: () => void;
};
export default useChatHistory;
