import React from 'react';
import cn from 'classnames';

import styles from './Tag.module.scss';

interface TagProps {
  text: string;
  active?: boolean;
  onClick: (text: string) => void;
}

const Tag: React.FC<TagProps> = ({
  text, active, onClick,
}) => (
  <button
    className={cn(styles.tag, {
      [styles.tagActive]: active,
    })}
    onClick={() => onClick(text)}
  >
    {text}
  </button>
);

export default Tag;
