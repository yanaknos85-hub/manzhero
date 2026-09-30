import React from 'react';
interface LogoProps {
    imageUrl?: string;
    animation?: boolean;
    position?: React.CSSProperties;
    onClick?: () => void;
    onMouseDown?: () => void;
}
declare const AiLogo: React.FC<LogoProps>;
export default AiLogo;
