
package com.example.bankapp.repository;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    @Query("SELECT t FROM BankTransaction t ORDER BY t.createdAt DESC")
    List<BankTransaction> findAllLatest();

    @Query("SELECT t FROM BankTransaction t WHERE " +
            "t.fromAccount = :account OR t.toAccount = :account " +
            "ORDER BY t.createdAt DESC")
    List<BankTransaction> findHistory(
            @Param("account") String account);

    @Query("SELECT t FROM BankTransaction t WHERE t.status = :status " +
            "ORDER BY t.createdAt DESC")
    List<BankTransaction> findByStatusLatest(
            @Param("status") TransactionStatus status);

    @Query("SELECT COUNT(t) FROM BankTransaction t WHERE t.status = :status")
    long countByStatus(@Param("status") TransactionStatus status);

    // Task 12.1: Find successful transfers above a given amount
    @Query("SELECT t FROM BankTransaction t WHERE " +
            "t.amount > :minAmount AND t.status = 'SUCCESS' " +
            "ORDER BY t.amount DESC")
    List<BankTransaction> findLargeTransfers(
            @Param("minAmount") BigDecimal minAmount);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM BankTransaction t " +
            "WHERE t.fromAccount = :account " +
            "AND t.toAccount <> 'WITHDRAWAL' " +
            "AND t.status = 'SUCCESS'")
    BigDecimal getTotalAmountSent(
            @Param("account") String account);
}
