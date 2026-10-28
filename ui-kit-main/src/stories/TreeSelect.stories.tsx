import React, { useState } from 'react';
import { Meta, StoryFn } from '@storybook/react';

import { ReactComponent as UserIcon } from '../icons/user.svg';
import { ConfigProvider, ITreeSelect, TreeSelect } from '../index';

export default {
  title: 'Elements/TreeSelect',
  component: TreeSelect,
} as Meta<ITreeSelect>;

const treeData = [
  {
    value: 'parent 1',
    title: 'parent 1',
    icon: <UserIcon />,
    children: [
      {
        value: 'parent 1-0',
        title: 'parent 1-0',
        icon: <UserIcon />,
        children: [
          {
            value: 'leaf1',
            title: 'leaf1',
            icon: <UserIcon />,
          },
          {
            value: 'leaf2',
            title: 'leaf2',
            icon: <UserIcon />,
          },
          {
            value: 'leaf3',
            title: 'leaf3',
            icon: <UserIcon />,
          },
          {
            value: 'leaf4',
            title: 'leaf4',
            icon: <UserIcon />,
          },
          {
            value: 'leaf5',
            title: 'leaf5',
            icon: <UserIcon />,
          },
          {
            value: 'leaf6',
            title: 'leaf6',
            icon: <UserIcon />,
          },
        ],
      },
      {
        value: 'parent 1-1',
        title: 'parent 1-1',
        icon: <UserIcon />,
        children: [
          {
            value: 'leaf11',
            title: 'leaf11',
            icon: <UserIcon />,
          },
        ],
      },
    ],
  },
];

const baseArgs: ITreeSelect = {
  disabled: false,
  multiple: false,
  treeCheckable: false,
  treeIcon: false,
  treeLine: false,
  bordered: true,
  showArrow: true,
  showSearch: true,
  allowClear: true,
  treeDefaultExpandAll: true,
  size: 'middle',
  placeholder: 'Сделайте выбор',
  showCheckedStrategy: 'SHOW_CHILD',
  style: { width: 300 },
  dropdownStyle: { maxHeight: 400, overflow: 'auto' },
  status: '',
  treeData,
};

const Template: StoryFn<ITreeSelect> = args => {
  const [value, setValue] = useState<string>();

  return (
    <ConfigProvider>
      <TreeSelect
        {...args}
        value={value}
        onChange={setValue}
      />
    </ConfigProvider>
  );
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

export const Large = Template.bind({});
Large.args = {
  ...baseArgs,
  size: 'large',
};
