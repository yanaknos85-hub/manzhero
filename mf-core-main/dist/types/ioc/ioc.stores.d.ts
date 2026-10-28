import { Container } from 'inversify';
import { IRootStore } from '../types/types';
export declare const rootContainer: Container;
export declare const initRootStore: (rootContainer: Container) => IRootStore;
