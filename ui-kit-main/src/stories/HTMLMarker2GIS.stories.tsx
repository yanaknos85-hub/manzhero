import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import {
  DEFAULT_ZOOM,
  HTMLMarker2GIS as HTMLMarker,
  HTMLMarker2GISProps,
  MOSCOW,
  Map2GIS,
  Map2GISProvider
} from '../index';

export default {
  title: 'Map/HTMLMarker2GIS',
  component: HTMLMarker,
} as Meta<HTMLMarker2GISProps>;

const Template: StoryFn<HTMLMarker2GISProps> = args => (
  <Map2GISProvider APIKey={process.env.REACT_APP_API_2GIS_KEY!}>
    <Map2GIS center={MOSCOW} zoom={DEFAULT_ZOOM}>
      <HTMLMarker {...args} />
    </Map2GIS>
  </Map2GISProvider>
);

const baseArgs: HTMLMarker2GISProps = {
  children: <div style={{ background: 'white', padding: '2px' }}>Это HTML Marker</div>,
  coordinates: MOSCOW,
};

export const HTMLMarker2GIS = Template.bind({});
HTMLMarker2GIS.args = {
  ...baseArgs,
};
