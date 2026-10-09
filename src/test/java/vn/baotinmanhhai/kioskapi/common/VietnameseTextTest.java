package vn.baotinmanhhai.kioskapi.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** II.2.7: "kim gia bao" phai tim duoc "Kim Gia Bảo" - chung minh VietnameseText.fold() dung. */
class VietnameseTextTest {

    @Test
    void foldsDiacriticsAndCase() {
        assertEquals("kim gia bao dat", VietnameseText.fold("Kim Gia Bảo Đạt"));
    }

    @Test
    void handlesDBarWithoutCombiningMark() {
        // 'đ'/'Đ' khong phai chu cai La-tinh + dau ket hop (khong tach boi NFD) nen phai thay rieng.
        assertEquals("dong xu", VietnameseText.fold("Đồng xu"));
    }

    @Test
    void nullIsEmptyString() {
        assertEquals("", VietnameseText.fold(null));
    }

    @Test
    void queryMatchesAccentedName() {
        String folded = VietnameseText.fold("Đồng Xu Kim Gia Bảo Hoa Sen, loại 3 chỉ - 24K");
        assertTrue(folded.contains(VietnameseText.fold("kim gia bảo")));
        assertTrue(folded.contains("kim gia bao"));
    }
}
