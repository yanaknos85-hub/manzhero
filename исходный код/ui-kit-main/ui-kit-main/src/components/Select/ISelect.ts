import { SelectProps } from 'antd/lib/select';

export interface ISelect extends SelectProps {
  classNameDropdown?: string;
  showDivider?: boolean;
  showCheckMark?: boolean;
  useLocaleCompare?: boolean;
}
