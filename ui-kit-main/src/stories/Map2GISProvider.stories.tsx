import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import { Map2GISProvider as Provider } from '../index';

export default {
  title: 'Map/Map2GISProvider',
  component: Provider,
} as Meta;

const Template: StoryFn = () => (
  <Provider APIKey={process.env.REACT_APP_API_2GIS_KEY!}>
    Провайдер контекста 2ГИС с загруженным бандлом. Компонент карты должен находиться внутри провайдера. Можно обернуть
    в него все приложение
  </Provider>
);

const baseArgs = {};

export const Map2GISProvider = Template.bind({});
Map2GISProvider.args = {
  ...baseArgs,
};
