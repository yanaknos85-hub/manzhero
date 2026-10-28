import React, { useState, useEffect } from 'react';
import type { FC, ReactNode } from 'react';
import { Alert as AlertAntd } from 'antd';
import cn from 'classnames';

import type IAlert from './IAlert';

import { ReactComponent as SuccessIcon } from '../../icons/success.svg';
import { ReactComponent as WarningIcon } from '../../icons/warning.svg';
import { ReactComponent as InfoIcon } from '../../icons/info.svg';
import { ReactComponent as ErrorIcon } from '../../icons/error.svg';
import styles from './Alert.module.scss';

const Alert: FC<IAlert> = ({
  className,
  type,
  ...props
}) => {
  const [icon, setIcon] = useState<ReactNode>();

  useEffect(() => {
    switch (type) {
      case 'success':
        setIcon(<SuccessIcon />);
        break;
      case 'warning':
        setIcon(<WarningIcon />);
        break;
      case 'info':
        setIcon(<InfoIcon />);
        break;
      case 'error':
        setIcon(<ErrorIcon />);
        break;
    }
  }, [type]);

  return (
    <AlertAntd
      className={cn(styles.alert, className)}
      type={type}
      icon={icon}
      {...props}
    />
  );
};

export default Alert;
