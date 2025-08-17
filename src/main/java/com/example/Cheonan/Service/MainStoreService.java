package com.example.Cheonan.Service;

import com.example.Cheonan.Dto.PageResponse;
import com.example.Cheonan.Dto.StoreCardDto;
import com.example.Cheonan.Entity.Store;
import com.example.Cheonan.Repository.Projection.NearbyRow;
import com.example.Cheonan.Repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;   // ★ 추가
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // ★ 추가
public class MainStoreService {


    private final StoreRepository storeRepository;

    // 천안 시청 기준
    private static final double CHEONAN_CENTER_LAT = 36.8150;
    private static final double CHEONAN_CENTER_LNG = 127.1130;
    private static final int CHEONAN_SERVICE_RADIUS_M = 25_000; // 20km

    /**
     * x=lng, y=lat
     * x,y 둘 다 있으면 근접순; 단, 천안 권역 밖이면 랜덤 전환
     * radius: 미터. null이면 기본 1500m.
     */
    public PageResponse<StoreCardDto> getCards(Integer size, Double x, Double y, Integer radius) {
        int limit   = (size == null || size <= 0) ? 10 : Math.min(size, 30);
        int rMeters = (radius == null || radius <= 0) ? 1500 : Math.min(radius, 10_000);

        List<StoreCardDto> items;

        boolean hasXY = (x != null && y != null);
        boolean inCheonan = false;

        if (hasXY) {
            // 주의: x=lng, y=lat
            inCheonan = GeoUtil.withinMeters(y, x, CHEONAN_CENTER_LAT, CHEONAN_CENTER_LNG, CHEONAN_SERVICE_RADIUS_M);
            log.debug("coords=({}, {}), inCheonan={}", y, x, inCheonan);
        }

        if (hasXY && inCheonan) {
            // 근접 모드 (lat=y, lng=x)
            List<NearbyRow> rows = storeRepository.findNearbyByHaversine(y, x, rMeters, limit);
            items = rows.stream().map(StoreCardDto::from).toList();

            if (items.isEmpty()) { // 근접 결과 없으면 랜덤 백업
                log.debug("nearby empty → fallback to random");
                items = storeRepository.pickRandom(limit).stream()
                        .map(StoreCardDto::from)
                        .toList();
            }
        } else {
            // 천안 권역 밖이거나 좌표 없음 → 랜덤
            log.debug("no coords or out of Cheonan → random");
            items = storeRepository.pickRandom(limit).stream()
                    .map(StoreCardDto::from)
                    .toList();
        }

        return PageResponse.<StoreCardDto>builder()
                .items(items)
                .size(items.size())
                .build();
    }
}