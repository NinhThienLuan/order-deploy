# FRONTEND STRUCTURE GUIDELINE

## React JSX – Feature-based Architecture

### I. Mục tiêu của structure này
- Tách rõ UI – Business – API
- Dễ scale khi project lớn
- Dễ maintain khi nhiều dev làm cùng
- Không bị loạn khi >50 page

### II. 📂 Tổng cấu trúc chuẩn
```text
src/
│
├── app/
│   ├── routes/
│   └── providers/
│
├── layouts/
│
├── features/
│
├── components/
│   └── ui/
│
├── services/
│
├── hooks/
├── utils/
├── constants/
├── styles/
└── assets/
```

---

### III. Quy tắc sử dụng từng folder

#### 1️⃣ `features/` – Trung tâm của hệ thống
👉 Mỗi folder trong `features` là một module nghiệp vụ độc lập.

**Ví dụ:**
```text
features/
  auth/
  warehouse/
  inventory/
  supplier/
```

**Bên trong một feature (Ví dụ warehouse):**
```text
warehouse/
  pages/
  components/
  warehouse.service.js
  warehouse.constants.js
```

**Quy tắc:**
- ✔️ Tất cả thứ liên quan đến warehouse phải nằm trong đây
- ✔️ Không import lung tung từ feature khác
- ✔️ Không để component nghiệp vụ vào `components/` global

#### 2️⃣ `services/` – API Global
Chỉ chứa:
- `apiClient.js` (axios config)
- Interceptor
- Global error handler

🚫 **Không để API theo feature ở đây**. API theo feature phải nằm trong feature.

#### 3️⃣ `components/ui/` – Reusable UI
Chỉ chứa component dùng toàn hệ thống:
- Button
- Input
- Card
- Modal

🚫 **Không để component nghiệp vụ như WarehouseTable ở đây**.

#### 4️⃣ `layouts/`
Chỉ chứa:
- Sidebar
- Header
- Footer
- Outlet

🚫 **Layout không chứa business logic**.

#### 5️⃣ `hooks/`
Chứa:
- `useAuth`
- `usePagination`
- `useDebounce`

👉 Nếu hook chỉ dùng cho 1 feature → để trong feature đó.

#### 6️⃣ `styles/`
Chứa:
- `global.css`
- `variables.css`

🚫 **Không chứa css của feature riêng lẻ**.

---

### IV. Luồng call API chuẩn
```text
Component
  ↓
Feature Service
  ↓
apiClient (global)
  ↓
Backend
```

**Ví dụ chuẩn:**

1. `apiClient.js`
```javascript
import axios from "axios";

const apiClient = axios.create({
 baseURL: import.meta.env.VITE_API_URL,
});

export default apiClient;
```

2. `warehouse.service.js`
```javascript
import apiClient from "@/services/apiClient";

export const getWarehouses = async () => {
 const res = await apiClient.get("/warehouses");
 return res.data;
};
```

3. `WarehousePage.jsx`
```javascript
import { getWarehouses } from "../warehouse.service";

useEffect(() => {
 getWarehouses().then(setData);
}, []);
```

---

### V. Quy tắc CSS
- ✔️ **CSS của component → đặt cạnh component**
- ✔️ Dùng **CSS Module** nếu có thể
- ✔️ **Không viết css global cho feature**

**Ví dụ:**
```text
WarehouseTable.jsx
WarehouseTable.module.css
```

---

### VI. Những điều bị cấm ❌
- ❌ Gọi axios trực tiếp trong component
- ❌ Hardcode role trong component
- ❌ Để component nghiệp vụ vào `components/ui`
- ❌ Để file > 300 dòng
- ❌ Import chéo feature

---

### VII. Quy tắc đặt tên
| Loại | Quy tắc |
|---|---|
| Component | `PascalCase` |
| Hook | `useSomething` |
| Service | `feature.service.js` |
| Constant | `UPPER_CASE` |

---

### VIII. Khi tạo feature mới
- **Bước 1**: Tạo folder: `features/product/`
- **Bước 2**: Tạo structure chuẩn:
  - `pages/`
  - `components/`
  - `product.service.js`
  - `product.constants.js`
- **Bước 3**: Chỉ code bên trong feature đó.

---

### IX. Khi project lớn hơn
Có thể nâng cấp theo pattern domain separation / vertical slice (như Airbnb, Netflix, Shopify).

---

### X. Tóm lại 5 nguyên tắc vàng
1. Chia theo feature (business domain)
2. Tách service khỏi component
3. CSS modular
4. Không import chéo feature
5. Component chỉ lo UI

---
---

## DESIGN SYSTEM & STYLING GUIDELINES

### 1. Color Palette (Bảng màu)

**Brand Colors:**
- **Primary (Rich Crema Amber):** `#D98324` (Dùng cho điểm nhấn, hover, active link, icon badge).
- **Secondary (Warm Saucer Beige):** `#E8C595` (Dùng cho các tag, text phụ trợ).

**Neutral / Background Colors (Richer & Thicker theme):**
- **Background Main:** `#FAF9F6` (Màu nền chính của website và form fields khi focus).
- **Background Offset / Surface:** `#EFECE3` (Màu nền phần filter, block highlights để tạo contrast).
- **White:** `#FFFFFF` (Nền của Card, Input fields).

**Text & Border Colors:**
- **Text Main (Warm Charcoal):** `#231F1E` (Text chính, Heading tiêu chuẩn, viền thẻ mặc định).
- **Text Muted:** `#231F1E` với Opacity 60% - tương đương `rgba(35, 31, 30, 0.6)` (Dùng cho breadcrumb, text phụ, table headers).
- **Border Default:** `rgba(35, 31, 30, 0.07)` đến `0.15` (Viền các input và card).

**Semantic Colors (Dành cho Badge / Trạng thái):**
- **Success (Green):** Text/Border `#1a7a42` hoặc `#27AE60` - Nền `rgba(39, 174, 96, 0.1)`.
- **Danger/Error (Red):** Text/Border `#C0392B` - Nền `rgba(192, 57, 43, 0.1)`.
- **Warning (Amber):** Kế thừa màu Primary `#D98324` - Nền `rgba(217, 131, 36, 0.12)`.
- **Neutral (Gray):** Nền `rgba(35, 31, 30, 0.06)`.

---

### 2. Typography (Kích thước & Font chữ)
Hệ thống kết hợp sự tương phản mạnh mẽ giữa Serif (Cổ điển) và Sans-serif (Hiện đại).

**Font Families:**
- **Heading Font:** Playfair Display, serif.
- **Body / UI Font:** Inter, sans-serif.

**Text Styles (Figma Text Properties):**
- **Page Title (Admin / Hero):** Playfair Display - Size `2.5rem (40px)` đến `7.5rem (120px)` tuỳ thiết bị - Weight 900 (Black).
- **Card Title Serif:** Playfair Display - Size `1.4rem (22px)` - Weight 700 (Bold).
- **UI Section Labels / Form Labels:** Inter - Size `0.65rem (10-11px)` - Weight 800 (ExtraBold) - Uppercase - Letter Spacing `2px - 3px`.
- **Body Text Defaults:** Inter - Size `0.85rem - 0.9rem (14px)` - Cần chú ý line-height rộng rải `1.6`.
- **Button Text:** Inter - Size `0.72rem (11.5px)` - Weight 800 (ExtraBold) - Uppercase - Letter Spacing `2px`.
- **Table Headers:** Inter - Size `0.62rem (10px)` - Weight 800 (ExtraBold) - Uppercase - Letter Spacing `2.5px`.

---

### 3. Shadows & Effects (Hiệu ứng thả bóng & Blur)

**Shadows (Drop Shadow):**
- **Button Hover Drop Shadow:** `0px 8px 25px rgba(217, 131, 36, 0.2)` (Cam nhẹ, tạo độ nổi cho button).
- **Focus Ring (Input/Trạng thái Active):** `0px 0px 0px 3px rgba(217, 131, 36, 0.08)`.
- **Toggle Switch Shadow:** `0px 1px 4px rgba(0, 0, 0, 0.2)`.

**Blurs (Backdrop Filter):**
- **Top Bar / Nav Backdrop:** Blur `12px` - Fill: `rgba(250, 249, 246, 0.95)` (Màu nền ngà mờ).
- **Modal Overlay Backdrop:** Blur `4px` - Fill: `rgba(35, 31, 30, 0.6)`.

> **Lưu ý (Chỉ định dạng Style):** Website có phủ một lớp SVG Grain Noise xuyên suốt trang với Opacity 8%, có thể tạo một layout noise blend mode Multiply trên cùng.

---

### 4. Components chính (Các thành phần UI)

#### A. Buttons (Các nút bấm - Auto Layout)
- **Padding chuẩn:** Dọc `0.9rem` (~14px) x Ngang `2.4rem` (~38px)
- **Primary Button:**
  - Default: Background `#231F1E` / Text `#FFFFFF`
  - Hover: Background `#D98324` / Tịnh tiến lên trên (Y: `-2px`) / Shadow cam.
- **Ghost Button:**
  - Default: Nền trong suốt / Text `#231F1E` / Stroke `1px rgba(35, 31, 30, 0.2)`
  - Hover: Đổi màu viền và text sang `#D98324`
- **Danger Button:** Background `#C0392B` / Text `#FFFFFF`
- **Icon Button:** Vuông `36x36px`, Stroke `1px rgba(35, 31, 30, 0.12)`, Căn giữa icon.

#### B. Form Fields (Khu vực nhập liệu)
- **Container Input:** Không bo góc (`Border-radius: 0`), Padding `0.8rem 1rem` (`13px x 16px`).
- **Màu nền (Default):** `#FAF9F6` / Stroke: `1px rgba(35, 31, 30, 0.15)`.
- **Trạng thái Focus:** Màu nền đổi thành `#FFFFFF` / Stroke thành `#D98324` / Bật hiệu ứng Focus Ring (bóng mờ cam).
- **Placeholder:** Text nghiêng (Italic), Opacity 30%.

#### C. Sidebar & Navigation Admin
- **Width:** Khóa ở mức `260px`
- **Màu nền:** `#231F1E` (Charcoal) - Text toàn bộ màu Trắng và Xám.
- **Trạng thái Active / Selected (Menu con):** Background `rgba(217, 131, 36, 0.1)` (Cam mờ) / Text `#D98324` / Có thêm Divider dọc `3px` bên trái màu `#D98324`.

#### D. Card & Panel Containers
- **Fill:** `#FFFFFF`
- **Stroke:** `1px` màu `rgba(35, 31, 30, 0.07)`
- **Góc (Radius):** Vuông vức hoàn toàn (`0px`) - bám sát tinh thần Editorial & Architecture.
- **Padding Card:** Nội khu vực `2rem` (~32px), phần thẻ Header (có gạch dưới) bottom margin/padding `1rem - 1.75rem`.
