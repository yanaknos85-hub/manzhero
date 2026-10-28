import { Checkbox as CheckboxAntd } from 'antd';
import cn from 'classnames';
import React from 'react';
import styles from './Checkbox.module.scss';
import { ICheckbox } from './ICheckbox';

const Checkbox: React.FC<ICheckbox> = ({
  className, size = 'middle', ...props
}) => (
  <CheckboxAntd
    {...props}
    className={cn(
      styles.Checkbox,
      { [styles[size]]: size },
      [className]
    )}
  />
);

export default Checkbox;
