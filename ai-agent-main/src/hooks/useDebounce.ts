import { useRef, useCallback } from 'react';

type Timer = ReturnType<typeof setTimeout>;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const useDebounce = (callback: (...args: any[]) => void, timeout = 500) => {
  const timer = useRef<Timer>();

  return useCallback(
    (...args) => {
      if (timer.current) {
        clearTimeout(timer?.current);
      }

      timer.current = setTimeout(() => {
        callback(...args);
      }, timeout);
    },
    [callback, timeout]
  );
};
