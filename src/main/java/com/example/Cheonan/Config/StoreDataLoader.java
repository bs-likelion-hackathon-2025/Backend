package com.example.Cheonan.Config;

import com.example.Cheonan.Entity.Store;
import com.example.Cheonan.Repository.StoreRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
@Component
@RequiredArgsConstructor
public class StoreDataLoader implements CommandLineRunner {

    private final StoreRepository storeRepository;

    @Override
    public void run(String... args) throws Exception {

        boolean isInsertEnabled = false; // true로 두면 CSV를 읽어 DB에 저장
        if (!isInsertEnabled) return;

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(
                new FileInputStream("src/main/resources/천안 가게 데이터(v3).csv"), "UTF-8"))) {

            String[] line;
            csvReader.readNext(); // 헤더 스킵

            while ((line = csvReader.readNext()) != null) {
                try {
                    // ===== 컬럼 파싱 =====
                    String name       = line[2].trim();
                    String address    = line[3].trim();
                    String xStr       = line[4].trim(); // x (위도/경도 중 무엇인지 프로젝트 규칙으로 통일)
                    String yStr       = line[5].trim(); // y
                    String kakaoLink  = line[7].trim();

                    String ratingStr  = line[8].trim();
                    Double rating     = (ratingStr.isBlank() || "NaN".equalsIgnoreCase(ratingStr))
                            ? null : Double.parseDouble(ratingStr);

                    String googleLink = line[10].trim();
                    String phoneNumber= line[12].trim();

                    String category1  = line[13].trim();
                    String category2  = line[14].trim();
                    String category3  = line[15].trim();
                    String category4  = line[16].trim();

                    // 요일별 영업시간 (CSV에 (월) 11:00~21:00 형태로 들어있는 값)
                    String mon = line[17].trim();
                    String tue = line[18].trim();
                    String wed = line[19].trim();
                    String thu = line[20].trim();
                    String fri = line[21].trim();
                    String sat = line[22].trim();
                    String sun = line[23].trim();

                    if (name.isEmpty()) {
                        throw new IllegalArgumentException("필수 필드(name) 빈 값");
                    }

                    Double x = (xStr.isBlank() || "NaN".equalsIgnoreCase(xStr)) ? null : Double.parseDouble(xStr);
                    Double y = (yStr.isBlank() || "NaN".equalsIgnoreCase(yStr)) ? null : Double.parseDouble(yStr);

                    // ===== Store 저장 =====
                    // 1) 빌더가 있다면 권장
                    Store store = Store.builder()
                            .name(name)
                            .address(address)
                            .x(x)
                            .y(y)
                            .kakaoLink(kakaoLink)
                            .rating(rating)
                            .googleLink(googleLink)
                            .phoneNumber(phoneNumber)
                            .category1(category1)
                            .category2(category2)
                            .category3(category3)
                            .category4(category4)
                            .mon(mon).tue(tue).wed(wed).thu(thu).fri(fri).sat(sat).sun(sun)
                            .build();

                    // 2) 또는 생성자에 맞춰 전달(생성자 시그니처가 위와 다르면 맞춰 수정)
                    // Store store = new Store(name, address, x, y, kakaoLink, rating, googleLink, phoneNumber,
                    //                         category1, category2, category3, category4, mon, tue, wed, thu, fri, sat, sun);

                    storeRepository.save(store);

                } catch (Exception e) {
                    System.out.println("⚠️ 저장 실패: " + Arrays.toString(line));
                    e.printStackTrace();
                }
            }
        }

        System.out.println("✅ 가게 데이터 저장 완료!");
    }
}