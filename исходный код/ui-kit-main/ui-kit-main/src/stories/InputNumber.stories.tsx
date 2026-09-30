import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { ConfigProvider, InputNumber } from '../index';
import type { IInputNumber } from '../index';

export default {
  title: 'Elements/InputNumber',
  component: InputNumber,
} as Meta<IInputNumber>;

const Template: StoryFn<IInputNumber> = args => (
  <ConfigProvider>
    <InputNumber {...args} />
  </ConfigProvider>
);

const baseArgs: IInputNumber = {
  defaultValue: 3,
  min: 0,
  max: 10,
  disabled: false,
  readOnly: false,
};

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};

export const Small = Template.bind({});
Small.args = {
  ...baseArgs,
  size: 'small',
};

export const Large = Template.bind({});
Large.args = {
  ...baseArgs,
  size: 'large',
};

export const Filled = Template.bind({});
Filled.args = {
  ...baseArgs,
  variant: 'filled',
};

export const Borderless = Template.bind({});
Borderless.args = {
  ...baseArgs,
  variant: 'borderless',
};

export const PrePostTab = Template.bind({});
PrePostTab.args = {
  ...baseArgs,
  defaultValue: 150,
  max: 1000,
  addonBefore: '+',
  addonAfter: '$',
  style: { width: 200 },
};

export const PrefixSuffix = Template.bind({});
PrefixSuffix.args = {
  ...baseArgs,
  defaultValue: 150,
  max: 1000,
  prefix: '$',
  suffix: 'USDT',
  style: { width: 200 },
};

export const Status = Template.bind({});
Status.args = {
  ...baseArgs,
  status: 'error',
};

export const HighPrecision = Template.bind({});
HighPrecision.args = {
  ...baseArgs,
  step: '0.00000000000001',
  stringMode: true,
  style: { width: 200 },
};
