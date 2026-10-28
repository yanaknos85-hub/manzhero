import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { ConfigProvider, ISpin, Spin } from '../index';

export default {
  title: 'Elements/Spin',
  component: Spin,
} as Meta<ISpin>;

const Template: StoryFn<ISpin> = args => (
  <ConfigProvider>
    <Spin {...args} />
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {};
