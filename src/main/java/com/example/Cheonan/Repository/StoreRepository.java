package com.example.Cheonan.Repository;

import com.example.Cheonan.Entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    // (다른 곳에서 쓰면 유지, 아니면 정리)
//    List<Store> findByCategory1ContainingIgnoreCase(String category1);
//    List<Store> findByNameContainingIgnoreCase(String keyword);
//    Page<Store> findByCategory1ContainingIgnoreCase(String category, Pageable pageable);

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
}