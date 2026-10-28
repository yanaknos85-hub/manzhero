import React from 'react';
import { Statistic as StatisticAntd } from 'antd';
import { IStatistic } from './IStatistic';

export const Statistic: React.FC<IStatistic> = props => (
  <StatisticAntd
    {...props}
  />
);

export default Statistic;
