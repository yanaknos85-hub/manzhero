import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { ConfigProvider, ISwitch, Switch } from '../index';

export default {
  title: 'Elements/Switch',
  component: Switch,
} as Meta<ISwitch>;

const baseArgs: ISwitch = {
  disabled: false,
};

const Template: StoryFn<ISwitch> = args => (
  <ConfigProvider>
    <Switch {...args} />
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
