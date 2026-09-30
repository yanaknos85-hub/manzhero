import React, { useState, useRef, useEffect } from 'react';
import type { FC } from 'react';
import { Slider as SliderAntd } from 'antd';
import cn from 'classnames';

import type { ISliderRange } from '../ISlider';
import styles from './ColoredSlider.module.scss';

const ColoredSlider: FC<ISliderRange> = ({
  className,
  min,
  max,
  value,
  defaultValue,
  onChange,
  ...props
}) => {
  const [_value, _setValue] = useState(value ?? defaultValue);

  const leftPartRef = useRef<HTMLDivElement>(null);
  const centerPartRef = useRef<HTMLDivElement>(null);
  const rightPartRef = useRef<HTMLDivElement>(null);
  const leftValueRef = useRef<HTMLDivElement>(null);
  const rightValueRef = useRef<HTMLDivElement>(null);

  const recalculateColorBars = (value: number[]) => {
    const [lowerBorder, upperBorder] = value;
    if (leftPartRef.current && centerPartRef.current && rightPartRef.current
      && leftValueRef.current && rightValueRef.current) {
      // Значения границ цветов
      leftPartRef.current.style.flexGrow = `${lowerBorder / 100}`;
      centerPartRef.current.style.flexGrow = `${(upperBorder - lowerBorder) / 100}`;
      rightPartRef.current.style.flexGrow = `${(100 - upperBorder) / 100}`;

      // Значения расположения цифр. Отступ 0px для 1 цифры, 3px для 2 и 6px для 3
      leftValueRef.current.style.left = `calc(${lowerBorder}% - ${(lowerBorder.toString().length - 1) * 3}px)`;
      rightValueRef.current.style.left = `calc(${upperBorder}% - ${(upperBorder.toString().length - 1) * 3}px)`;
    }
  };

  const handleChange = (value: number[]) => {
    _setValue(value);
    recalculateColorBars(value);
    onChange?.(value);
  };

  useEffect(() => {
    if (_value) {
      recalculateColorBars(_value);
    }
  }, []);

  return (
    <div className={styles.ColoredSlider}>
      <div className={styles.ColoredSlider__min}>
        {min ?? 0}
      </div>

      <div className={styles.ColoredSlider__wrapper}>
        <SliderAntd
          {...props}
          vertical={false}
          dots={false}
          reverse={false}
          min={min}
          max={max}
          value={_value}
          className={cn(styles.slider, [className])}
          onChange={handleChange}
        />
        <div ref={leftPartRef} className={styles.slider__leftPart} />
        <div ref={centerPartRef} className={styles.slider__centerPart} />
        <div ref={rightPartRef} className={styles.slider__rightPart} />

        <div ref={leftValueRef} className={styles.slider__leftValue}>
          {_value?.[0] ?? 30}
        </div>

        <div ref={rightValueRef} className={styles.slider__rightValue}>
          {_value?.[1] ?? 60}
        </div>
      </div>

      <div className={styles.ColoredSlider__max}>
        {max ?? 100}
      </div>
    </div>
  );
};

export default ColoredSlider;
