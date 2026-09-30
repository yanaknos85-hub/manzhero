import { TextProps } from 'antd/lib/typography/Text';
import { TitleProps } from 'antd/lib/typography/Title';
import { ParagraphProps } from 'antd/lib/typography/Paragraph';
import { SizeType } from 'antd/lib/config-provider/SizeContext';

export interface IText extends TextProps {
  className?: string;
  size?: SizeType;
}

export interface ITitle extends TitleProps {
  className?: string;
  size?: SizeType;
}

export interface IParagraph extends ParagraphProps {
  className?: string;
  size?: SizeType;
}
