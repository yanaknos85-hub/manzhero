import { Drawer as DrawerAntd } from 'antd';
import cn from 'classnames';
import React from 'react';
import { IDrawer } from './IDrawer';
import styles from './Drawer.module.scss';

const Drawer: React.FC<IDrawer> = ({ className, ...props }) => (
  <DrawerAntd {...props} className={cn(styles.Drawer, [className])} />
);

export default Drawer;
