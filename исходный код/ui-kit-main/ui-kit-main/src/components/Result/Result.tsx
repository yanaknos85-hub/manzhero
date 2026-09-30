import React from 'react';
import { Result as ResultAntd } from 'antd';
import cn from 'classnames';

import { ReactComponent as SuccessIcon } from '../../icons/success.svg';
import { ReactComponent as ErrorIcon } from '../../icons/error.svg';
import { ReactComponent as InfoIcon } from '../../icons/info.svg';
import { ReactComponent as WarningIcon } from '../../icons/warning.svg';
import { IResult } from './IResult';
import styles from './Result.module.scss';

const icons = {
  success: SuccessIcon,
  error: ErrorIcon,
  info: InfoIcon,
  warning: WarningIcon,
  403: null,
  404: null,
  500: null,
};

const Result: React.FC<IResult> = ({
  className,
  maxWidth,
  children,
  iconSize = 60,
  ...props
}) => {
  const getResultIcon = () => {
    const Icon = icons[props.status!];
    return Icon ? <Icon width={iconSize} height={iconSize} /> : false;
  };

  return (
    <ResultAntd
      icon={getResultIcon()}
      className={cn(styles.result, [className])}
      style={{ maxWidth, ...props.style }}
      {...props}
    >
      {children}
    </ResultAntd>
  );
};

export default Result;
