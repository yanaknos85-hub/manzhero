import React, { useState } from 'react';
import { Modal } from 'antd';
import TextArea from 'antd/lib/input/TextArea';

import { Assessment } from '../../api/ai-assistant/ai-assistant.type';
import { DISLIKE_OPTIONS, EvaluationTypes } from '../../constants/constants';
import { ChatHistory } from '../../types/types';
import { ignore } from '../../utils';
import { UUID } from '../../utils/io-ts';

import Tag from '../Tag/Tag';
import styles from './DislikeModal.module.scss';

interface DislikeModalProps {
  dialog: ChatHistory | null;
  onAssessment: (dialogId: UUID, assessment: Assessment) => void;
  onClose: () => void;
}

const DislikeModal: React.FC<DislikeModalProps> = ({
  dialog, onAssessment, onClose,
}) => {
  const [variants, setVariants] = useState<string[]>([]);
  const [comment, setComment] = useState('');
  const [isLoading, setLoading] = useState(false);

  const onVariants = (value: string) => {
    setVariants(variants.includes(value)
      ? variants.filter(variant => variant !== value)
      : [...variants, value]);
  };

  const onChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setComment(e.target.value);
  };

  const handleCancel = () => {
    setVariants([]);
    setComment('');
    onClose();
  };

  const handleSend = () => {
    if (!dialog) return;
    setLoading(true);

    (onAssessment(dialog.id, {
      question: dialog.question,
      answer: dialog.answer!,
      userEvaluation: dialog.userEvaluation,
      adminEvaluation: EvaluationTypes.NOT_EVALUATED,
      comment: [...variants, comment.trim()].join(', '),
    }) as unknown as Promise<void>)
      .then(handleCancel)
      .catch(ignore)
      .finally(() => setLoading(false));
  };

  return (
    <Modal
      open={!!dialog}
      centered={false}
      className={styles.modal}
      wrapClassName={styles.wrapper}
      confirmLoading={isLoading}
      width={640}
      closable={false}
      title="Что вам не понравилось?"
      onCancel={handleCancel}
      onOk={handleSend}
      destroyOnClose
      okText="Отправить"
      cancelText="Отмена"
      okButtonProps={{ disabled: !variants.length && !comment.trim() }}
      cancelButtonProps={{ type: 'text' }}
    >
      <div>
        <p className={styles.description}>
          Выберите подходящие варианты, либо напишите свой комментарий
        </p>
        <div className={styles.variants}>
          {DISLIKE_OPTIONS.map((variant, idx) => (
            <Tag
              key={`${variant}-${idx}`}
              text={variant}
              active={variants.includes(variant)}
              onClick={onVariants}
            />
          ))}
        </div>
        <TextArea
          value={comment}
          maxLength={255}
          showCount
          className={styles.textArea}
          placeholder="Ваш комментарий"
          onChange={onChange}
        />
      </div>
    </Modal>
  );
};

export default DislikeModal;
