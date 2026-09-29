package ru.sberbank.ditsib.corpclient.database.model.docs;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Ведение информации об авторе и редакторе пользовательских документов")
class EmployeeDocumentTest {

    @Test
    @DisplayName("Обновление информации об авторе в новом документе")
    void updateAuthorInfo_newDocument() {
        var ed = new EmployeeDocument();
        var currentUser = UUID.randomUUID();
        ed.updateAuthorInfo(currentUser);

        assertNotNull(ed.getCreationUser());
        assertEquals(currentUser, ed.getCreationUser());
        assertNotNull(ed.getCreationTime());
    }

    @Test
    @DisplayName("Обновление информации об авторе в существующем документе")
    void updateAuthorInfo_changeDocument() {
        var ed = new EmployeeDocument();
        var currentUser = UUID.randomUUID();
        ed.updateAuthorInfo(currentUser);

        var otherUser = UUID.randomUUID();
        ed.updateAuthorInfo(otherUser);

        assertNotNull(ed.getCreationUser());
        assertEquals(currentUser, ed.getCreationUser());
        assertNotNull(ed.getCreationTime());

        assertNotNull(ed.getChangeUser());
        assertEquals(otherUser, ed.getChangeUser(),
                "Автор изменения должен записываться в отдельный атрибут, чтобы сохранить информацию о первоначальном авторе");
        assertNotNull(ed.getChangeTime(),
                "Время изменения должно записываться в отдельный атрибут, чтобы сохранить информацию о первоначальном времени создания");
    }

}