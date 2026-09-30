import { Meta, StoryFn } from '@storybook/react';
import React, { useState } from 'react';
import { ConfigProvider, ISelect, Select } from '../index';

export default {
  title: 'Elements/Select',
  component: Select,
} as Meta<ISelect>;

const baseArgs: ISelect = {
  size: 'middle',
  style: { width: 300 },
  placeholder: 'Сделайте выбор',
  disabled: false,
  status: '',
  bordered: true,
  showSearch: false,
  allowClear: false,
  showDivider: false,
  showCheckMark: false,
  options: [
    {
      value: 'TAXI',
      label: 'Такси',
    },
    {
      value: 'PUBLIC',
      label: 'Общественный',
    },
    {
      value: 'DEDICATED',
      label: 'Доставка',
    },
  ],
};

const Template: StoryFn<ISelect> = args => {
  const [localValue, setValue] = useState<string>();

  const onChange = (value: string) => {
    setValue(value);
  };

  const onSearch = (value: string) => {
    // eslint-disable-next-line no-console
    console.log('search:', value);
  };

  return (
    <ConfigProvider>
      <Select
        {...args}
        optionFilterProp="children"
        filterOption={(input, option) => ((option?.label ?? '') as string).toLowerCase().includes(input.toLowerCase())}
        value={localValue}
        onChange={onChange}
        onSearch={onSearch}
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

export const Multiple = Template.bind({});
Multiple.args = {
  ...baseArgs,
  mode: 'multiple',
};
