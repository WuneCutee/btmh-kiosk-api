package vn.baotinmanhhai.kioskapi.common;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Bo dau + ha chu thuong cho tim kiem khong phan biet dau/hoa-thuong (II.2.7: "kim gia bao" phai
 * khop "Kim Gia Bảo"). Lam o tang ung dung (Java), KHONG dua vao Postgres extension "unaccent":
 * extension can quyen CREATE EXTENSION (khong chac co tren moi moi truong trien khai, du an nay uu
 * tien "chay on dinh tren may khac" hon la toi uu DB); du lieu san pham cung khong co man quan tri
 * rieng trong pham vi du an nay nen co the duoc nhap thang bang SQL (ngoai JPA) - mot cot "ten da
 * bo dau" luu san se khong chac duoc dong bo trong truong hop do. Bo loc o Java dung cho moi
 * truong hop nhap lieu.
 */
public final class VietnameseText {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private VietnameseText() {
    }

    /** "Kim Gia Bảo Đạt" -> "kim gia bao dat". null -> "". */
    public static String fold(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String stripped = DIACRITICS.matcher(normalized).replaceAll("");
        return stripped
                .replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase(java.util.Locale.ROOT);
    }
}
