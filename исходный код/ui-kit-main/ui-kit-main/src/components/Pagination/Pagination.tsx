import React, { useState, useEffect, useCallback } from 'react';
import { Pagination as PaginationAntd } from 'antd';
import cn from 'classnames';

import { IPagination } from './IPagination';
import { Select } from '../Select';
import { ReactComponent as ArrowIcon } from '../../icons/arrow.svg';
import styles from './Pagination.module.scss';

const Pagination: React.FC<IPagination> = ({
  total,
  pagination,
  setPagination,
  className,
  disabled,
  hideOnSinglePage = true,
  showSizeChanger = true,
  pageSizeOptions = [10, 20, 50, 100],
  ...props
}) => {
  const [isOpenSelect, setIsOpenSelect] = useState(false);

  const onClose = useCallback(() => {
    setIsOpenSelect(false);
  }, []);

  useEffect(() => {
    document.removeEventListener('click', onClose);

    if (isOpenSelect) {
      document.addEventListener('click', onClose);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpenSelect]);

  useEffect(() => () => {
    document.removeEventListener('click', onClose);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const toggleSelect = () => setIsOpenSelect(is => !is);

  const handleChangePage = (page: number) => {
    setPagination({ ...pagination, page: page - 1 });
  };

  const handleChangeSize = (size: number) => {
    setPagination({ page: 0, size });
  };

  const itemRender: IPagination['itemRender'] = (_, type, originalElement) => {
    if (type === 'prev' || type === 'next') {
      return (
        <button
          className="sbt-kit-pagination-item-link"
          type="button"
          tabIndex={-1}
        >
          <ArrowIcon />
        </button>
      );
    }

    return originalElement;
  };

  const selectOptions = pageSizeOptions.map(option => ({ value: option }));

  return (
    <div className={cn(styles.paginationContainer, [className])}>
      {showSizeChanger && (
        <div
          className={cn(styles.sizeSelector, {
            [styles.disabledSelector]: disabled,
          })}
          role="button"
          tabIndex={-1}
          onKeyDown={toggleSelect}
          onClick={toggleSelect}
        >
          <span className={styles.selectDescription}>Показывать по</span>
          <Select
            open={isOpenSelect}
            size="small"
            disabled={disabled}
            className={styles.select}
            options={selectOptions}
            defaultValue={selectOptions[0].value}
            value={pagination.size}
            onChange={handleChangeSize}
          />
        </div>
      )}
      <PaginationAntd
        className={styles.pagination}
        pageSize={pagination.size}
        current={pagination.page + 1}
        total={total}
        hideOnSinglePage={hideOnSinglePage}
        disabled={disabled}
        onChange={handleChangePage}
        itemRender={itemRender}
        {...props}
        showSizeChanger={false}
      />
    </div>
  );
};

export default Pagination;
