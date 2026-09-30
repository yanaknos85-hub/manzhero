import { CheckboxProps } from 'antd/lib/checkbox';
import { SizeType } from 'antd/lib/config-provider/SizeContext';

export interface ICheckbox extends CheckboxProps {
  className?: string;
  size?: Exclude<SizeType, 'large'>;
}
