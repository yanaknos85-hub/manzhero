import React from 'react';

import Tag from '../Tag/Tag';
import styles from './Questions.module.scss';

interface QuestionsProps {
  questions: string[];
  onMessage: (message: string) => void;
}

const Questions: React.FC<QuestionsProps> = ({ questions, onMessage }) => (
  <div className={styles.container}>
    {questions.map((question, idx) => (
      <Tag
        key={idx}
        text={question}
        onClick={onMessage}
      />
    ))}
  </div>
);

export default Questions;
