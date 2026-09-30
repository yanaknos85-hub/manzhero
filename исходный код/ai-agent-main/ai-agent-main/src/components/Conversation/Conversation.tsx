import React from 'react';
import Spin from 'antd/lib/spin';

import { Assessment } from '../../api/ai-assistant/ai-assistant.type';
import { initialQuestions } from '../../constants/constants';
import { ChatHistory } from '../../types/types';
import { UUID } from '../../utils/io-ts';
import { getCurrentDate } from '../../utils';

import AnswerCard from '../AnswerCard/AnswerCard';
import EmptyChat from '../EmptyChat/EmptyChat';
import Questions from '../Questions/Questions';

import styles from './Conversation.module.scss';

interface ConversationProps {
  chatHistory: ChatHistory[];
  onAssessment: (dialogId: UUID, assessment: Assessment) => void;
  onMessage: (message: string) => void;
  namePrompt: string;
  isLoading: boolean;
  description?: string;
}

const Conversation: React.FC<ConversationProps> = ({
  chatHistory,
  onAssessment,
  onMessage,
  namePrompt,
  isLoading,
  description,
}) => (
  <div className={styles.container}>
    {chatHistory.length ? (
      <>
        <span className={styles.date}>{getCurrentDate()}</span>
        {chatHistory.map(dialog => (
          <div className={styles.conversation} key={dialog.id}>
            <p className={styles.question}>{dialog.question}</p>
            {dialog.answer && (
              <AnswerCard
                dialog={dialog}
                onAssessment={onAssessment}
              />
            )}
          </div>
        ))}
        {isLoading && (
          <div className={styles.loader}>
            Обработка данных...
            <Spin size="small" />
          </div>
        )}
      </>
    ) : (
      <>
        <EmptyChat title={namePrompt} description={description} />
        <Questions questions={initialQuestions} onMessage={onMessage} />
      </>
    )}
  </div>
);

export default Conversation;
