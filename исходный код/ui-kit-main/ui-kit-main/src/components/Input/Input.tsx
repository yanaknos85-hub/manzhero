import { Input as AntDesignInput } from 'antd';
import classNames from 'classnames';
import React from 'react';
import { IInput, InputRef } from './IInput';
import styles from './Input.module.scss';

const Input = React.forwardRef<InputRef, IInput>(
  ({
    mode = 'default', type, className, size = 'middle', ...props
  }, ref) => {
    const El = mode === 'search' ? AntDesignInput.Search : AntDesignInput;

    return (
      <El
        {...props}
        size={size}
        className={classNames(styles.Input, className, {
          [styles[mode]]: mode,
          [styles.disabled]: props.disabled,
        })}
        ref={ref}
      />
    );
  }
);

Input.displayName = 'Input';

export default Input;
