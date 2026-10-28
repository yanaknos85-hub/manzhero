import { Space as SpaceAntd } from 'antd';
import cn from 'classnames';
import React from 'react';
import { ISpace } from './ISpace';
import styles from './Space.module.scss';

const Space: React.FC<ISpace> = ({ className, ...props }) => (
  <SpaceAntd {...props} className={cn(styles.Space, [className])} />
);

export default Space;
