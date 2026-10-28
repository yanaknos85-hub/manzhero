import { Button as ButtonAntd } from 'antd';
import { ButtonType } from 'antd/lib/button/buttonHelpers';
import cn from 'classnames';
import React from 'react';
import styles from './Button.module.scss';
import { IButton } from './IButton';

const Button: React.FC<IButton> = ({
  className, children, disabled, type = 'primary', size = 'middle', ...props
}) => {
  const externalTypes = ['secondary']; // кастомные типы, которых нет в Анте
  const isExternalType = externalTypes.includes(type);

  return (
    <ButtonAntd
      {...props}
      type={(isExternalType ? 'text' : type) as ButtonType}
      size={size}
      disabled={disabled}
      className={cn(
        styles.Button,
        {
          ...(isExternalType ? { [styles[type]]: type } : {}),
          ...(isExternalType ? { [styles.disabled]: disabled } : {}),
        },
        [className]
      )}
    >
      {children}
    </ButtonAntd>
  );
};

export default Button;
