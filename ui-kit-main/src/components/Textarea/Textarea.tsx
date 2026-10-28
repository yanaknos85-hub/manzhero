import { Input } from 'antd';
import classNames from 'classnames';
import React from 'react';
import { ITextarea } from './ITextarea';
import styles from './Textarea.module.scss';

const Textarea: React.FC<ITextarea> = ({
  className, size = 'middle', ...props
}) => (
  <Input.TextArea
    size={size}
    className={classNames(styles.Textarea, className, {})}
    {...props}
  />
);

export default Textarea;
