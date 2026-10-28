/* eslint-disable no-console */
import React from 'react';
import { message, notification } from 'antd';
import Paragraph from 'antd/lib/typography/Paragraph';
import { inject, injectable } from 'inversify';
import cn from 'classnames';

import type { IConfigStore } from 'stores/Config/Config.interface';

import { useMenuResizingSpy } from '../../hooks/useMenuResizingsSpy';
import { ReactComponent as CloseIcon } from '../../assets/closeIcon.svg';
import Cat from '../../assets/cat.png';
import { SDO_SUPPORT_PHONE, SUPPORT_PHONE } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';

import { Console, ILogger, Notify } from './Logger.interface';

import styles from './logger.module.scss';

let nextKey = 0;

@injectable()
export class CommonLogger implements ILogger {
  notificationKeys: string[] = [];

  @inject(TYPES.IConfigStore)
  private configStore!: IConfigStore;

  toConsole = (type: Console, value: unknown): void => {
    console[type](value);
  };

  toConsoleGroup = (type: Console, description: string, title: string, ...args: any[]): void => {
    console.group(title);
    console[type](description);
    if (args.length > 0) {
      args.forEach(x => console.info(x));
    }
    console.groupEnd();
  };

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  toNotify = (type: Notify, description: string, title: string, duration = 5, code?: number): void => {
    const phone = this.configStore.env.IS_SDO ? SDO_SUPPORT_PHONE : SUPPORT_PHONE;
    const sdoLimitedAccess = this.configStore.env.IS_SDO && code === 403;

    const renderNotifyActions = () => (
      <div className={styles.actions}>
        <button className={styles.closeBtn} onClick={this.closeAll}>
          Закрыть все...
        </button>
      </div>
    );

    const descriptionNode = (description: string) => (
      <div className={cn({ [styles.desc]: type === 'error' })}>
        {description}

        {type === 'error' ? (
          <>
            <a href={`tel:${phone.code}`} className={styles.phone}>{phone.title}</a>
            <footer className={styles.footer}>
              <Paragraph type="secondary" className={styles.code}>{code && `Ошибка ${code}`}</Paragraph>
              {this.notificationKeys.length > 1 && renderNotifyActions()}
            </footer>
          </>
        ) : renderNotifyActions()}
      </div>
    );

    const logToConsole = description.length > 100;
    if (logToConsole) {
      this.toConsoleGroup('warn', description, title);
    }

    const key = `${nextKey++}`;
    this.notificationKeys.push(key);

    notification[type]({
      key,
      message: title,
      description: !sdoLimitedAccess && descriptionNode(logToConsole ? `${description.slice(0, 100)}... look to console` : description),
      duration,
      icon: type === 'error' ? (
        <img
          src={Cat}
          alt="Сберкот"
          className={styles.icon}
        />
      ) : undefined,
      className: cn(styles.logger, {
        [styles.error]: type === 'error',
      }),
      closeIcon: <CloseIcon />,
    });
  };

  toMessage = (type: Notify, description: string): void => {
    message.config({ top: 100 });
    message[type](description);

    useMenuResizingSpy();
  };

  toError(description: string, title: string, ...args: any[]): void {
    // this.toNotify('error', description, title); // в консоль надо смотреть
    this.toConsoleGroup('error', description, title, ...args);
  }

  closeAll = (): void => {
    const notificationsToClose = [...this.notificationKeys];
    this.notificationKeys.length = 0;

    const closeLastNotification = (): void => {
      const key = notificationsToClose.pop();
      if (key) {
        requestAnimationFrame(() => {
          notification.close(key);
          if (notificationsToClose.length) {
            closeLastNotification();
          }
        });
      }
    };

    closeLastNotification();
  };
}
