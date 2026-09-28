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
├── config/         Cấu hình security, CORS, properties, seed dữ liệu, scheduled job
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

# Tuỳ chọn: tạo tài khoản quản trị đầu tiên lúc khởi động
APP_BOOTSTRAP_ADMIN_USERNAME=admin
APP_BOOTSTRAP_ADMIN_PASSWORD=doi_mat_khau_nay_ngay
APP_BOOTSTRAP_ADMIN_EMAIL=admin@example.com
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

### Seed dữ liệu

Không cần chèn tay. Lúc khởi động, một `ApplicationRunner` tự tạo đủ ba vai trò nếu chưa
có trong database.

Cùng lúc đó, nếu ba biến bootstrap ở trên được đặt, ứng dụng tạo một tài khoản quản trị
đầu tiên. Bỏ trống thì bước này bị bỏ qua. Tài khoản chỉ được tạo một lần, lần khởi động
sau không ghi đè gì. Đây là cách thoát khỏi thế bí ban đầu: endpoint đăng ký luôn cấp vai
trò CUSTOMER, nên nếu không có sẵn một quản trị viên thì không ai nâng quyền được cho ai.

Đổi mật khẩu ngay sau lần đăng nhập đầu tiên, rồi gỡ ba biến bootstrap khỏi file môi
trường.

---

## Mô hình dữ liệu

| Entity | Vai trò | Quan hệ |
| --- | --- | --- |
| User | Tài khoản | thuộc một vai trò, có nhiều lịch đặt |
| Role | Vai trò CUSTOMER, OWNER, ADMIN | có nhiều người dùng |
| Branch | Chi nhánh, có giờ mở và đóng cửa | có nhiều sân, thuộc một chủ sân tuỳ chọn |
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

### Phân cấp vai trò

Ba vai trò xếp thành một chuỗi bao hàm: ADMIN bao hàm OWNER, OWNER bao hàm CUSTOMER. Nhờ
vậy một endpoint chỉ cần khai báo mức thấp nhất được phép, các vai trò cao hơn tự động đi
qua. Ví dụ endpoint yêu cầu OWNER thì ADMIN cũng vào được mà không phải liệt kê thêm.

Phân cấp này được nối vào bộ xử lý biểu thức của method security, nên áp dụng cho cả
`hasRole` trong chú thích lẫn kiểm tra ở tầng đường dẫn.

### Ba tầng phân quyền

| Tầng | Nơi khai báo | Trả lời câu hỏi |
| --- | --- | --- |
| Đường dẫn | Cấu hình security | Endpoint này có công khai không |
| Vai trò | Chú thích trên controller hoặc service | Vai trò nào được gọi |
| Quyền sở hữu | Bean kiểm tra quyền gọi trong biểu thức SpEL | Bản ghi cụ thể này có thuộc về người gọi không |

Tầng thứ ba là phần mới nhất. Một bean chuyên trách trả lời ba câu hỏi: người này có sở
hữu chi nhánh đó không, có sở hữu sân đó không, và có sở hữu chi nhánh chứa sân của lịch
đặt đó không. Các chú thích gọi thẳng bean này trong biểu thức, ví dụ quyền sửa một sân
được diễn đạt thành "là quản trị viên, hoặc sở hữu chính sân đó".

Với sân, kiểm tra quyền nằm ở tầng service chứ không phải controller, vì nó cần đọc
database để biết ai là chủ. Chủ sân chỉ thao tác được trên chi nhánh của mình, và không
được chuyển một sân sang chi nhánh khác.

Với lịch đặt, ba nhóm xem được một bản ghi: quản trị viên, người đã đặt, và chủ chi nhánh
chứa sân đó. Khi chủ sân tìm kiếm lịch đặt, bộ lọc tự thêm điều kiện giới hạn theo chi
nhánh mình sở hữu, nên họ không thấy dữ liệu của chi nhánh khác. Quản trị viên không bị
giới hạn này.

### Thay đổi vai trò

Chỉ quản trị viên đổi được vai trò của người khác. Có hai ràng buộc:

- Không hạ vai trò một chủ sân đang còn quản lý chi nhánh. Phải chuyển giao chi nhánh
  trước, nếu không hệ thống từ chối với mã 409.
- Vai trò mới **chưa có hiệu lực ngay** với access token đang lưu hành, vì vai trò nằm
  trong token chứ không đọc lại từ database mỗi request. Độ trễ tối đa bằng thời hạn
  access token, tức 15 phút. Hệ thống ghi một dòng cảnh báo vào log mỗi lần đổi vai trò
  để việc này không bị quên.

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
| PUT | `/users/me` | Đã đăng nhập | Tự sửa hồ sơ của mình |
| GET | `/users/{username}` | ADMIN hoặc chính chủ | Xem một hồ sơ |
| PUT | `/users/{id}` | ADMIN | Cập nhật hồ sơ người khác |
| PATCH | `/users/{id}/role` | ADMIN | Đổi vai trò, xem lưu ý về độ trễ token |
| DELETE | `/users/{id}` | ADMIN | Xoá tài khoản |

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
| POST | `/fields` | ADMIN, hoặc chủ của chi nhánh đích | Tạo sân |
| PUT | `/fields/{id}` | ADMIN, hoặc chủ của sân đó | Cập nhật sân |
| DELETE | `/fields/{id}` | ADMIN | Xoá sân |

Khi cập nhật, `branchId` phải trùng chi nhánh hiện tại của sân. Chuyển sân sang chi nhánh
khác không được hỗ trợ và bị từ chối với mã 400.

Tham số lọc khi tìm sân: `branchName`, `district`, `fieldType`, `status`, `minPrice`,
`maxPrice`, cộng `page`, `size` và `sort`.

### Bookings

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| GET | `/bookings` | ADMIN xem tất cả, OWNER xem chi nhánh mình | Tìm kiếm |
| GET | `/bookings/me` | Đã đăng nhập | Lịch đặt của chính mình |
| GET | `/bookings/{id}` | Người đã đặt, chủ chi nhánh, ADMIN | Chi tiết một lịch đặt |
| POST | `/bookings` | Đã đăng nhập | Tạo lịch đặt |
| PATCH | `/bookings/{id}/cancel` | Người đã đặt | Huỷ lịch đặt |
| DELETE | `/bookings/{id}` | ADMIN | Xoá lịch đặt |

Tham số lọc khi tìm lịch đặt: `userId`, `username`, `branchName`, `status`, `bookingDate`,
cộng phân trang. Với tài khoản OWNER, hệ thống tự thêm một điều kiện giới hạn kết quả về
các chi nhánh người đó sở hữu. Client không đặt được điều kiện này và cũng không bỏ được.

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

**Vì sao dùng phân cấp vai trò thay vì liệt kê từng vai trò.** Trước đây mỗi endpoint phải
ghi rõ cả ADMIN lẫn OWNER. Cách đó dễ sót: thêm một endpoint mới mà quên ADMIN là quản trị
viên mất quyền một cách âm thầm. Khai báo ADMIN bao hàm OWNER, OWNER bao hàm CUSTOMER đưa
quy tắc về một chỗ duy nhất, còn endpoint chỉ cần nói mức tối thiểu.

**Vì sao kiểm tra quyền sở hữu nằm ở tầng service.** Câu hỏi "sân này có phải của bạn
không" cần truy vấn database, nên không trả lời được ở tầng đường dẫn. Đặt kiểm tra ngay
trên phương thức service khiến luật đi cùng nghiệp vụ: bất kỳ ai gọi phương thức đó, từ
controller nào, cũng bị kiểm tra như nhau.

**Vì sao lọc theo chủ sân ở tầng truy vấn chứ không lọc sau khi lấy về.** Nếu tải toàn bộ
lịch đặt rồi mới bỏ bớt, phân trang sẽ sai và dữ liệu của chi nhánh khác vẫn đi qua bộ
nhớ ứng dụng. Thêm điều kiện vào câu truy vấn khiến database chỉ trả về đúng phần được
phép xem.

**Vì sao không cho hạ vai trò một chủ sân đang còn chi nhánh.** Hạ quyền mà không chuyển
giao sẽ để lại chi nhánh không ai quản lý, còn người vừa bị hạ vẫn là chủ trên dữ liệu
nhưng không còn quyền thao tác. Chặn ngay tại thời điểm đổi vai trò rẻ hơn nhiều so với đi
dọn dữ liệu mồ côi sau này.

**Vì sao chấp nhận độ trễ 15 phút khi đổi vai trò.** Giải pháp triệt để là đọc vai trò từ
database ở mỗi request, nhưng như vậy mất đi ưu điểm chính của JWT là không phải truy vấn.
Cách thay thế là thu hồi toàn bộ token của người vừa bị đổi vai trò. Hiện chưa làm vì
blacklist đang đánh theo từng token chứ không theo người dùng. Trong lúc đó, access token
15 phút giới hạn cửa sổ rủi ro ở mức chấp nhận được, và mỗi lần đổi vai trò đều ghi log.

**Vì sao tạo tài khoản quản trị lúc khởi động.** Endpoint đăng ký luôn cấp vai trò
CUSTOMER, và chỉ quản trị viên mới nâng quyền được. Không có lối vào ban đầu thì hệ thống
mới dựng lên sẽ không có ai đủ quyền làm gì. Tài khoản này chỉ được tạo khi có cấu hình
rõ ràng, tạo đúng một lần, và ghi cảnh báo nhắc đổi mật khẩu.

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

- [x] Giới hạn phạm vi vai trò OWNER theo chi nhánh mình quản lý
- [ ] Cho phép ADMIN và OWNER xác nhận lịch đặt đang chờ
- [ ] Cho phép chủ chi nhánh huỷ lịch đặt trên sân của mình
- [ ] Thu hồi toàn bộ token của một người khi vai trò thay đổi, để bỏ độ trễ 15 phút
- [ ] Đưa thông tin chủ sở hữu vào response của chi nhánh, hiện chỉ lưu chứ chưa trả về

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

- Tự tạo ba vai trò lúc khởi động, kèm tuỳ chọn tạo tài khoản quản trị đầu tiên
- Thêm phân cấp vai trò ADMIN trên OWNER trên CUSTOMER, và bean kiểm tra quyền sở hữu
  dùng trong biểu thức phân quyền
- Thêm quyền sở hữu chi nhánh: chủ sân chỉ quản lý sân và xem lịch đặt của chi nhánh mình
- Thêm endpoint quản trị đổi vai trò người dùng và endpoint tự sửa hồ sơ
- Thêm logout thu hồi cả access token và refresh token
- Thêm refresh token có xoay vòng và bảng token bị thu hồi
- Tách dịch vụ JWT khỏi dịch vụ xác thực, chuyển cấu hình sang các record properties có
  kiểm tra ràng buộc lúc khởi động
- Chuẩn hoá mọi phản hồi lỗi về cùng cấu trúc, kể cả lỗi phát sinh trong filter chain
- Chuyển tầng service sang làm việc với DTO, đưa thông tin bí mật ra biến môi trường
