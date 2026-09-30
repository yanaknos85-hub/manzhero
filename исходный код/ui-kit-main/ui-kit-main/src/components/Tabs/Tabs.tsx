import { Tabs as TabsAntd } from 'antd';
import cn from 'classnames';
import React from 'react';
import { ITabs, ITabsPane } from './ITabs';
import styles from './Tabs.module.scss';

const Tabs: React.FC<ITabs> = ({ className, ...props }) => (
  <TabsAntd {...props} className={cn(styles.Tabs, [className])} />
);

const TabsDeprecated: React.FC<ITabs> = ({
  className, children, ...props
}) => (
  <TabsAntd {...props} className={cn(styles.Tabs, [className])}>
    {children}
  </TabsAntd>
);

const TabsPaneDeprecated: React.FC<ITabsPane> = ({
  className, children, ...props
}) => (
  <TabsAntd.TabPane {...props} className={cn(styles.TabsPane, [className])}>
    {children}
  </TabsAntd.TabPane>
);

export { TabsDeprecated, TabsPaneDeprecated };
export default Tabs;
