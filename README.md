# Soccer Field Booking API

REST API quản lý đặt sân bóng đá: chi nhánh, sân, lịch đặt và tài khoản người dùng.
Xây dựng bằng Spring Boot 3.3.13 trên Java 21, dữ liệu lưu ở MySQL, xác thực bằng JWT
với access token ngắn hạn và refresh token có xoay vòng.

---

## Mục lục

- [Tech stack](#tech-stack)
- [Kiến trúc](#kiến-trúc)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)
- [Chạy dự án](#chạy-dự-án)
- [Mô hình dữ liệu](#mô-hình-dữ-liệu)
- [Xác thực và phân quyền](#xác-thực-và-phân-quyền)
- [API reference](#api-reference)
- [Định dạng response](#định-dạng-response)
- [Design decisions](#design-decisions)
- [Roadmap](#roadmap)
- [Changelog](#changelog)

---

## Tech stack

| Thành phần | Lựa chọn |
| --- | --- |
| Ngôn ngữ | Java 21 |
| Framework | Spring Boot 3.3.13 |
| Bảo mật | Spring Security 6, OAuth2 Resource Server, Nimbus JOSE JWT |
| Dữ liệu | Spring Data JPA, Hibernate, MySQL |
| Validation | Jakarta Bean Validation |
| Tiện ích | Lombok |
| Build | Maven Wrapper |

---

## Kiến trúc

Luồng xử lý đi qua bốn lớp, mỗi lớp chỉ phụ thuộc vào lớp kế dưới:

```
Controller  ->  Service (interface + impl)  ->  Repository  ->  Entity
     |                    |
    DTO  <----------  Mapper
```

Nguyên tắc chính:

- **Entity không rời khỏi tầng service.** Đầu vào là record request, đầu ra là record
  response. Mapper viết tay thực hiện chuyển đổi. Nhờ vậy mật khẩu không bao giờ bị
  serialize và quan hệ vòng giữa người dùng, lịch đặt và sân không gây đệ quy vô hạn.
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
├── config/         Cấu hình security, CORS, properties, scheduled job
├── controller/     REST endpoints
├── dto/
│   ├── request/    Record đầu vào, gắn Bean Validation
│   └── response/   Record đầu ra
├── entity/         JPA entity và enum
├── exception/      Exception nghiệp vụ và bộ xử lý lỗi toàn cục
├── mapper/         Chuyển entity sang DTO
├── repository/
│   └── specification/   Bộ lọc động bằng Criteria API
├── security/       Validator token tuỳ biến
└── service/
    └── impl/       Cài đặt nghiệp vụ
```

---

## Chạy dự án

### Yêu cầu

- JDK 21
- MySQL 8 đang chạy

### Biến môi trường

Tạo file `.env` ở thư mục gốc. File này đã nằm trong danh sách bỏ qua của Git và không
được commit.

```properties
DB_URL=jdbc:mysql://localhost:3306/soccer_field_data
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SIGNER_KEY=chuoi_bi_mat_toi_thieu_64_ky_tu_vi_HS512_can_khoa_512_bit_khong_duoc_ngan_hon
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

Khoá ký phải dài tối thiểu 64 ký tự. Ứng dụng kiểm tra điều kiện này lúc khởi động và
dừng ngay nếu không đạt.

### Khởi động

```bash
# Tạo database
mysql -u root -p -e "CREATE DATABASE soccer_field_data CHARACTER SET utf8mb4;"

# Chạy
./mvnw spring-boot:run
```

Hibernate tự tạo bảng theo chế độ cập nhật lược đồ tự động. API phục vụ tại
`http://localhost:8080`.

### Seed dữ liệu bắt buộc

Bảng vai trò phải có sẵn ba bản ghi, nếu không endpoint đăng ký sẽ lỗi vì không tìm thấy
vai trò mặc định:

```sql
INSERT INTO roles (name) VALUES ('CUSTOMER'), ('OWNER'), ('ADMIN');
```

---

## Mô hình dữ liệu

| Entity | Vai trò | Quan hệ |
| --- | --- | --- |
| User | Tài khoản | thuộc một vai trò, có nhiều lịch đặt |
| Role | Vai trò CUSTOMER, OWNER, ADMIN | có nhiều người dùng |
| Branch | Chi nhánh, có giờ mở và đóng cửa | có nhiều sân |
| Field | Sân, có loại và giá cơ bản | thuộc một chi nhánh |
| Booking | Lịch đặt, có trạng thái và tổng tiền | thuộc một người dùng và một sân |
| PricingRule | Giá theo thứ và khung giờ | thuộc một sân, chưa được dùng, xem Roadmap |
| InvalidatedToken | Token đã thu hồi | không có quan hệ |

Bốn enum đang dùng: tên vai trò, loại sân, trạng thái sân và trạng thái lịch đặt. Tất cả
lưu dạng chuỗi trong database để dễ đọc và không vỡ khi thêm giá trị mới.

---

## Xác thực và phân quyền

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

Token bị thu hồi lưu trong bảng `invalidated_tokens` theo định danh `jti`. Một job định kỳ
chạy lúc 3 giờ sáng mỗi ngày xoá các bản ghi đã quá hạn, vì token hết hạn tự nó đã vô hiệu.

### Bốn lớp validator

Bộ giải mã JWT ghép bốn kiểm tra chạy tuần tự:

| Validator | Từ chối khi |
| --- | --- |
| Kiểm tra thời gian | Token hết hạn hoặc chưa tới hạn dùng |
| Kiểm tra nhà phát hành | Không khớp cấu hình |
| Kiểm tra loại token | Dùng refresh token để gọi API |
| Kiểm tra danh sách thu hồi | Token đã bị thu hồi hoặc thiếu định danh |

### Phân quyền hai tầng

Tầng đường dẫn khai báo trong cấu hình security, quyết định endpoint nào công khai. Tầng
phương thức dùng biểu thức SpEL cho các luật phụ thuộc dữ liệu, ví dụ chỉ chủ sở hữu mới
xem được lịch đặt của mình.

---

## API reference

Tiền tố chung là `/api/v1`. Cột quyền ghi Public nghĩa là không cần token.

### Auth

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/auth/login` | Public | Đăng nhập, nhận cặp token |
| POST | `/auth/refresh` | Public | Xoay vòng refresh token |
| POST | `/auth/logout` | Đã đăng nhập | Thu hồi access và refresh token |

### Users

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/users` | Public | Đăng ký, mặc định vai trò CUSTOMER |
| GET | `/users` | ADMIN | Danh sách người dùng |
| GET | `/users/me` | Đã đăng nhập | Hồ sơ của chính mình |
| GET | `/users/{username}` | ADMIN hoặc chính chủ | Xem một hồ sơ |
| PUT | `/users/{id}` | ADMIN | Cập nhật hồ sơ |
| DELETE | `/users/{id}` | ADMIN | Xoá tài khoản |

### Roles

| Method | Endpoint | Quyền |
| --- | --- | --- |
| GET | `/roles` | ADMIN |
| GET | `/roles/{id}` | ADMIN |

### Branches

| Method | Endpoint | Quyền |
| --- | --- | --- |
| GET | `/branches` | Public |
| GET | `/branches/{id}` | Public |
| POST | `/branches` | ADMIN |
| PUT | `/branches/{id}` | ADMIN |
| DELETE | `/branches/{id}` | ADMIN |

### Fields

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| GET | `/fields` | Public | Tìm kiếm có lọc và phân trang |
| GET | `/fields/{id}` | Public | Chi tiết một sân |
| POST | `/fields` | ADMIN, OWNER | Tạo sân |
| PUT | `/fields/{id}` | ADMIN, OWNER | Cập nhật sân |
| DELETE | `/fields/{id}` | ADMIN | Xoá sân |

Tham số lọc khi tìm sân: `branchName`, `district`, `fieldType`, `status`, `minPrice`,
`maxPrice`, cộng `page`, `size` và `sort`.

### Bookings

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| GET | `/bookings` | ADMIN, OWNER | Tìm kiếm toàn hệ thống |
| GET | `/bookings/me` | Đã đăng nhập | Lịch đặt của chính mình |
| GET | `/bookings/{id}` | Chủ sở hữu, ADMIN, OWNER | Chi tiết một lịch đặt |
| POST | `/bookings` | Đã đăng nhập | Tạo lịch đặt |
| PATCH | `/bookings/{id}/cancel` | Chủ sở hữu | Huỷ lịch đặt |
| DELETE | `/bookings/{id}` | ADMIN | Xoá lịch đặt |

Tham số lọc khi tìm lịch đặt: `userId`, `username`, `branchName`, `status`, `bookingDate`,
cộng phân trang.

---

## Định dạng response

Thành công:

```json
{
  "message": "Booking created successfully",
  "statusCode": 201,
  "timestamp": "2026-09-12T14:30:00",
  "data": { "id": 42, "bookingDate": "2026-09-20", "status": "PENDING" }
}
```

Lỗi validation:

```json
{
  "message": "Validation failed",
  "statusCode": 400,
  "timestamp": "2026-09-12T14:30:00",
  "path": "/api/v1/users",
  "data": {
    "email": "Email is not in correct format",
    "password": "Password must be at least 8 characters"
  }
}
```

Các mã trạng thái đang dùng: 400 dữ liệu sai, 401 thiếu hoặc sai token, 403 không đủ
quyền, 404 không tìm thấy, 409 xung đột dữ liệu, 500 lỗi không lường trước.

Lỗi phát sinh trong filter chain, trước khi tới controller, được hai lớp xử lý riêng ghi
JSON, nên client luôn nhận cùng một cấu trúc dù lỗi xảy ra ở đâu.

---

## Design decisions

**Vì sao booking yêu cầu đăng nhập.** Guest booking giảm ma sát nhưng mở ra khả năng đặt
kín sân hàng loạt vì không có gì ràng buộc trách nhiệm. Phương án đã cân nhắc: shadow user
kết hợp xác minh OTP và booking tự hết hạn sau 15 phút. Chưa triển khai vì phụ thuộc SMS
provider bên ngoài, nằm ngoài phạm vi đợt này.

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

**Vì sao refresh token không chứa vai trò.** Vai trò chỉ nằm trong access token. Khi làm
mới, ứng dụng đọc lại vai trò từ database, nên việc hạ quyền một tài khoản có hiệu lực
ngay ở lần làm mới kế tiếp thay vì phải chờ refresh token hết hạn.

**Vì sao mapper viết tay thay vì MapStruct.** Số lượng entity còn nhỏ, mapper thủ công
không cần thêm annotation processor vào vòng đời build và dễ đọc khi debug. Sẽ cân nhắc
lại nếu số lượng DTO tăng đáng kể.

**Vì sao dùng Specification thay vì nhiều phương thức truy vấn.** Bộ lọc sân và lịch đặt
có sáu tiêu chí tuỳ chọn. Viết phương thức cho từng tổ hợp sẽ bùng nổ số lượng, nên dùng
Criteria API dựng điều kiện động và chỉ thêm tiêu chí nào thực sự được truyền vào.

---

## Roadmap

Cập nhật mục này khi hoàn thành từng hạng mục. Đánh dấu đã xong và ghi lại ở Changelog.

### Nghiệp vụ đặt sân

- [ ] Chống đặt trùng giờ: phát hiện chồng lấn kèm khoá bi quan khi ghi
- [ ] Kiểm tra khung giờ đặt nằm trong giờ mở cửa của chi nhánh
- [ ] Tính giá theo quy tắc giá thay vì nhân giá cơ bản
- [ ] Chính sách huỷ: chặn huỷ khi quá hạn chót trước giờ đá
- [ ] Endpoint xem khung giờ trống của một sân theo ngày
- [ ] Tự động chuyển lịch đặt quá hạn sang trạng thái hết hạn hoặc hoàn tất

### Phân quyền

- [ ] Giới hạn phạm vi vai trò OWNER theo chi nhánh mình quản lý
- [ ] Cho phép ADMIN và OWNER xác nhận lịch đặt đang chờ

### Hạ tầng

- [ ] Thay cập nhật lược đồ tự động bằng Flyway migration
- [ ] Tài liệu API bằng OpenAPI và Swagger UI
- [ ] Kiểm thử: unit test cho tầng service, integration test cho controller
- [ ] Giới hạn tần suất gọi endpoint đăng nhập
- [ ] Dockerfile và docker-compose kèm MySQL
- [ ] Xoá mã chết còn sót: hai DTO introspect không còn endpoint, và phần triển khai
      giao diện chi tiết người dùng trong entity tài khoản

### Cân nhắc thêm

- [ ] Guest booking kèm xác minh OTP, xem mục Design decisions
- [ ] Tích hợp thanh toán
- [ ] Thông báo qua email khi đặt và huỷ lịch

---

## Changelog

Ghi theo thứ tự mới nhất trước. Mỗi mục nêu ngắn gọn thay đổi đáng chú ý.

### Chưa phát hành

- Thêm logout thu hồi cả access token và refresh token
- Thêm refresh token có xoay vòng và bảng token bị thu hồi
- Tách dịch vụ JWT khỏi dịch vụ xác thực, chuyển cấu hình sang các record properties có
  kiểm tra ràng buộc lúc khởi động
- Chuẩn hoá mọi phản hồi lỗi về cùng cấu trúc, kể cả lỗi phát sinh trong filter chain
- Chuyển tầng service sang làm việc với DTO, đưa thông tin bí mật ra biến môi trường
