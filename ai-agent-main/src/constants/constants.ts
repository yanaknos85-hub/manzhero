/** Типы оценок */
export enum EvaluationTypes {
  LIKE = 'LIKE',
  DISLIKE = 'DISLIKE',
  NOT_EVALUATED = 'NOT_EVALUATED',
}

export enum AIType {
  WebCorp = 'WEB_CORP',
  Client = 'CLIENT',
  Dispatcher = 'DISPATCHER',
  Autoservice = 'AUTOSERVICE',
}

export const defaultSetting = {
  namePrompt: 'Ассистент',
};

export const DISLIKE_OPTIONS = [
  'Мало информации',
  'Неверная информация',
  'Непонятный ответ',
  'Нет ответа на мой вопрос',
  'Другое',
];

export const initialQuestions = [];

export const AI_LOGO_POSITION_KEY = 'aiLogoPosition';

export const AGENT_ID_KEY = 'activeAgentId';

export const months = [
  'января',
  'февраля',
  'марта',
  'апреля',
  'мая',
  'июня',
  'июля',
  'августа',
  'сентября',
  'октября',
  'ноября',
  'декабря',
];
