import React from 'react';
import type { FC } from 'react';
import { InputNumber as InputNumberAntd } from 'antd';
import cn from 'classnames';

import type { IInputNumber } from './IInputNumber';
import styles from './InputNumber.module.scss';

const InputNumber: FC<IInputNumber> = ({ className, ...props }) => (
  <InputNumberAntd {...props} className={cn(styles.InputNumber, [className])} />
);

export default InputNumber;
