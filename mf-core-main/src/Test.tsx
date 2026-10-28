import React, { ReactNode, Suspense } from 'react';

export const CustomSuspense: ({
  children,
  fallback,
}: {
  children: ReactNode;
  fallback: any;
}) => JSX.Element = ({ children, fallback }: { children: ReactNode; fallback: any }): JSX.Element => (
  <Suspense fallback={fallback}>{children}</Suspense>
);
