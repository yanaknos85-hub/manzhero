package ru.sber.transport.integrations.dto;

import java.util.List;

public record ContractorInfoRequest(
        String url,
        String login,
        String password,
        List<String> orderPartnerIds
) {
}
