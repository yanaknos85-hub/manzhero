import { Modal as ModalAntd } from 'antd';
import cn from 'classnames';
import React from 'react';
import styles from './Modal.module.scss';
import { IModal } from './IModal';

const Modal: React.FC<IModal> = ({ className, ...props }) => (
  <ModalAntd
    {...props}
    className={cn(
      styles.Checkbox,
      { },
      [className]
    )}
  />
);

export default Modal;
