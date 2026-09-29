package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binding.BindingsLifecycleController;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.service.BinderControlService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
class BinderControlServiceImpl implements BinderControlService {

    private final BindingsLifecycleController lifecycleController;

    @Override
    public void start(String bindingName, Consumer<?, ?> consumer) {
        changeConsumerState(bindingName, true);
        if (consumer != null) {
            consumer.resume(consumer.assignment());
        }
    }

    @Override
    public void stop(String bindingName, Consumer<?, ?> consumer) {
        if (consumer != null) {
            consumer.pause(consumer.assignment());
        }
        changeConsumerState(bindingName, false);
    }

    private List<Binding<?>> getBinding(String bindingName) {
        List<Binding<?>> binding;
        do {
            binding = lifecycleController.queryState(bindingName);
        } while (binding.isEmpty());
        return binding;
    }

    private void changeConsumerState(String bindingName, boolean start) {
        for (var query : getBinding(bindingName)) {
            if (query.isPaused() && start) {
                query.resume();
                log.info("Consumer of binding %s resumed".formatted(bindingName));
            } else if (query.isRunning() && !start) {
                query.pause();
                log.info("Consumer of binding %s paused".formatted(bindingName));
            }
        }
    }
}
