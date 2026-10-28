import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import {
  Map2GIS as Map, Map2GISProps, Map2GISProvider, MOSCOW
} from '../index';

export default {
  title: 'Map/Map2GIS',
  component: Map,
} as Meta<Map2GISProps>;

const Template: StoryFn<Map2GISProps> = args => (
  <Map2GISProvider APIKey={process.env.REACT_APP_API_2GIS_KEY!}>
    <Map {...args} />
  </Map2GISProvider>
);

const baseArgs: Map2GISProps = {
  fullScreenControl: true,
};

export const Map2GIS = Template.bind({});
Map2GIS.args = {
  ...baseArgs,
  // eslint-disable-next-line
  onMapCreate: () => {}, // пустой метод нужен, иначе будет ошибка при выводе компонента в сторибуке.
  center: MOSCOW,
};
