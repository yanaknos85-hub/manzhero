import { Spin as SpinAntd } from 'antd';
import cn from 'classnames';
import React from 'react';
import { ISpin } from './ISpin';
import styles from './Spin.module.scss';

const Spin: React.FC<ISpin> = ({ className, ...props }) => (
  <SpinAntd {...props} className={cn(styles.Spin, [className])} />
);

export default Spin;
