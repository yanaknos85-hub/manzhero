import { Meta, StoryFn } from '@storybook/react';
import React, { useState } from 'react';
import { AutoComplete, ConfigProvider, IAutoComplete } from '../index';

export default {
  title: 'Composite/Autocomplete',
  component: AutoComplete,
} as Meta<IAutoComplete>;

const Template: StoryFn<IAutoComplete> = args => {
  const [localValue, setLocalValue] = useState<string | undefined>();
  const [localOptions, setLocalOptions] = useState<{ value: string; labelForInput: string; label: JSX.Element }[]>([]);

  const renderLabel = (first: string, second: string) => (
    <>
      <b>{first}</b>
      <br />
      <span>{second}</span>
    </>
  );

  const onChange = (value: string) => {
    // eslint-disable-next-line no-console
    console.log('onChange:', value);
    setLocalValue(value);
  };

  const onSelect = (value: string) => {
    // eslint-disable-next-line no-console
    console.log('onSelect:', value);

    if (value) {
      setLocalValue(localOptions.find(option => option.value === value)?.labelForInput);
    }
  };

  const onSearch = (value: string) => {
    // eslint-disable-next-line no-console
    console.log('onSearch:', value);

    if (value.length > 1) {
      setLocalOptions([
        {
          value: 'Строителей 10, Россиия, Москва',
          labelForInput: 'Строителей 10',
          label: renderLabel('Строителей 10', 'Россиия, Москва'),
        },
        {
          value: 'Кухмистерова 12, Россиия, Москва',
          labelForInput: 'Кухмистерова 12',
          label: renderLabel('Кухмистерова 12', 'Россиия, Москва'),
        },
        {
          value: 'Доватора 2, Россиия, Москва',
          labelForInput: 'Доватора 2',
          label: renderLabel('Доватора 2', 'Россиия, Москва'),
        },
      ]);
    } else {
      setLocalOptions([]);
    }
  };

  return (
    <ConfigProvider>
      <AutoComplete
        {...args}
        value={localValue}
        onChange={onChange}
        onSelect={onSelect}
        onSearch={onSearch}
        options={localOptions}
      />
    </ConfigProvider>
  );
};

const baseArgs: IAutoComplete = {
  size: 'middle',
  style: { width: 300 },
  placeholder: 'Введите адрес',
  notFoundContent: 'Ничего не найдено',
  disabled: false,
  status: '',
  withInput: false,
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
