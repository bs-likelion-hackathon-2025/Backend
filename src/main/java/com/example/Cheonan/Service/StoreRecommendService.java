package com.example.Cheonan.Service;

import com.example.Cheonan.Dto.IntentResult;
import com.example.Cheonan.Entity.Store;
import com.example.Cheonan.Repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StoreRecommendService {
    private final StoreRepository storeRepository;

    // 규칙 기반 키워드→카테고리 매핑 (동의어/오타/확장어 추가 가능)
    private static final Map<String, String> FOOD_TO_CATEGORY = Map.ofEntries(
            Map.entry("떡볶이", "분식"),
            Map.entry("김밥", "분식"),
            Map.entry("분식", "분식"),
            Map.entry("탕수육", "중식"),
            Map.entry("짜장", "중식"),
            Map.entry("짬뽕", "중식"),
            Map.entry("중식", "중식"),
            Map.entry("초밥", "일식"),
            Map.entry("스시", "일식"),
            Map.entry("라멘", "일식"),
            Map.entry("일식", "일식"),
            Map.entry("삼겹살", "한식"),
            Map.entry("한정식", "한식"),
            Map.entry("국밥", "한식"),
            Map.entry("한식", "한식"),
            Map.entry("파스타", "양식"),
            Map.entry("피자", "양식"),
            Map.entry("스테이크", "양식"),
            Map.entry("양식", "양식"),
            Map.entry("커피", "카페"),
            Map.entry("카페", "카페"),
            Map.entry("디저트", "카페")
    );

    public List<Store> recommendByMultiIntentOrFallback(String reply, IntentResult intent) {
        final int K_PER_CAT = 1;   // 후보 카테고리당 가져올 가게 수
        final int LIMIT = 3;      // 최종 응답 수

        // 1) 의도에서 후보 모으기
        List<String> c2 = safeTop3(intent != null ? intent.getCategory2Candidates() : null);
        List<String> c3 = safeTop3(intent != null ? intent.getCategory3Candidates() : null);
        List<String> c4 = safeTop3(intent != null ? intent.getCategory4Candidates() : null);

        // 2) 후보가 없으면 reply에서 규칙 기반 다중 추출
        if (c2.isEmpty() && c3.isEmpty() && c4.isEmpty()) {
            List<String> extracted = extractMultipleCategoriesFromReply(reply, 3);
            c2 = extracted;
        }

        // 3) 후보별 Top-K 조회
        List<Store> fromC2 = fetchTopKPerKeyword(c2, K_PER_CAT);
        List<Store> fromC3 = fetchTopKPerKeyword(c3, K_PER_CAT);
        List<Store> fromC4 = fetchTopKPerKeyword(c4, K_PER_CAT);

        // 4) 라운드로빈으로 섞어서 다양성 확보
        List<Store> mixed = interleaveDistinct(List.of(fromC2, fromC3, fromC4), LIMIT);

        // 5) 부족하면 category1 또는 reply 키워드로 백필
        if (mixed.size() < LIMIT) {
            String c1 = (intent != null && notBlank(intent.getCategory1()))
                    ? normalizeCategory(intent.getCategory1())
                    : null;
            if (c1 != null) {
                List<Store> backfill = storeRepository
                        .findByAnyCategoryOrderByRatingDesc(c1, PageRequest.of(0, LIMIT))
                        .getContent();
                mixed = backfillDistinctAppend(mixed, backfill, LIMIT);
            }
        }

        return mixed;
    }

    // ===== 유틸 =====

    private List<Store> fetchTopKPerKeyword(List<String> kws, int kPerCat) {
        if (kws == null || kws.isEmpty()) return Collections.emptyList();
        List<Store> acc = new ArrayList<>();
        for (String kw : kws) {
            if (!notBlank(kw)) continue;
            var page = storeRepository.findByAnyCategoryOrderByRatingDesc(kw, PageRequest.of(0, kPerCat));
            acc.addAll(page.getContent());
        }
        return acc;
    }

    private List<Store> interleaveDistinct(List<List<Store>> buckets, int limit) {
        List<Store> out = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        int i = 0;
        while (out.size() < limit) {
            boolean progressed = false;
            for (List<Store> b : buckets) {
                if (i < b.size()) {
                    Store s = b.get(i);
                    if (s.getId() != null && seen.add(s.getId())) {
                        out.add(s);
                        progressed = true;
                        if (out.size() >= limit) break;
                    }
                }
            }
            if (!progressed) break;
            i++;
        }
        return out;
    }

    private List<Store> backfillDistinctAppend(List<Store> base, List<Store> fill, int limit) {
        Set<Long> seen = new HashSet<>();
        for (Store s : base) if (s.getId() != null) seen.add(s.getId());
        for (Store s : fill) {
            if (s.getId() != null && seen.add(s.getId())) {
                base.add(s);
                if (base.size() >= limit) break;
            }
        }
        return base;
    }

    private List<String> safeTop3(List<String> xs) {
        if (xs == null) return List.of();
        return xs.stream()
                .filter(this::notBlank)
                .map(this::normalizeCategory)
                .distinct()
                .limit(3)
                .toList();
    }

    private List<String> extractMultipleCategoriesFromReply(String reply, int n) {
        if (reply == null || reply.isBlank()) return List.of();
        String text = normalize(reply);

        record Hit(String rep, int idx) {}
        List<Hit> hits = new ArrayList<>();

        for (var e : FOOD_TO_CATEGORY.entrySet()) {
            String key = normalize(e.getKey());
            int idx = text.indexOf(key);
            if (idx >= 0) hits.add(new Hit(e.getValue(), idx));
        }

        return hits.stream()
                .sorted(Comparator.comparingInt(h -> h.idx))
                .map(h -> h.rep)
                .distinct()
                .limit(n)
                .toList();
    }

    private String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).trim();
    }

    private String normalizeCategory(String raw) {
        if (raw == null) return null;
        String n = normalize(raw);
        if (Stream.of("한식", "중식", "일식", "양식", "카페", "분식").anyMatch(n::equals)) return n;
        return FOOD_TO_CATEGORY.getOrDefault(n, raw);
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}