import { ConfigProviderProps } from 'antd/lib/config-provider';
import antdRU from 'antd/lib/locale/ru_RU';
import theme from '../theme';

const Config: ConfigProviderProps = {
  prefixCls: 'sbt-kit',
  theme,
  locale: antdRU,
};

export default Config;
