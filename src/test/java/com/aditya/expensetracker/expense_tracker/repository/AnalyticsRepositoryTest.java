package com.aditya.expensetracker.expense_tracker.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.aditya.expensetracker.expense_tracker.config.JpaAuditingConfig;
import com.aditya.expensetracker.expense_tracker.entity.Category;
import com.aditya.expensetracker.expense_tracker.entity.Expense;
import com.aditya.expensetracker.expense_tracker.entity.ExpenseType;
import com.aditya.expensetracker.expense_tracker.entity.Role;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.repository.projection.CategorySummaryProjection;
import com.aditya.expensetracker.expense_tracker.repository.projection.MonthlySummaryProjection;
import com.aditya.expensetracker.expense_tracker.service.AgentApiTokenService;
import com.aditya.expensetracker.expense_tracker.support.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaAuditingConfig.class, AnalyticsRepositoryTest.TestAuditingConfig.class})
class AnalyticsRepositoryTest extends AbstractIntegrationTest {

    @TestConfiguration
    static class TestAuditingConfig {

        @Bean
        public AuditorAware<Long> springSecurityAuditorAware() {
            return Optional::empty;
        }
    }

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @MockitoBean
    private AgentApiTokenService tokenService;

    private User user;

    private User persistUser() {

        User newUser = User.builder()
                .name("Test User")
                .email("test-" + System.nanoTime() + "@example.com")
                .password("irrelevant")
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(newUser);
    }

    private Expense persistExpense(
            User owner,
            BigDecimal amount,
            ExpenseType type,
            Category category,
            LocalDate date
    ) {

        Expense expense = Expense.builder()
                .title("test expense")
                .amount(amount)
                .type(type)
                .category(category)
                .expenseDate(date)
                .user(owner)
                .build();

        return entityManager.persistAndFlush(expense);
    }

    @Test
    void getMonthlySummary_groupsByCalendarMonth_andReturnsRealLocalDates() {

        user = persistUser();

        persistExpense(
                user, new BigDecimal("100.00"), ExpenseType.EXPENSE,
                Category.FOOD, LocalDate.of(2026, 7, 5));

        persistExpense(
                user, new BigDecimal("50.00"), ExpenseType.EXPENSE,
                Category.FOOD, LocalDate.of(2026, 7, 20));

        persistExpense(
                user, new BigDecimal("200.00"), ExpenseType.EXPENSE,
                Category.TRANSPORT, LocalDate.of(2026, 8, 2));

        entityManager.flush();
        entityManager.clear();

        List<MonthlySummaryProjection> rows = analyticsRepository.getMonthlySummary(
                user.getId(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                null,
                null
        );

        assertThat(rows).hasSize(2);

        MonthlySummaryProjection july = rows.stream()
                .filter(row -> row.getMonth().equals(LocalDate.of(2026, 7, 1)))
                .findFirst()
                .orElseThrow();

        assertThat(july.getExpense()).isEqualByComparingTo("150.00");

        MonthlySummaryProjection august = rows.stream()
                .filter(row -> row.getMonth().equals(LocalDate.of(2026, 8, 1)))
                .findFirst()
                .orElseThrow();

        assertThat(august.getExpense()).isEqualByComparingTo("200.00");
    }

    @Test
    void softDeletedExpense_excludedFromJpqlAndNativeAnalyticsQueries() {

        user = persistUser();

        Expense keep = persistExpense(
                user, new BigDecimal("40.00"), ExpenseType.EXPENSE,
                Category.FOOD, LocalDate.of(2026, 7, 10));

        Expense toDelete = persistExpense(
                user, new BigDecimal("999.00"), ExpenseType.EXPENSE,
                Category.SHOPPING, LocalDate.of(2026, 7, 12));

        entityManager.flush();

        expenseRepository.delete(toDelete);
        entityManager.flush();
        entityManager.clear();

        List<CategorySummaryProjection> categories = analyticsRepository.getCategorySummary(
                user.getId(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                null,
                ExpenseType.EXPENSE
        );

        assertThat(categories)
                .extracting(CategorySummaryProjection::getCategory)
                .containsExactly("FOOD");

        List<MonthlySummaryProjection> monthly = analyticsRepository.getMonthlySummary(
                user.getId(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                null,
                null
        );

        assertThat(monthly).hasSize(1);
        assertThat(monthly.get(0).getExpense()).isEqualByComparingTo("40.00");

        assertThat(expenseRepository.findById(keep.getId())).isPresent();
        assertThat(expenseRepository.findById(toDelete.getId())).isEmpty();
    }
}