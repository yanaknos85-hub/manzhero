import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import segments from '../exampleData/routeSegments.json';
import {
  MOSCOW, Map2GIS, Map2GISProvider, Polyline2GIS as Polyline, Polyline2GISProps
} from '../index';

export default {
  title: 'Map/Plyline2GIS',
  component: Polyline,
} as Meta<Polyline2GISProps>;

const Template: StoryFn<Polyline2GISProps> = args => (
  <Map2GISProvider APIKey={process.env.REACT_APP_API_2GIS_KEY!}>
    <Map2GIS center={[37.571242, 55.762224]} zoom={19}>
      <Polyline {...args} />
    </Map2GIS>
  </Map2GISProvider>
);

const baseArgs: Polyline2GISProps = {
  coordinates: [
    [37.571242, 55.762224],
    [37.571296, 55.762372],
  ],
};

export const Line = Template.bind({});
Line.args = {
  ...baseArgs,
};

const renderTripRouteTemplate: (route: number[][][]) => StoryFn<Polyline2GISProps> = routeCoordinates => {
  return function TripRouteTemplate(args) {
    return (
      <Map2GISProvider APIKey={process.env.REACT_APP_API_2GIS_KEY!}>
        <Map2GIS center={MOSCOW} zoom={12}>
          {routeCoordinates.map((segment, index) => (
            <Polyline
              {...args}
              key={index}
              coordinates={segment}
            />
          ))}
        </Map2GIS>
      </Map2GISProvider>
    );
  };
};

const route = segments.map(segment => segment.coordinates
  .filter(coord => coord.latitude && coord.latitude)
  .map(coord => [coord.longitude, coord.latitude])
);

export const TripRoute = renderTripRouteTemplate(route).bind({});
TripRoute.args = {
  ...baseArgs,
};
