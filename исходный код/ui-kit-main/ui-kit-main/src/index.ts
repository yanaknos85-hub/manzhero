import './styles/global.scss';
import '../static/sb-sans.css';

import Form from 'antd/lib/form';
import antdRU from 'antd/lib/locale/ru_RU';
import antdEN from 'antd/lib/locale/en_US';

export * from './ConfigProvider';

// Libs
export { Form };
export * from 'antd/lib/form';

export { antdRU, antdEN };

// Components
export { Typography as TypographyOriginal } from 'antd';
export * from './components/Typography';

export { Button as ButtonOriginal } from 'antd';
export type { ButtonProps as ButtonPropsOriginal } from 'antd';
export * from './components/Button';
export * from './components/Link';

export { Input as InputOriginal } from 'antd';
export type { InputProps as InputPropsOriginal } from 'antd';
export * from './components/Input';
export * from './components/Textarea';

export { Select as SelectOriginal } from 'antd';
export type { SelectProps as SelectPropsOriginal } from 'antd';
export type { OptionProps as OptionPropsOriginal } from 'antd/lib/select';
export * from './components/Select';

export { Checkbox as CheckboxOriginal } from 'antd';
export type { CheckboxProps as CheckboxPropsOriginal } from 'antd';
export * from './components/Checkbox';

export { Radio as RadioOriginal } from 'antd';
export type { RadioProps as RadioPropsOriginal } from 'antd';
export * from './components/Radio';

export { Spin as SpinOriginal } from 'antd';
export type { SpinProps as SpinPropsOriginal } from 'antd';
export * from './components/Spin';

export { Switch as SwitchOriginal } from 'antd';
export type { SwitchProps as SwitchPropsOriginal } from 'antd';
export * from './components/Switch';

export { Tabs as TabsOriginal } from 'antd';
export type { TabsProps as TabsPropsOriginal } from 'antd';
export * from './components/Tabs';

export { Modal as Modalntd } from 'antd';
export type { ModalProps as ModalPropsOriginal } from 'antd';
export * from './components/Modal';

export { Pagination as PaginationOriginal } from 'antd';
export type { PaginationProps as PaginationPropsOriginal } from 'antd';
export * from './components/Pagination';

export { Table as TableOriginal } from 'antd';
export type { TableProps as TablePropsOriginal } from 'antd';
export * from './components/Table';

export { Space as SpaceOriginal } from 'antd';
export type { SpaceProps as SpacePropsOriginal } from 'antd';
export * from './components/Space';

export { Steps as StepsOriginal } from 'antd';
export type { StepsProps as StepsPropsOriginal } from 'antd';
export * from './components/Steps';

export { Drawer as DrawerOriginal } from 'antd';
export type { DrawerProps as DrawerPropsOriginal } from 'antd';
export * from './components/Drawer';

export { Avatar as AvatarOriginal } from 'antd';
export type { AvatarProps as AvatarPropsOriginal } from 'antd';
export * from './components/Avatar';

export { Menu as MenuOriginal } from 'antd';
export type { MenuProps as MenuPropsOriginal } from 'antd';
export * from './components/Menu';

export { Dropdown as DropdownOriginal } from 'antd';
export type { DropdownProps as DropdownPropsOriginal } from 'antd';
export * from './components/Dropdown';

export { AutoComplete as AutoCompleteOriginal } from 'antd';
export type { AutoCompleteProps as AutoCompletePropsOriginal } from 'antd';
export * from './composite/Autocomplete';

export { QRCode as QRCodeOriginal } from 'antd';
export type { QRCodeProps } from 'antd';
export * from './components/QRCode';

export { Popover as PopoverOriginal } from 'antd';
export type { PopoverProps } from 'antd';
export * from './components/Popover';

export { Statistic as StatisticOriginal } from 'antd';
export type { StatisticProps } from 'antd';
export * from './components/Statistic';

export * from './map';

export { TreeSelect as TreeSelectOriginal } from 'antd';
export type { TreeSelectProps } from 'antd';
export * from './components/TreeSelect';

export { Card as CardOriginal } from 'antd';
export type { CardProps } from 'antd';
export * from './components/Card';

export { Empty as EmptyOriginal } from 'antd';
export type { EmptyProps } from 'antd';
export * from './components/Empty';

export { Popconfirm as PopconfirmOriginal } from 'antd';
export type { PopconfirmProps } from 'antd';
export * from './components/Popconfirm';

export { Result as ResultOriginal } from 'antd';
export type { ResultProps } from 'antd';
export * from './components/Result';

export { Tag as TagOriginal } from 'antd';
export type { TagProps } from 'antd';
export * from './components/Tag';

export { Timeline as TimelineOriginal } from 'antd';
export type { TimelineProps } from 'antd';
export * from './components/Timeline';

export { Tree as TreeOriginal } from 'antd';
export type { TreeProps } from 'antd';
export * from './components/Tree';

export { Slider as SliderOriginal } from 'antd';
export type { SliderSingleProps, SliderRangeProps } from 'antd/lib/slider';
export * from './components/Slider';

export { Rate as RateOriginal } from 'antd';
export type { RateProps } from 'antd';
export * from './components/Rate';

export { InputNumber as InputNumberOriginal } from 'antd';
export type { InputNumberProps } from 'antd';
export * from './components/InputNumber';

export { Alert as AlertAntd } from 'antd';
export type { AlertProps } from 'antd';
export * from './components/Alert';

export { Cascader as CascaderOriginal } from 'antd';
export type { CascaderProps } from 'antd';
export * from './components/Cascader';
