import React from 'react';
interface TagProps {
    text: string;
    active?: boolean;
    onClick: (text: string) => void;
}
declare const Tag: React.FC<TagProps>;
export default Tag;
