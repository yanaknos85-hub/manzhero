package ru.sber.ditsib.transport.humanreadableid.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.humanreadableid.model.Prefix;
import ru.sber.transport.humanreadableid.service.HumanReadbaleIdFormatter;
import ru.sber.transport.humanreadableid.service.impl.HumanReadableIdFormatterImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("lib_human_readable_generator")
class HumanReadableIdFormatterImplTest {
    
    
    HumanReadbaleIdFormatter humanReadbaleIdFormatter = new HumanReadableIdFormatterImpl();
    
    @Test
    void format() {
        assertEquals("OT-0099-00000666", humanReadbaleIdFormatter.format(Prefix.OT, 99L, 666L));
    }
}