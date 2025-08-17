package com.example.Cheonan.Controller;

import com.example.Cheonan.Dto.PageResponse;
import com.example.Cheonan.Dto.StoreCardDto;
import com.example.Cheonan.Service.MainStoreService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("api/")
@RestController
@Validated
public class MainStoreController {

    private final MainStoreService mainStoreService;

    public MainStoreController(MainStoreService mainStoreService) {
        this.mainStoreService = mainStoreService;
    }

    @Operation(summary = "메인 페이지 하단 천안 맛집")
    @GetMapping("/mainpage/store")
    public ResponseEntity<PageResponse<StoreCardDto>> mainPageStore(
            @RequestParam(defaultValue = "30")
            @Min(1) @Max(50)Integer size,

            @RequestParam(required = false)
            @DecimalMin(value = "33.0") @DecimalMax(value = "43.0")
            Double lat,

            @RequestParam(required = false)
            @DecimalMin(value = "124.0") @DecimalMax(value = "132.0")
            Double lng,

            @RequestParam(defaultValue = "3000")
            @Min(100) @Max(10000) Integer radius
    ) {
        var resp = mainStoreService.getCards(size, lng, lat, radius);
        return ResponseEntity.ok(resp);
    }
}
