package com.example.Cheonan.Entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "food")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodId;  // 음식 순서

    @Column(nullable = false)
    private String name; // 음식 이름

    @Column(nullable = false)
    private String category; // 음식 분류

}