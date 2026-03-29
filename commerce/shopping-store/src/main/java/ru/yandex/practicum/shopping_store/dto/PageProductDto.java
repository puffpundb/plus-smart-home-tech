package ru.yandex.practicum.shopping_store.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageProductDto {

	private long totalElements;
	private int totalPages;
	private boolean first;
	private boolean last;
	private int size;
	private List<ProductDto> content;
	private int number;
	private List<SortObject> sort;
	private int numberOfElements;
	private PageableObject pageable;
	private boolean empty;

	@Data
	@Builder
	public static class SortObject {
		private String direction;
		private String nullHandling;
		private boolean ascending;
		private String property;
		private boolean ignoreCase;
	}

	@Data
	@Builder
	public static class PageableObject {
		private long offset;
		private List<SortObject> sort;
		private boolean unpaged;
		private boolean paged;
		private int pageNumber;
		private int pageSize;
	}
}