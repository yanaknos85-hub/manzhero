import { AutoComplete as AntdAutocomplete } from 'antd';
import classNames from 'classnames';
import React from 'react';
import styles from './Autocomplete.module.scss';
import { IAutoComplete } from './IAutocomplete';
import { Input } from '../../components/Input';
import { ReactComponent as SearchIcon } from '../../icons/search.svg';

const Autocomplete: React.FC<IAutoComplete> = ({
  className,
  classNameInput,
  classNameDropdown,
  placeholder,
  withInput,
  size = 'middle',
  children,
  ...props
}) => (
  <AntdAutocomplete
    {...props}
    size={size}
    className={classNames(styles.Autocomplete, className, {
      [styles.withInput]: withInput,
      [styles.disabled]: props.disabled,
      [styles[size]]: size,
    })}
    placeholder={!withInput && placeholder}
    suffixIcon={!withInput && <SearchIcon />}
    popupClassName={classNames(styles.AutocompleteDropdown, classNameDropdown, {})}
  >
    {withInput && (
      <Input
        mode="search"
        className={classNames(styles.AutocompleteInput, classNameInput, {})}
        size={size}
        placeholder={placeholder}
      />
    )}
  </AntdAutocomplete>
);

export default Autocomplete;
