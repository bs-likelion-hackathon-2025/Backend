package com.example.Cheonan.Util;

import com.example.Cheonan.Dto.ChatRecommendResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent";

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiClient() {
        // 타임아웃 설정 (운영 내성)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(12).toMillis());
        this.restTemplate = new RestTemplate(factory);
    }

    public ChatRecommendResponse getFoodRecommendationWithIntent(String userMessage) {
        // 1) 프롬프트
        String prompt = buildPrompt(userMessage);

        // 2) 요청 바디 (role 생략 가능하지만 명시해도 무방)
        Map<String, Object> contentPart = Map.of("text", prompt);
        Map<String, Object> content = Map.of("role", "user", "parts", List.of(contentPart));
        Map<String, Object> body = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            String urlWithKey = GEMINI_URL + "?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(urlWithKey, request, Map.class);

            Map<String, Object> resBody = response.getBody();
            if (resBody == null) {
                return emptyReply("Gemini 응답이 비어 있어요(null body).");
            }

            // candidates 방어적 파싱
            List<Map<String, Object>> candidates = safeList(resBody.get("candidates"));
            if (candidates.isEmpty()) {
                // safety/promptFeedback 케이스를 로그에 남김
                Map<String, Object> promptFeedback = safeMap(resBody.get("promptFeedback"));
                log.warn("Gemini candidates empty. promptFeedback={}", truncate(promptFeedback.toString(), 400));
                return emptyReply("추천 결과가 비어 있어요.");
            }

            // parts에서 첫 text 추출
            String rawText = extractFirstTextFromCandidates(candidates);
            if (rawText == null || rawText.isBlank()) {
                return emptyReply("추천 결과 텍스트가 비어 있어요.");
            }

            // 첫 JSON 객체만 안전 추출
            String json = extractFirstJsonObject(rawText);
            if (json == null) {
                log.warn("No JSON detected in model text. text={}", truncate(rawText, 400));
                return emptyReply("JSON 파싱 실패(응답 형식이 올바르지 않음).");
            }

            // JSON 유효성 1차 확인 후 DTO 매핑
            JsonNode node = mapper.readTree(json);
            if (!node.isObject()) {
                return emptyReply("JSON 구조가 객체가 아닙니다.");
            }
            // 여기서 바로 DTO 변환
            ChatRecommendResponse dto = mapper.readValue(json, ChatRecommendResponse.class);
            return dto;

        } catch (HttpStatusCodeException he) {
            String bodyText = he.getResponseBodyAsString();
            log.error("Gemini HTTP {}: {}", he.getStatusCode(), truncate(bodyText, 500));
            return emptyReply("Gemini HTTP 오류: " + he.getStatusCode().value());
        } catch (Exception e) {
            log.error("Gemini 호출 실패: {}", e.toString());
            return emptyReply("Gemini 추천에 실패했어요: " + e.getMessage());
        }
    }

    // -------------------- 내부 유틸 --------------------

    private String buildPrompt(String userMessage) {
        // 프롬프트 본문은 그대로 유지(충청도 말투, 스키마, 후보 목록)
        return """
                당신은 충청도 말투의 식당 추천 챗봇입니다.
                - 아래 사용자 요청을 읽고, 간단하지만 핵심을 짚는 2줄 답변(reply)을 충청도 말투로 생성합니다.
                - 그리고 JSON 객체를 함께 반환합니다. 
                        
                카테고리1 후보:
                [ '한식', '술집', '카페', '치킨', '중식', '분식', '일식', '양식', '뷔페', '간식', '패스트푸드', '아시아음식', '샤브샤브', '도시락', '샐러드','퓨전요리', '기사식당', '패밀리레스토랑', '야식', '철판요리']

                카테고리2 후보:
                ['육류,고기', '해물,생선', '호프,요리주점', '중국요리', '국수', '커피전문점', '실내포장마차', '국밥', '피자', '찌개,전골', '돈까스,우동', '순대', '테마카페', '한식뷔페', '떡볶이', '일본식주점', '해장국', '초밥,롤', '양꼬치', '이탈리안', '감자탕', '냉면', '참치회', '동남아음식', '한정식', '사철탕,영양탕', '오뎅바', '맘스터치', '제과,베이커리', '쌈밥', '햄버거', '처갓집양념치킨', '일본식라면', '두부전문점', '곰탕', '교촌치킨', '반찬가게', '칵테일바', '설렁탕', '일식집', '다방', '유흥주점', '가마치통닭', '페리카나', '한솥도시락', '굽네치킨', '퓨전한식', '노랑통닭', '죽', '등촌샤브칼국수', '네네치킨', '닭강정', '60계치킨', '호식이두마리치킨', '푸라닭치킨', '멕시카나치킨', '와인바', '수제비', '식품서비스업', '자담치킨', '샌드위치', 'BBQ', '훌랄라참숯치킨', '샤브마니아', '기영이숯불두마리치킨', '토스트', '고기뷔페', '고봉민김밥인', '보드람치킨', 'BHC치킨', '장호덕손만두', '주먹밥', '지코바', '본도시락', '스테이크,립', '전통찻집', '얌샘김밥',  '치킨플러스', '또래오래', '또봉이통닭', '후라이드참잘하는집', '북촌손만두', '롯데리아', '바른치킨', '꾸브라꼬숯불치킨', '땅땅치킨', '퓨전일식', '해탄', '명인만두', '오븐에빠진닭', '샤브올데이', '맥시칸치킨', '불로만숯불바베큐', '맛닭꼬', '샐러디', '큰통치킨', '동근이숯불두마리치킨', '포케올데이', '영칼로리포케', '백소정', '오봉집', '치킨신드롬', '토끼정', '배터지는생동까스', '면식당', '부어치킨', '구어조은닭', '청년치킨', '다사랑치킨', '봉이치킨', '인도음식', '명랑핫도그', '멕시칸,브라질', '깐부치킨', '아비꼬', '경아두마리치킨','채선당', '삼첩분식', '메가혼밥', '스테프핫도그', '지지고', '화신바베큐치킨', '오븐마루치킨', '누구나홀딱반한닭', '해산물', '프랑킨바베큐치킨', '아이스크림', '순살만공격', '해산물뷔페', '오늘통닭', '아라치', '빕스', '치킨마루', '공차', '탄탄면공방', '치킨더홈', '야들리애치킨', '화락바베큐치킨', '싸다김밥', '명동칼국수샤브샤브', '치킨과바람피자', '애슐리', '애플꼬마김밥', '바비큐보스','티바두마리치킨', '타코비','도넛', '본스치킨', '디디치킨', '스쿨푸드', '브런치빈']
                        
                카테고리3 후보:
                ['닭요리', '갈비', '삼겹살', '곱창,막창', '족발,보쌈', '회', '칼국수', '디저트카페', '장어', '오리', '아구', '추어', '베트남음식', '매운탕,해물탕', '투다리', '빽다방', '조개', '게,대게', '역전할머니맥주', '불고기,두루치기', '본죽&비빔밥cafe', '피나치공', '바다양푼이동태탕', '시선', '태국음식', '탕화쿵푸마라탕', '전주현대옥', '메가MGC커피', '청년다방', '죽이야기', '애견카페', '동대문엽기떡볶이', '크라운호프', '국수나무', '키즈카페', '프랭크버거', '컴포즈커피', '부라보맥주', '양평해장국', '한촌설렁탕', '미스터피자', '봉구스밥버거', '본죽', '빽보이피자', '그놈포차', '스크린골프연습장', '피자스쿨', '일품양평해장국', '뚝배기양평해장국', '삼동소바', '반올림피자', '피자마루', '백채김치찌개', '제주은희네해장국', '큰맘할매순대국', '이삭토스트', '최고당돈가스', '한마음정육식당', '청년피자', '대동집', '깡우동', '피자알볼로', '투썸플레이스', '동경규동', '냅다청양집', '고피자', '신전떡볶이', '김복남맥주', '히노아지', '우리할매떡볶이', '출장요리', '철길부산집', '롤링파스타', '삼산회관', '토리키치', '어부네코다리조림', '굴,전복', '피자파는집', '배떡', '고돼지', '국밥참맛있는집', '청담피자', '교동면옥', '닭강정공방', '고기극찬', '유로코피자', '치어스', '떡볶이참잘하는집 떡참', '쏘시지요', '두끼떡볶이', '스텔라떡볶이', '프레드피자', '육수당', '무모한초밥', '만복집', '금화왕돈까스', '복어', '원조부안집', '신참떡볶이', '피자닭터', '옥된장', '생마차', '은화수식당', '텐퍼센트커피', '하루엔소쿠', '이차돌', '인쌩맥주', '응급실국물떡볶이', '더벤티', '킹콩부대찌개', '미태리', '망향비빔국수', '라홍방마라탕', '호맥', '용우동', '이디야커피', '생활맥주', '죠샌드위치', '땅스부대찌개', '모락로제떡볶이&닭강정', '나인블럭', '간빠이', '본가신촌설렁탕', '제사음식', '긴자료코', '파스쿠찌', '수작', '김형제고기의철학', '꾸이한끼', '스터디카페,스터디룸', '스크린야구장', '명동칼국수', '백스비어', '도야짬뽕', '감성커피', '금별맥주', '미사리밀빛초계국수', '쿠우쿠우', '차,커피', '정성카츠', '주류도매,주류유통', '육칠이', '달리는커피', '피자스톰', '오구피자', '노랑강정', '레드버튼', '왓더버거', '안주가', '피자와썹', '블루샥', '카라반', '라화쿵부', '가마솥순대국밥', '맘스피자', '야키토리잔잔', '봉명동내커피', '피자헛', '사주카페', '채선당한가득', '달빛에구운고등어', '육쌈냉면', '한신포차', '숯토리', '봉구비어', '북카페', '피쉬앤그릴', '놀부부대찌개', '논골집', '이화수전통육개장', '술속의밤', '담소소사골순대육개장', '바른암소한우', '밀러타임', '이비가짬뽕', '가르텐비어', '포트캔커피', '쌈촌', '홍익돈까스', '맛찬들왕소금구이', '빅스타피자', '고반식당', '실내동물원', '피자먹다', '카페인중독', '천씨씨커피', '미카도스시', '매머드익스프레스', '신룽푸마라탕', '비스트로피자', '갓생맥주', '별난아재맥주', '프릭스버거','오유미당', '경양카츠', '봉수아피자', '롯데시네마', '탐나는피자', '달토끼의떡볶이흡입구역', '와플칸', '한우88도매장', '소림마라', '국대떡볶이', '포차천국', '원유로스페셜티', '해물상회', '보배반점', '하노이맥주밤거리', '돈까스클럽', ]
                        
                카테고리4 후보:
                ['삼계탕', '가장맛있는족발', '명륜진사갈비', '두찜', '유가네닭갈비', '메콩타이', '족발야시장', '완미족발', '기대만족', '오점오닭갈비', '요거트아이스크림의정석', '팔각도', '설빙', '원할머니보쌈족발', '본가장수촌', '송담추어탕', '막창도둑', '족발신선생', '마왕족발', '푸줏간', '곱분이곱창', '할매솥뚜껑삼겹살', '육회바른연어', '육미제당', '일품대패', '장충동왕족발', '지호한방삼계탕', '하남돼지집', '인생극장', '동궁찜닭', '미분당', '곱깨비', '인생아구찜','요거트월드', '만족오향족발', '귀한족발', '불막열삼', '상상오리', '연타발', '노티드', '삼겹당', '갯벌의조개', '제줏간', '원조설악추어탕', '도야족발', '육회야문연어']
                        
                JSON 스키마:
                   {
                     "reply": "사용자에게 보여줄 말(충청도 말투, 한 문단)",
                     "intent": {
                        "category1": "카테고리1 중 하나 또는 null",
                        "category2Candidates": ["카테고리2 후보(최대 3개)"],
                        "category3Candidates": ["카테고리3 후보(최대 3개)"],
                        "category4Candidates": ["카테고리4 후보(최대 3개)"]
                     },
                     "stores": []  // 이 필드는 서버가 채워 넣습니다.
                   }
               - 카테고리2/3/4 후보는 위에 제공한 후보 리스트에서만 고르세요.
               - 문맥상 다양성이 필요하면 2~3개, 확신이 낮으면 1~2개만 고르세요.
               - 출력은 오직 위 JSON 하나만. 추가 설명, 코드블럭(```), 주석 금지.
                사용자 요청: %s
                """.formatted(userMessage);
    }

    // candidates → 첫 text 추출
    @SuppressWarnings("unchecked")
    private String extractFirstTextFromCandidates(List<Map<String, Object>> candidates) {
        for (Map<String, Object> cand : candidates) {
            Map<String, Object> contentMap = safeMap(cand.get("content"));
            List<Map<String, Object>> parts = safeList(contentMap.get("parts"));
            for (Map<String, Object> part : parts) {
                Object t = part.get("text");
                if (t instanceof String s && !s.isBlank()) {
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * 응답 텍스트에서 첫 번째 JSON 객체({ ... })만 안전하게 추출.
     * 코드펜스 ```json ... ``` 같은 래핑도 제거.
     */
    private String extractFirstJsonObject(String text) {
        if (text == null) return null;
        // 코드펜스/백틱 제거
        String cleaned = text.replaceAll("```(json)?", "").trim();

        // 빠른 경로: 정규식으로 첫 중괄호~짝 맞는 중괄호 범위 찾기 실패 시 스택 파서
        String fast = regexExtractJson(cleaned);
        if (fast != null) return fast;

        return stackExtractJson(cleaned);
    }

    // 간단 정규식: 문자열 시작부터 올바른 괄호쌍 추출(탐욕X)
    private String regexExtractJson(String s) {
        // { ... } 블록을 가장 먼저 만나는 패턴 (단, 중첩은 완벽히 처리 못함 → 실패 시 stack 사용)
        Pattern p = Pattern.compile("\\{.*?\\}", Pattern.DOTALL);
        Matcher m = p.matcher(s);
        if (m.find()) {
            String candidate = m.group();
            // 최소한의 유효성 체크
            try {
                mapper.readTree(candidate);
                return candidate;
            } catch (JsonProcessingException ignore) {
                return null;
            }
        }
        return null;
    }

    // 중첩 중괄호를 처리하는 스택 기반 추출
    private String stackExtractJson(String s) {
        int start = s.indexOf('{');
        if (start < 0) return null;
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    String sub = s.substring(start, i + 1);
                    try {
                        mapper.readTree(sub);
                        return sub;
                    } catch (JsonProcessingException e) {
                        // continue searching
                    }
                }
            }
        }
        return null;
    }

    // null-safe helpers
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> safeList(Object o) {
        if (o instanceof List<?> l) {
            //noinspection unchecked
            return (List<Map<String, Object>>) l;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> safeMap(Object o) {
        if (o instanceof Map<?, ?> m) {
            //noinspection unchecked
            return (Map<String, Object>) m;
        }
        return Collections.emptyMap();
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private ChatRecommendResponse emptyReply(String msg) {
        return ChatRecommendResponse.builder()
                .reply(msg)
                .intent(null)
                .stores(List.of())
                .build();
    }
}