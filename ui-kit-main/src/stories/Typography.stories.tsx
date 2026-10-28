import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import {
  Text, Title, Paragraph, IText, ITitle, IParagraph, ConfigProvider
} from '../index';

export default {
  title: 'Elements/Typography',
  component: Text,
} as Meta<IText>;

const TextTemplate: StoryFn<IText> = () => (
  <ConfigProvider>
    <Text>Сбертранспорт</Text>
  </ConfigProvider>
);

const TitleTemplate: StoryFn<ITitle> = () => (
  <ConfigProvider>
    <Title>Сбертранспорт</Title>
  </ConfigProvider>
);

const ParagraphTemplate: StoryFn<IParagraph> = () => (
  <ConfigProvider>
    <Paragraph>Сбертранспорт</Paragraph>
  </ConfigProvider>
);

export const TText = TextTemplate.bind({});
TText.args = {};

export const TTitle = TitleTemplate.bind({});
TTitle.args = {};

export const TParagraph = ParagraphTemplate.bind({});
TParagraph.args = {};
