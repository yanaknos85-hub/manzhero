import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import {
  ConfigProvider, IPopover, Popover, Button
} from '../index';

export default {
  title: 'Elements/Popover',
  component: Popover,
} as Meta<IPopover>;

const Template: StoryFn<IPopover> = args => (
  <ConfigProvider>
    <Popover {...args}>
      <Button type="primary">Click Me!</Button>
    </Popover>
  </ConfigProvider>
);

const baseArgs: IPopover = {
  title: 'This is a popover',
  content: 'This is the content of the popover',
};

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};

export const TriggeredFocus = Template.bind({});
TriggeredFocus.args = {
  ...baseArgs,
  trigger: 'focus',
};

export const TriggeredClick = Template.bind({});
TriggeredClick.args = {
  ...baseArgs,
  trigger: 'click',
};

export const Placement = Template.bind({});
Placement.args = {
  ...baseArgs,
  placement: 'rightBottom',
};
