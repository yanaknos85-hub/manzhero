import { Radio as RadioAntd } from 'antd';
import cn from 'classnames';
import React from 'react';
import { IRadio } from './IRadio';
import styles from './Radio.module.scss';

const Radio: React.FC<IRadio> = ({
  className, size = 'middle', ...props
}) => (
  <RadioAntd
    {...props}
    className={cn(
      styles.Radio,
      { [styles[size]]: size },
      [className]
    )}
  />
);

const RadioGroup = RadioAntd.Group;

export { RadioGroup };

export default Radio;
