import React from 'react';

import * as LogoIcon from '../../assets/ai-logo.svg';
import * as AgentLogoIcon from '../../assets/agent-logo.svg';
import * as ArrowIcon from '../../assets/arrow.svg';
import * as CloseIcon from '../../assets/close.svg';
import * as CopyIcon from '../../assets/copy.svg';
import * as DislikeIcon from '../../assets/dislike.svg';
import * as LikeIcon from '../../assets/like.svg';
import * as SendIcon from '../../assets/send.svg';

const icons = {
  logo: <LogoIcon.default />,
  agent: <AgentLogoIcon.default />,
  arrow: <ArrowIcon.default />,
  close: <CloseIcon.default />,
  copy: <CopyIcon.default />,
  dislike: <DislikeIcon.default />,
  like: <LikeIcon.default />,
  send: <SendIcon.default />,
};

export type IconType = keyof typeof icons;

interface IconProps extends React.SVGProps<SVGSVGElement> {
  name: IconType;
}

const Icon: React.FC<IconProps> = ({ name, ...props }) => {
  const IconComponent = icons[name];
  return React.cloneElement(IconComponent, { ...props });
};

export default Icon;
