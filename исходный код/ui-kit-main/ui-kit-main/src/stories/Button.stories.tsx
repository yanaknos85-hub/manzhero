import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { Button, ConfigProvider, IButton } from '../index';

export default {
  title: 'Elements/Button',
  component: Button,
} as Meta<IButton>;

const Template: StoryFn<IButton> = args => (
  <ConfigProvider>
    <Button {...args} />
  </ConfigProvider>
);

const baseArgs: IButton = {
  children: 'Сбертранспорт',
  type: 'primary',
  size: 'middle',
  disabled: false,
  danger: false,
  ghost: false,
  loading: false,
  block: false,
};

export const Primary = Template.bind({});
Primary.args = {
  ...baseArgs,
  type: 'primary',
};

export const Secondary = Template.bind({});
Secondary.args = {
  ...baseArgs,
  type: 'secondary',
};

export const Large = Template.bind({});
Large.args = {
  ...baseArgs,
  size: 'large',
};

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
