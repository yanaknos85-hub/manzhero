import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import {
  ConfigProvider, Button, IDropdown, Dropdown, IMenu
} from '../index';

export default {
  title: 'Elements/Dropdown',
  component: Dropdown,
} as Meta<IDropdown>;

const items: IMenu['items'] = [
  { key: '1', label: 'Option 1' },
  { key: '2', label: 'Option 2' },
  { key: '3', label: 'Option 3' },
];

const baseArgs: IDropdown = {
  menu: { items },
};

const Template: StoryFn<IDropdown> = args => (
  <ConfigProvider>
    <Dropdown {...args}>
      <Button>
        Hover me
      </Button>
    </Dropdown>
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
