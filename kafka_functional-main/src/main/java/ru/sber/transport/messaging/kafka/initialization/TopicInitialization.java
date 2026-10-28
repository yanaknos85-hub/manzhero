package ru.sber.transport.messaging.kafka.initialization;

import ru.sber.transport.messaging.kafka.binding.OutputBridge;

/**
 * Интерфейс инициализации канала данных.
 */
public interface TopicInitialization {
    
    /**
     * Имя канала для инициализации.
     *
     * @return канал.
     */
    OutputBridge channel();
    
    /**
     * Инициализация канала.
     */
    void initialize();
    
}
