import { ComponentToken } from 'antd/lib/select/style';
import { AliasToken } from 'antd/lib/theme/interface';

export const Select: Partial<AliasToken | ComponentToken> = {
  controlHeightSM: 40,
  controlHeight: 48,
  controlHeightLG: 48,

  colorPrimaryHover: '#D6D6D6',

  borderRadiusXS: 8,
  borderRadiusSM: 8,
  borderRadiusLG: 8,

  // эти 2 паддинга вообще ничего общего друг с другом не имеют!
  paddingSM: 16,
  paddingXXS: 0,

  controlPaddingHorizontalSM: 16,
  controlPaddingHorizontal: 16,

  controlItemBgActive: '#F2F3F6',
};
