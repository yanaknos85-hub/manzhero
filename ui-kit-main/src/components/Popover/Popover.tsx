import React from 'react';
import { Popover as PopoverAntd } from 'antd';
import { IPopover } from './IPopover';

export const Popover: React.FC<IPopover> = props => (
  <PopoverAntd {...props} />
);

export default Popover;
