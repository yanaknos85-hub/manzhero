import { useEffect, useState } from 'react';
import { useDebounce } from './useDebounce';

interface Props {
  initialPosition?: { x: number; y: number };
  itemSize?: number;
  localKey?: string;
}

const useDraggable = ({
  initialPosition, localKey, itemSize = 0,
}: Props) => {
  const [isDragging, setIsDragging] = useState(false);
  const [hasMoved, setHasMoved] = useState(false);
  const [position, setPosition] = useState(initialPosition);

  const savePositions = useDebounce(() => {
    localStorage.setItem(localKey!, JSON.stringify(position));
  }, 1000);

  const handleMouseDown = () => {
    setIsDragging(true);
    setHasMoved(false);
  };

  const handleMouseMove = (e: MouseEvent) => {
    if (isDragging) {
      const offsetSize = itemSize / 2;
      const newX = e.clientX - offsetSize;
      const newY = e.clientY - offsetSize;

      // Ограничиваем позицию по всем четырём сторонам экрана
      const boundedX = Math.max(0, Math.min(newX, window.innerWidth - itemSize));
      const boundedY = Math.max(0, Math.min(newY, window.innerHeight - itemSize));

      setPosition({
        x: boundedX,
        y: boundedY,
      });
      setHasMoved(true);
    }
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  const onPositionToViewport = (newPos: { x: number; y: number }) => {
    const { x, y } = newPos;
    const maxX = window.innerWidth - itemSize;
    const maxY = window.innerHeight - itemSize;

    return {
      x: Math.max(0, Math.min(x, maxX)),
      y: Math.max(0, Math.min(y, maxY)),
    };
  };

  const debouncedResize = useDebounce(() => {
    if (position) {
      const correctedPos = onPositionToViewport(position);

      if (correctedPos.x !== position.x || correctedPos.y !== position.y) {
        setPosition(correctedPos);
      }
    }
  }, 200);

  useEffect(() => {
    window.addEventListener('resize', debouncedResize);

    return () => {
      window.removeEventListener('resize', debouncedResize);
    };
  }, [position, debouncedResize]);

  useEffect(() => {
    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isDragging]);

  useEffect(() => {
    if (localKey && position) {
      savePositions();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [position]);

  useEffect(() => {
    if (localKey) {
      const savedPosition = localStorage.getItem(localKey);

      if (savedPosition) {
        setPosition(JSON.parse(savedPosition));
      }
    }
  }, [localKey]);

  return {
    position,
    hasMoved,
    handleMouseDown,
  };
};

export default useDraggable;
