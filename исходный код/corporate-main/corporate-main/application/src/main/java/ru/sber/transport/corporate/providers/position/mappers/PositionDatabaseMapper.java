package ru.sber.transport.corporate.providers.position.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.providers.mappers.DatabaseMapper;
import ru.sber.transport.database.corporate.tables.records.PositionRecord;

import java.time.OffsetDateTime;

/**
 * Маппер бизнес сущности и базы данных.
 */
@Mapper(imports = OffsetDateTime.class)
public interface PositionDatabaseMapper extends DatabaseMapper<Position, PositionRecord> {

    /**
     * Конвертация модели в бизнес сущность.
     *
     * @param source исходная сущность.
     * @return бизнес сущность.
     */
    @Mapping(target = "noApproveRequired", source = "selfApproved")
    @Mapping(target = "structureType", source = "orgStructureType")
    @Mapping(target = "humanReadableId", source = "humanreadableid")
    Position toBusiness(PositionRecord source);

    /**
     * Обновление бизнес сущности.
     *
     * @param target целевая сущность.
     * @param source исходная сущность.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "qualifier", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "selfApproved", source = "noApproveRequired")
    @Mapping(target = "orgStructureType", source = "structureType")
    @Mapping(target = "humanreadableid", source = "humanReadableId")
    @Mapping(target = "status", source = "status", defaultValue = "ACTIVE")
    @Mapping(target = "updateTime", expression = "java(OffsetDateTime.now())")
    void update(@MappingTarget PositionRecord target, Position source);

    /**
     * Конвертация бизнес сущности в сущность базы данных.
     *
     * @param position бизнес сущность.
     * @return сущность базы данных.
     */
    @Mapping(target = "selfApproved", source = "noApproveRequired")
    @Mapping(target = "qualifier", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "orgStructureType", source = "structureType")
    @Mapping(target = "humanreadableid", source = "humanReadableId")
    @Mapping(target = "status", source = "status", defaultValue = "ACTIVE")
    @Mapping(target = "updateTime", expression = "java(OffsetDateTime.now())")
    PositionRecord toDatabase(Position position);

}
