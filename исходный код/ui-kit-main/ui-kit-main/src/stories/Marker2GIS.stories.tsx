import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import MapCar from '../icons/map-car.svg';
import {
  DEFAULT_ZOOM, MOSCOW, Map2GIS, Map2GISProvider, Marker2GIS as Marker, Marker2GISProps
} from '../index';

export default {
  title: 'Map/Marker2GIS',
  component: Marker,
} as Meta<Marker2GISProps>;

const Template: StoryFn<Marker2GISProps> = args => (
  <Map2GISProvider APIKey={process.env.REACT_APP_API_2GIS_KEY!}>
    <Map2GIS center={MOSCOW} zoom={DEFAULT_ZOOM}>
      <Marker {...args} />
    </Map2GIS>
  </Map2GISProvider>
);

const baseArgs: Marker2GISProps = {
  coordinates: MOSCOW,
};

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};

export const CustomIcon = Template.bind({});
CustomIcon.args = {
  ...baseArgs,
  icon: MapCar,
};
