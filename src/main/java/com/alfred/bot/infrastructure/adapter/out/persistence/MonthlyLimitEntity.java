package com.alfred.bot.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "monthly_limits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MonthlyLimitEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer month;
    private Integer year;
    private BigDecimal amount;
}
