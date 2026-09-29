package ru.sberbank.ditsib.transport.limits.config;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.hibernate.HibernateException;
import org.hibernate.event.internal.DefaultDeleteEventListener;
import org.hibernate.event.internal.DefaultMergeEventListener;
import org.hibernate.event.internal.DefaultPersistEventListener;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.DeleteEvent;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.internal.SessionImpl;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.LimitStatsRefreshService;

import java.util.Optional;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class HibernateEventsConfiguration implements BeanPostProcessor {

    public HibernateEventsConfiguration(EntityManager entityManager, LimitStatsRefreshService limitStatsRefreshService) {
        try (var executor = Executors.newSingleThreadExecutor(new BasicThreadFactory.Builder().namingPattern("build-%s").build())) {
            executor.submit(() -> {
                log.debug("All components found. Init refresh mechanism");
                var sessionFactory = ((SessionImpl) entityManager.getDelegate()).getSessionFactory();

                Optional.ofNullable(sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class))
                    .ifPresent(eventListenerRegistry -> {

                        eventListenerRegistry.getEventListenerGroup(EventType.MERGE).prependListener(new DefaultMergeEventListener() {
                            @Override
                            public void onMerge(MergeEvent event) throws HibernateException {
                                super.onMerge(event);
                                refreshViews(event.getEntity(), limitStatsRefreshService);
                            }
                        });
                        eventListenerRegistry.getEventListenerGroup(EventType.PERSIST).prependListener(new DefaultPersistEventListener() {
                            @Override
                            public void onPersist(PersistEvent event) throws HibernateException {
                                super.onPersist(event);
                                refreshViews(event.getObject(), limitStatsRefreshService);
                            }
                        });
                        eventListenerRegistry.getEventListenerGroup(EventType.DELETE).prependListener(new DefaultDeleteEventListener() {
                            @Override
                            public void onDelete(DeleteEvent event) throws HibernateException {
                                super.onDelete(event);
                                refreshViews(event.getObject(), limitStatsRefreshService);
                            }
                        });
                    });
            });
        }
    }

    private void refreshViews(Object entity, LimitStatsRefreshService limitStatsRefreshService) {
        var isLimit = entity instanceof Limit;
        var isSharing = entity instanceof LimitSharing;
        var isPerPeriod = entity instanceof LimitSharingPerPeriod;
        if (isLimit || isSharing || isPerPeriod) {
            limitStatsRefreshService.refresh(LimitStatistic.class)
                    .thenComposeAsync(refresh -> limitStatsRefreshService.refresh(LimitStatisticSpending.class));
        } else if (entity instanceof LimitSpending) {
            limitStatsRefreshService.refresh(LimitStatisticSpending.class);
        }
    }

}
