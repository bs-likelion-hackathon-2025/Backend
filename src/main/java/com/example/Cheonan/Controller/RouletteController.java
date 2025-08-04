package com.example.Cheonan.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/roulette")
@Tag(name = "룰렛 API", description = "카테고리별 랜덤 메뉴 선택")
public class RouletteController {

    private final Map<String, List<String>> menuMap = Map.of(
            "한식", List.of("김치찌개", "불고기", "비빔밥", "된장찌개", "제육볶음"),
            "중식", List.of("짜장면", "짬뽕", "탕수육", "마파두부", "꿔바로우"),
            "양식", List.of("파스타", "스테이크", "리조또", "피자", "햄버거"),
            "일식", List.of("스시", "우동", "돈카츠", "규동", "라멘")
    );

    @Operation(summary = "카테고리별 랜덤 메뉴 반환", description = "한식/중식/양식/일식 중 하나를 선택하여 랜덤 메뉴를 반환합니다.")
    @GetMapping("/{category}")
    public ResponseEntity<?> getRandomMenu(@PathVariable String category) {
        List<String> menus = menuMap.get(category);
        if (menus == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "해당 카테고리는 존재하지 않습니다."));
        }

        int randomIndex = new Random().nextInt(menus.size());
        String selected = menus.get(randomIndex);
        return ResponseEntity.ok(Map.of(
                "category", category,
                "menu", selected
        ));
    }
}