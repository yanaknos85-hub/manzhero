import React, { createContext, useContext, useMemo } from 'react';
import type { FC } from 'react';

import { useBundle } from '../hooks/useBundle';

interface Options {
  key: string;
  tileServer?: string;
  tileProtocol?: 'http' | 'https';
}

/** Хук для получения value контекста */
const useHook = ({
  key, tileServer, tileProtocol,
}: Options) => {
  const bundle = useBundle();

  return useMemo(
    () => ({
      bundle,
      key,
      tileServer,
      tileProtocol,
    }),
    [bundle, key, tileServer, tileProtocol]
  );
};

export const Map2GISContext = createContext<ReturnType<typeof useHook>>({} as ReturnType<typeof useHook>);

/**
 * @returns bundle - бандл 2gis для построения карты
 */
export const use2GIS = () => {
  const value = useContext(Map2GISContext);

  if (!Object.keys(value).length) {
    throw new Error('use2GIS must be inside a Map2GISProvider with a value');
  }

  return value;
};

export type Map2GISProviderProps = Omit<Options, 'key'> & {
  APIKey: string;
};

export const Map2GISProvider: FC<Map2GISProviderProps> = ({ children, ...props }) => {
  const value = useHook({
    ...props,
    key: props.APIKey,
  });

  return <Map2GISContext.Provider value={value}>{children}</Map2GISContext.Provider>;
};
