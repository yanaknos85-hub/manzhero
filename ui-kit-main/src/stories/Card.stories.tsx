import React from 'react';
import { EditOutlined, EllipsisOutlined, SettingOutlined } from '@ant-design/icons';
import { Meta, StoryFn } from '@storybook/react';

import { ConfigProvider, ICardProps, Card } from '../index';

export default {
  title: 'Elements/Card',
  component: Card,
} as Meta<ICardProps>;

const baseArgs: ICardProps = {
  bordered: false,
  loading: false,
  hoverable: false,
  title: 'Card title',
  style: { width: 300 },
};

const Template: StoryFn<ICardProps> = args => (
  <ConfigProvider>
    <Card {...args}>
      <div>Card content</div>
      <div>Card content</div>
      <div>Card content</div>
    </Card>
  </ConfigProvider>
);

export const Middle = Template.bind({});
Middle.args = {
  ...baseArgs,
  size: 'default',
};

export const Small = Template.bind({});
Small.args = {
  ...baseArgs,
  size: 'small',
};

export const Actions = Template.bind({});
Actions.args = {
  ...baseArgs,
  actions: [
    <SettingOutlined key="setting" />,
    <EditOutlined key="edit" />,
    <EllipsisOutlined key="ellipsis" />,
  ],
};

export const TabList = Template.bind({});
TabList.args = {
  ...baseArgs,
  tabList: [
    { key: 'tab1', tab: 'tab1' },
    { key: 'tab2', tab: 'tab2' },
  ],
};
