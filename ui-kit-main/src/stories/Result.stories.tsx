import React from 'react';
import { Meta, StoryFn } from '@storybook/react';
import { ConfigProvider, IResult, Result } from '../index';

export default {
  title: 'Elements/Result',
  component: Result,
} as Meta<IResult>;

const baseArgs: IResult = {
  status: 'success',
  title: 'Всё получилось!',
  subTitle: 'Заявка создана и доступна в разделе',
  maxWidth: 300,
  iconSize: 60,
};

const Template: StoryFn<IResult> = args => (
  <ConfigProvider>
    <Result {...args} />
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
