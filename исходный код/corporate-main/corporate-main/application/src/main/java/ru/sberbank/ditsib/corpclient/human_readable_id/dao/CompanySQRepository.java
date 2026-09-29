package ru.sberbank.ditsib.corpclient.human_readable_id.dao;

import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.human_readable_id.model.CompanySQ;
import ru.sber.transport.humanreadableid.dao.AbstractRepository;


/**
 * CompanySQRepository repository
 */
@Repository
public interface CompanySQRepository extends AbstractRepository<CompanySQ> {

}
