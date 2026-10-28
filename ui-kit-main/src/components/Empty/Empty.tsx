import React from 'react';
import { Empty as EmptyAntd } from 'antd';
import cn from 'classnames';

import { IEmpty } from './IEmpty';
import folderIcon from '../../images/folder.png';
import styles from './Empty.module.scss';

const Empty: React.FC<IEmpty> = ({
  className, children, title, description, imgSize = 280, ...props
}) => (
  <EmptyAntd
    image={folderIcon}
    imageStyle={{ width: imgSize }}
    {...props}
    description={title ? null : description}
    className={cn(styles.empty, [className], {
      [styles.titleContainer]: title,
    })}
  >
    <>
      {title && (
        <section className={styles.textSide}>
          <strong className={styles.title}>{title}</strong>
          {description && <p className={styles.description}>{description}</p>}
        </section>
      )}
      <div className={cn('', { [styles.emptyFooter]: title && children })}>
        {children}
      </div>
    </>
  </EmptyAntd>
);

export default Empty;
