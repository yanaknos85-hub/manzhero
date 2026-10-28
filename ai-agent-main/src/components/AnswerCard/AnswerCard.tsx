import React from 'react';
import cn from 'classnames';

import { Assessment } from '../../api/ai-assistant/ai-assistant.type';
import { EvaluationTypes } from '../../constants/constants';
import Icon from '../../components/Icon/Icon';
import { useAssistantContext } from '../../context/Assistant.context';
import { ChatHistory } from '../../types/types';
import { handleCopy } from '../../utils';
import { UUID } from '../../utils/io-ts';

import styles from './AnswerCard.module.scss';

interface AnswerCardProps {
  dialog: ChatHistory;
  onAssessment: (dialogId: UUID, assessment: Assessment) => void;
}

const AnswerCard: React.FC<AnswerCardProps> = ({ dialog, onAssessment }) => {
  const { onMessage } = useAssistantContext();

  const {
    id: dialogId, question, answer, userEvaluation,
  } = dialog;

  const onCopy = (text: string) => {
    handleCopy(text);
    onMessage('success', 'Текст скопирован');
  };

  const onEvaluation = (type: EvaluationTypes) => () => {
    if (userEvaluation !== EvaluationTypes.NOT_EVALUATED) return;

    onAssessment(dialogId, {
      question,
      answer: answer!,
      userEvaluation: type,
      adminEvaluation: EvaluationTypes.NOT_EVALUATED,
    });
  };

  return (
    <div className={styles.answer}>
      <p className={styles.text}>{answer}</p>
      <div className={styles.actions}>
        <Icon name="copy" onClick={() => onCopy(answer!)} />
        <Icon
          name="like"
          className={cn({
            [styles.likeActive]: userEvaluation === EvaluationTypes.LIKE,
            [styles.inActive]: userEvaluation !== EvaluationTypes.NOT_EVALUATED,
          })}
          onClick={onEvaluation(EvaluationTypes.LIKE)}
        />
        <Icon
          name="dislike"
          className={cn({
            [styles.dislikeActive]: userEvaluation === EvaluationTypes.DISLIKE,
            [styles.inActive]: userEvaluation !== EvaluationTypes.NOT_EVALUATED,
          })}
          onClick={onEvaluation(EvaluationTypes.DISLIKE)}
        />
      </div>
    </div>
  );
};

export default AnswerCard;
