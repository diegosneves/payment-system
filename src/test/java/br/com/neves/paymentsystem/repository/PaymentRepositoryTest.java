package br.com.neves.paymentsystem.repository;

import br.com.neves.paymentsystem.enums.PaymentSource;
import br.com.neves.paymentsystem.enums.PaymentStatus;
import br.com.neves.paymentsystem.model.Payment;
import br.com.neves.paymentsystem.utils.DataConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PaymentRepositoryTest {
    
    @Autowired
    private PaymentRepository repository;
    
    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("should sum daily payments by payer id")
    void shouldSumDailyPaymentsByPayerId() {
        final var expectedSum = new BigDecimal("400.00");
        final UUID payerId = UUID.randomUUID();

        final var today = LocalDate.now();
        final var startOfDay =  DataConverter.BRAZIL.toInstant(today.atStartOfDay());
        final var endOfDay = DataConverter.BRAZIL.toInstant(today.plusDays(1).atStartOfDay());

        final var firstPayment = Payment.builder()
                .payerId(payerId)
                .paymentSource(PaymentSource.PIX)
                .amount(new BigDecimal("50.00"))
                .status(PaymentStatus.PENDING)
                .build();

        final var secondPayment = Payment.builder()
                .payerId(payerId)
                .paymentSource(PaymentSource.PIX)
                .amount(new BigDecimal("350.00"))
                .status(PaymentStatus.PENDING)
                .build();

        this.repository.saveAll(List.of(firstPayment, secondPayment));

        final var actual = this.repository.sumPaymentsByPayerIdAndDate(payerId, startOfDay, endOfDay);

        assertThat(actual)
                .isNotNull()
                .isEqualByComparingTo(expectedSum);

    }

    @Test
    @DisplayName("should not sum payments from different day")
    void shouldNotSumPaymentsFromDifferentDay() {
        final var expectedSum = new BigDecimal("50.00");
        final UUID payerId = UUID.randomUUID();

        final var today = LocalDate.now();
        final var startOfDay =  DataConverter.BRAZIL.toInstant(today.atStartOfDay());
        final var endOfDay = DataConverter.BRAZIL.toInstant(today.plusDays(1).atStartOfDay());
        final var yesterdayAtTen = DataConverter.BRAZIL.toInstant(today.atStartOfDay().minusDays(1).plusHours(10));

        final var todayPayment = Payment.builder()
                .payerId(payerId)
                .paymentSource(PaymentSource.PIX)
                .amount(new BigDecimal("50.00"))
                .status(PaymentStatus.PENDING)
                .build();

        final var anotherPayment = Payment.builder()
                .payerId(payerId)
                .paymentSource(PaymentSource.PIX)
                .amount(new BigDecimal("350.00"))
                .status(PaymentStatus.PENDING)
                .build();

        this.repository.saveAll(List.of(todayPayment, anotherPayment));
        this.entityManager.flush();

        this.entityManager.getEntityManager().createQuery("UPDATE Payment p SET p.createdAt = :createdAt WHERE p.id = :id")
                .setParameter("createdAt", yesterdayAtTen)
                .setParameter("id", anotherPayment.getId())
                .executeUpdate();
        this.entityManager.clear();


        final var actual = this.repository.sumPaymentsByPayerIdAndDate(payerId, startOfDay, endOfDay);

        assertThat(actual)
                .isNotNull()
                .isEqualByComparingTo(expectedSum);
    }

    @Test
    @DisplayName("Should return zero when there are no payments for the payerId on the day")
    void shouldReturnZeroWhenNoPayments() {
        final var expectedSum = new BigDecimal("0.00");
        final UUID payerId = UUID.randomUUID();

        final var today = LocalDate.now();
        final var startOfDay =  DataConverter.BRAZIL.toInstant(today.atStartOfDay());
        final var endOfDay = DataConverter.BRAZIL.toInstant(today.plusDays(1).atStartOfDay());

        final var actual = this.repository.sumPaymentsByPayerIdAndDate(payerId, startOfDay, endOfDay);

        assertThat(actual)
                .isNotNull()
                .isEqualByComparingTo(expectedSum);
    }

    @Test
    @DisplayName("Should find payments by payerId")
    void shouldFindPaymentsByPayerId() {
        final UUID payerId = UUID.randomUUID();
        final var firstPaymentAmount = new BigDecimal("50.00");
        final var secondPaymentAmount = new BigDecimal("350.00");

        final var firstPayment = Payment.builder()
                .payerId(payerId)
                .paymentSource(PaymentSource.PIX)
                .amount(firstPaymentAmount)
                .status(PaymentStatus.PENDING)
                .build();

        final var secondPayment = Payment.builder()
                .payerId(payerId)
                .paymentSource(PaymentSource.CREDIT_CARD)
                .amount(secondPaymentAmount)
                .status(PaymentStatus.PENDING)
                .build();

        this.repository.saveAll(List.of(firstPayment, secondPayment));

        final var actual = this.repository.findAllByPayerId(payerId);

        assertThat(actual)
                .isNotNull()
                .hasSize(2)
                        .satisfiesExactlyInAnyOrder(
                                first -> {
                                    assertThat(first.getPayerId())
                                            .isNotNull()
                                            .isEqualTo(payerId);
                                    assertThat(first.getAmount())
                                            .isNotNull()
                                            .isEqualByComparingTo(firstPaymentAmount);
                                    assertThat(first.getCreatedAt())
                                            .isNotNull();
                                    assertThat(first.getStatus())
                                            .isNotNull()
                                            .isEqualTo(PaymentStatus.PENDING);
                                    assertThat(first.getPaymentSource())
                                            .isNotNull()
                                            .isEqualTo(PaymentSource.PIX);
                                },
                                second -> {
                                    assertThat(second.getPayerId())
                                            .isNotNull()
                                            .isEqualTo(payerId);
                                    assertThat(second.getAmount())
                                            .isNotNull()
                                            .isEqualByComparingTo(secondPaymentAmount);
                                    assertThat(second.getCreatedAt())
                                            .isNotNull();
                                    assertThat(second.getStatus())
                                            .isNotNull()
                                            .isEqualTo(PaymentStatus.PENDING);
                                    assertThat(second.getPaymentSource())
                                            .isNotNull()
                                            .isEqualTo(PaymentSource.CREDIT_CARD);
                                }
                        );
    }

    @Test
    @DisplayName("Should return empty list when there are no payments for the payerId")
    void shouldReturnEmptyListForNonExistentPayerId() {
        final var actual = this.repository.findAllByPayerId(UUID.randomUUID());

        assertThat(actual)
                .isNotNull()
                .isEmpty();
    }

}
