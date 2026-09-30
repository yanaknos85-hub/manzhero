import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { ConfigProvider, ISpace, Space } from '../index';

export default {
  title: 'Elements/Space',
  component: Space,
} as Meta<ISpace>;

const baseArgs: ISpace = {
  direction: 'vertical',
  align: 'center',
  size: 'middle',
};

const Template: StoryFn<ISpace> = args => (
  <ConfigProvider>
    <Space {...args}>
      <span>Сбер</span>
      <span>Траспорт</span>
    </Space>
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
