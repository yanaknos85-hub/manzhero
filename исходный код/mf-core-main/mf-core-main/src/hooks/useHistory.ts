import { History, createBrowserHistory } from 'history';

export const useHistory: () => History = (
  (history: History): (() => History) => (): History => history
)(createBrowserHistory());
