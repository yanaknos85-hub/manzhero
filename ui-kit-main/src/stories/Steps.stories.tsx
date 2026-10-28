import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { ConfigProvider, ISteps, Steps } from '../index';

export default {
  title: 'Elements/Steps',
  component: Steps,
} as Meta<ISteps>;

const description = 'You can hover on the dot.';

const baseArgs: ISteps = {
  current: 1,
  items: [
    {
      title: 'Finished',
      description,
    },
    {
      title: 'In Progress',
      description,
    },
    {
      title: 'Waiting',
      description,
    },
    {
      title: 'Waiting',
      description,
    },
  ],
};

const Template: StoryFn<ISteps> = args => (
  <ConfigProvider>
    <Steps {...args} />
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
