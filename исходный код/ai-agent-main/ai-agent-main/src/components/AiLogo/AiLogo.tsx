import React from 'react';
import cn from 'classnames';

import Icon from '../../components/Icon/Icon';
import styles from './AiLogo.module.scss';

interface LogoProps {
  imageUrl?: string;
  animation?: boolean;
  position?: React.CSSProperties;
  onClick?: () => void;
  onMouseDown?: () => void;
}

const AiLogo: React.FC<LogoProps> = ({
  imageUrl, animation, position, onClick, onMouseDown,
}) => imageUrl ? (
  <button className={styles.logo} onClick={onClick}>
    <img src={imageUrl} alt="AiLogo" />
  </button>
) : (
  <Icon
    name="logo"
    className={cn(styles.logo, {
      [styles.logoPulse]: animation,
    })}
    style={position}
    onClick={onClick}
    onMouseDown={onMouseDown}
  />
);

export default AiLogo;
