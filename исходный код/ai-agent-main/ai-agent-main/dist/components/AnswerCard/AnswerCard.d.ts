import React from 'react';
import { Assessment } from '../../api/ai-assistant/ai-assistant.type';
import { ChatHistory } from '../../types/types';
import { UUID } from '../../utils/io-ts';
interface AnswerCardProps {
    dialog: ChatHistory;
    onAssessment: (dialogId: UUID, assessment: Assessment) => void;
}
declare const AnswerCard: React.FC<AnswerCardProps>;
export default AnswerCard;
