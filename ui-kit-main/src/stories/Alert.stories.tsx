import React from 'react';
import { Meta, StoryFn } from '@storybook/react';

import { ConfigProvider, IAlert, Alert } from '../index';

export default {
  title: 'Elements/Alert',
  component: Alert,
  argTypes: {
    type: {
      options: ['success', 'warning', 'info', 'error'],
      control: { type: 'select' },
    },
  },
} as Meta<IAlert>;

const baseArgs: IAlert = {
  type: 'success',
  message: 'Title text',
  description: '',
  showIcon: false,
  closable: false,
};

const Template: StoryFn<IAlert> = args => (
  <ConfigProvider>
    <Alert {...args} />
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
