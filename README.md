# Soccer Field Booking API

REST API quản lý đặt sân bóng đá: chi nhánh, sân, bảng giá, lịch đặt và tài khoản người
dùng. Xây dựng bằng Spring Boot 3.3.13 trên Java 21, dữ liệu lưu ở MySQL, xác thực bằng
JWT với access token ngắn hạn và refresh token có xoay vòng.

Hệ thống chống đặt trùng giờ bằng khoá bi quan, tính giá theo khung giờ và ngày trong
tuần, và tự động chuyển trạng thái lịch đặt bằng các job định kỳ.

---

## Mục lục

- [Tech stack](#tech-stack)
- [Kiến trúc](#kiến-trúc)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)
- [Chạy dự án](#chạy-dự-án)
- [Mô hình dữ liệu](#mô-hình-dữ-liệu)
- [Nghiệp vụ đặt sân](#nghiệp-vụ-đặt-sân)
- [Xác thực và phân quyền](#xác-thực-và-phân-quyền)
- [API reference](#api-reference)
- [Định dạng response](#định-dạng-response)
- [Design decisions](#design-decisions)
- [Vấn đề đã biết](#vấn-đề-đã-biết)
- [Roadmap](#roadmap)
- [Changelog](#changelog)

---

## Tech stack

| Thành phần | Lựa chọn |
| --- | --- |
| Ngôn ngữ | Java 21 |
| Framework | Spring Boot 3.3.13 |
| Bảo mật | Spring Security 6, OAuth2 Resource Server, Nimbus JOSE JWT |
| Dữ liệu | Spring Data JPA, Hibernate, MySQL 8 |
| Validation | Jakarta Bean Validation |
| Tài liệu API | springdoc-openapi 2.6, Swagger UI |
| Tiện ích | Lombok |
| Build và đóng gói | Maven Wrapper, Docker, Docker Compose |

---

## Kiến trúc

Luồng xử lý đi qua bốn lớp, mỗi lớp chỉ phụ thuộc vào lớp kế dưới. Các phép tính thuần
nằm riêng trong một gói tiện ích để tầng service gọi tới.

```
Controller  ->  Service (interface + impl)  ->  Repository  ->  Entity
     |                |
    DTO  <------  Mapper
                      |
                 util/ (tính toán thuần: thời gian, giá, khung giờ trống)
```

Nguyên tắc chính:

- **Entity không rời khỏi tầng service.** Đầu vào là record request, đầu ra là record
  response. Mapper viết tay thực hiện chuyển đổi. Nhờ vậy mật khẩu không bao giờ bị
  serialize và quan hệ vòng giữa người dùng, lịch đặt và sân không gây đệ quy vô hạn.
- **Logic tính toán tách khỏi JPA và Spring.** Kiểm tra thời gian đặt sân, tính giá và
  sinh lưới khung giờ trống là các hàm thuần, nhận record chứ không nhận entity. Chúng
  test được mà không cần database hay Spring context.
- **Thời gian lấy qua một `Clock` được inject.** Không có lời gọi thời gian hệ thống rải
  rác trong code. Muốn test kịch bản "hai giờ trước giờ đá" chỉ cần thay đồng hồ.
- **Service có interface riêng.** Controller phụ thuộc abstraction, dễ thay thế và mock.
- **Response đồng nhất.** Mọi phản hồi bọc trong một phong bì chung gồm `message`,
  `statusCode`, `timestamp`, `path` và `data`.
- **Stateless.** Không có session phía server. Mỗi request tự mang danh tính trong JWT.
- **Tắt open-in-view.** Session JPA đóng khi service trả về, buộc nạp đủ dữ liệu trong
  transaction thay vì lazy-load lúc serialize.

---

## Cấu trúc thư mục

```
src/main/java/com/ms/test_api/
├── config/         Security, CORS, OpenAPI, Clock, seed dữ liệu, các job định kỳ
├── controller/     REST endpoints
├── dto/
│   ├── request/    Record đầu vào, gắn Bean Validation
│   └── response/   Record đầu ra
├── entity/         JPA entity và enum
├── exception/      Exception nghiệp vụ và bộ xử lý lỗi toàn cục
├── mapper/         Chuyển entity sang DTO
├── repository/
│   └── specification/   Bộ lọc động bằng Criteria API
├── security/       Validator token tuỳ biến và bean kiểm tra quyền sở hữu
├── service/
│   └── impl/       Cài đặt nghiệp vụ
└── util/           Chính sách đặt sân, kiểm tra thời gian, tính giá, khung giờ trống,
                    chuẩn hoá số điện thoại
```

---

## Chạy dự án

Có hai cách: chạy bằng Docker Compose, hoặc chạy trực tiếp với MySQL cài sẵn trên máy.

### Biến môi trường

Tạo file `.env` ở thư mục gốc. File này đã nằm trong danh sách bỏ qua của Git và của
Docker, không bao giờ bị commit hay đóng vào image.

```properties
# --- Bắt buộc ---
DB_USERNAME=soccer
DB_PASSWORD=your_password
JWT_SIGNER_KEY=chuoi_bi_mat_toi_thieu_64_ky_tu_vi_HS512_can_khoa_512_bit_khong_duoc_ngan_hon
CORS_ALLOWED_ORIGINS=http://localhost:3000

# --- Chỉ dùng khi chạy trực tiếp, Docker Compose tự đặt giá trị này ---
DB_URL=jdbc:mysql://localhost:3306/soccer_field_data

# --- Chỉ dùng cho container MySQL trong Docker Compose ---
MYSQL_ROOT_PASSWORD=root_password
MYSQL_USER=soccer
MYSQL_PASSWORD=your_password

# --- Tuỳ chọn: tạo tài khoản quản trị đầu tiên lúc khởi động ---
APP_BOOTSTRAP_ADMIN_USERNAME=admin
APP_BOOTSTRAP_ADMIN_PASSWORD=doi_mat_khau_nay_ngay
APP_BOOTSTRAP_ADMIN_EMAIL=admin@example.com

# --- Tuỳ chọn: nạp dữ liệu demo, chỉ dùng ở môi trường dev ---
APP_SEED_DEMO=true
```

Khoá ký phải dài tối thiểu 64 ký tự. Ứng dụng kiểm tra điều kiện này lúc khởi động và
dừng ngay nếu không đạt.

Khi chạy bằng Docker Compose, `DB_USERNAME` và `DB_PASSWORD` phải trùng với `MYSQL_USER`
và `MYSQL_PASSWORD`, vì container MySQL tạo đúng tài khoản đó cho backend dùng.

### Cách 1: Docker Compose

```bash
docker compose up --build
```

Compose dựng hai container. MySQL 8 mở ra máy chủ ở cổng 3307 để tránh đụng MySQL cài
sẵn. Backend chỉ khởi động sau khi MySQL vượt qua healthcheck. Dữ liệu nằm trong một
volume riêng nên không mất khi tắt container.

```bash
# Xoá sạch dữ liệu để nạp lại từ đầu
docker compose down -v
```

Dockerfile build theo hai giai đoạn. Giai đoạn đầu tải dependency trước rồi mới chép
mã nguồn, nên sửa code không làm tải lại toàn bộ thư viện. Giai đoạn sau chỉ chứa JRE và
file jar.

### Cách 2: Chạy trực tiếp

Yêu cầu JDK 21 và MySQL 8 đang chạy.

```bash
mysql -u root -p -e "CREATE DATABASE soccer_field_data CHARACTER SET utf8mb4;"
./mvnw spring-boot:run
```

Hibernate tự tạo bảng theo chế độ cập nhật lược đồ tự động.

### Sau khi khởi động

| Địa chỉ | Nội dung |
| --- | --- |
| `http://localhost:8080/api/v1` | API |
| `http://localhost:8080/swagger-ui.html` | Swagger UI, thử API trực tiếp trên trình duyệt |
| `http://localhost:8080/v3/api-docs` | Đặc tả OpenAPI dạng JSON |

Trong Swagger UI, gọi endpoint đăng nhập, chép access token, rồi bấm Authorize để các
request sau tự gắn token.

### Seed dữ liệu

Lúc khởi động có hai bộ seed chạy theo thứ tự cố định.

**Bộ thứ nhất luôn chạy.** Nó tạo đủ ba vai trò nếu chưa có. Nếu ba biến bootstrap được
đặt, nó tạo thêm một tài khoản quản trị đầu tiên, đúng một lần. Đổi mật khẩu ngay sau lần
đăng nhập đầu, rồi gỡ ba biến đó khỏi file môi trường.

**Bộ thứ hai chỉ chạy khi bật cờ demo.** Nó bỏ qua toàn bộ nếu database đã có chi nhánh,
nên khởi động lại không nhân đôi dữ liệu. Nội dung nạp vào:

| Loại | Dữ liệu |
| --- | --- |
| Tài khoản | `customer1`, `customer2`, `owner1`, `owner2`, cùng mật khẩu `password123` |
| Chi nhánh | Quận 1 thuộc `owner1`, Thủ Đức thuộc `owner2` |
| Sân | 5 sân: 4 sân ở Quận 1 trong đó một sân đang bảo trì, 1 sân ở Thủ Đức |
| Bảng giá | Quận 1 có giờ thường, giờ cao điểm và cuối tuần đắt hơn. Thủ Đức giá phẳng |
| Lịch đặt | 4 lịch ở sân 5B, mỗi lịch một trạng thái |

Hai chủ sân được tách ra hai chi nhánh để thử được trường hợp "là OWNER nhưng không phải
chủ sân này", phải nhận mã 403. Mật khẩu demo được mã hoá bằng chính bộ mã hoá của ứng
dụng, và giá lịch đặt mẫu được tính bằng chính dịch vụ tính giá, nên khớp với kết quả khi
gọi API thật.

Không bao giờ bật cờ demo ở môi trường thật. Xem thêm mục Vấn đề đã biết trước khi bật.

---

## Mô hình dữ liệu

| Entity | Vai trò | Quan hệ |
| --- | --- | --- |
| User | Tài khoản, đăng nhập bằng username hoặc số điện thoại | thuộc một vai trò, có nhiều lịch đặt |
| Role | Vai trò CUSTOMER, OWNER, ADMIN | có nhiều người dùng |
| Branch | Chi nhánh, có giờ mở và đóng cửa | có nhiều sân và nhiều quy tắc giá, thuộc một chủ sân tuỳ chọn |
| Field | Sân, có loại, trạng thái và giá cơ bản theo giờ | thuộc một chi nhánh |
| PricingRule | Hệ số nhân giá cho một khung giờ, áp dụng mọi ngày hoặc một thứ cụ thể | thuộc một chi nhánh |
| Booking | Lịch đặt, có trạng thái và tổng tiền đã chốt | thuộc một người dùng và một sân |
| InvalidatedToken | Token đã thu hồi | không có quan hệ |

Số điện thoại là duy nhất và luôn lưu ở dạng chuẩn quốc tế, ví dụ `+84901234567`, dù
người dùng nhập `0901 234 567` hay `84901234567`.

Bảng lịch đặt có hai chỉ mục: theo sân và ngày để kiểm tra trùng giờ nhanh, theo người
dùng và trạng thái để liệt kê lịch của một người. Bảng quy tắc giá có chỉ mục theo chi
nhánh và thứ trong tuần.

Các enum lưu dạng chuỗi trong database để dễ đọc và không vỡ khi thêm giá trị mới.

---

## Nghiệp vụ đặt sân

### Chính sách

Các hằng số nghiệp vụ tập trung trong một lớp chính sách duy nhất.

| Quy tắc | Giá trị |
| --- | --- |
| Độ dài một ô giờ | 30 phút |
| Thời lượng một lịch đặt | 60 đến 180 phút |
| Đặt trước tối đa | 30 ngày |
| Lịch chờ tự hết hạn sau | 30 phút |
| Hạn chót huỷ | 2 giờ trước giờ đá |

### Vòng đời lịch đặt

```
               chủ sân xác nhận             hết giờ đá
   PENDING  ─────────────────────>  CONFIRMED  ─────────>  COMPLETED
     │  │                              │    │                (job)
     │  │ quá 30 phút chưa xác nhận    │    │
     │  └─────────> EXPIRED (job)      │    │
     │                                 │    │
     └── khách huỷ / chủ sân từ chối ──┴────┴──>  CANCELLED
```

| Trạng thái | Chiếm khung giờ | Ai đưa vào |
| --- | --- | --- |
| PENDING | Có | Khách tạo lịch |
| CONFIRMED | Có | Chủ chi nhánh hoặc ADMIN xác nhận |
| CANCELLED | Không | Khách huỷ, hoặc chủ chi nhánh hoặc ADMIN từ chối |
| EXPIRED | Không | Job, khi lịch chờ quá 30 phút |
| COMPLETED | Không | Job, khi giờ kết thúc đã qua |

Chỉ hai trạng thái đầu chiếm khung giờ. Lịch đã huỷ hoặc hết hạn nhả chỗ ngay cho người
khác đặt.

Khách chỉ huỷ được lịch của chính mình, khi lịch đang chờ hoặc đã xác nhận, và phải còn ít
nhất 2 giờ trước giờ đá. Chủ sân từ chối được cả lịch đã xác nhận, không bị giới hạn hạn
chót đó.

### Kiểm tra khi tạo lịch

Một yêu cầu đặt sân phải qua lần lượt các bước sau:

1. Sân tồn tại và đang hoạt động.
2. Giờ kết thúc sau giờ bắt đầu.
3. Ngày đặt không ở quá khứ và không xa quá 30 ngày.
4. Giờ bắt đầu và kết thúc rơi đúng mốc 30 phút, ví dụ 18:00 hoặc 18:30.
5. Thời lượng từ 60 đến 180 phút.
6. Toàn bộ khoảng giờ nằm trong giờ mở cửa của chi nhánh.
7. Nếu đặt cho hôm nay, giờ bắt đầu phải sau thời điểm hiện tại.
8. Không chồng lấn với lịch nào đang chiếm khung giờ trên cùng sân, cùng ngày.

Bước 1 đến 7 sai trả mã 400. Bước 8 sai trả mã 409.

### Chống đặt trùng giờ

Hai người cùng bấm đặt một khung giờ trong cùng một khoảnh khắc là tình huống thật, không
phải lý thuyết. Kiểm tra trùng rồi mới ghi là không đủ, vì cả hai request có thể cùng đọc
thấy "còn trống" trước khi bên nào kịp ghi.

Hệ thống giải quyết bằng cách khoá dòng dữ liệu của sân trước khi kiểm tra trùng. Request
thứ hai phải đợi request thứ nhất commit xong mới đọc được, và lúc đó nó thấy khung giờ
đã bị chiếm. Khoá có thời gian chờ tối đa 3 giây. Quá thời gian đó, người dùng nhận mã 409
kèm lời nhắn thử lại, thay vì request treo vô hạn.

Hai khoảng giờ được coi là chồng nhau theo quy ước nửa mở. Lịch 18:00 đến 19:00 và lịch
19:00 đến 20:00 nối tiếp nhau, không chồng.

Xác nhận, từ chối và huỷ cũng khoá dòng của lịch đặt, nên một job hết hạn chạy đúng lúc
chủ sân bấm xác nhận không thể làm lệch trạng thái.

### Tính giá

Mỗi sân có một giá cơ bản theo giờ. Mỗi chi nhánh có một bảng quy tắc giá, mỗi quy tắc
gồm một khung giờ và một hệ số nhân. Quy tắc có thể áp dụng cho mọi ngày, hoặc chỉ cho
một thứ cụ thể trong tuần.

Giá được tính theo từng ô 30 phút. Với mỗi ô, hệ số được chọn theo thứ tự ưu tiên:

1. Quy tắc riêng cho đúng thứ đó và phủ giờ bắt đầu của ô.
2. Nếu không có, quy tắc chung cho mọi ngày phủ giờ đó.
3. Nếu không có nữa, hệ số bằng 1.

Ví dụ với bảng giá demo ở Quận 1, sân giá cơ bản 300.000 đồng mỗi giờ:

| Khung giờ | Ngày thường | Thứ Bảy, Chủ Nhật |
| --- | --- | --- |
| 06:00 đến 17:00 | hệ số 1,0 | hệ số 1,0 |
| 17:00 đến 22:00 | hệ số 1,5 | hệ số 2,0 |

Đặt 16:00 đến 18:00 ngày thứ Hai sẽ có một giờ giá thường và một giờ cao điểm, tổng
750.000 đồng.

Giá được tính một lần lúc tạo lịch và chốt cứng vào lịch đặt. Chủ sân đổi bảng giá về sau
không làm thay đổi giá của lịch đã đặt.

### Xem trước khi đặt

Hai endpoint công khai giúp client hiển thị lịch và giá mà không cần đăng nhập:

- **Khung giờ trống** trả về lưới ô 30 phút trong giờ mở cửa của một ngày. Mỗi ô có cờ
  còn trống hay không, và đơn giá theo giờ tại ô đó. Sân đang bảo trì có toàn bộ ô bị
  khoá. Nếu xem lịch hôm nay, các ô đã qua cũng bị khoá.
- **Báo giá** nhận ngày, giờ bắt đầu và giờ kết thúc, chạy cùng bộ kiểm tra thời gian như
  lúc đặt thật, rồi trả về tổng tiền. Số tiền này khớp chính xác với số tiền sẽ bị tính
  khi tạo lịch.

### Job định kỳ

| Job | Tần suất | Việc làm |
| --- | --- | --- |
| Hết hạn lịch chờ | Mỗi phút | Lịch PENDING tạo quá 30 phút chuyển sang EXPIRED |
| Hoàn tất lịch | Mỗi phút | Lịch CONFIRMED đã qua giờ kết thúc chuyển sang COMPLETED |
| Dọn token | 3 giờ sáng mỗi ngày | Xoá bản ghi token thu hồi đã quá hạn |

Hai job đầu dùng một câu cập nhật hàng loạt duy nhất thay vì tải từng lịch lên. Tần suất
quét đổi được qua thuộc tính `app.booking.expiry-scan-interval`, mặc định một phút.

---

## Xác thực và phân quyền

### Đăng nhập

Người dùng đăng nhập bằng một trường định danh duy nhất kèm mật khẩu. Nếu định danh trông
như số điện thoại Việt Nam, hệ thống chuẩn hoá rồi tìm theo số điện thoại. Ngược lại, hệ
thống tìm theo username.

Hai cách hiểu này không bao giờ nhầm nhau, vì username bắt buộc bắt đầu bằng chữ cái và
chỉ chứa chữ, số và dấu gạch dưới.

Sai định danh hay sai mật khẩu đều trả về cùng một thông báo, để kẻ dò không biết tài
khoản nào có thật.

### Hai loại token

| | Access token | Refresh token |
| --- | --- | --- |
| Thời hạn | 15 phút | 7 ngày |
| Dùng để | Gọi API | Lấy cặp token mới |
| Chứa vai trò | Có | Không |
| Gửi kèm request | Header Authorization dạng Bearer | Chỉ trong body của refresh và logout |

Cả hai đều ký HS512 và mang một định danh duy nhất ở claim `jti`, cùng một claim phân biệt
loại token.

### Luồng

1. **Đăng nhập.** Kiểm tra mật khẩu bằng BCrypt, phát hành cặp token.
2. **Gọi API.** Bộ giải mã xác minh chữ ký, hạn dùng, nhà phát hành, loại token và danh
   sách thu hồi. Vai trò lấy từ claim `scope`, thêm tiền tố `ROLE_`.
3. **Làm mới.** Refresh token cũ bị thu hồi ngay khi dùng, cặp token mới được phát hành.
   Đây là cơ chế xoay vòng: một refresh token chỉ dùng được đúng một lần.
4. **Đăng xuất.** Thu hồi cả access token đang dùng lẫn refresh token gửi kèm. Nếu refresh
   token đã hỏng hoặc hết hạn, logout vẫn thành công vì mục tiêu đã đạt.

### Thu hồi token

Token bị thu hồi lưu trong bảng `invalidated_tokens` theo định danh `jti`. Job dọn dẹp
hằng ngày xoá các bản ghi đã quá hạn, vì token hết hạn tự nó đã vô hiệu.

### Bốn lớp validator

Bộ giải mã JWT ghép bốn kiểm tra chạy tuần tự:

| Validator | Từ chối khi |
| --- | --- |
| Kiểm tra thời gian | Token hết hạn hoặc chưa tới hạn dùng |
| Kiểm tra nhà phát hành | Không khớp cấu hình |
| Kiểm tra loại token | Dùng refresh token để gọi API |
| Kiểm tra danh sách thu hồi | Token đã bị thu hồi hoặc thiếu định danh |

### Phân cấp vai trò

Ba vai trò xếp thành một chuỗi bao hàm: ADMIN bao hàm OWNER, OWNER bao hàm CUSTOMER. Một
endpoint chỉ cần khai báo mức thấp nhất được phép, các vai trò cao hơn tự động đi qua.

### Ba tầng phân quyền

| Tầng | Nơi khai báo | Trả lời câu hỏi |
| --- | --- | --- |
| Đường dẫn | Cấu hình security | Endpoint này có công khai không |
| Vai trò | Chú thích trên controller hoặc service | Vai trò nào được gọi |
| Quyền sở hữu | Bean kiểm tra quyền gọi trong biểu thức SpEL | Bản ghi cụ thể này có thuộc về người gọi không |

Bean kiểm tra quyền sở hữu trả lời ba câu hỏi: người này có sở hữu chi nhánh đó không, có
sở hữu sân đó không, và có sở hữu chi nhánh chứa sân của lịch đặt đó không.

Những thao tác đi qua cả ba tầng:

- **Tạo và sửa sân.** Phải mang vai trò OWNER trở lên, và phải là chủ chi nhánh đích hoặc
  là ADMIN. Không được chuyển sân sang chi nhánh khác.
- **Xác nhận và từ chối lịch đặt.** Phải là chủ chi nhánh chứa sân đó, hoặc là ADMIN.
- **Xem một lịch đặt.** Người đã đặt, chủ chi nhánh chứa sân, hoặc ADMIN.
- **Tìm kiếm lịch đặt.** Chủ sân chỉ thấy lịch thuộc chi nhánh mình. Điều kiện này được
  thêm thẳng vào câu truy vấn, client không đặt được và cũng không bỏ được. ADMIN thấy
  toàn bộ.

### Thay đổi vai trò

Chỉ quản trị viên đổi được vai trò của người khác. Có hai ràng buộc:

- Không hạ vai trò một chủ sân đang còn quản lý chi nhánh. Phải chuyển giao chi nhánh
  trước, nếu không hệ thống từ chối với mã 409.
- Vai trò mới **chưa có hiệu lực ngay** với access token đang lưu hành, vì vai trò nằm
  trong token chứ không đọc lại từ database mỗi request. Độ trễ tối đa bằng thời hạn
  access token, tức 15 phút. Mỗi lần đổi vai trò đều được ghi cảnh báo vào log.

---

## API reference

Tiền tố chung là `/api/v1`. Cột quyền ghi Public nghĩa là không cần token. Danh sách đầy
đủ kèm schema xem tại Swagger UI.

### Auth

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/auth/login` | Public | Đăng nhập bằng username hoặc số điện thoại, nhận cặp token |
| POST | `/auth/refresh` | Public | Xoay vòng refresh token |
| POST | `/auth/logout` | Đã đăng nhập | Thu hồi access và refresh token |

Body đăng nhập:

```json
{ "identifier": "0901234567", "password": "password123" }
```

### Users

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/users` | Public | Đăng ký, mặc định vai trò CUSTOMER |
| GET | `/users` | ADMIN | Danh sách người dùng |
| GET | `/users/me` | Đã đăng nhập | Hồ sơ của chính mình |
| PUT | `/users/me` | Đã đăng nhập | Tự sửa hồ sơ của mình |
| GET | `/users/{username}` | ADMIN hoặc chính chủ | Xem một hồ sơ |
| PUT | `/users/{id}` | ADMIN | Cập nhật hồ sơ người khác |
| PATCH | `/users/{id}/role` | ADMIN | Đổi vai trò, xem lưu ý về độ trễ token |
| DELETE | `/users/{id}` | ADMIN | Xoá tài khoản |

Số điện thoại khi đăng ký và cập nhật là tuỳ chọn. Nếu có, nó phải là số di động Việt Nam
hợp lệ và chưa bị tài khoản khác dùng.

### Roles

| Method | Endpoint | Quyền |
| --- | --- | --- |
| GET | `/roles` | ADMIN |
| GET | `/roles/{id}` | ADMIN |

### Branches

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| GET | `/branches` | Public | Danh sách chi nhánh |
| GET | `/branches/{id}` | Public | Chi tiết một chi nhánh |
| GET | `/branches/me` | OWNER | Chi nhánh mình quản lý |
| POST | `/branches` | ADMIN | Tạo chi nhánh, gán chủ qua `ownerId` |
| PUT | `/branches/{id}` | ADMIN | Cập nhật, đổi chủ qua `ownerId` |
| DELETE | `/branches/{id}` | ADMIN | Xoá chi nhánh |

Trường `ownerId` là tuỳ chọn. Bỏ trống nghĩa là chi nhánh chưa có chủ. Tài khoản được gán
phải đang mang vai trò OWNER, nếu không request bị từ chối với mã 400.

### Fields

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| GET | `/fields` | Public | Tìm kiếm có lọc và phân trang |
| GET | `/fields/{id}` | Public | Chi tiết một sân |
| GET | `/fields/{id}/availability?date=` | Public | Lưới khung giờ trống kèm đơn giá |
| GET | `/fields/{id}/quote?date=&startTime=&endTime=` | Public | Báo giá cho một khoảng giờ |
| POST | `/fields` | OWNER của chi nhánh đích, hoặc ADMIN | Tạo sân |
| PUT | `/fields/{id}` | OWNER của sân đó, hoặc ADMIN | Cập nhật sân |
| DELETE | `/fields/{id}` | ADMIN | Xoá sân |

Ngày theo định dạng `yyyy-MM-dd`, giờ theo định dạng `HH:mm`. Tham số lọc khi tìm sân:
`branchName`, `district`, `fieldType`, `status`, `minPrice`, `maxPrice`, cộng `page`,
`size` và `sort`.

### Bookings

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| GET | `/bookings` | ADMIN xem tất cả, OWNER xem chi nhánh mình | Tìm kiếm |
| GET | `/bookings/me` | Đã đăng nhập | Lịch đặt của chính mình |
| GET | `/bookings/{id}` | Người đã đặt, chủ chi nhánh, ADMIN | Chi tiết một lịch đặt |
| POST | `/bookings` | Đã đăng nhập | Tạo lịch đặt ở trạng thái chờ |
| PATCH | `/bookings/{id}/confirm` | Chủ chi nhánh, ADMIN | Xác nhận lịch đang chờ |
| PATCH | `/bookings/{id}/reject` | Chủ chi nhánh, ADMIN | Từ chối lịch đang chờ hoặc đã xác nhận |
| PATCH | `/bookings/{id}/cancel` | Người đã đặt | Huỷ lịch, chậm nhất 2 giờ trước giờ đá |
| DELETE | `/bookings/{id}` | ADMIN | Xoá lịch đặt |

Body tạo lịch:

```json
{ "fieldId": 1, "bookingDate": "2026-10-10", "startTime": "18:00", "endTime": "19:30" }
```

Tham số lọc khi tìm lịch đặt: `userId`, `username`, `branchName`, `status`, `bookingDate`,
cộng phân trang.

---

## Định dạng response

Thành công:

```json
{
  "message": "Booking created successfully",
  "statusCode": 201,
  "timestamp": "2026-10-06T14:30:00",
  "data": { "id": 42, "bookingDate": "2026-10-10", "status": "PENDING", "totalPrice": 900000.00 }
}
```

Lỗi validation:

```json
{
  "message": "Validation failed",
  "statusCode": 400,
  "timestamp": "2026-10-06T14:30:00",
  "path": "/api/v1/users",
  "data": {
    "email": "Email is not in correct format",
    "password": "Password must be at least 8 characters"
  }
}
```

| Mã | Ý nghĩa |
| --- | --- |
| 400 | Dữ liệu sai, vi phạm chính sách thời gian đặt sân |
| 401 | Thiếu token, token sai, hết hạn hoặc đã bị thu hồi |
| 403 | Không đủ vai trò, hoặc không sở hữu bản ghi |
| 404 | Không tìm thấy |
| 409 | Trùng giờ, đang có người khác đặt cùng lúc, trạng thái không cho phép chuyển, trùng dữ liệu |
| 500 | Lỗi không lường trước |

Lỗi phát sinh trong filter chain, trước khi tới controller, được hai lớp xử lý riêng ghi
JSON, nên client luôn nhận cùng một cấu trúc dù lỗi xảy ra ở đâu.

---

## Design decisions

### Đặt sân

**Vì sao booking yêu cầu đăng nhập.** Guest booking giảm ma sát nhưng mở ra khả năng đặt
kín sân hàng loạt vì không có gì ràng buộc trách nhiệm. Phương án đã cân nhắc: shadow user
kết hợp xác minh OTP và booking tự hết hạn sau 15 phút. Chưa triển khai vì phụ thuộc SMS
provider bên ngoài, nằm ngoài phạm vi đợt này.

**Vì sao khoá dòng của sân thay vì dùng ràng buộc duy nhất.** Ràng buộc duy nhất chỉ chặn
được hai lịch trùng y hệt giờ bắt đầu. Nó không chặn được lịch 18:00 đến 19:30 chồng lên
lịch 19:00 đến 20:00. Khoá lạc quan bằng số phiên bản cũng không hợp, vì lịch mới là một
dòng mới, không có dòng cũ nào để so phiên bản. Khoá dòng của sân tuần tự hoá đúng những
request cần tuần tự, tức các request đặt cùng một sân, còn các sân khác vẫn chạy song song.

**Vì sao lịch đang chờ vẫn chiếm chỗ.** Nếu lịch chờ không chiếm chỗ, hai khách có thể
cùng giữ một khung giờ, và chủ sân phải chọn ai thắng. Cho lịch chờ chiếm chỗ thì công
bằng hơn, nhưng sinh ra rủi ro khách bấm đặt rồi bỏ đi. Job hết hạn sau 30 phút là cái giá
trả cho lựa chọn đó.

**Vì sao job hết hạn quét mỗi phút.** Quét mỗi 30 phút thì một lịch có thể giữ chỗ tới gần
60 phút. Quét mỗi phút đưa sai số về dưới một phút. Câu cập nhật dựa trên chỉ mục và hầu
như không khớp dòng nào, nên chạy thường xuyên vẫn rẻ.

**Vì sao chia giờ theo ô 30 phút.** Cho phép giờ tuỳ ý sẽ sinh ra những khoảng trống lẻ 10
hay 15 phút giữa hai lịch, không ai đặt được. Ô 30 phút giữ lưới gọn, đủ linh hoạt cho trận
một tiếng rưỡi, và khớp với cách chủ sân thật vẫn chia giờ.

**Vì sao bảng giá dùng hệ số nhân thay vì giá tuyệt đối.** Một chi nhánh có nhiều sân với
giá cơ bản khác nhau. Lưu giá tuyệt đối thì mỗi sân cần một bảng giá riêng. Lưu hệ số thì
một bảng giá cấp chi nhánh dùng chung cho mọi sân, và đổi giá cơ bản của một sân không
phải sửa bảng giá.

**Vì sao quy tắc theo thứ ghi đè quy tắc chung.** Chủ sân thường nghĩ theo kiểu "giá cao
điểm là 1,5, nhưng cuối tuần là 2". Cho phép quy tắc riêng chồng lên quy tắc chung diễn
đạt đúng ý đó mà không phải cắt bảng giá chung thành từng mảnh cho từng ngày.

**Vì sao chốt giá lúc đặt.** Khách đã thấy và đồng ý một số tiền. Nếu giá đi theo bảng giá
hiện hành, chủ sân tăng giá sau đó sẽ khiến khách phải trả nhiều hơn số đã thoả thuận.

**Vì sao báo giá chạy cùng bộ kiểm tra với lúc đặt.** Nếu báo giá dễ dãi hơn, client có thể
hiển thị giá cho một khoảng giờ mà lúc đặt thật lại bị từ chối. Dùng chung một bộ kiểm tra
và một hàm tính giá đảm bảo hai con số luôn khớp.

**Vì sao logic tính toán là hàm thuần.** Tính giá, sinh lưới giờ trống và kiểm tra thời gian
là nơi dễ sai nhất và nhiều trường hợp biên nhất. Tách chúng khỏi JPA và Spring cho phép
viết test chạy trong mili giây, không cần dựng database.

**Vì sao inject đồng hồ.** Các quy tắc như "không huỷ trong 2 giờ trước giờ đá" hay "lịch
chờ hết hạn sau 30 phút" không thể test đáng tin nếu code gọi thẳng thời gian hệ thống.
Đồng hồ được inject cho phép test cố định một thời điểm bất kỳ.

### Xác thực

**Vì sao cho đăng nhập bằng số điện thoại.** Ở Việt Nam, khách đặt sân quen dùng số điện
thoại hơn username. Ràng buộc username bắt đầu bằng chữ cái giúp một ô nhập duy nhất phân
biệt được hai loại định danh mà không cần hỏi người dùng.

**Vì sao chuẩn hoá số điện thoại trước khi lưu.** Cùng một số có thể được gõ theo nhiều
kiểu. Không chuẩn hoá thì ràng buộc duy nhất vô dụng, và đăng nhập bằng một kiểu gõ khác sẽ
không tìm thấy tài khoản.

**Vì sao tách access token và refresh token.** Một token duy nhất buộc phải chọn giữa bảo
mật và trải nghiệm: hạn ngắn thì người dùng đăng nhập lại liên tục, hạn dài thì token bị lộ
gây hại lâu. Tách đôi cho phép access token sống 15 phút để giới hạn thiệt hại, refresh
token sống 7 ngày để giữ phiên.

**Vì sao xoay vòng refresh token.** Refresh token dùng một lần rồi bị thu hồi. Nếu kẻ tấn
công lấy được token và dùng trước, lần làm mới kế tiếp của người dùng thật sẽ thất bại,
biến một vụ đánh cắp âm thầm thành sự cố nhìn thấy được.

**Vì sao blacklist lưu trong database thay vì bộ nhớ.** JWT vốn không thể thu hồi, nên cần
một nơi ghi nhận token đã huỷ. Bộ nhớ trong mất sạch khi khởi động lại và không chia sẻ
được giữa nhiều instance. Bảng dữ liệu chỉ lưu định danh và hạn dùng, kèm job dọn dẹp hằng
ngày nên không phình to. Nếu sau này cần thông lượng cao hơn, có thể thay bằng Redis mà
không đổi interface.

**Vì sao refresh token không chứa vai trò.** Khi làm mới, ứng dụng đọc lại vai trò từ
database, nên việc hạ quyền một tài khoản có hiệu lực ngay ở lần làm mới kế tiếp thay vì
phải chờ refresh token hết hạn.

**Vì sao chấp nhận độ trễ 15 phút khi đổi vai trò.** Giải pháp triệt để là đọc vai trò từ
database ở mỗi request, nhưng như vậy mất đi ưu điểm chính của JWT. Cách thay thế là thu
hồi toàn bộ token của người vừa bị đổi vai trò. Hiện chưa làm vì blacklist đang đánh theo
từng token chứ không theo người dùng.

**Vì sao tạo tài khoản quản trị lúc khởi động.** Endpoint đăng ký luôn cấp vai trò
CUSTOMER, và chỉ quản trị viên mới nâng quyền được. Không có lối vào ban đầu thì hệ thống
mới dựng lên sẽ không có ai đủ quyền làm gì.

### Phân quyền

**Vì sao dùng phân cấp vai trò thay vì liệt kê từng vai trò.** Liệt kê dễ sót: thêm một
endpoint mới mà quên ADMIN là quản trị viên mất quyền một cách âm thầm. Phân cấp đưa quy
tắc về một chỗ duy nhất.

**Vì sao kiểm tra quyền sở hữu nằm ở tầng service.** Câu hỏi "sân này có phải của bạn
không" cần truy vấn database. Đặt kiểm tra trên phương thức service khiến luật đi cùng
nghiệp vụ: bất kỳ ai gọi phương thức đó, từ đâu, cũng bị kiểm tra như nhau.

**Vì sao lọc theo chủ sân ở tầng truy vấn.** Tải toàn bộ rồi mới bỏ bớt sẽ làm sai phân
trang, và dữ liệu của chi nhánh khác vẫn đi qua bộ nhớ ứng dụng.

**Vì sao không cho hạ vai trò một chủ sân đang còn chi nhánh.** Hạ quyền mà không chuyển
giao sẽ để lại chi nhánh không ai quản lý được. Chặn ngay lúc đổi vai trò rẻ hơn đi dọn dữ
liệu mồ côi sau này.

### Mã nguồn

**Vì sao seed demo viết bằng Java thay vì file SQL.** Mật khẩu mẫu chép từ đâu đó thường
không khớp với hash. Số điện thoại chèn tay sẽ không qua bước chuẩn hoá nên đăng nhập bằng
số không tìm thấy. Viết bằng Java buộc dữ liệu demo đi qua đúng các đường mà dữ liệu thật
đi qua.

**Vì sao mapper viết tay thay vì MapStruct.** Số lượng entity còn nhỏ, mapper thủ công
không cần thêm annotation processor và dễ đọc khi debug.

**Vì sao dùng Specification thay vì nhiều phương thức truy vấn.** Bộ lọc sân và lịch đặt có
sáu tiêu chí tuỳ chọn. Viết phương thức cho từng tổ hợp sẽ bùng nổ số lượng.

---

## Vấn đề đã biết

Những điểm dưới đây được phát hiện khi rà soát code, chưa được sửa.

- **Quy tắc giá "mọi ngày" không lưu được vào database.** Toàn bộ thiết kế tính giá coi
  thứ trong tuần để trống là áp dụng cho mọi ngày. Nhưng cột này trong entity đang khai
  báo không được để trống, nên Hibernate tạo cột bắt buộc. Hệ quả trực tiếp là bộ seed
  demo sẽ thất bại trên database mới, vì nó chèn ba quy tắc chung. Ứng dụng có thể không
  khởi động được khi bật cờ demo. Cách sửa là bỏ ràng buộc không rỗng trên cột đó, rồi
  sửa cột trong database nếu bảng đã được tạo.
- **Múi giờ phụ thuộc máy chạy.** Đồng hồ dùng múi giờ mặc định của hệ thống. Image JRE
  chính thức mặc định chạy theo giờ UTC, chậm hơn giờ Việt Nam 7 tiếng. Trong Docker,
  kiểm tra "không đặt giờ đã qua", hạn chót huỷ và job hoàn tất lịch sẽ lệch 7 tiếng. Cách
  sửa là cố định đồng hồ theo múi giờ `Asia/Ho_Chi_Minh`, hoặc đặt biến `TZ` cho container.
- **Chưa có cách quản lý bảng giá qua API.** Bảng giá chỉ có được qua seed demo. Bộ kiểm
  tra quy tắc giá mới, gồm kiểm tra mốc 30 phút và không chồng lấn cùng phạm vi ngày, đã
  viết xong nhưng chưa có endpoint nào gọi tới.
- **Cờ demo chưa được truyền vào Docker Compose.** Đặt biến demo trong file môi trường
  chưa đủ khi chạy bằng Compose, vì danh sách biến của container backend chưa có nó. Cần
  thêm một dòng vào phần môi trường của dịch vụ backend.
- **Từ chối và huỷ cùng ra một trạng thái.** Lịch bị chủ sân từ chối và lịch do khách tự
  huỷ đều thành CANCELLED, nên không phân biệt được về sau khi thống kê.
- **Chưa có test nghiệp vụ.** Bộ test chỉ có một trường hợp kiểm tra context khởi động.

---

## Roadmap

Cập nhật mục này khi hoàn thành từng hạng mục. Đánh dấu đã xong và ghi lại ở Changelog.

### Nghiệp vụ đặt sân

- [x] Chống đặt trùng giờ: phát hiện chồng lấn kèm khoá bi quan khi ghi
- [x] Kiểm tra khung giờ đặt nằm trong giờ mở cửa của chi nhánh
- [x] Tính giá theo quy tắc giá thay vì nhân giá cơ bản
- [x] Chính sách huỷ: chặn huỷ khi quá hạn chót trước giờ đá
- [x] Endpoint xem khung giờ trống của một sân theo ngày
- [x] Endpoint báo giá trước khi đặt
- [x] Tự động chuyển lịch đặt quá hạn sang trạng thái hết hạn hoặc hoàn tất
- [ ] Endpoint quản lý bảng giá cho chủ chi nhánh, dùng bộ kiểm tra quy tắc giá có sẵn
- [ ] Cho phép lưu quy tắc giá áp dụng mọi ngày, xem Vấn đề đã biết
- [ ] Tách trạng thái từ chối khỏi trạng thái huỷ

### Phân quyền

- [x] Giới hạn phạm vi vai trò OWNER theo chi nhánh mình quản lý
- [x] Cho phép ADMIN và OWNER xác nhận lịch đặt đang chờ
- [x] Cho phép chủ chi nhánh từ chối lịch đặt trên sân của mình
- [ ] Thu hồi toàn bộ token của một người khi vai trò thay đổi, để bỏ độ trễ 15 phút
- [ ] Đưa thông tin chủ sở hữu vào response của chi nhánh, hiện chỉ lưu chứ chưa trả về

### Hạ tầng

- [x] Tài liệu API bằng OpenAPI và Swagger UI
- [x] Dockerfile và docker-compose kèm MySQL
- [x] Xoá hai DTO introspect không còn endpoint
- [ ] Cố định múi giờ của đồng hồ, xem Vấn đề đã biết
- [ ] Truyền cờ demo vào container backend trong Docker Compose
- [ ] Thay cập nhật lược đồ tự động bằng Flyway migration
- [ ] Kiểm thử: unit test cho các hàm tính toán, integration test cho chống đặt trùng
- [ ] Giới hạn tần suất gọi endpoint đăng nhập
- [ ] Xoá phần triển khai giao diện chi tiết người dùng trong entity tài khoản, không còn
      được dùng
- [ ] Sửa ghi chú đầu lớp của job hoàn tất lịch, hiện đang chép nguyên từ job hết hạn

### Cân nhắc thêm

- [ ] Guest booking kèm xác minh OTP, xem mục Design decisions
- [ ] Tích hợp thanh toán
- [ ] Thông báo qua email khi đặt, xác nhận và huỷ lịch

---

## Changelog

Ghi theo thứ tự mới nhất trước. Mỗi mục nêu ngắn gọn thay đổi đáng chú ý.

### Chưa phát hành

**Đặt sân**

- Cho phép ADMIN xác nhận và từ chối lịch đặt trên mọi sân
- Thêm bộ seed dữ liệu demo, bật bằng cờ cấu hình
- Thêm endpoint báo giá cho một khoảng giờ
- Thêm job tự chuyển lịch đã xác nhận sang hoàn tất khi qua giờ kết thúc
- Thêm job tự chuyển lịch chờ quá 30 phút sang hết hạn
- Thêm luồng xác nhận và từ chối lịch đặt cho chủ chi nhánh
- Thêm chính sách huỷ: chậm nhất 2 giờ trước giờ đá
- Tính giá theo bảng hệ số cấp chi nhánh, quy tắc theo thứ ghi đè quy tắc chung
- Thêm endpoint lưới khung giờ trống kèm đơn giá từng ô
- Chống đặt trùng giờ bằng khoá dòng của sân, lỗi chờ khoá trả mã 409
- Thêm bộ kiểm tra thời gian đặt sân: mốc 30 phút, thời lượng, giờ mở cửa, đặt trước
- Inject đồng hồ để các quy tắc thời gian test được

**Tài khoản**

- Cho phép đăng nhập bằng số điện thoại, chuẩn hoá và ràng buộc duy nhất số điện thoại
- Bắt buộc username bắt đầu bằng chữ cái

**Hạ tầng**

- Thêm Swagger UI và đặc tả OpenAPI
- Thêm Dockerfile hai giai đoạn và Docker Compose kèm MySQL có healthcheck
- Chuẩn hoá ký tự xuống dòng qua file thuộc tính Git
- Bỏ DevTools khỏi dependency, xoá hai DTO introspect

**Đợt trước**

- Tự tạo ba vai trò lúc khởi động, kèm tuỳ chọn tạo tài khoản quản trị đầu tiên
- Thêm phân cấp vai trò ADMIN trên OWNER trên CUSTOMER, và bean kiểm tra quyền sở hữu
- Thêm quyền sở hữu chi nhánh: chủ sân chỉ quản lý sân và xem lịch đặt của chi nhánh mình
- Thêm endpoint quản trị đổi vai trò người dùng và endpoint tự sửa hồ sơ
- Thêm logout thu hồi cả access token và refresh token
- Thêm refresh token có xoay vòng và bảng token bị thu hồi
- Tách dịch vụ JWT khỏi dịch vụ xác thực, chuyển cấu hình sang các record properties có
  kiểm tra ràng buộc lúc khởi động
- Chuẩn hoá mọi phản hồi lỗi về cùng cấu trúc, kể cả lỗi phát sinh trong filter chain
- Chuyển tầng service sang làm việc với DTO, đưa thông tin bí mật ra biến môi trường
