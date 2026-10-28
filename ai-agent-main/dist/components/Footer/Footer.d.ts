import React from 'react';
interface FooterProps {
    isDisabled: boolean;
    onMessage: (message: string) => void;
}
declare const Footer: React.FC<FooterProps>;
export default Footer;
