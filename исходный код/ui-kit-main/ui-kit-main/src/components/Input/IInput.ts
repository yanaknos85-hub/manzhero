import { InputProps } from 'antd/lib/input/Input';

export type { InputRef } from 'antd/lib/input/Input';

export type Mode = 'default' | 'search';

export type IInput = InputProps & { mode?: Mode };
