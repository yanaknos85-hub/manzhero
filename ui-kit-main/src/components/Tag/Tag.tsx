import React from 'react';
import { Tag as TagAntd } from 'antd';
import cn from 'classnames';

import { ReactComponent as CloseIcon } from '../../icons/close.svg';
import { ITag } from './ITag';
import styles from './Tag.module.scss';

const Tag: React.FC<ITag> = ({
  className, children, bordered, size = 'default', color = 'default', ...props
}) => (
  <TagAntd
    className={cn(styles.tag, [className], {
      [styles.default]: color === 'default',
      [styles.bordered]: bordered,
      [styles[`tag_${size}`]]: size,
    })}
    bordered={bordered}
    color={color}
    closeIcon={<CloseIcon />}
    {...props}
  >
    {children}
  </TagAntd>
);

export default Tag;
