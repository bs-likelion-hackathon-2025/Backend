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
        final int LIMIT = 3;
        final int K_PER_CAT_PRIMARY = 3; // 우선 버킷은 더 많이 땡겨오고
        final int K_PER_CAT_OTHERS  = 2; // 나머지는 적당히

        // 후보 수집
        List<String> c2 = safeTop3(intent != null ? intent.getCategory2Candidates() : null);
        List<String> c3 = safeTop3(intent != null ? intent.getCategory3Candidates() : null);
        List<String> c4 = safeTop3(intent != null ? intent.getCategory4Candidates() : null);
        if (c2.isEmpty() && c3.isEmpty() && c4.isEmpty()) {
            c2 = extractMultipleCategoriesFromReply(reply, 3);
        }

        // 1순위 키워드 선정: c2 첫 항목을 기본 1순위로
        String primaryKw = !c2.isEmpty() ? c2.get(0) : (!c3.isEmpty() ? c3.get(0) : (!c4.isEmpty() ? c4.get(0) : null));

        // 버킷별 조회(우선 버킷은 더 많이)
        List<Store> bPrimary = (primaryKw != null)
                ? storeRepository.findByAnyCategoryOrderByRatingDesc(primaryKw, PageRequest.of(0, K_PER_CAT_PRIMARY)).getContent()
                : List.of();

        // 나머지 키워드들 모아서 한 버킷으로
        List<String> otherKws = new ArrayList<>();
        for (int i = 0; i < c2.size(); i++) if (!c2.get(i).equals(primaryKw)) otherKws.add(c2.get(i));
        otherKws.addAll(c3);
        otherKws.addAll(c4);

        List<Store> bOthers = new ArrayList<>();
        for (String kw : safeTop3(otherKws)) {
            bOthers.addAll(storeRepository
                    .findByAnyCategoryOrderByRatingDesc(kw, PageRequest.of(0, K_PER_CAT_OTHERS))
                    .getContent());
        }

        // 버킷 목록: 0번=우선, 1번=나머지, 2번=백업(없으면 빈 리스트)
        List<Store> bBackup = List.of(); // 필요하면 넣기
        List<List<Store>> buckets = List.of(bPrimary, bOthers, bBackup);

        // 가중 라운드 로빈: 우선 버킷을 2배
        int[] pattern = {0, 0, 1, 2}; // 0을 두 번
        List<Store> mixed = interleaveDistinctWeighted(buckets, pattern, LIMIT);

        // 모자라면 primary로 백필
        if (mixed.size() < LIMIT && primaryKw != null) {
            var fill = storeRepository.findByAnyCategoryOrderByRatingDesc(primaryKw, PageRequest.of(0, LIMIT)).getContent();
            mixed = backfillDistinctAppend(mixed, fill, LIMIT);
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

    private List<Store> interleaveDistinctWeighted(List<List<Store>> buckets, int[] pattern, int limit) {
        List<Store> out = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        int[] idx = new int[buckets.size()]; // 각 버킷에서 어디까지 꺼냈는지

        int p = 0;
        while (out.size() < limit) {
            int b = pattern[p % pattern.length]; // 이번에 뽑을 버킷 인덱스
            if (b < buckets.size()) {
                List<Store> bucket = buckets.get(b);
                // 다음 유효 아이템을 찾는다(중복은 건너뜀)
                while (idx[b] < bucket.size() && (bucket.get(idx[b]).getId() == null ||
                        !seen.add(bucket.get(idx[b]).getId()))) {
                    idx[b]++;
                }
                if (idx[b] < bucket.size()) {
                    out.add(bucket.get(idx[b]++));
                    if (out.size() >= limit) break;
                }
            }
            p++;
            // 모든 버킷이 더 이상 꺼낼 게 없으면 종료
            boolean anyLeft = false;
            for (int i = 0; i < buckets.size(); i++) {
                if (idx[i] < buckets.get(i).size()) { anyLeft = true; break; }
            }
            if (!anyLeft) break;
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