package ru.sber.transport.corporate_sync.cache.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.model.Filter;
import ru.sber.transport.corporate.business.model.HasOrganizationStructure;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.business.providers.OrganizationProvider;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.*;

@RequiredArgsConstructor
@Component
@Slf4j
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class OrganizationsCacheImpl implements OrganizationsCache {

    private final OrganizationProvider organizationProvider;

    @SuppressWarnings("Autowired")
    private final List<Provider<? extends HasOrganizationStructure, ? extends Filter>> providers;

    @SuppressWarnings("Autowired")
    private final Map<Class<? extends HasOrganizationStructure>, Map<String, HasOrganizationStructure>> data;

    private Organization organization;

    @Override
    public void organization(String organizationId) {
        this.organization = organizationProvider.get(organizationId).orElseThrow(() -> new EntityNotFoundException(Organization.class, Map.of("syncId", organizationId)));
        log.debug("Cache for {} created", organizationId);
    }

    @Override
    public void clear() {
        data.clear();
        if (organization == null) {
            return;
        }
        log.info("Cache for {} cleared", organization.getSyncId());
        organization = null;
    }

    @Override
    public UUID getOrganization() {
        return organization.getId();
    }

    @Override
    public <T extends HasOrganizationStructure, F extends Filter> Optional<T> get(Class<T> dataClass, Class<F> filterClass, String syncId) {
        var items = data.getOrDefault(dataClass, new HashMap<>());
        if (!items.containsKey(syncId)) {
            items.put(syncId, getProvider(dataClass, filterClass).get(organization.getId(), syncId).orElse(null));
            data.put(dataClass, items);
        }
        var item = items.get(syncId);
        return ReflectionUtils.cast(Optional.ofNullable(item));
    }

    @Override
    public <T extends HasOrganizationStructure> void put(Class<T> dataClass, String syncId, T element) {
        var item = data.getOrDefault(dataClass, new HashMap<>());
        item.put(syncId, element);
        data.put(dataClass, item);
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    private Provider<HasOrganizationStructure, Filter> getProvider(Class<? extends HasOrganizationStructure> dataClass, Class<? extends Filter> filterClass) {
        var type = ResolvableType.forClassWithGenerics(Provider.class, dataClass, filterClass);
        var bean = providers.parallelStream().filter(type::isInstance).findAny()
                .orElseThrow(() -> new NoSuchBeanDefinitionException("Provider<%s>".formatted(dataClass.getSimpleName())));
        return ReflectionUtils.cast(bean);
    }
}
