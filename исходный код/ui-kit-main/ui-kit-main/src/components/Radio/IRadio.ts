import { RadioProps, RadioChangeEvent } from 'antd/lib/radio';
import { SizeType } from 'antd/lib/config-provider/SizeContext';

export type { RadioChangeEvent };

export interface IRadio extends RadioProps {
  className?: string;
  size?: Exclude<SizeType, 'large'>;
}
