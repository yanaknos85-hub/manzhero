import React from 'react';
import { Assessment } from '../../api/ai-assistant/ai-assistant.type';
import { ChatHistory } from '../../types/types';
import { UUID } from '../../utils/io-ts';
interface ConversationProps {
    chatHistory: ChatHistory[];
    onAssessment: (dialogId: UUID, assessment: Assessment) => void;
    onMessage: (message: string) => void;
    namePrompt: string;
    isLoading: boolean;
    description?: string;
}
declare const Conversation: React.FC<ConversationProps>;
export default Conversation;
