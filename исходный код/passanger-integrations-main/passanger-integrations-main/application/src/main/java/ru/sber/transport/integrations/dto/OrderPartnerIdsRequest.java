package ru.sber.transport.integrations.dto;

import java.util.List;

public record OrderPartnerIdsRequest(
        List<String> orderPartnerIds
) {
}
