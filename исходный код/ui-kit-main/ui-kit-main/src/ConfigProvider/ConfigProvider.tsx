import { ConfigProvider } from 'antd';
import { ConfigProviderProps } from 'antd/lib/config-provider';
import React from 'react';

import defaultConfig from './config';

const Provider: React.FC<{ children: React.ReactNode } & ConfigProviderProps> = ({ children, ...config }) => (
  <ConfigProvider {...defaultConfig} {...config}>
    {children}
  </ConfigProvider>
);

export default Provider;
