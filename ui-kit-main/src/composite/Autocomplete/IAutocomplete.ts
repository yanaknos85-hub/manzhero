import { AutoCompleteProps } from 'antd/lib/auto-complete';

export type IAutoComplete = AutoCompleteProps & {
  classNameInput?: string;
  classNameDropdown?: string;
  placeholder?: string;
  withInput?: boolean;
};
