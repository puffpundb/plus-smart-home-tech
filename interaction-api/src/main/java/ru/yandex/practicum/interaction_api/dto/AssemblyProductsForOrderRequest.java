package ru.yandex.practicum.interaction_api.dto;

import java.util.Map;
import java.util.UUID;

public class AssemblyProductsForOrderRequest {
	private UUID orderId;
	private Map<UUID, Long> products;
}
