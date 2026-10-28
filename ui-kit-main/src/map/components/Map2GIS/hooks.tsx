/* eslint-disable react-hooks/exhaustive-deps */
import React from 'react';
import {
  Control, LngLatBounds, Map, MapOptions,
  Padding
} from '@2gis/mapgl/types';
import { MutableRefObject, useEffect, useState } from 'react';
import { DEFAULT_OPTIONS, DEFAULT_PADDING } from '../../constants/2gis.constants';
import { use2GIS } from '../../context/2gis.context';
import { requestFullScreen } from '../../../utils/requestFullScreen';
import ReactDOMServer from 'react-dom/server';
import { ReactComponent as Expand } from '../../../icons/expand.svg';
import { ReactComponent as Collapse } from '../../../icons/collapse.svg';
import styles from './Map2GIS.module.scss';

/**
 * Создание новой карты
 * @returns инстанас новой карты
 */
export const useMap = (
  ref: MutableRefObject<HTMLDivElement | null>,
  /**
   * Нужно, чтобы достучаться до оставшихся опций.
   * Внимание! При изменении будет перерисовываться вся карта
  */
  defaultOptions?: Omit<MapOptions, 'key'>
) => {
  const {
    bundle,
    key,
    tileServer,
    tileProtocol,
  } = use2GIS();

  const [map, setMap] = useState<Map | undefined>();

  useEffect(() => {
    if (!bundle || !ref.current) {
      return;
    }

    const tileOptions = tileServer && tileProtocol ? { tileServer, tileProtocol } : {};

    const newMap: Map = new bundle.Map(ref.current, {
      ...DEFAULT_OPTIONS,
      ...defaultOptions,
      ...tileOptions,
      key,
    });

    setMap(newMap);

    return () => {
      newMap.destroy();
      setMap(undefined);
    };
  }, [
    bundle,
    JSON.stringify(defaultOptions),
    key,
    tileServer,
    tileProtocol,
  ]);

  return map;
};

/** Изменение центра карты */
export const useCenter = (map: Map | undefined, center: number[]) => {
  useEffect(() => {
    map?.setCenter(center);
  }, [map, JSON.stringify(center)]);
};

/** Изменение зума карта */
export const useZoom = (map: Map | undefined, zoom: number) => {
  useEffect(() => {
    map?.setZoom(zoom);
  }, [map, zoom]);
};

/** Кнопка разворачивания на весь экран */
export const useFullScreen = (
  map: Map | undefined,
  ref: MutableRefObject<HTMLDivElement | null>,
  fullScreenUse: boolean
) => {
  const { bundle } = use2GIS();

  const [isFullScreenOpened, setIsFullScreenOpened] = useState(false);

  // Кнопка развернуть
  useEffect(() => {
    if (!map || !bundle) {
      return;
    }

    const request = () => {
      requestFullScreen(ref.current);
    };

    let fullScreenControl: Control;

    if (fullScreenUse && !isFullScreenOpened) {
      fullScreenControl = new bundle.Control(
        map,
        ReactDOMServer.renderToString(<Expand id="fullScreen" className={styles.fullScreenControl} />),
        {
          position: 'topRight',
        }
      );

      fullScreenControl
        ?.getContainer()
        .querySelector('#fullScreen')
        ?.addEventListener('click', request);
    }

    return () => {
      fullScreenControl
        ?.getContainer()
        .querySelector('#fullScreen')
        ?.removeEventListener('click', request);
      fullScreenControl?.destroy();
    };
  }, [fullScreenUse, isFullScreenOpened, map, bundle]);

  // Кнопка сверуть
  useEffect(() => {
    if (!map || !bundle) {
      return;
    }

    const request = () => {
      document.exitFullscreen();
    };

    let fullScreenControl: Control;

    if (fullScreenUse && isFullScreenOpened) {
      fullScreenControl = new bundle.Control(
        map,
        ReactDOMServer.renderToString(<Collapse id="fullScreenClose" className={styles.fullScreenControl} />),
        {
          position: 'topRight',
        }
      );

      fullScreenControl
        ?.getContainer()
        .querySelector('#fullScreenClose')
        ?.addEventListener('click', request);
    }

    return () => {
      fullScreenControl
        ?.getContainer()
        .querySelector('#fullScreenClose')
        ?.removeEventListener('click', request);
      fullScreenControl?.destroy();
    };
  }, [fullScreenUse, isFullScreenOpened, map, bundle]);

  useEffect(() => {
    const toggleFullScreen = () => {
      setIsFullScreenOpened(!!document.fullscreenElement);
    };

    document.addEventListener('fullscreenchange', toggleFullScreen);
    document.addEventListener('webkitfullscreenchange', toggleFullScreen);

    return () => {
      document.removeEventListener('fullscreenchange', toggleFullScreen);
      document.removeEventListener('webkitfullscreenchange', toggleFullScreen);
    };
  }, []);
};

/**
 * Изменение масштаба для вписания прямоугольной области по координатам.
 * @param {Map} map - инстанс карты
 * @param {LngLatBounds} bounds - координаты southWest, northEast углов области.
 * @param {Padding} padding - отступы от края контейнера в пикселах. необязательный (дефолтные отступы 20px).
 */
export const useFitBounds = (map: Map | undefined, bounds: LngLatBounds | undefined, padding?: Padding) => {
  useEffect(() => {
    if (bounds) map?.fitBounds(bounds, { padding: padding || DEFAULT_PADDING });
  }, [map, JSON.stringify(bounds)]);
};
