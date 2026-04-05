package ru.yandex.practicum.shopping_store.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortField {

	PRODUCT_NAME("productName"),
	PRICE("price"),
	PRODUCT_CATEGORY("productCategory"),
	PRODUCT_STATE("productState");

	private final String fieldName;

	public static SortField fromString(String value) {
		for (SortField field : values()) {
			if (field.fieldName.equalsIgnoreCase(value)) {
				return field;
			}
		}
		throw new IllegalArgumentException("Invalid sort field: " + value);
	}
}