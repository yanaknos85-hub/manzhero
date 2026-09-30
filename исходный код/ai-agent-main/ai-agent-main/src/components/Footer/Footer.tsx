import React, { useState, useEffect, useRef } from 'react';
import { Button, Input, InputRef } from 'antd';

import Icon from '../../components/Icon/Icon';

interface FooterProps {
  isDisabled: boolean;
  onMessage: (message: string) => void;
}

const Footer: React.FC<FooterProps> = ({ isDisabled, onMessage }) => {
  const [message, setMessage] = useState('');
  const inputRef = useRef<InputRef>(null);

  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  const onSend = () => {
    const text = message.trim();

    text && onMessage(text);
    setTimeout(() => setMessage(''));
  };

  const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setMessage(e.target.value);
  };

  return (
    <>
      <Input
        ref={inputRef}
        value={message}
        placeholder="Напишите, чем помочь"
        onChange={onChange}
        onPressEnter={onSend}
        maxLength={255}
      />
      <Button
        type="text"
        icon={<Icon name="send" />}
        disabled={!message || isDisabled}
        onClick={onSend}
      />
    </>
  );
};

export default Footer;
