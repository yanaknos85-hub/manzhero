import React from 'react';
import { Meta, StoryFn } from '@storybook/react';
import {
  ConfigProvider, IPopconfirm, Popconfirm, Button
} from '../index';

export default {
  title: 'Elements/Popconfirm',
  component: Popconfirm,
} as Meta<IPopconfirm>;

const baseArgs: IPopconfirm = {
  title: 'Удаление',
  description: 'Вы уверены, что хотите удалить эту запись?',
  disabled: false,
  showCancel: true,
};

const Template: StoryFn<IPopconfirm> = args => (
  <ConfigProvider>
    <Popconfirm {...args}>
      <Button style={{ height: 40, margin: '40px 30px' }} disabled={args.disabled}>Удалить</Button>
    </Popconfirm>
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
