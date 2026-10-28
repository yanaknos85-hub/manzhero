import React from 'react';
import { Card as CardAntd } from 'antd';
import cn from 'classnames';

import { ICardProps } from './ICard';
import styles from './Card.module.scss';

export const Card: React.FC<ICardProps> = ({ className, ...props }) => (
  <CardAntd
    className={cn(styles.card, className, {})}
    {...props}
  />
);

export default Card;
