import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { Checkbox, ConfigProvider, ICheckbox } from '../index';

export default {
  title: 'Elements/Checkbox',
  component: Checkbox,
} as Meta<ICheckbox>;

const baseArgs: ICheckbox = {
  disabled: false,
};

const Template: StoryFn<ICheckbox> = args => (
  <ConfigProvider>
    <Checkbox {...args} />
  </ConfigProvider>
);

export const Middle = Template.bind({});
Middle.args = {
  ...baseArgs,
  size: 'middle',
};

export const Small = Template.bind({});
Small.args = {
  ...baseArgs,
  size: 'small',
};
