import React from 'react';
import { Cascader as CascaderAntd } from 'antd';
import cn from 'classnames';

import { ICascader } from './ICascader';
import styles from './Cascader.module.scss';

const Cascader: React.FC<ICascader> = ({
  className, popupClassName, ...props
}) => (
  <CascaderAntd
    className={cn(styles.cascader, className, {})}
    popupClassName={cn(styles.cascaderDropdown, popupClassName, {})}
    {...props}
  />
);

export default Cascader;
