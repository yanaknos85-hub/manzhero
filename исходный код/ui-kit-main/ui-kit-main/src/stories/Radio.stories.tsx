import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import {
  ConfigProvider, IRadio, Radio, RadioChangeEvent, RadioGroup
} from '../index';

export default {
  title: 'Elements/Radio',
  component: Radio,
} as Meta<IRadio>;

const baseArgs: IRadio = {
  disabled: false,
};

const Template: StoryFn<IRadio> = args => {
  const [value, setValue] = React.useState(1);

  const onChange = (e: RadioChangeEvent) => {
    setValue(e.target.value);
  };

  return (
    <ConfigProvider>
      <RadioGroup onChange={onChange} value={value}>
        <Radio {...args} value={1}>A</Radio>
        <Radio {...args} value={2}>B</Radio>
        <Radio {...args} value={3}>C</Radio>
        <Radio {...args} value={4}>D</Radio>
      </RadioGroup>
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
