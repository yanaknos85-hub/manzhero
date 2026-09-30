import {intervalsCreator } from './formatters_TypeScript';
import React, { FC, useEffect, useState } from 'react';
import ErrorBoundary from './ErrorBoundary';

intervalsCreator(1, 2);

enum Links {
  LnikA = 'linkA',
  LnikB = 'linkB',
}

const arr = [
  1,
  2,
];

const singA = { a: 1, b: 2 };
const singB = { a: 1, b: 2, c: 4 };

const obj = {
  a: 1,
  b: 2
};

const str = "123";

const func = () =>
  <div>val</div>

console.log(Links, str, arr, obj, func);

const Test: FC<{ on?: boolean; off?: boolean; value?: () => void; children?: JSX.Element }> = ({
  on,
  children,
}) => (
  on ? (
    <h1>{children}</h1>
  ) : (
    <h1>Wow</h1>
  )
);

const Prov = (): JSX.Element => {
  return (
    <ErrorBoundary>
      Ошибка
    </ErrorBoundary>
  );
};

export default function App() {
  const [val, setVal] = useState(0);

  useEffect(() => {
    setVal(1);
  }, []);

  return () => (
    <>
      <label>Label</label>
        <br />
      <div onKeyDown={() => {}}>sdfddf</div>
      World
      {arr.map(() => (
        <div>1</div>
      ))}
      <h1>Hello {val}</h1>
      <Test on={false} off={false} {...arr} />
    </>
  );
}
