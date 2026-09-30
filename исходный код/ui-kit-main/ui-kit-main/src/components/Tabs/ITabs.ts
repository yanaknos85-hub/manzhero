import { TabsProps, TabPaneProps } from 'antd/lib/tabs';

export interface ITabs extends TabsProps {
  className?: string;
}
export interface ITabsPane extends TabPaneProps {
  className?: string;
}
