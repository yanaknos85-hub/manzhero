import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { ConfigProvider, IQRCode, QRCode } from '../index';

export default {
  title: 'Elements/QRCode',
  component: QRCode,
} as Meta<IQRCode>;

const Template: StoryFn<IQRCode> = args => (
  <ConfigProvider>
    <QRCode {...args} />
  </ConfigProvider>
);

const baseArgs: IQRCode = {
  value: 'https://ant.design/',
};

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};

export const Loading = Template.bind({});
Loading.args = {
  ...baseArgs,
  status: 'loading',
};

export const Expired = Template.bind({});
Expired.args = {
  ...baseArgs,
  status: 'expired',
};

export const Scanned = Template.bind({});
Scanned.args = {
  ...baseArgs,
  status: 'scanned',
};
