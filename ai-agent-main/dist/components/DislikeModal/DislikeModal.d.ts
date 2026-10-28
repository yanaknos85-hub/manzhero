import React from 'react';
import { Assessment } from '../../api/ai-assistant/ai-assistant.type';
import { ChatHistory } from '../../types/types';
import { UUID } from '../../utils/io-ts';
interface DislikeModalProps {
    dialog: ChatHistory | null;
    onAssessment: (dialogId: UUID, assessment: Assessment) => void;
    onClose: () => void;
}
declare const DislikeModal: React.FC<DislikeModalProps>;
export default DislikeModal;
