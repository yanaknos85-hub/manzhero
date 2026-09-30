import { Avatar as AvatarAntd } from 'antd';
import cn from 'classnames';
import React from 'react';
import { IAvatar } from './IAvatar';
import styles from './Avatar.module.scss';

const Avatar: React.FC<IAvatar> = ({ className, ...props }) => (
  <AvatarAntd {...props} className={cn(styles.Avatar, [className])} />
);

export default Avatar;
