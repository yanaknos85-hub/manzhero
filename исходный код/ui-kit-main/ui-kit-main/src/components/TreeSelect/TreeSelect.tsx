import React from 'react';
import { TreeSelect as TreeSelectAntd } from 'antd';
import cn from 'classnames';

import { ITreeSelect } from './ITreeSelect';
import styles from './TreeSelect.module.scss';

export const TreeSelect: React.FC<ITreeSelect> = ({
  className, popupClassName, ...props
}) => (
  <TreeSelectAntd
    className={cn(styles.TreeSelect, className, {})}
    popupClassName={cn(styles.TreeSelectDropdown, popupClassName, {})}
    {...props}
  />
);

export default TreeSelect;
