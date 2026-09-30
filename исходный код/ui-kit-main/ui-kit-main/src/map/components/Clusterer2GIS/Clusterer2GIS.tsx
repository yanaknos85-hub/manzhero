import type { Map } from '@2gis/mapgl/types';
import { Clusterer } from '@2gis/mapgl-clusterer';
import type { 
  ClustererOptions,
  ClustererPointerEvent,
  InputMarker,
} from '@2gis/mapgl-clusterer';
import { memo, useEffect, useRef } from 'react';
import type { FC } from 'react';
import { use2GIS } from '../../context/2gis.context';

export interface Clusterer2GISProps extends ClustererOptions {
  map?: Map;
  markers: InputMarker[];
  onClick?: (userData: unknown) => void;
}

export const Clusterer2GIS: FC<Clusterer2GISProps> = memo(({
  map,
  markers,
  onClick,
  ...options
}) => {
  const { bundle } = use2GIS();
  const clustererRef = useRef<Clusterer | null>(null);

  useEffect(() => {
    if (!map || !bundle) {
      return;
    }

    const clusterer = new Clusterer(map, { ...options });
    clustererRef.current = clusterer;

    return () => {
      clusterer.destroy();
      clustererRef.current = null;
    };
  }, [map, bundle, JSON.stringify(options)]);

  useEffect(() => {
    const clusterer = clustererRef.current;
    if (!clusterer) {
      return;
    }

    clusterer.load(markers);
  }, [markers]);

  useEffect(() => {
    const clusterer = clustererRef.current;
    if (!clusterer || !map) {
      return;
    }

    const handleClick = (event: ClustererPointerEvent) => {
      if (event.target.type === 'cluster') {
        const zoom = clusterer.getClusterExpansionZoom(event.target.id);
        map.setCenter(event.lngLat, { duration: 300 });
        map.setZoom(zoom, { duration: 300 });

        return;
      }
      
      const { userData } = event.target.data;
      if (onClick && userData) {
        onClick(userData);
      }
    };

    clusterer.on('click', handleClick);

    return () => {
      clusterer.off('click', handleClick);
    };
  }, [map, onClick])

  return null;
});
