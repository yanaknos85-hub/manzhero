import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import type { CSSProperties } from 'react';
import { ConfigProvider, Slider } from '../index';
import type {
  TSlider,
  ISliderRange,
  ISliderSingle
} from '../index';

export default {
  title: 'Elements/Slider',
  component: Slider,
} as Meta<TSlider>;

const blockStyle: CSSProperties = { height: 200, width: 300 };

const TemplateSingle: StoryFn<ISliderSingle> = args => (
  <ConfigProvider>
    <div style={blockStyle}>
      <Slider {...args} />
    </div>
  </ConfigProvider>
);

const baseArgsSingle: ISliderSingle = {
  defaultValue: 30,
};

const TemplateRange: StoryFn<ISliderRange> = args => (
  <ConfigProvider>
    <div style={blockStyle}>
      <Slider {...args} />
    </div>
  </ConfigProvider>
);

const baseArgsRange: ISliderRange = {
  range: true,
  defaultValue: [20, 50],
};

const baseArgs = {
  disabled: false,
  reverse: false,
  vertical: false,
  dots: false,
  included: true,
  min: 0,
  max: 100,
  step: 1,
};

export const Single = TemplateSingle.bind({});
Single.args = {
  ...baseArgs,
  ...baseArgsSingle,
};

export const Range = TemplateRange.bind({});
Range.args = {
  ...baseArgs,
  ...baseArgsRange,
};

export const Colored = TemplateRange.bind({});
Colored.args = {
  ...baseArgs,
  ...baseArgsRange,
  coloredMode: true,
};
