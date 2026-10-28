import React from 'react';
import { Select as AntSelect } from 'antd';
import { OptionProps, DefaultOptionType as OptionType } from 'antd/lib/select';
import classNames from 'classnames';

import { ISelect } from './ISelect';
import styles from './Select.module.scss';

const getLabelText = (optionValue: keyof OptionType): string => {
  if (typeof optionValue === 'string') {
    return optionValue.toLowerCase();
  }

  if (typeof optionValue === 'number') {
    return `${optionValue}`;
  }

  try {
    if (React.isValidElement(optionValue)) {
      const { props: { children } } = React.Children.toArray(optionValue)[0] as Omit<OptionType, 'children'>;

      if (typeof children === 'string') {
        return children.toLowerCase();
      }

      return children.map(getLabelText).join('').toLowerCase();
    }
  } catch {
    return '';
  }

  return '';
};

const onLocaleCompare = (optionFilterProp: ISelect['optionFilterProp']) => (optionA: OptionType, optionB: OptionType) => (
  getLabelText(optionA[optionFilterProp!]).localeCompare(getLabelText(optionB[optionFilterProp!]))
);

const Select: React.FC<ISelect> = ({
  className,
  classNameDropdown,
  showDivider,
  showCheckMark,
  size = 'middle',
  optionFilterProp = 'label',
  useLocaleCompare,
  filterSort,
  ...props
}) => {
  const isMultiMode = props.mode === 'multiple' || props.mode === 'tags';

  return (
    <AntSelect
      filterOption={(input, option) => getLabelText(option?.[optionFilterProp]).includes(input.toLowerCase())}
      filterSort={filterSort ? filterSort : useLocaleCompare ? onLocaleCompare(optionFilterProp) : undefined}
      {...props}
      size={size}
      className={classNames(styles.Select, className, {
        [styles.SelectDivider]: showDivider,
        [styles.SelectMultiple]: isMultiMode,
      })}
      popupClassName={classNames(styles.SelectDropdown, classNameDropdown, {
        [styles.SelectCheckMark]: showCheckMark || isMultiMode,
      })}
    />
  );
};

export default Select;

export const Option: React.FC<OptionProps> = props => <AntSelect.Option {...props} />;
