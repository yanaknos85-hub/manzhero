import { Button as ButtonAntd } from 'antd';
import cn from 'classnames';
import React from 'react';
import styles from './Link.module.scss';
import { IButton } from '../Button/IButton';

const Link: React.FC<Omit<IButton, 'type'>> = ({
  className,
  children,
  href = '#',
  size = 'middle',
  disabled,
  ...props
}) => (
  <ButtonAntd
    {...props}
    type="link"
    size={size}
    href={href}
    disabled={disabled}
    className={cn(
      styles.Link,
      {
        [styles.disabled]: disabled,
        [styles[size]]: size,
      },
      [className]
    )}
  >
    {children}
  </ButtonAntd>
);

export default Link;
