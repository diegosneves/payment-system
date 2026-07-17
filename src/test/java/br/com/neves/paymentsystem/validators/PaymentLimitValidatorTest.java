package br.com.neves.paymentsystem.validators;


import br.com.neves.paymentsystem.exceptions.PaymentLimitException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentLimitValidatorTest {

    @Test
    @DisplayName("should validate payment limit")
    void shouldValidatePaymentLimit() {
        final var amount = new BigDecimal("500.00");

        final Boolean actual = PaymentLimitValidator.isWithinLimit(amount);

        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("should validate payment limit when amount is greater than limit")
    void shouldValidatePaymentLimitWhenAmountIsGreaterThanLimit() {
        final var amount = new BigDecimal("3000.00");

        final Boolean actual = PaymentLimitValidator.isWithinLimit(amount);

        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("should validate payment limit when amount is null")
    void shouldValidatePaymentLimitWhenAmountIsNull(){
        final Boolean actual = PaymentLimitValidator.isWithinLimit(null);

        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("should validate payment limit when amount is zero")
    void shouldNotAcceptWhenAmountIsZero(){
        final var amount = BigDecimal.ZERO;
        final var expectedErrorMessage = "Amount must be greater than zero";

        assertThatThrownBy(() -> PaymentLimitValidator.isWithinLimit(amount))
                .isInstanceOf(PaymentLimitException.class)
                .hasMessage(expectedErrorMessage)
                .asInstanceOf(InstanceOfAssertFactories.type(PaymentLimitException.class))
                .extracting(PaymentLimitException::getErrors)
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .hasSize(1);
    }

    @Test
    @DisplayName("should validate payment limit when amount is less zero")
    void shouldNotAcceptNegativeAmount(){
        final var amount = new BigDecimal("-50.00");
        final var expectedErrorMessage = "Amount must be greater than zero";

        assertThatThrownBy(() -> PaymentLimitValidator.isWithinLimit(amount))
                .isInstanceOf(PaymentLimitException.class)
                .hasMessage(expectedErrorMessage)
                .asInstanceOf(InstanceOfAssertFactories.type(PaymentLimitException.class))
                .extracting(PaymentLimitException::getErrors)
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .hasSize(1);
    }

    @Test
    @DisplayName("Payment should be successful when amount is below the daily limit")
    void shouldAcceptAmountBelowTheLimit() {
        BigDecimal amount = new BigDecimal("1999.99");
        assertThat(PaymentLimitValidator.isWithinLimit(amount)).isTrue();
    }

    @Test
    @DisplayName("Payment should be successful when amount equal the daily limit")
    void shouldAcceptAmountEqualToTheLimit() {
        BigDecimal amount = new BigDecimal("2000.00");
        assertThat(PaymentLimitValidator.isWithinLimit(amount)).isTrue();
    }

    @Test
    @DisplayName("Payment should not be successful when amount is higher than the daily limit")
    void shouldNotAcceptAmountHigherThanTheLimit() {
        BigDecimal amount = new BigDecimal("2000.01");
        assertThat(PaymentLimitValidator.isWithinLimit(amount)).isFalse();
    }


}
