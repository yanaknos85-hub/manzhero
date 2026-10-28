import { Typography as TypographyAntd } from 'antd';
import cn from 'classnames';
import React from 'react';
import { IText, ITitle, IParagraph } from './ITypography';
import styles from './Typography.module.scss';

const Text: React.FC<IText> = ({
  className, size = 'middle', ...props
}) => (
  <TypographyAntd.Text
    {...props}
    className={cn(
      styles.Text,
      { [styles[size]]: size },
      [className]
    )}
  />
);

const Title: React.FC<ITitle> = ({
  className, size = 'middle', ...props
}) => (
  <TypographyAntd.Title
    {...props}
    className={cn(
      styles.Title,
      { [styles[size]]: size },
      [className]
    )}
  />
);

const Paragraph: React.FC<IParagraph> = ({
  className, size = 'middle', ...props
}) => (
  <TypographyAntd.Text
    {...props}
    className={cn(
      styles.Paragraph,
      { [styles[size]]: size },
      [className]
    )}
  />
);

export { Text, Title, Paragraph };
