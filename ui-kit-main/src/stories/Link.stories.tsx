import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { ConfigProvider, ILink, Link } from '../index';

export default {
  title: 'Elements/Link',
  component: Link,
} as Meta<ILink>;

const Template: StoryFn<ILink> = args => (
  <ConfigProvider>
    <Link {...args} />
  </ConfigProvider>
);

const baseArgs: ILink = {
  children: 'Сбертранспорт',
  href: '#href',
  disabled: false,
};

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
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
