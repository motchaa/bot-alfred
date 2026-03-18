package com.alfred.bot.infrastructure.adapter.out.persistence;

import com.alfred.bot.domain.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, Long> {
    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE MONTH(t.createdAt) = :month AND YEAR(t.createdAt) = :year AND t.type = :type")
    BigDecimal sumAmountByTypeAndMonthAndYear(@Param("type")TransactionType type, @Param("month") int month, @Param("year") int year);

    @Query("SELECT t.categoryId, SUM(t.amount) FROM TransactionEntity t WHERE MONTH(t.createdAt) = :month AND YEAR(t.createdAt) = :year GROUP BY t.categoryId")
    List<Object[]> sumAmountByCategoryGrouped(@Param("month") int month, @Param("year") int year);

    @Query("SELECT t FROM TransactionEntity t WHERE MONTH(t.createdAt) = :month AND YEAR(t.createdAt) = :year ORDER BY t.createdAt DESC")
    List<TransactionEntity> findByMonthAndYear(@Param("month") int month, @Param("year") int year);
}
