package com.example.Cheonan.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)            // 가게명 필수
    private String name;

    @Column(columnDefinition = "TEXT")   // 주소 길이 여유
    private String address;

    // 위경도(프로젝트 규칙에 맞게 x=lon, y=lat 등 의미를 주석으로 명시해두면 좋아요)
    private Double x;
    private Double y;

    @Column(columnDefinition = "TEXT")
    private String kakaoLink;

    private Double rating;               // null 허용

    @Column(columnDefinition = "TEXT")
    private String googleLink;

    private String phoneNumber;

    // 카테고리들
    private String category1;
    private String category2;
    private String category3;
    private String category4;

    // 요일별 영업시간 (예: "11:00~21:00", "휴무" 등)
    private String mon;
    private String tue;
    private String wed;
    private String thu;
    private String fri;
    private String sat;
    private String sun;

    @Transient
    public String getTodayHours() {
        DayOfWeek dow = LocalDate.now().getDayOfWeek();

        // 오늘 요일 라벨
        String label = switch (dow) {
            case MONDAY    -> "(월)";
            case TUESDAY   -> "(화)";
            case WEDNESDAY -> "(수)";
            case THURSDAY  -> "(목)";
            case FRIDAY    -> "(금)";
            case SATURDAY  -> "(토)";
            case SUNDAY    -> "(일)";
        };

        // 원본 값
        String raw = switch (dow) {
            case MONDAY    -> mon;
            case TUESDAY   -> tue;
            case WEDNESDAY -> wed;
            case THURSDAY  -> thu;
            case FRIDAY    -> fri;
            case SATURDAY  -> sat;
            case SUNDAY    -> sun;
        };
        if (raw == null || raw.isBlank()) return null;

        String trimmed = raw.trim();

        // 1) 이미 "(월)" 같은 요일 프리픽스가 들어있다면 그대로 반환
        if (trimmed.startsWith("(")) {
            return trimmed;
        }

        // 2) 요일 프리픽스가 없다면 보기 좋게 라벨을 붙여서 반환
        //    (원래 DB에 "(월)"이 들어올 때는 sanitize로 빼고, 여기서 라벨을統一해서 붙이는 전략도 가능)
        return label + " " + trimmed;
    }
}