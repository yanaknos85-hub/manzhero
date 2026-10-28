import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { ConfigProvider, Rate } from '../index';
import type { IRate } from '../index';

export default {
  title: 'Elements/Rate',
  component: Rate,
} as Meta<IRate>;

const Template: StoryFn<IRate> = args => (
  <ConfigProvider>
    <Rate {...args} />
  </ConfigProvider>
);

const baseArgs: IRate = {
  defaultValue: 4,
  count: 5,
  allowClear: true,
  allowHalf: false,
  disabled: false,
};

export const Small = Template.bind({});
Small.args = {
  ...baseArgs,
  size: 'small',
};

export const Medium = Template.bind({});
Medium.args = {
  ...baseArgs,
};

export const Large = Template.bind({});
Large.args = {
  ...baseArgs,
  size: 'large',
};

export const DynamicColor = Template.bind({});
DynamicColor.args = {
  ...baseArgs,
  size: 'large',
  dynamicColor: true,
};
