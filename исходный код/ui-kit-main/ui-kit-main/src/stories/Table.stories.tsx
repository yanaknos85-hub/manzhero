import React from 'react';
import { Meta, StoryFn } from '@storybook/react';
import { AnyObject } from 'antd/lib/_util/type';

import { ConfigProvider, ITable, Table } from '../index';

export default {
  title: 'Elements/Table',
  component: Table,
} as Meta<ITable<AnyObject>>;

const data = [...new Array(100)].map((_, idx) => ({
  key: idx + 1,
  name: `Mike-${idx + 1}`,
  age: 10 + idx,
  address: `${idx + 1} Downing Street`,
}));

const columns = [
  {
    title: 'Name',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: 'Age',
    dataIndex: 'age',
    key: 'age',
    // @ts-ignore
    sorter: (a, b) => a.age - b.age,
  },
  {
    title: 'Address',
    dataIndex: 'address',
    key: 'address',
  },
];

const baseArgs: ITable<AnyObject> = {
  bordered: false,
  borderRow: true,
  darkRow: false,
  loading: false,
  columns: columns,
  dataSource: data,
};

const Template: StoryFn<ITable<AnyObject>> = args => (
  <ConfigProvider>
    <Table {...args} />
  </ConfigProvider>
);

export const Small = Template.bind({});
Small.args = {
  ...baseArgs,
  size: 'small',
};

export const Middle = Template.bind({});
Middle.args = {
  ...baseArgs,
  size: 'middle',
};

export const Large = Template.bind({});
Large.args = {
  ...baseArgs,
  size: 'large',
};
