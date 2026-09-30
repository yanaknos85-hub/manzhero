interface Props {
    initialPosition?: {
        x: number;
        y: number;
    };
    itemSize?: number;
    localKey?: string;
}
declare const useDraggable: ({ initialPosition, localKey, itemSize, }: Props) => {
    position: {
        x: number;
        y: number;
    } | undefined;
    hasMoved: boolean;
    handleMouseDown: () => void;
};
export default useDraggable;
