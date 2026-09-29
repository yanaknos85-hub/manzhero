package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.corpclient.database.model.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
