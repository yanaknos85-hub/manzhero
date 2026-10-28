import React from 'react';

import styles from './EmptyChat.module.scss';

interface EmptyChatProps {
  title: string;
  description?: string;
}

const EmptyChat: React.FC<EmptyChatProps> = ({ title, description }) => (
  <div className={styles.empty}>
    <span className={styles.emptyTitle}>{title}</span>
    {description && <span className={styles.emptyDescription}>{description}</span>}
  </div>
);

export default EmptyChat;
