package ru.yandex.practicum.shopping_store.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.shopping_store.dto.PageProductDto;
import ru.yandex.practicum.shopping_store.dto.ProductDto;
import ru.yandex.practicum.shopping_store.entity.ProductEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ShoppingStoreMapper {

	public static ProductDto toProductDto(ProductEntity pe) {
		if (pe == null) return null;

		return ProductDto.builder()
				.productId(pe.getId())
				.productName(pe.getProductName())
				.description(pe.getDescription())
				.imageSrc(pe.getImageSrc())
				.quantityState(pe.getQuantityState())
				.productState(pe.getProductState())
				.productCategory(pe.getProductCategory())
				.price(pe.getPrice())
				.build();
	}

	public static ProductEntity toProductEntity(ProductDto pd) {
		if (pd == null) return null;

		return ProductEntity.builder()
				.id(pd.getProductId())
				.productName(pd.getProductName())
				.description(pd.getDescription())
				.imageSrc(pd.getImageSrc())
				.quantityState(pd.getQuantityState())
				.productState(pd.getProductState())
				.productCategory(pd.getProductCategory())
				.price(pd.getPrice())
				.build();
	}

	public static PageProductDto toPageProductDtoFromEntity(Page<ProductEntity> page) {
		List<ProductDto> dtoContent = page.getContent().stream()
				.map(ShoppingStoreMapper::toProductDto)
				.collect(Collectors.toList());

		List<PageProductDto.SortObject> sortObjects = page.getSort()
				.stream()
				.map(ShoppingStoreMapper::toSortObject)
				.collect(Collectors.toList());

		PageProductDto.PageableObject pageableObject = PageProductDto.PageableObject.builder()
				.offset(page.getPageable().getOffset())
				.sort(sortObjects)
				.unpaged(page.getPageable().isUnpaged())
				.paged(page.getPageable().isPaged())
				.pageNumber(page.getNumber())
				.pageSize(page.getSize())
				.build();

		return PageProductDto.builder()
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.first(page.isFirst())
				.last(page.isLast())
				.size(page.getSize())
				.content(dtoContent)
				.number(page.getNumber())
				.sort(sortObjects)
				.numberOfElements(page.getNumberOfElements())
				.pageable(pageableObject)
				.empty(page.isEmpty())
				.build();
	}

	public static PageProductDto toPageProductDto(Page<ProductDto> page) {
		List<PageProductDto.SortObject> sortObjects = page.getSort()
				.stream()
				.map(ShoppingStoreMapper::toSortObject)
				.collect(Collectors.toList());

		PageProductDto.PageableObject pageableObject = PageProductDto.PageableObject.builder()
				.offset(page.getPageable().getOffset())
				.sort(sortObjects)
				.unpaged(page.getPageable().isUnpaged())
				.paged(page.getPageable().isPaged())
				.pageNumber(page.getNumber())
				.pageSize(page.getSize())
				.build();

		return PageProductDto.builder()
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.first(page.isFirst())
				.last(page.isLast())
				.size(page.getSize())
				.content(page.getContent())
				.number(page.getNumber())
				.sort(sortObjects)
				.numberOfElements(page.getNumberOfElements())
				.pageable(pageableObject)
				.empty(page.isEmpty())
				.build();
	}

	private static PageProductDto.SortObject toSortObject(Sort.Order order) {
		return PageProductDto.SortObject.builder()
				.direction(order.getDirection().name())
				.nullHandling(order.getNullHandling().name())
				.ascending(order.isAscending())
				.property(order.getProperty())
				.ignoreCase(order.isIgnoreCase())
				.build();
	}
}