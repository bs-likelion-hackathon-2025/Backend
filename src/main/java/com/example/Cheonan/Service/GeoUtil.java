package com.example.Cheonan.Service;

// 천안 지역 아닌 경우 분류
public final class GeoUtil {
    private GeoUtil() {}


    // 하버사인 거리(m)
    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // 중심점 기준 반경(m) 안에 있는지
    public static boolean withinMeters(double lat, double lng,
                                       double centerLat, double centerLng,
                                       double radiusMeters) {
        return distanceMeters(lat, lng, centerLat, centerLng) <= radiusMeters;
    }
}