import { Meta, StoryFn } from '@storybook/react';
import React, { useState } from 'react';
import {
  ConfigProvider, IDrawer, Drawer, Button
} from '../index';

export default {
  title: 'Elements/Drawer',
  component: Drawer,
} as Meta<IDrawer>;

const baseArgs: IDrawer = {
  title: 'Basic Drawer',
  placement: 'right',
};

const Template: StoryFn<IDrawer> = args => {
  const [open, setOpen] = useState(false);

  const showDrawer = () => {
    setOpen(true);
  };

  const onClose = () => {
    setOpen(false);
  };

  return (
    <ConfigProvider>
      <Button type="primary" onClick={showDrawer}>
        Open
      </Button>

      <Drawer
        {...args}
        onClose={onClose}
        open={open}
      >
        <span>Сбер</span>
        <span>Траспорт</span>
      </Drawer>
    </ConfigProvider>
  );
};

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
