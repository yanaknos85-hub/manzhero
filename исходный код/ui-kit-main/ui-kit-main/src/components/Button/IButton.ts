import { ButtonProps } from 'antd/lib/button/button';
import { ButtonType } from 'antd/lib/button/buttonHelpers';

export interface IButton extends Omit<ButtonProps, 'type'> {
  className?: string;
  type?: ButtonType | 'secondary';
}
