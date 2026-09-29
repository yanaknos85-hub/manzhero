package ru.sberbank.ditsib.corpclient.mapper;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.Department;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка маппера подразделений")
class DepartmentMapperTest {

    private final DepartmentMapper mapper = new DepartmentMapperImpl(new ActiveStatusMapperImpl());

    @DisplayName("Проверка определения уровня подразделения")
    @Test
    void test_extractLevel() {
        var dep1 = Department.builder().name("1").build();
        var dep2 = Department.builder().name("2").parent(dep1).build();
        var dep3 = Department.builder().name("3").parent(dep2).build();
        var dep4 = Department.builder().name("4").parent(dep3).build();
        var dep5 = Department.builder().name("5").parent(dep4).build();
        var dep6 = Department.builder().name("6").parent(dep5).build();
        var dep7 = Department.builder().name("7").parent(dep6).build();
        var dep8 = Department.builder().name("8").parent(dep7).build();
        var dep9 = Department.builder().name("9").parent(dep8).build();
        var dep10 = Department.builder().name("10").parent(dep9).build();

        assertThat(mapper.extractLevel(dep1)).isEqualTo(1);
        assertThat(mapper.extractLevel(dep2)).isEqualTo(2);
        assertThat(mapper.extractLevel(dep3)).isEqualTo(3);
        assertThat(mapper.extractLevel(dep4)).isEqualTo(4);
        assertThat(mapper.extractLevel(dep5)).isEqualTo(5);
        assertThat(mapper.extractLevel(dep6)).isEqualTo(6);
        assertThat(mapper.extractLevel(dep7)).isEqualTo(7);
        assertThat(mapper.extractLevel(dep8)).isEqualTo(8);
        assertThat(mapper.extractLevel(dep9)).isEqualTo(9);
        assertThat(mapper.extractLevel(dep10)).isEqualTo(10);

        var actual = mapper.departmentToDTO(dep4);

        assertThat(actual.getName()).isEqualTo(dep4.getName());
        assertThat(actual.getLevel()).isEqualTo(4);
    }

}