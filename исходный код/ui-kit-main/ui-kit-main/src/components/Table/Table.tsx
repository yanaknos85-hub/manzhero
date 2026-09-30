import React, { useEffect, useRef, useState } from 'react';
import { Table as TableAntd } from 'antd';
import { AnyObject } from 'antd/lib/_util/type';
import cn from 'classnames';

import { Pagination } from '../Pagination';
import { Empty } from '../Empty';
import { ITable } from './ITable';
import styles from './Table.module.scss';

function Table<T extends AnyObject>({
  className,
  rowClassName,
  scroll,
  darkRow,
  autoHeight,
  tableMarginBottom = 10,
  pagePadding = 16,
  otherPaddings = 1,
  emptyParams,
  paginationParams,
  borderRow = true,
  borderSpacing,
  headerType,
  ...props
}: ITable<T>) {
  const tableWrapper = useRef<HTMLDivElement>(null);
  const paginationWrapper = useRef<HTMLDivElement>(null);

  const [tableHeight, setTableHeight] = useState(0);
  const [paginationConfig, setPaginationConfig] = useState({ page: 0, size: 10 });

  const getClientHeight = () => tableWrapper.current?.querySelector('.sbt-kit-table-header')?.clientHeight || 0;

  useEffect(() => {
    if (!autoHeight) return;

    const resize = () => {
      const scrollTop = window.scrollY || document.documentElement.scrollTop;
      const topOffset = tableWrapper.current?.getBoundingClientRect().top ?? 0 + scrollTop;
      const viewportHeight = window.innerHeight;
      const paginationHeight = paginationWrapper.current?.offsetHeight || 0;
      const antdTableHeaderHeight = getClientHeight();

      setTableHeight(
        viewportHeight
        - topOffset
        - antdTableHeaderHeight
        - paginationHeight
        - tableMarginBottom
        - pagePadding
        - otherPaddings
      );
    };

    resize();

    window.addEventListener('resize', resize);

    return () => {
      window.removeEventListener('resize', resize);
    };
  }, [autoHeight, tableMarginBottom, pagePadding, otherPaddings]);

  const antdTableHeaderHeight = getClientHeight();

  return (
    <>
      <div ref={tableWrapper}>
        <TableAntd
          locale={{
            emptyText() {
              return <Empty description="Нет данных" {...emptyParams} />;
            },
          }}
          {...props}
          pagination={
            paginationParams === undefined && {
              current: paginationConfig.page + 1,
              pageSize: paginationConfig.size,
            }
          }
          className={cn(styles.table, [className], {
            [styles.borderRow]: borderRow,
            [styles[`borderSpacing_${borderSpacing}`]]: borderSpacing,
            [styles[`headerType_${headerType}`]]: headerType,
          })}
          rowClassName={(record, index, indent) => {
            let className = '';
            if (darkRow && index % 2 === 1) {
              className += styles.darkRow;
            }

            if (rowClassName) {
              if (typeof rowClassName === 'function') {
                className += ` ${rowClassName(record, index, indent)}`;
              } else if (typeof rowClassName === 'string') {
                className += ` ${rowClassName}`;
              }
            }
            return className;
          }}
          scroll={{
            ...scroll,
            y: autoHeight ? tableHeight || 'auto' : scroll?.y,
          }}
          style={{
            minHeight: autoHeight ? tableHeight + antdTableHeaderHeight : 'none',
            marginBottom: tableMarginBottom,
            ...props.style,
          }}
        />
      </div>
      <div ref={paginationWrapper}>
        {paginationParams !== false && (
          <Pagination
            {...paginationParams}
            total={paginationParams?.total || props.dataSource?.length || 0}
            pagination={paginationParams?.pagination ?? paginationConfig}
            setPagination={paginationParams?.setPagination ?? setPaginationConfig}
          />
        )}
      </div>
    </>
  );
}

export default Table;
