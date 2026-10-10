# kiosk-api

Backend **doc lap, tu khoi tao** cho App Kiosk, trien khai DUNG 16 API trong "Tai Lieu Tich Hop
API" (phien ban V1.0.1, phase 1) giua Bao Tin Manh Hai va doi phat trien App Kiosk. Tai lieu nay
thuoc pham vi bao mat noi bo - project nay chi bam sat dac ta cua tai lieu, KHONG import/goi/dung
chung database voi hai du an anh em `Backend/webview` va `Backend/kiosk-admin`, du webview da co
mot ban trien khai (rong hon, khong gioi han trong tai lieu nay) cho cung nghiep vu.

## Vi sao co project nay

Nguoi dung yeu cau mot backend **tach rieng hoan toan**, chi theo dung dac ta trong tai lieu tich
hop (16 API: lay token thiet bi, quang cao man cho, gio mo ban, banner, danh muc, danh sach/tim
kiem/chi tiet san pham, SSE gia realtime, tinh/thanh + cua hang nhan hang, han muc giao dich, bao
loi tu may, kich hoat may, xoay khoa, bootstrap) - khong phu thuoc code hay schema cua webview/
kiosk-admin. Vai cho kho (sinh/xoay khoa RSA, mo hinh bang) duoc THAM KHAO thiet ke da chung minh
dung cua webview (xem muc "Tham khao thiet ke" ben duoi) nhung viet lai entity/package rieng.

## Kien truc / quyet dinh ky thuat

- Spring Boot 3.3.7, Java 21, Maven wrapper (copy tu `Backend/kiosk-admin`).
- **`ddl-auto=update`** (khong dung Flyway): kiosk-api so huu TOAN BO schema cua chinh no (khong
  co bang nao cua du an khac can bao ve, khac han canh huong `validate` cua kiosk-admin). Day la
  lua chon don gian hoa cho lan trien khai dau; mot ban trien khai production nghiem tuc hon nen
  chuyen sang Flyway voi migration tuong minh truoc khi di vao von hanh thuc te lau dai (xem muc
  "Ngoai pham vi").
- **Khong Redis, khong message broker** - gia dinh **single-instance**: chong phat lai nonce dung
  bang Postgres (`kiosk_device_nonce`, don dep ban ghi > 10 phut bang `@Scheduled` moi 5 phut);
  gioi han goi (rate limit) dung `ConcurrentHashMap` trong-tien-trinh theo pattern
  `LoginRateLimiterService` cua kiosk-admin; dang ky ket noi SSE dung `Map<deviceCode, SseEmitter>`
  trong-tien-trinh; bo dem version gia/SSE luu 1 dong trong Postgres
  (`kiosk_price_version`) de song qua lan restart app. **Han che da biet**: khong chay duoc nhieu
  instance song song (rate limit/SSE se khong dong bo giua cac instance) - neu can scale ngang,
  phai doi sang Redis (INCR cho version, rate limit, pub/sub cho SSE).
- Chu ky thiet bi: RSASSA-PSS-SHA256 (salt 32 byte, trailer field 1, MGF1-SHA256) - xem
  `DeviceSignatureService`. Test vector chinh thuc trong tai lieu (muc II.5.2) duoc kiem bang
  `KioskSignatureTestVectorTest` (7 test, PASS) - day la bang kiem chung quan trong nhat cua toan
  bo du an.
- JWT HS256 (jjwt 0.12.6), issuer **"kiosk-api"** (KHONG dung "backend-webview" - ten do thuoc ve
  webview), secret rieng, khong chia se voi webview/kiosk-admin.
- `TraceIdFilter`: kiem `X-Trace-Id` TRUOC moi xu ly khac (thieu/sai UUID -> 400 ngay), echo lai
  header response, dung lam correlation id trong log qua MDC.
- `DeviceAuthFilter`: kiem Bearer JWT cho moi API tru 3 API xac thuc thiet bi (II.2.1/II.2.14/
  II.2.15) va `/internal/ops/**`; kiem `sub` khop `X-Device-Code`; chan ngay may REVOKED (403/121,
  moi API); chan 403/201 ("Kenh KIOSK dang tam dung") cho cac API du lieu II.2.2-II.2.11 va SSE -
  KHONG chan II.2.12 (bootstrap) va II.2.13 (bao loi) dung theo tai lieu.
- Thu tu kiem cho 3 API chu ky (II.5.2): header X-Trace-Id (400) -> body (@Valid, 400) -> rate
  limit (429) -> timestamp +-5p (401/102) -> tim khoa cua may (401/104) -> chu ky (401/101) -> GHI
  nonce (CHI SAU khi chu ky dung, 401/103 neu trung) -> trang thai may (121/122/123/124) -> logic
  rieng API - trien khai trong `DeviceAuthService`.
- `GlobalExceptionHandler`: KHONG BAO GIO lo message/stacktrace cua exception ra client (999/500
  chi co message co dinh), map `MethodArgumentNotValidException` sang dung cau truc
  `VALIDATION_ERROR` voi `data=[{field, code}]`.
- `SecurityHeadersFilter`: `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy` cho moi
  response.
- `/internal/ops/**` tach biet HOAN TOAN khoi `/api/v1/kiosk/**` va `/api/v1/miniapp/**`, bao ve
  bang header tinh `X-Ops-Key` (bien moi truong `OPS_API_KEY`), KHONG dung JWT - xem muc rieng
  ben duoi.
- Docker: multi-stage giong `Backend/webview/Dockerfile`, **port mac dinh 8091** (khong trung
  webview 8080, kiosk-admin 8090).

## Tham khao thiet ke (khong import code/schema)

- Mo hinh bang thiet bi/khoa/ma kich hoat dua theo thiet ke da chung minh dung cua webview:
  `Backend/webview/src/main/resources/db/migration/V1.2__kiosk_device_activation_rsa.sql` va
  `V1.3__kiosk_device_key.sql` (danh sach khoa ACTIVE/RETIRING/RETIRED, 1 khoa ACTIVE/may). kiosk-
  api tu viet entity/bang RIENG (`kiosk_device`, `kiosk_device_key`, `kiosk_activation_code`...),
  KHONG dung chung database.
- Thuat toan sinh ma kich hoat (base32 12 ky tu, bam SHA-256, TTL 24h, huy sau 5 lan sai) viet lai
  tu `Backend/kiosk-admin/.../service/KioskDeviceService.java` va `.../util/HashUtil.java` - cung
  logic, package/class moi hoan toan (`vn.baotinmanhhai.kioskapi.device.*`).
- Phong cach rate limiter in-memory tham khao `Backend/kiosk-admin/.../LoginRateLimiterService.java`.

## Seam van hanh noi bo (KHONG thuoc tai lieu tich hop)

Du an khong co admin UI rieng trong pham vi cong viec nay, nhung II.2.14 (kich hoat may) can mot
may o trang thai PENDING kem ma kich hoat da ton tai truoc - khong co cach nao khac de dua du
lieu test vao he thong. Vi vay them 2 endpoint noi bo, ro rang tach khoi API cong khai:

| Method | Path | Mo ta |
|---|---|---|
| POST | `/internal/ops/devices` | Tao may PENDING + phat ma kich hoat dau tien (tra plaintext MOT LAN) |
| POST | `/internal/ops/devices/{deviceCode}/revoke` | Thu hoi may (set REVOKED, gui `device-revoked` qua SSE neu dang mo) |

Bao ve bang header `X-Ops-Key` phai khop bien moi truong `OPS_API_KEY`. **Day khong phai mot phan
cua dac ta BTMH** - mot trien khai thuc te nghiem tuc hon nen thay the bang mot cong cu quan tri
noi bo rieng (vi du ket noi vao `kiosk-admin` hoac mot portal that).

## Danh sach 16 API + SSE

Tat ca duoi `/api/v1/kiosk/**` va `/api/v1/miniapp/**`, tru 3 dong dau can `Authorization: Bearer
<accessToken>`.

| # | Method | Path | Ma tai lieu |
|---|---|---|---|
| 1 | POST | `/api/v1/kiosk/device-sessions` | II.2.1 - Lay token thiet bi (chu ky, khong Bearer) |
| 2 | GET | `/api/v1/kiosk/ads/playlist` | II.2.2 - Danh sach phat quang cao |
| 3 | GET | `/api/v1/miniapp/system-configs` | II.2.3 - Gio mo ban (legacy) |
| 4 | GET | `/api/v1/miniapp/banners` | II.2.4 - Banner |
| 5 | GET | `/api/v1/miniapp/categories` | II.2.5 - Danh muc san pham |
| 6 | GET | `/api/v1/miniapp/products` | II.2.6 - Danh sach san pham + gia |
| 7 | POST | `/api/v1/miniapp/products/search` | II.2.7 - Tim kiem san pham |
| 8 | GET | `/api/v1/miniapp/products/{id}` | II.2.8 - Chi tiet san pham |
| 9 | GET | `/api/v1/miniapp/provinces/with-stores` | II.2.9 - Tinh/thanh co cua hang |
| 10 | GET | `/api/v1/miniapp/stores/by-province` | II.2.10 - Cua hang theo tinh/thanh |
| 11 | GET | `/api/v1/miniapp/limits/active` | II.2.11 - Han muc giao dich |
| 12 | GET | `/api/v1/kiosk/bootstrap` | II.2.12 - Cau hinh khoi dong |
| 13 | POST | `/api/v1/kiosk/errors` | II.2.13 - Bao loi tu may |
| 14 | POST | `/api/v1/kiosk/devices/enroll` | II.2.14 - Kich hoat may (chu ky, khong Bearer) |
| 15 | POST | `/api/v1/kiosk/devices/rotate-key` | II.2.15 - Xoay khoa (2 chu ky, khong Bearer) |
| 16 | GET | `/api/v1/kiosk/prices/stream` | II.3 - SSE gia realtime |

## Cach chay

1. Chuan bi Postgres rong, tao database (vi du `kiosk_api`), set cac bien moi truong (xem
   `application.yml` cho ten day du va gia tri mac dinh):
   - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
   - `KIOSK_API_JWT_SECRET` (>= 32 byte, rieng cho du an nay)
   - `OPS_API_KEY` (pre-shared key cho `/internal/ops/**`)
   - `SERVER_PORT` (mac dinh 8091)
2. Chay: `./mvnw.cmd spring-boot:run` (hoac `./mvnw spring-boot:run` tren Linux/Mac). Tren
   **Windows**, neu Postgres bao loi `FATAL: invalid value for parameter "TimeZone": "Asia/Saigon"`
   (JDBC driver khong nhan dien alias timezone nay tren JVM Windows), chay voi
   `JAVA_TOOL_OPTIONS="-Duser.timezone=UTC" ./mvnw.cmd spring-boot:run` - da xac nhan can thiet khi
   test that tren moi truong nay, cung mot loi da biet o `Backend/kiosk-admin`.
3. `ddl-auto=update` se tu tao schema khi khoi dong lan dau (khong can migration thu cong).
4. Tao may test de bat dau Luong 1 (kich hoat):
   ```bash
   curl -X POST http://localhost:8091/internal/ops/devices \
     -H "X-Ops-Key: <OPS_API_KEY>" -H "X-Trace-Id: <uuid>" -H "Content-Type: application/json" \
     -d '{"deviceCode":"KIOSK-TT-01","storeId":12,"storeName":"Chi nhanh Ngo Quyen"}'
   ```
   Response tra `activationCode` plaintext MOT LAN - dung ngay de goi `POST
   /api/v1/kiosk/devices/enroll` (ky bang private key App tu sinh, xem muc II.5.2 cua tai lieu).

## Kiem thu da thuc hien

- `./mvnw.cmd -q -DskipTests compile` - **PASS**.
- `./mvnw.cmd -q test` - **PASS**, 20/20 test, 5 class: `KioskSignatureTestVectorTest` (7, test
  vector chinh thuc trong tai lieu), `VietnameseTextTest` (4), `ProductServiceSearchTest` (4),
  `ProvinceStoreServiceTest` (2), `ErrorReportServiceTest` (3).
- **Da chay THAT voi Postgres 15 that (Docker, du lieu da xoa sau khi test xong) va goi HTTP THAT
  (curl + chu ky RSASSA-PSS ky bang openssl, khong phai mock)** - khong con la "chi compile +
  mock" nhu ban truoc. Da xac nhan hoat dong dung qua request/response that:
  - Kich hoat may (II.2.14): ca luong tao moi VA luong goi lai idempotent (cung publicKey -> 200
    "lap lai" thay vi 201), VA chu ky bi sua (timestamp khac voi luc ky) bi tu choi dung 401/101.
  - Lay token (II.2.1), xoay khoa (II.2.15): xoay khoa xong, CA khoa cu lan khoa moi deu lay token
    duoc trong 10 phut chong lap (dung thiet ke da ghi trong muc "Nhung diem con ban khoan").
  - Thu hoi may qua `/internal/ops/devices/{code}/revoke`: request ke tiep tra dung 403/121 NGAY,
    khong can doi token het han.
  - Bootstrap + ETag: lan 2 goi kem `If-None-Match` dung gia tri ETag lan 1 -> tra dung 304, 0
    byte body.
  - Tim san pham (II.2.7) voi dung vi du cua tai lieu: query "kim gia bao" (khong dau) khop dung
    san pham ten "Đồng Xu Kim Gia Bảo Hoa Sen..." (co dau) - xac nhan VietnameseText.fold() hoat
    dong dung tren du lieu that, khong chi trong unit test.
  - Tinh/thanh co cua hang (II.2.9): seed 2 tinh, chi 1 tinh co cua hang - phat hien bang test that
    bang 1 loi (xem muc "Da sua" ben duoi).
  - Bao loi (II.2.13) + gioi han 1 lan/phut: goi lan 2 trong cung phut tra dung 429 kem header
    `Retry-After`.
  - SSE (II.3): nhan dung `snapshot` ngay luc ket noi voi du lieu that; mo ket noi thu 2 cho CUNG
    1 deviceCode -> ket noi 1 nhan dung su kien `replaced` roi dong, ket noi 2 nhan snapshot rieng.
  - Cac loi chung: thieu/sai dinh dang `X-Trace-Id` -> 400 dung truoc moi xu ly khac; thieu
    `Authorization` -> 401; `pageSize=101` -> 400 `VALIDATION_ERROR` dung cau truc `{field, code}`.
  - **Phat hien va sua 2 loi THAT nho qua lan test nay** (xem commit `1e2bb92`): (1) job don dep
    nonce dinh ky (`@Scheduled` moi 5 phut) thieu `@Transactional` tren 1 query `@Modifying`, nen
    tu luc app chay chua bao gio don dep thanh cong lan nao (bi nuot loi am tham, khong crash app
    nhung chuc nang khong hoat dong); (2) API tinh/thanh co cua hang tra ve CA tinh co 0 cua hang
    bat, sai voi chinh mo ta chuc nang cua no.
  - **Luu y ve moi truong Windows**: chay that tren Windows can them `-Duser.timezone=UTC` khi
    khoi dong (`java -Duser.timezone=UTC -jar ...`), neu khong Postgres JDBC driver se loi `FATAL:
    invalid value for parameter "TimeZone": "Asia/Saigon"` - cung loi da biet va ghi trong README
    cua `kiosk-admin`, khong phai loi rieng cua du an nay.

## Ngoai pham vi (can lam them truoc khi dua vao van hanh thuc te)

- **Khong co admin UI** de quan ly ngay-qua-ngay noi dung (quang cao, banner, danh muc, san pham,
  gia, cua hang, han muc, cau hinh kenh). Hien tai phai sua truc tiep bang SQL, hoac xay mot cong
  cu quan tri rieng (co the noi tiep tu `kiosk-admin`).
- **Khong co nguon gia thuc** day vao he thong - `PriceStreamService.broadcastPriceChange(...)` va
  `broadcastSnapshot()` da viet san nhung chua co job/nguon nao goi toi; can noi voi he thong gia
  vang thuc cua BTMH.
- **Khong scale ngang duoc** - xem muc "Khong Redis" tren; rate limit/nonce/SSE registry deu gia
  dinh 1 instance duy nhat.
- **Trang thai rate limit mat khi restart** (trong-tien-trinh, khong ben bi) - chap nhan duoc cho
  muc dich chong lam dung don gian, khong phai co che chong DDoS nghiem tuc.
- **Sap xep tinh/thanh, cua hang theo bang chu cai tieng Viet** dung `java.text.Collator` voi
  locale "vi" - xap xi, chua duoc kiem chung ky voi moi truong hop dau/chu dac biet.
- Khong co job don dep dinh ky cho bang `kiosk_device_nonce` ngoai `@Scheduled` moi 5 phut da co
  (du nay da du dung cho muc dich chong phat lai 10 phut).

### Da sua trong lan ra soat thu 2 (sau bao cao dau tien)

- **ETag/If-None-Match** cho II.2.2 (`ads/playlist`) va II.2.12 (`bootstrap`) da trien khai
  (`EtagSupport` + `WebRequest.checkNotModified()`): khong doi -> 304 khong body, dung header
  `If-None-Match` cua request. Rieng bootstrap: ETag chi hash phan "on dinh" (gio mo/dong cua,
  hotline, idleTimeoutSeconds...), KHONG hash `serverTime` (doi moi lan goi) - neu khong 304 se
  khong bao gio xay ra vi noi dung luon khac nhau.
- **Tim kiem san pham (II.2.7) gio khong phan biet dau** ("kim gia bao" khop "Kim Gia Bảo"): loc o
  tang ung dung bang `VietnameseText.fold()` (NFD + bo dau ket hop + `đ`/`Đ` rieng + lowercase)
  thay vi Postgres `unaccent` - chu dich KHONG dung extension DB can quyen `CREATE EXTENSION` (co
  the khong co tren moi moi truong trien khai) va vi du an nay khong co admin UI nen du lieu san
  pham co the duoc nhap thang bang SQL ngoai JPA (mot cot "ten da bo dau" luu san se khong chac
  duoc dong bo trong truong hop do). Xem `VietnameseTextTest` va `ProductServiceSearchTest`.
- **`keyRotationRequired` (12 thang)** gio tinh bang `ChronoUnit.MONTHS` (thang duong lich that),
  khong con xap xi `Duration.ofDays(365)`.
- **Gioi han goi (60/phut mac dinh) gio ap dung DUNG cho TAT CA API qua Bearer token**: ban dau chi
  II.2.1/II.2.12 (10/phut), II.2.13 (1/phut) va mo SSE (6/phut) tu goi `RateLimiterService` rieng
  le trong tung controller - 8 API con lai (ads/playlist, banner, category, product list/search/
  detail, province, store, limit) **khong he co gioi han goi nao**. Da chuyen tap trung toan bo
  viec kiem gioi han vao `DeviceAuthFilter` (map `RATE_LIMIT_OVERRIDES` cho 3 ngoai le, mac dinh
  60/phut cho moi duong dan con lai) - dong thoi sua luon diem "rate limit o errors kiem SAU khi
  da parse JSON body": gio kiem o filter, chay TRUOC ca DispatcherServlet/controller parse body.

## Nhung diem con bang khoan / da don gian hoa do dac ta chua ro rang

- **`II.2.14` khi khong tim thay deviceCode**: tai lieu ghi code 111 ("Ma kich hoat khong dung")
  bao gom ca truong hop "ma may khong ton tai" - da trien khai dung vay, nhung thu tu kiem chi
  tiet (vi du: kiem deviceCode ton tai TRUOC hay SAU khi so khop ma kich hoat) khong duoc tai lieu
  liet ke tuan tu ro rang - da chon thu tu hop ly nhat (device not found -> 111 ngay).
- **II.2.1/II.2.15**: khi dang trong giai doan chong lap (RETIRING), ca khoa cu va khoa moi deu
  duoc chap nhan de ky `device-sessions` - suy luan tu cau "App thu II.2.1 bang khoa moi truoc,
  nhan code 104 thi quay ve khoa cu", ham y khoa cu con dung duoc trong giai doan nay; tai lieu
  khong noi ro 100% nhung day la cach doc hop ly duy nhat de luong "khoi dong lai giua chung" hoat
  dong.
- **storeId/storeName trong II.2.1/II.2.12** lay tu `kiosk_device.store_id/store_name` (gan luc
  tao may qua `/internal/ops/devices`) - KHAC voi danh sach cua hang nhan hang (II.2.9/II.2.10, so
  huu rieng boi `kiosk_store`). Day la 2 khai niem khac nhau trong tai lieu (noi dat may vs noi
  khach nhan hang) - da tach thanh 2 bang rieng.

---

mvnw/mvnw.cmd/.mvn/ duoc copy tu `Backend/kiosk-admin` (cung Maven wrapper) - dung `./mvnw.cmd
<goal>` theo dung quy uoc cac repo khac trong BTMH, khong can cai Maven rieng.
