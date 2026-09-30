import { Steps as StepsAntd } from 'antd';
import cn from 'classnames';
import React from 'react';
import { ISteps } from './ISteps';
import styles from './Steps.module.scss';

const Steps: React.FC<ISteps> = ({ className, ...props }) => (
  <StepsAntd {...props} className={cn(styles.Steps, [className])} />
);

export default Steps;
