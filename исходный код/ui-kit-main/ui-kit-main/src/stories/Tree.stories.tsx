import React from 'react';
import { Meta, StoryFn } from '@storybook/react';

import { ConfigProvider, ITree, ITreeDataNode, Tree } from '../index';

export default {
  title: 'Elements/Tree',
  component: Tree,
} as Meta<ITree>;

const treeData: ITreeDataNode[] = [
  {
    title: '0-0',
    key: '0-0',
    children: [
      {
        title: '0-0-0',
        key: '0-0-0',
        children: [
          { title: '0-0-0-0', key: '0-0-0-0' },
          { title: '0-0-0-1', key: '0-0-0-1' },
          { title: '0-0-0-2', key: '0-0-0-2' },
        ],
      },
      {
        title: '0-0-1',
        key: '0-0-1',
        children: [
          { title: '0-0-1-0', key: '0-0-1-0' },
          { title: '0-0-1-1', key: '0-0-1-1' },
          { title: '0-0-1-2', key: '0-0-1-2' },
        ],
      },
      {
        title: '0-0-2',
        key: '0-0-2',
      },
    ],
  },
  {
    title: '0-1',
    key: '0-1',
    children: [
      { title: '0-1-0-0', key: '0-1-0-0' },
      { title: '0-1-0-1', key: '0-1-0-1' },
      { title: '0-1-0-2', key: '0-1-0-2' },
    ],
  },
  {
    title: '0-2',
    key: '0-2',
  },
];

const baseArgs: ITree = {
  showLine: false,
  disabled: false,
  draggable: false,
  blockNode: false,
  search: false,
  placeholder: 'Поиск...',
};

const Template: StoryFn<ITree> = args => (
  <ConfigProvider>
    <Tree
      {...args}
      checkable
      treeData={treeData}
    />
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
  treeData
};
