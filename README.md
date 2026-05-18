# TestGen - AI-Powered Test Case Generator

Hệ thống sinh test case tự động cho lập trình thi đấu sử dụng AI (Groq/Gemini).

## 📋 Mục Lục

- [Tính Năng](#-tính-năng)
- [Phân Công Công Việc](#-phân-công-công-việc)
- [Hướng Dẫn Cài Đặt](#-hướng-dẫn-cài-đặt)
- [Hướng Dẫn Sử Dụng](#-hướng-dẫn-sử-dụng)
- [Kết Quả Chạy Thử Nghiệm](#-kết-quả-chạy-thử-nghiệm)
- [Cấu Trúc Project](#️-cấu-trúc-project)
- [Known Issues](#-known-issues)

---

## 🎯 Tính Năng

- ✅ **Phân tích đề bài**: Tự động phân tích đề bài từ text hoặc hình ảnh (OCR)
- ✅ **Sinh test cases**: Sinh test cases đa dạng (edge cases, medium, large)
- ✅ **Sinh code mẫu**: Sinh AC, WA, TLE code tự động
- ✅ **Đánh giá code**: Compile và chạy code trên test cases
- ✅ **OCR hỗ trợ**: Đọc đề bài từ hình ảnh (Tesseract OCR)
- ✅ **Multi-provider AI**: Hỗ trợ Groq và Gemini API

---

## 👥 Phân Công Công Việc

### Nguyễn Thành Huy - Backend AI & Integration
**Nhiệm vụ:**
- Tích hợp Groq API và Gemini API
- Xây dựng AIService, AIConnector, PromptBuilder
- Xử lý JSON response từ AI
- Sinh test cases và code mẫu (AC/WA/TLE)
- Tích hợp OCR với Gemini Vision API

**Files phụ trách:**
- `src/main/java/com/competitive/ai/*`
- `src/main/java/vn/testgen/backend/BackendService.java`
- `src/main/java/vn/testgen/util/KeyManager.java`

**Thời gian:** 2 tuần

---

### Hòa - GUI Development
**Nhiệm vụ:**
- Thiết kế giao diện Swing
- Xây dựng các panel: ProblemInput, TestGen, Evaluation, Settings
- Tích hợp UI với Backend
- Xử lý events và user interactions
- Theme và styling

**Files phụ trách:**
- `src/main/java/vn/testgen/ui/*`
- `src/main/java/vn/testgen/Main.java`

**Thời gian:** 2 tuần

---

### Trung - Compiler & Testing Services
**Nhiệm vụ:**
- Xây dựng CompilerService (compile C++/Java)
- TestcaseRunner (chạy code với test cases)
- Xử lý timeout, memory limit
- Phân loại verdict (AC/WA/TLE/RE/CE)
- Unit testing

**Files phụ trách:**
- `src/main/java/com/competitive/service/*`
- `src/test/java/com/competitive/bugfix/*`

**Thời gian:** 2 tuần

---

## 🚀 Hướng Dẫn Cài Đặt

### Yêu Cầu Hệ Thống

- **Java JDK**: 17 trở lên
- **C++ Compiler**: g++ (MinGW hoặc MSYS2)
- **Hệ điều hành**: Windows 10/11
- **RAM**: Tối thiểu 4GB
- **Dung lượng**: ~100MB

### Bước 1: Clone Repository

```bash
git clone https://github.com/your-username/BT-Submission.git
cd BT-Submission
```

### Bước 2: Kiểm Tra Java

```bash
java -version
```

Nếu chưa có Java, tải tại: https://www.oracle.com/java/technologies/downloads/

### Bước 3: Kiểm Tra C++ Compiler

```bash
g++ --version
```

Nếu chưa có g++:
- Tải MinGW: https://sourceforge.net/projects/mingw/
- Hoặc MSYS2: https://www.msys2.org/

### Bước 4: Cài Đặt Dependencies

Các thư viện đã có sẵn trong `lib/`:
- `jackson-core-2.15.2.jar`
- `jackson-databind-2.15.2.jar`
- `jackson-annotations-2.15.2.jar`
- `okhttp-4.12.0.jar`
- `okio-jvm-3.6.0.jar`

**Không cần cài đặt thêm!**

### Bước 5: Cài Đặt Tesseract OCR (Tùy chọn - cho tính năng đọc đề từ ảnh)

Nếu muốn dùng tính năng **đọc đề bài từ hình ảnh (OCR)**:

1. **Tải Tesseract** từ: https://github.com/UB-Mannheim/tesseract/wiki
   - Chọn phiên bản Windows 64-bit (ví dụ: `tesseract-ocr-w64-setup-5.4.0.20240606.exe`)
   - Hoặc tải bản portable (zip): https://github.com/UB-Mannheim/tesseract/releases

2. **Cài đặt**:
   - **Nếu dùng installer**: Cài đặt bình thường, chọn thêm ngôn ngữ English
   - **Nếu dùng bản portable (zip)**: Giải nén vào thư mục `tesseract/` trong project

3. **Cấu trúc thư mục cần có**:
   ```
   TestGen/
   └── tesseract/
       ├── tesseract.exe
       ├── tessdata/
       │   └── eng.traineddata
       └── (các file .dll khác)
   ```

4. **Kiểm tra**: Mở app → tab **Nhập Đề Bài** → chọn ảnh → nếu hiển thị "[OCR] Tesseract found" là OK

> **Lưu ý**: Thư mục `tesseract/` đã được gitignore do kích thước lớn (~200MB). Mỗi người dùng cần tải riêng.

### Bước 6: Cấu Hình API Keys (BẮT BUỘC)

**Bạn cần cung cấp API keys để sử dụng ứng dụng.**

#### Lấy API Keys miễn phí:
- **Groq**: https://console.groq.com/keys (14,400 requests/ngày) - Khuyến nghị
- **Gemini**: https://makersuite.google.com/app/apikey (20 requests/ngày)

#### Cách 1: Qua UI Settings (Dễ nhất - Khuyến nghị)
1. Chạy app
2. Vào tab **Settings**
3. Nhập API keys vào các ô tương ứng
4. Click "Save"

#### Cách 2: Qua config.properties
```bash
copy config.properties.example config.properties
```

Sửa file `config.properties`:
```properties
groq.api.key=your_groq_api_key_here
gemini.api.key=your_gemini_api_key_here
```

#### Cách 3: Qua Environment Variables
```cmd
set GROQ_API_KEY=your_groq_api_key_here
set GEMINI_API_KEY=your_gemini_api_key_here
```

**Ưu tiên load**: config.properties → Environment Variables → UI Settings

### Bước 7: Compile và Chạy

```bash
compile-and-run.bat
```

Hoặc nếu đã compile:
```bash
java -cp "out;lib/*" vn.testgen.Main
```

---

## 📖 Hướng Dẫn Sử Dụng

### 1. Khởi Động Ứng Dụng

Double-click `compile-and-run.bat` hoặc chạy:
```bash
java -cp "out;lib/*" vn.testgen.Main
```

### 2. Tab "Nhập Đề Bài"

#### Cách 1: Nhập Text
1. Paste đề bài vào ô text
2. Hoặc click "Chọn file" để load từ file `.txt`

#### Cách 2: Upload Hình Ảnh (OCR)
1. Click "Chọn ảnh"
2. Chọn file ảnh (.png, .jpg, .jpeg)
3. Đợi OCR xử lý (5-10 giây)
4. Kiểm tra và sửa text nếu cần

**Lưu ý OCR:**
- Ảnh phải rõ nét, độ phân giải cao
- Nền trắng, chữ đen cho kết quả tốt nhất
- Tránh ảnh bị nghiêng hoặc mờ

### 3. Tab "Sinh Test"

1. **Chọn số lượng test cases**: 1-20 (khuyến nghị 5-10)
2. **Click "Sinh test"**
3. **Đợi AI xử lý**: 10-30 giây tùy số lượng
4. **Xem kết quả**:
   - Input/Output cho mỗi test case
   - Có thể edit test cases thủ công
   - Click "Lưu test" để export

**Các loại test được sinh:**
- Edge cases (n=0, n=1, max values)
- Small cases (n < 10)
- Medium cases (10 ≤ n ≤ 100)
- Large cases (n > 100)

### 4. Tab "Đánh Giá Code"

#### Đánh giá code của bạn:
1. Paste code C++ vào ô "Code của bạn"
2. Click "Đánh giá"
3. Xem kết quả cho từng test case:
   - ✅ **AC** (Accepted): Đúng
   - ❌ **WA** (Wrong Answer): Sai output
   - ⏱️ **TLE** (Time Limit Exceeded): Quá thời gian
   - 💥 **RE** (Runtime Error): Lỗi runtime
   - 🔧 **CE** (Compilation Error): Lỗi compile

#### Sinh code mẫu từ AI:
1. Click "Sinh AC Code" - Code đúng
2. Click "Sinh WA Code" - Code sai (để test)
3. Click "Sinh TLE Code" - Code chậm (để test)

**Time limit mặc định**: 2 giây/test case

### 5. Tab "Settings"

#### Cấu hình AI Provider:
- **Groq** (llama-3.3-70b): 14,400 req/ngày - Khuyến nghị
- **Gemini** (gemini-2.0-flash): 20 req/ngày
- **OpenAI** (gpt-4o): Trả phí

#### Nhập API Keys:
- Text API Key: Cho sinh test và code
- Vision API Key: Cho OCR

#### Lưu cấu hình:
Click "Lưu" để áp dụng thay đổi

---

## 🧪 Kết Quả Chạy Thử Nghiệm

### Test Case 1: Bài Toán Đơn Giản (Two Sum)

**Đề bài:**
```
Cho mảng n số nguyên và số k. Tìm 2 số có tổng bằng k.
Input: n, k, a[1..n]
Output: 2 chỉ số i, j (hoặc -1 nếu không tìm thấy)
```

**Kết quả sinh test:**
- ✅ Sinh được 10 test cases trong 15 giây
- ✅ Bao gồm edge cases: n=0, n=1, không có cặp
- ✅ Test cases đa dạng: small, medium, large

**Kết quả đánh giá code:**
```
Test 1: AC (0.01s)
Test 2: AC (0.02s)
Test 3: WA (Expected: "1 3", Got: "1 2")
Test 4: AC (0.15s)
Test 5: TLE (>2.00s)
...
Tổng: 7/10 AC
```

### Test Case 2: Bài Toán Trung Bình (Longest Increasing Subsequence)

**Đề bài:**
```
Tìm độ dài dãy con tăng dài nhất
Input: n, a[1..n]
Output: Độ dài LIS
```

**Kết quả:**
- ✅ Sinh được 8 test cases trong 25 giây
- ✅ AI sinh code AC với độ phức tạp O(n log n)
- ✅ AI sinh code WA với thuật toán sai
- ✅ AI sinh code TLE với độ phức tạp O(n²)

**Đánh giá:**
- Code AC: 8/8 test passed (100%)
- Code WA: 3/8 test passed (37.5%)
- Code TLE: 2/8 test passed (timeout trên large cases)

### Test Case 3: OCR từ Hình Ảnh

**Input:** Ảnh chụp đề bài từ Codeforces

**Kết quả OCR:**
- ✅ Độ chính xác: ~85%
- ✅ Nhận diện được: text, số, ký tự đặc biệt
- ❌ Cần sửa: một số ký tự toán học (≤, ≥)
- ⏱️ Thời gian: 8 giây

**Sau khi sửa OCR:**
- ✅ Sinh test thành công
- ✅ Kết quả tương tự nhập text thủ công

### Test Case 4: Performance Test

**Cấu hình:**
- CPU: Intel i5-8250U
- RAM: 8GB
- OS: Windows 11

**Kết quả:**
| Số test cases | Thời gian sinh | Memory |
|---------------|----------------|--------|
| 5             | 12s            | 150MB  |
| 10            | 23s            | 180MB  |
| 15            | 35s            | 210MB  |
| 20            | 48s            | 250MB  |

**Compile & Run:**
| Ngôn ngữ | Compile time | Run time (per test) |
|----------|--------------|---------------------|
| C++      | 1-2s         | 0.01-0.5s           |
| Java     | 2-3s         | 0.05-1s             |

### Tổng Kết Thử Nghiệm

✅ **Thành công:**
- Sinh test cases chính xác và đa dạng
- Đánh giá code đúng verdict (AC/WA/TLE/RE)
- OCR hoạt động tốt với ảnh chất lượng cao
- UI responsive, dễ sử dụng

⚠️ **Hạn chế:**
- AI đôi khi sinh code không tối ưu
- OCR yêu cầu ảnh rõ nét
- Giới hạn API calls (14,400/ngày với Groq)
- Chỉ hỗ trợ C++ cho evaluation

---

## 🏗️ Cấu Trúc Project

```
BT-Submission/
├── src/
│   ├── main/java/
│   │   ├── com/competitive/
│   │   │   ├── ai/                    # AI Services
│   │   │   │   ├── AIConnector.java   # API connector
│   │   │   │   ├── AIService.java     # Main AI service
│   │   │   │   ├── PromptBuilder.java # Prompt engineering
│   │   │   │   └── ResponseParser.java # Parse AI response
│   │   │   ├── model/                 # Data models
│   │   │   │   ├── Problem.java
│   │   │   │   ├── Testcase.java
│   │   │   │   └── SampleCode.java
│   │   │   └── service/               # Services
│   │   │       ├── CompilerService.java
│   │   │       └── TestcaseRunner.java
│   │   └── vn/testgen/
│   │       ├── backend/
│   │       │   └── BackendService.java # Backend integration
│   │       ├── model/                  # GUI models
│   │       │   ├── Problem.java
│   │       │   └── TestCase.java
│   │       ├── ui/                     # GUI components
│   │       │   ├── MainFrame.java
│   │       │   ├── ProblemInputPanel.java
│   │       │   ├── TestGenPanel.java
│   │       │   ├── EvaluationPanel.java
│   │       │   ├── SettingsPanel.java
│   │       │   ├── Components.java
│   │       │   └── Theme.java
│   │       ├── util/                   # Utilities
│   │       │   ├── Constants.java
│   │       │   ├── Logger.java
│   │       │   ├── Validator.java
│   │       │   └── KeyManager.java
│   │       └── Main.java               # Entry point
│   └── test/java/                      # Unit tests
│       └── com/competitive/bugfix/
├── lib/                                # Dependencies
│   ├── jackson-*.jar
│   ├── okhttp-*.jar
│   └── okio-*.jar
├── tesseract/                          # Tesseract OCR (optional)
├── compile-and-run.bat                 # Build & run script
├── config.properties.example           # Config template
├── .gitignore
└── README.md
```

---

## 🐛 Known Issues

### 1. OCR Accuracy
- **Vấn đề**: OCR đôi khi nhận diện sai ký tự đặc biệt
- **Giải pháp**: Kiểm tra và sửa text sau OCR

### 2. API Rate Limits
- **Vấn đề**: Groq giới hạn 14,400 requests/ngày
- **Giải pháp**: Dùng API key riêng hoặc chuyển sang Gemini

### 3. Timeout với Large Test Cases
- **Vấn đề**: Code chậm có thể timeout
- **Giải pháp**: Tăng time limit trong Settings (mặc định 2s)

### 4. Compiler Path
- **Vấn đề**: g++ không tìm thấy
- **Giải pháp**: Thêm g++ vào PATH environment variable

### 5. AI Generated Code Quality
- **Vấn đề**: AI đôi khi sinh code không tối ưu
- **Giải pháp**: Review và tối ưu code thủ công

---

## 📚 Tài Liệu Tham Khảo

- [Groq API Documentation](https://console.groq.com/docs)
- [Gemini API Documentation](https://ai.google.dev/docs)
- [Tesseract OCR](https://github.com/tesseract-ocr/tesseract)
- [Jackson JSON](https://github.com/FasterXML/jackson)
- [OkHttp](https://square.github.io/okhttp/)

---

## 📞 Liên Hệ

**Nhóm phát triển:**
- Nguyễn Thành Huy - Backend AI
- Hòa - GUI Development  
- Trung - Compiler Services

**Email**: [huyhgbv1204@gmail.com]

**GitHub**: [https://github.com/huynguyen12406/TestGen]

---

## 📄 License

Educational project - HCMUT  
© 2026 All rights reserved

---

**Ngày tạo**: 18/05/2026  
**Version**: 2.0  
**Last updated**: 18/05/2026
