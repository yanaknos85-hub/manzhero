import { Meta, StoryFn } from '@storybook/react';
import React, { useState } from 'react';
import { ConfigProvider, ITextarea, Textarea } from '../index';

export default {
  title: 'Elements/Textarea',
  component: Textarea,
} as Meta<ITextarea>;

const Template: StoryFn<ITextarea> = args => {
  const [localValue, setValue] = useState<string>('Сбертранспорт');

  const onChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
    const target = event.target || event.currentTarget;
    setValue(target.value);
  };

  return (
    <ConfigProvider>
      <Textarea
        {...args}
        value={localValue}
        onChange={onChange}
      />
    </ConfigProvider>
  );
};

const baseArgs: ITextarea = {
  size: 'middle',
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
