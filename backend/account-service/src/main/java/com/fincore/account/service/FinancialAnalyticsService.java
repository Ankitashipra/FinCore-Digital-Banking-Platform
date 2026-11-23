package com.fincore.account.service;

import com.fincore.account.dto.*;
import com.fincore.account.entity.*;
import com.fincore.account.repository.AccountRepository;
import com.fincore.account.repository.FinancialTransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FinancialAnalyticsService {

    private final AccountRepository accountRepository;
    private final FinancialTransactionRepository transactionRepository;

    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MMM yyyy");

    public FinancialAnalyticsService(
            AccountRepository accountRepository,
            FinancialTransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public FinancialAnalyticsResponse getAnalytics(
            String ownerId, int months) {

        if (months < 1 || months > 24) {
            throw new IllegalArgumentException(
                    "Months must be between 1 and 24");
        }

        UUID userId = UUID.fromString(ownerId);

        List<UUID> accountIds = accountRepository
                .findByOwnerUserId(userId)
                .stream()
                .map(Account::getId)
                .toList();

        if (accountIds.isEmpty()) {
            return emptyResponse();
        }

        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(months - 1);

        LocalDateTime startDate = firstMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = currentMonth.plusMonths(1)
                .atDay(1).atStartOfDay();

        List<FinancialTransaction> transactions =
                transactionRepository.findAnalyticsTransactions(
                        accountIds, startDate, endDate);

        Map<YearMonth, BigDecimal> monthlyIncome = new TreeMap<>();
        Map<YearMonth, BigDecimal> monthlyExpenses = new TreeMap<>();

        Map<String, BigDecimal> categoryAmounts = new HashMap<>();
        Map<String, Long> categoryCounts = new HashMap<>();

        for (int i = 0; i < months; i++) {
            YearMonth month = firstMonth.plusMonths(i);
            monthlyIncome.put(month, BigDecimal.ZERO);
            monthlyExpenses.put(month, BigDecimal.ZERO);
        }

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        long transactionCount = 0;

        for (FinancialTransaction transaction : transactions) {

            boolean ownedSource = transaction.getFromAccountId() != null
                    && accountIds.contains(transaction.getFromAccountId());

            boolean ownedDestination = transaction.getToAccountId() != null
                    && accountIds.contains(transaction.getToAccountId());

            YearMonth month = YearMonth.from(
                    transaction.getCreatedAt());

            if (!monthlyIncome.containsKey(month)) {
                continue;
            }

            if (transaction.getStatus() != TransactionStatus.COMPLETED) {
                continue;
            }

            BigDecimal amount = transaction.getAmount();

            if (transaction.getTransactionType() == TransactionType.DEPOSIT
                    && ownedDestination) {

                totalIncome = totalIncome.add(amount);
                monthlyIncome.merge(month, amount, BigDecimal::add);
                transactionCount++;

            } else if (transaction.getTransactionType()
                    == TransactionType.TRANSFER && ownedSource) {

                totalExpenses = totalExpenses.add(amount);
                monthlyExpenses.merge(month, amount, BigDecimal::add);
                transactionCount++;

                String category = categorize(
                        transaction.getDescription());

                categoryAmounts.merge(
                        category, amount, BigDecimal::add);

                categoryCounts.merge(category, 1L, Long::sum);
            }
        }

        List<MonthlyAnalyticsResponse> monthlyResults =
                new ArrayList<>();

        for (YearMonth month : monthlyIncome.keySet()) {

            BigDecimal income = monthlyIncome.get(month);
            BigDecimal expenses = monthlyExpenses.get(month);

            monthlyResults.add(new MonthlyAnalyticsResponse(
                    month.format(MONTH_FORMAT),
                    income,
                    expenses,
                    income.subtract(expenses),
                    transactions.stream()
                            .filter(t -> YearMonth.from(t.getCreatedAt())
                                    .equals(month))
                            .filter(t -> t.getStatus()
                                    == TransactionStatus.COMPLETED)
                            .filter(t ->
                                    (t.getTransactionType()
                                            == TransactionType.DEPOSIT
                                            && accountIds.contains(
                                                    t.getToAccountId()))
                                    ||
                                    (t.getTransactionType()
                                            == TransactionType.TRANSFER
                                            && accountIds.contains(
                                                    t.getFromAccountId())))
                            .count()
            ));
        }

        List<CategorySpendingResponse> categories =
                categoryAmounts.entrySet()
                        .stream()
                        .map(entry -> new CategorySpendingResponse(
                                entry.getKey(),
                                entry.getValue(),
                                categoryCounts.get(entry.getKey())))
                        .sorted(Comparator.comparing(
                                CategorySpendingResponse::amount).reversed())
                        .toList();

        return new FinancialAnalyticsResponse(
                totalIncome,
                totalExpenses,
                totalIncome.subtract(totalExpenses),
                transactionCount,
                monthlyResults,
                categories
        );
    }

    private String categorize(String description) {

        if (description == null || description.isBlank()) {
            return "Other";
        }

        String value = description.toLowerCase(Locale.ROOT);

        if (value.contains("food") || value.contains("restaurant")
                || value.contains("grocery")) {
            return "Food & Groceries";
        }

        if (value.contains("rent") || value.contains("house")) {
            return "Housing";
        }

        if (value.contains("travel") || value.contains("fuel")
                || value.contains("transport")) {
            return "Transport";
        }

        if (value.contains("bill") || value.contains("electricity")
                || value.contains("utility")) {
            return "Bills & Utilities";
        }

        if (value.contains("shopping") || value.contains("clothing")) {
            return "Shopping";
        }

        if (value.contains("health") || value.contains("medical")) {
            return "Healthcare";
        }

        if (value.contains("education") || value.contains("course")) {
            return "Education";
        }

        return "Other";
    }

    private FinancialAnalyticsResponse emptyResponse() {
        return new FinancialAnalyticsResponse(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                List.of(),
                List.of()
        );
    }
}