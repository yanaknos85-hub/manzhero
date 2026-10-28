import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import {
  ConfigProvider, ITabs, Tabs, TabsDeprecated, TabsPaneDeprecated
} from '../index';

export default {
  title: 'Elements/Tabs',
  component: Tabs,
} as Meta<ITabs>;

const baseArgs: ITabs = {
  items: [
    {
      label: 'Пассажирские перевозки', key: 'item-1', children: 'Content 1',
    }, // remember to pass the key prop
    {
      label: 'Грузовые перевозки', key: 'item-2', children: 'Content 2',
    },
    {
      label: 'Автосервис', key: 'item-3', children: 'Content 3',
    },
    {
      label: 'Парковки', key: 'item-4', children: 'Content 4',
    },
  ],
};

const Template: StoryFn<ITabs> = args => (
  <ConfigProvider>
    <Tabs {...args} />
  </ConfigProvider>
);

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};

const TemplateDeprecated: StoryFn<ITabs> = args => (
  <ConfigProvider>
    <TabsDeprecated {...args}>
      <TabsPaneDeprecated tab="Пассажирские перевозки" key="item-1">
        Content 1
      </TabsPaneDeprecated>
      <TabsPaneDeprecated tab="Грузовые перевозки" key="item-2">
        Content 2
      </TabsPaneDeprecated>
      <TabsPaneDeprecated tab="Автосервис" key="item-3">
        Content 3
      </TabsPaneDeprecated>
      <TabsPaneDeprecated tab="Парковки" key="item-4">
        Content 4
      </TabsPaneDeprecated>
    </TabsDeprecated>
  </ConfigProvider>
);

export const Deprecated = TemplateDeprecated.bind({});
Deprecated.args = {};
