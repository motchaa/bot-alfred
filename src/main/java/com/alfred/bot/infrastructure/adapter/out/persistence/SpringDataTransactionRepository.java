package com.alfred.bot.infrastructure.adapter.out.persistence;

import com.alfred.bot.domain.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, Long> {
    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.type = :type AND t.createdAt >= :start AND t.createdAt <= :end")
    BigDecimal sumAmountByTypeInRange(@Param("type") TransactionType type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c.name, SUM(t.amount) " + "FROM TransactionEntity t " + "JOIN CategoryEntity c ON t.categoryId = c.id " + "WHERE t.createdAt >= :start AND t.createdAt <= :end " + "GROUP BY c.name")
    List<Object[]> sumAmountByCategoryGrouped(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t FROM TransactionEntity t WHERE t.createdAt >= :start AND t.createdAt <= :end ORDER BY t.createdAt DESC")
    List<TransactionEntity> findByRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
