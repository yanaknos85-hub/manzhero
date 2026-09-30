import { Meta, StoryFn } from '@storybook/react';
import React, { useState } from 'react';
import { ReactComponent as SearchIcon } from '../icons/search.svg';
import { ConfigProvider, IInput, Input } from '../index';

export default {
  title: 'Elements/Input',
  component: Input,
} as Meta<IInput>;

const Template: StoryFn<IInput> = args => {
  const [localValue, setValue] = useState<string>();

  const onChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const target = event.target || event.currentTarget;
    setValue(target.value);
  };

  return (
    <ConfigProvider>
      <Input
        {...args}
        value={localValue}
        onChange={onChange}
      />
    </ConfigProvider>
  );
};

const baseArgs: IInput = {
  type: 'text',
  size: 'middle',
  placeholder: 'Заполните поле',
  disabled: false,
  status: '',
  bordered: true,
  maxLength: 100,
  showCount: false,
  allowClear: false,
};

export const Large = Template.bind({});
Large.args = {
  ...baseArgs,
  size: 'large',
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

export const Search = Template.bind({});
Search.args = {
  ...baseArgs,
  mode: 'search',
};

export const WithIcon = Template.bind({});
WithIcon.args = {
  ...baseArgs,
  suffix: <SearchIcon />,
};
