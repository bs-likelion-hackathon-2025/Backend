package com.example.Cheonan.Dto;

import lombok.Data;

import javax.swing.text.Document;
import com.example.Cheonan.Dto.KakaoMeta;
import java.util.List;

@Data
public class KakaoResponse {
    private List<KakaoDocument> documents;
    private KakaoMeta meta; // 필요 없으면 생략 가능
}