import React from 'react';
import { Meta, StoryFn } from '@storybook/react';
import { ConfigProvider, IEmpty, Empty } from '../index';

export default {
  title: 'Elements/Empty',
  component: Empty,
} as Meta<IEmpty>;

const baseArgs: IEmpty = {
  description: 'Нет данных',
  imgSize: 280,
};

const Template: StoryFn<IEmpty> = args => (
  <ConfigProvider>
    <Empty {...args} />
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
