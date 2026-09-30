import React from 'react';
import { Popconfirm as PopconfirmAntd } from 'antd';
import cn from 'classnames';

import { IPopconfirm } from './IPopconfirm';
import styles from './Popconfirm.module.scss';

const Popconfirm: React.FC<IPopconfirm> = ({
  rootClassName,
  children,
  cancelText = 'Отмена',
  okText = 'Да',
  ...props
}) => (
  <PopconfirmAntd
    cancelText={cancelText}
    okText={okText}
    rootClassName={cn(styles.popconfirm, [rootClassName])}
    {...props}
  >
    {children}
  </PopconfirmAntd>
);

export default Popconfirm;
