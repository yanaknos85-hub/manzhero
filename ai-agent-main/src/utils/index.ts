import { months } from '../constants/constants';

export const ignore = (): void => undefined;

export const handleCopy = (text: string) => {
  if (window.isSecureContext && navigator.clipboard) {
    navigator.clipboard.writeText(text);
  } else {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
  }
};

const getDate = (num: number) => num < 10 ? '0' + num : num;

export const getCurrentDate = () => {
  const today = new Date();

  const day = getDate(today.getDate());
  const month = months[today.getMonth()];

  return `Сегодня, ${day} ${month}`;
};
