import React, { useState } from 'react';
import { Meta, StoryFn } from '@storybook/react';
import { ConfigProvider, IPagination, Pagination } from '../index';

export default {
  title: 'Elements/Pagination',
  component: Pagination,
} as Meta<IPagination>;

const baseArgs: IPagination = {
  showSizeChanger: true,
  disabled: false,
  defaultCurrent: 1,
  total: 500,
  pagination: { page: 0, size: 10 },
  setPagination: () => null,
};

const Template: StoryFn<IPagination> = args => {
  const [pagination, setPagination] = useState<IPagination['pagination']>(args.pagination);

  return (
    <ConfigProvider>
      <Pagination
        {...args}
        pagination={pagination}
        setPagination={setPagination}
      />
    </ConfigProvider>
  );
};

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
