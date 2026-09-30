import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { ConfigProvider, IMenu, Menu } from '../index';

export default {
  title: 'Elements/Menu',
  component: Menu,
} as Meta<IMenu>;

type MenuItem = Required<IMenu>['items'][number];

const items: MenuItem[] = [
  { key: '1', label: 'Option 1' },
  { key: '2', label: 'Option 2' },
  { key: '3', label: 'Option 3' },
];

const baseArgs: IMenu = {
  items,
};

const Template: StoryFn<IMenu> = args => (
  <ConfigProvider>
    <Menu {...args} />
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
