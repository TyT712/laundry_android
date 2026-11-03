# Ứng dụng Quản lý tiệm giặt là – Android Java/XML

## Chức năng
- **Phân quyền** Quản lý / Nhân viên. Tài khoản test (mật khẩu `123456`): `admin` (Quản lý), `nhanvien` (Nhân viên).
- Đăng nhập, đăng ký (tài khoản mới luôn là Nhân viên), quản lý thông tin cá nhân và đổi mật khẩu.
- Quản lý khách hàng: thêm, sửa, xóa, tra cứu theo tên/SĐT/địa chỉ.
- Quản lý danh mục dịch vụ và đơn giá (chỉ Quản lý).
- Đơn giặt: chọn khách hàng + dịch vụ từ danh mục, số kg, ghi chú, tự tính tiền, trạng thái xử lý, tình trạng thanh toán; xem chi tiết, sửa, xóa.
- Thống kê số đơn, doanh thu đã/chưa thanh toán (chỉ Quản lý).
- SQLite cục bộ; mật khẩu được băm SHA-256 kèm salt.

## Cấu trúc mã nguồn (`com.example.laundrymanager`)
| Package | Vai trò |
| --- | --- |
| `model` | Các `record`/`enum` dữ liệu: `User`, `Customer`, `Service`, `Order`, `Stats`, `Role`, `OrderStatus`, `PaymentStatus` |
| `data` | `DBHelper` (schema, dữ liệu mẫu) và các `*Repository` truy vấn SQLite |
| `ui` | `BaseActivity` (kiểm tra quyền, khung chung) và các màn hình |
| `util` | `Session`, `PasswordHasher`, `Formats`, `Ui` |

Giao diện nằm trong `res/layout`, chuỗi trong `res/values/strings.xml`, kiểu dùng chung trong `res/values/styles.xml`.

Một khách hàng/dịch vụ đã có đơn giặt sẽ không thể xóa (ràng buộc khóa ngoại `RESTRICT`).

## Chạy dự án
1. Mở thư mục bằng Android Studio, chờ Gradle Sync (cần Android SDK 37 hoặc chỉnh `compileSdk`/`targetSdk`).
2. Chạy trên Emulator hoặc điện thoại Android.

## Lưu ý
Khi nâng `VERSION` trong `DBHelper`, dữ liệu cũ sẽ bị xóa và tạo lại. Chưa có đồng bộ máy chủ hay thanh toán trực tuyến.
