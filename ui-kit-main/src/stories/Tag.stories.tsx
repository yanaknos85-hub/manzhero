import React from 'react';
import { CheckCircleOutlined } from '@ant-design/icons';
import { Meta, StoryFn } from '@storybook/react';
import { ConfigProvider, ITag, Tag } from '../index';

export default {
  title: 'Elements/Tag',
  component: Tag,
} as Meta<ITag>;

const baseArgs: ITag = {
  closable: true,
  visible: true,
  bordered: false,
  size: 'default',
  color: 'default',
  icon: <CheckCircleOutlined />,
};

const Template: StoryFn<ITag> = args => (
  <ConfigProvider>
    <Tag {...args}>Value</Tag>
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
