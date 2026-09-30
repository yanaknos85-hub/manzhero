import { PaginationProps } from 'antd/lib/pagination';

export interface IPagination extends PaginationProps {
  pagination: { page: number; size: number };
  total: number;
  setPagination: (pagination: IPagination['pagination']) => void;
  className?: string;
}
