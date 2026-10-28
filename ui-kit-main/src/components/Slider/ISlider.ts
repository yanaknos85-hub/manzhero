import {
  SliderSingleProps as ISliderSingle,
  SliderRangeProps
} from 'antd/lib/slider';

interface ISliderRange extends SliderRangeProps {
  coloredMode?: boolean;
}

export type { ISliderSingle, ISliderRange };
