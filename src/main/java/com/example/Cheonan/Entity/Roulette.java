package com.example.Cheonan.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roulette")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roulette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예: '8월 점심 룰렛', '치킨 페스티벌 룰렛'
    @Column(nullable = false)
    private String name;

    private String description;

    private boolean active;

    private LocalDateTime createdAt;

}