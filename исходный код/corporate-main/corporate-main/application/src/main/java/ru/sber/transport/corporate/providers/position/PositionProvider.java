package ru.sber.transport.corporate.providers.position;

import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.business.model.PositionFilter;
import ru.sber.transport.corporate.business.providers.Provider;

/**
 * Провайдер данных о должностях
 */
public interface PositionProvider extends Provider<Position, PositionFilter> {
}