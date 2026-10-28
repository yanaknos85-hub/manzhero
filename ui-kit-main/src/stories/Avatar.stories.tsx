import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { ConfigProvider, IAvatar, Avatar } from '../index';
import { ReactComponent as UserIcon } from '../icons/user.svg';

export default {
  title: 'Elements/Avatar',
  component: Avatar,
} as Meta<IAvatar>;

const baseArgs: IAvatar = {
  size: 'default',
  icon: <UserIcon />,
};

const Template: StoryFn<IAvatar> = args => (
  <ConfigProvider>
    <Avatar {...args} />
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
