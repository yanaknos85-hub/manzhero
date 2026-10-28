import React from 'react';
import { QRCode as QRCodeAntd } from 'antd';
import { IQRCode } from './IQRCode';

const QRCode: React.FC<IQRCode> = props => {
  const { value } = props;
  return (
    <QRCodeAntd
      {...props}
      value={value || ''}
    />
  );
};

export default QRCode;
