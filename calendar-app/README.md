# Calendar App

Ứng dụng lịch nhẹ bằng Java Swing, thiết kế theo kiến trúc BEAN → BO → DAO.

Tính năng chính

- Giao diện Swing hiện đại sử dụng FlatLaf
- Thêm / liệt kê cuộc hẹn, đặt reminder
- Lưu trữ bằng MySQL (mặc định) và seed dữ liệu khi chạy lần đầu
- Component tùy chỉnh: DateTime picker, mini-calendar sidebar, card-based list

Yêu cầu

- Java 17 (hoặc JDK tương thích)
- Maven 3.x
- MySQL (hoặc cấu hình `src/main/resources/db.properties` cho H2 nếu muốn)

Cấu trúc thư mục (tóm tắt)

```
calendar-app/
├─ pom.xml
├─ README.md
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  ├─ com.calendar/
│  │  │  │  ├─ controller/       # controllers (xử lý sự kiện UI -> gọi BO)
│  │  │  │  ├─ model/
│  │  │  │  │  ├─ bean/           # POJOs (Appointment, Reminder...)
│  │  │  │  │  ├─ dto/            # Data transfer objects
│  │  │  │  │  ├─ bo/             # Business logic (validate + workflow)
│  │  │  │  │  ├─ dao/            # Database access objects
│  │  │  │  │  └─ db/             # DB connection & schema init
│  │  │  │  ├─ view/              # Swing views (MainUI, dialogs, components)
│  │  │  │  │  └─ component/      # reusable UI components (DateTimePicker, MiniCalendar)
│  │  │  │  └─ util/              # helpers (ThemeManager...)
│  │  ├─ resources/
│  │  │  ├─ db.properties        # DB config
│  │  │  └─ ui/                  # (đề xuất) chèn giao diện, icons, fonts, themes ở đây
│  └─ test/
```

Giải thích các phần chính

- `controller/`: nhận input từ UI, gọi `AppointmentBO` để xử lý lưu/validate và cập nhật `MainUI`.
- `model/bean` và `model/dto`: chứa cấu trúc dữ liệu (Appointment, Reminder...).
- `model/bo`: chứa logic nghiệp vụ (kiểm tra lịch bị trùng, rules, outcome messages).
- `model/dao`: truy vấn DB (CRUD cho appointments, users, reminders...).
- `model/db/DBConnection`: khởi tạo kết nối, seed dữ liệu; **bây giờ chỉ reset schema khi `db.reset=true`**.
- `view/` và `view/component/`: toàn bộ giao diện Swing, các dialog và component tái sử dụng.
- `util/ThemeManager`: helper để bật FlatLaf light/dark.

Nơi để đặt tài nguyên giao diện (icons, fonts, theme)

- Mặc định project có `src/main/resources`; mình đề xuất tạo cấu trúc sau để chứa UI assets và theme:

```
src/main/resources/ui/
├─ icons/        # các icon PNG/SVG được dùng trong giao diện
├─ fonts/        # font tùy chỉnh (.ttf)
├─ themes/       # theme FlatLaf tùy chỉnh hoặc file cấu hình
└─ images/       # ảnh minh họa, background
```

Hướng dẫn dùng tài nguyên trong code

- Truy xuất file resource từ classpath:

```java
InputStream is = MyClass.class.getResourceAsStream("/ui/icons/calendar_icon.png");
ImageIcon icon = new ImageIcon(ImageIO.read(is));
```

- Với FlatLaf bạn có thể cung cấp file `.properties` hoặc `.theme.json` và tải nó khi khởi tạo LAF (tham khảo `ThemeManager`).

Lưu ý: nếu bạn thêm font, load chúng từ `src/main/resources/fonts` rồi đăng ký với `GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(...)`.

Chạy nhanh (build & run)

```bash
cd "e:/HOCKI6/OOAD/CODE/calendar-app"
mvn clean package
mvn exec:java -Dexec.mainClass=com.calendar.Main
```

Ghi chú quan trọng

- Để tránh xóa dữ liệu trong DB, `DBConnection` chỉ chạy `resetSchema(...)` khi bạn bật `db.reset=true` trong `src/main/resources/db.properties`.
- Form Add Appointment bây giờ chỉ highlight lỗi khi người dùng thực sự tương tác với trường (touched state).

Đẩy repo lên GitHub (tùy chọn)

- Nếu repo đã có remote, thực hiện:

```bash
git add README.md
git commit -m "docs: add README with project structure and UI assets guide"
git push origin main
```

---

License: MIT
