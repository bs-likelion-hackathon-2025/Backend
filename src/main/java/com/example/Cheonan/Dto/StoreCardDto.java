package com.example.Cheonan.Dto;

import com.example.Cheonan.Entity.Store;
import com.example.Cheonan.Repository.Projection.NearbyRow; // ★ 추가
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class StoreCardDto {
    private Long id;
    private String category;      // category1
    private String name;
    private String addressShort;  // 한 줄 주소
    private String placeUrl;      // kakaoLink 우선

    // 기존: Store -> DTO
    public static StoreCardDto from(Store s) {
        return StoreCardDto.builder()
                .id(s.getId())
                .category(nullToEmpty(s.getCategory1()))
                .name(nullToEmpty(s.getName()))
                .addressShort(compactAddress(s.getAddress()))
                .placeUrl(s.getKakaoLink())
                .build();
    }

    // ★ 추가: NearbyRow -> DTO (근접검색용)
    public static StoreCardDto from(NearbyRow r) {
        return StoreCardDto.builder()
                .id(r.getId())
                .category(nullToEmpty(r.getCategory1()))
                .name(nullToEmpty(r.getName()))
                .addressShort(compactAddress(r.getAddress()))
                .placeUrl(r.getKakaoLink())
                .build();
    }

    private static String compactAddress(String addr) {
        if (addr == null || addr.isBlank()) return null;
        String[] tok = addr.trim().split("\\s+");
        int n = Math.min(tok.length, 4);
        return String.join(" ", java.util.Arrays.copyOfRange(tok, 0, n));
    }


    private static String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }
}