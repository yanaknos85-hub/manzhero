import React, { useState, useEffect } from 'react';
import type { FC } from 'react';
import { Rate as RateAntd } from 'antd';
import cn from 'classnames';

import type { IRate } from './IRate';
import styles from './Rate.module.scss';

const Rate: FC<IRate> = ({
  className,
  size = 'medium',
  dynamicColor,
  value,
  defaultValue,
  count = 5,
  middleRating = (count + 1) / 2,
  onChange,
  onHoverChange,
  ...props
}) => {
  const [rating, setRating] = useState(value ?? defaultValue);
  const [visibleRating, setVisibleRating] = useState(rating);
  const isPositive = visibleRating && (visibleRating > middleRating);

  const handleChange = (value: number) => {
    setRating(value);
    setVisibleRating(value);
    onChange?.(value);
  };

  const handleHoverChange = (value: number) => {
    if (value) {
      setVisibleRating(value);
    } else {
      setVisibleRating(rating);
    }
    onHoverChange?.(value);
  };

  useEffect(() => {
    if (typeof value === 'number') {
      setRating(value);
      setVisibleRating(value);
    }
  }, [value]);

  return (
    <RateAntd
      {...props}
      value={rating}
      count={count}
      className={cn(
        styles.Rate,
        styles[`Rate_${size}`],
        {
          [styles.Rate_dynamicBad]: dynamicColor && !isPositive,
          [styles.Rate_dynamicGood]: dynamicColor && isPositive,
        },
        [className]
      )}
      onChange={handleChange}
      onHoverChange={handleHoverChange}
    />
  );
};

export default Rate;
