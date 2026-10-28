import { TableProps } from 'antd/lib/table';
import { IPagination } from '../Pagination/IPagination';
import { IEmpty } from '../Empty';
export interface ITable<RecordType> extends Omit<TableProps<RecordType>, 'pagination'> {
  className?: string;
  /** Добавляет цветовой разделитель */
  darkRow?: boolean;
  borderRow?: boolean;
  /** Добавляет расстояние между границами ячеек в таблице по вертикали */
  borderSpacing?: 'small' | 'default' | 'large';
  headerType?: 'default' | 'shadow' | 'empty';
  /** Высота таблицы подстраивается под высоту свободного пространства, чтобы у страницы не было скролла */
  autoHeight?: boolean;
  tableMarginBottom?: number;
  pagePadding?: number;
  /** Дополнительные отступы без которых на некоторых экранах появляется скролл */
  otherPaddings?: number;
  emptyParams?: IEmpty;
  paginationParams?: IPagination | false;
}
