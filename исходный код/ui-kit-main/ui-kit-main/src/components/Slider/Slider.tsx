import React from 'react';
import type { FC } from 'react';
import { Slider as SliderAntd } from 'antd';
import cn from 'classnames';

import type { TSlider } from './TSlider';
import ColoredSlider from './ColoredSlider/ColoredSlider';
import styles from './Slider.module.scss';

const Slider: FC<TSlider> = props => props.range && props.coloredMode ? (
  <ColoredSlider {...props} className={props.className} />
) : (
  <SliderAntd {...props} className={cn(styles.Slider, [props.className])} />
);

export default Slider;
