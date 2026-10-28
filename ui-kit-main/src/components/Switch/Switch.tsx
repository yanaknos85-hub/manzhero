import { Switch as SwitchAntd } from 'antd';
import cn from 'classnames';
import React from 'react';
import { ISwitch } from './ISwitch';
import styles from './Switch.module.scss';

const Switch: React.FC<ISwitch> = ({ className, ...props }) => (
  <SwitchAntd
    {...props}
    className={cn(
      styles.Switch,
      [className]
    )}
  />
);

export default Switch;
