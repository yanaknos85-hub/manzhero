package ru.sberbank.ditsib.corpclient.grpc;

import ru.sber.transport.corporate.sync.grpc.service.State;

/**
 * GRPC-клиент для получения данных о сотруднике из ЕАСУП
 * */
public interface EasupEmployeeGrpcClient {

    /**
     * Получение данных о сотруднике по ТН
     *
     * @param personnelNumber   табельный номер сотрудника
     * @return данные сотрудника
     * */
    State.Employee getEmployee(String personnelNumber);
}
