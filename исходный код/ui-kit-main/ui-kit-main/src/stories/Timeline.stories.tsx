import React from 'react';
import { FrownOutlined, SmileOutlined } from '@ant-design/icons';
import { Meta, StoryFn } from '@storybook/react';

import { ConfigProvider, Timeline } from '../index';
import type { ITimeline } from '../index';

export default {
  title: 'Elements/Timeline',
  component: Timeline,
} as Meta<ITimeline>;

const baseArgs: ITimeline = {
  items: [
    { children: 'Шаг 1' },
    { children: 'Шаг 2' },
    { children: 'Шаг 3' },
    { children: 'Шаг 4' },
    { children: 'Шаг 5' },
  ],
};

const Template: StoryFn<ITimeline> = args => (
  <ConfigProvider>
    <Timeline {...args}>Value</Timeline>
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};

export const RightMode = Template.bind({});
RightMode.args = {
  ...baseArgs,
  mode: 'right',
};

export const AlternateMode = Template.bind({});
AlternateMode.args = {
  ...baseArgs,
  mode: 'alternate',
};

export const CustomDots = Template.bind({});
CustomDots.args = {
  items: [
    { children: 'Шаг 1', dot: <FrownOutlined /> },
    { children: 'Шаг 2', dot: <FrownOutlined /> },
    { children: 'Шаг 3', dot: <FrownOutlined /> },
    { children: 'Шаг 4', dot: <FrownOutlined /> },
    { children: 'Шаг 5', dot: <SmileOutlined /> },
  ],
};
