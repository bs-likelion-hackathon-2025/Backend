package com.example.Cheonan.Dto;

import lombok.Data;
import java.util.List;

@Data
public class KakaoResponse {
    private List<KakaoDocument> documents;
    private KakaoMeta meta;
}