package com.example.Cheonan.Repository;

import com.example.Cheonan.Entity.Store;
import com.example.Cheonan.Repository.Projection.NearbyRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    // (다른 곳에서 쓰면 유지, 아니면 정리)
//    List<Store> findByCategory1ContainingIgnoreCase(String category1);
    Page<Store> findByNameContainingIgnoreCase(String category, Pageable pageable);

    /**
     * category1~4 중 어느 필드에라도 키워드가 포함된 가게를
     * 평점 내림차순(NUll=0으로 치환)으로 정렬해 페이지네이션.
     * - Pageable의 Sort는 이 JPQL의 ORDER BY가 우선됨(명시적 고정 정렬).
     */
    @Query("""
        SELECT s
        FROM Store s
        WHERE (
               UPPER(s.category1) LIKE UPPER(CONCAT('%', :kw, '%'))
            OR UPPER(s.category2) LIKE UPPER(CONCAT('%', :kw, '%'))
            OR UPPER(s.category3) LIKE UPPER(CONCAT('%', :kw, '%'))
            OR UPPER(s.category4) LIKE UPPER(CONCAT('%', :kw, '%'))
        )
        ORDER BY COALESCE(s.rating, 0) DESC
    """)
    Page<Store> findByAnyCategoryOrderByRatingDesc(@Param("kw") String keyword, Pageable pageable);


    // 슬라이더용 랜덤 n개
    @Query(value = """
    SELECT
      *
    FROM store
    ORDER BY RAND()
    LIMIT :limit
""", nativeQuery = true)
    List<Store> pickRandom(@Param("limit") int limit);

    //  거리 통한 추천- 반경: 미터 단위
    @Query(value = """
    SELECT 
      s.id               AS id,
      s.name             AS name,
      s.category1        AS category1,
      s.kakao_link       AS kakaoLink,
      s.address          AS address,
      s.y                AS lat,
      s.x                AS lng,
      (6371000 * ACOS(
        COS(RADIANS(:lat)) * COS(RADIANS(s.y)) * COS(RADIANS(s.x) - RADIANS(:lng))
        + SIN(RADIANS(:lat)) * SIN(RADIANS(s.y))
      )) AS distanceMeters
    FROM store s
    HAVING distanceMeters <= :radiusMeters
    ORDER BY distanceMeters ASC
    LIMIT :limit
""", nativeQuery = true)
    List<NearbyRow> findNearbyByHaversine(@Param("lat") double lat,
                                          @Param("lng") double lng,
                                          @Param("radiusMeters") int radiusMeters,
                                          @Param("limit") int limit);
}

