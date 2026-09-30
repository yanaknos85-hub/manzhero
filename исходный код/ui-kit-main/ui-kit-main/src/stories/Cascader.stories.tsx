import React, { useState } from 'react';
import { Meta, StoryFn } from '@storybook/react';

import {
  ConfigProvider, ICascader, Cascader
} from '../index';

export default {
  title: 'Elements/Cascader',
  component: Cascader,
} as Meta<ICascader>;

const options = [
  {
    value: 'zhejiang',
    label: 'Zhejiang',
    children: [
      {
        value: 'hangzhou',
        label: 'Hangzhou',
        children: [
          {
            value: 'xihu1',
            label: 'West Lake 1',
          },
          {
            value: 'xihu2',
            label: 'West Lake 2',
          },
        ],
      },
    ],
  },
  {
    value: 'jiangsu',
    label: 'Jiangsu',
    children: [
      {
        value: 'nanjing',
        label: 'Nanjing',
        children: [
          {
            value: 'zhonghuamen',
            label: 'Zhong Hua Men',
          },
        ],
      },
    ],
  },
];

const baseArgs: ICascader = {
  disabled: false,
  multiple: false,
  bordered: true,
  changeOnSelect: false,
  showArrow: true,
  showSearch: true,
  allowClear: true,
  loading: false,
  maxTagCount: 'responsive',
  placeholder: 'Сделайте выбор',
  showCheckedStrategy: 'SHOW_CHILD',
  style: { width: 300 },
  dropdownStyle: { maxHeight: 400, overflow: 'auto' },
  status: '',
  options,
};

const Template: StoryFn<ICascader> = (args: React.JSX.IntrinsicAttributes & React.PropsWithChildren<ICascader>) => {
  const [value, setValue] = useState([]);

  return (
    <ConfigProvider>
      <Cascader
        {...args}
        value={value}
        // @ts-ignore
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
