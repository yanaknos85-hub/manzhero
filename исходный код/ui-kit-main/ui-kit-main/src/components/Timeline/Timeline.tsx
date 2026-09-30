import React, { useState, useMemo } from 'react';
import type { FC } from 'react';
import { Timeline as TimelineAntd } from 'antd';
import cn from 'classnames';

import type ITimeline from './ITimeline';

import { ReactComponent as CurrentPoint } from '../../icons/TimeLine/currentPoint.svg';
import { ReactComponent as DonePoint } from '../../icons/TimeLine/donePoint.svg';
import styles from './Timeline.module.scss';

const Timeline: FC<ITimeline> = ({
  className,
  items,
  ...props
}) => {
  const [customDots, setCustomDots] = useState<boolean>(false);

  const list = useMemo(() => (
    items?.map((item, index) => {
      if (!customDots && item.dot) {
        setCustomDots(true);
      }

      return {
        ...item,
        key: index,
        dot: item.dot ? item.dot : (items.length - 1 === index ? <CurrentPoint /> : <DonePoint />),
      };
    })
  ), [items]);

  return (
    <TimelineAntd
      className={cn(styles.timeline, !customDots && styles['timeline--lineColor'], [className])}
      items={list}
      {...props}
    />
  );
};

export default Timeline;
