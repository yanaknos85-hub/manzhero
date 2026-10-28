import React from 'react';
declare const icons: {
    logo: React.JSX.Element;
    agent: React.JSX.Element;
    arrow: React.JSX.Element;
    close: React.JSX.Element;
    copy: React.JSX.Element;
    dislike: React.JSX.Element;
    like: React.JSX.Element;
    send: React.JSX.Element;
};
export type IconType = keyof typeof icons;
interface IconProps extends React.SVGProps<SVGSVGElement> {
    name: IconType;
}
declare const Icon: React.FC<IconProps>;
export default Icon;
