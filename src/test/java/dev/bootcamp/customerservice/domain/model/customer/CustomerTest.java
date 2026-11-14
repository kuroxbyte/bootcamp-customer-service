package dev.bootcamp.customerservice.domain.model.customer;

import dev.bootcamp.customerservice.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    private static Customer personal() {
        Customer customer = new Customer();
        customer.setName("Ana");
        customer.setType(CustomerType.PERSONAL);
        customer.setProfile(CustomerProfile.STANDARD);
        return customer;
    }

    @Test
    void updatesNameAndProfileOfTheSameType() {
        Customer customer = personal();

        customer.update("Ana Torres", CustomerProfile.VIP);

        assertThat(customer.getName()).isEqualTo("Ana Torres");
        assertThat(customer.getProfile()).isEqualTo(CustomerProfile.VIP);
    }

    @Test
    void rejectsEmptyNameOrProfileOfAnotherType() {
        Customer customer = personal();

        assertThatThrownBy(() -> customer.update(" ", CustomerProfile.VIP))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> customer.update(null, CustomerProfile.VIP))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> customer.update("Ana", CustomerProfile.PYME))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(customer.getProfile()).isEqualTo(CustomerProfile.STANDARD);
    }

    @Test
    void closingKeepsTheDate() {
        Customer customer = personal();
        assertThat(customer.isClosed()).isFalse();

        customer.close(LocalDate.of(2026, 11, 16));

        assertThat(customer.isClosed()).isTrue();
        assertThat(customer.getClosedAt()).isEqualTo(LocalDate.of(2026, 11, 16));
    }
}
