package ru.yandex.practicum.payment.mapper;

import ru.yandex.practicum.interaction_api.dto.PaymentDto;
import ru.yandex.practicum.payment.entity.Payment;

public class PaymentMapper {
	public static PaymentDto toDto(Payment payment) {
		if (payment == null) return null;

		return PaymentDto.builder()
				.paymentId(payment.getPaymentId())
				.totalPayment(payment.getTotalPayment())
				.deliveryTotal(payment.getDeliveryTotal())
				.feeTotal(payment.getFeeTotal())
				.build();
	}

	public static Payment toEntity(PaymentDto dto) {
		if (dto == null) return null;
		return Payment.builder()
				.paymentId(dto.getPaymentId())
				.totalPayment(dto.getTotalPayment())
				.deliveryTotal(dto.getDeliveryTotal())
				.feeTotal(dto.getFeeTotal())
				.build();
	}
}
