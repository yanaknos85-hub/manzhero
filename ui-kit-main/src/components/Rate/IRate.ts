import type { RateProps } from 'antd/lib/rate';

export interface IRate extends RateProps {
  size?: 'small' | 'medium' | 'large';
  dynamicColor?: boolean;
  middleRating?: number;
}
