package com.competitive.ai;

import com.competitive.model.Problem;

/**
 * Xây dựng các prompt chuẩn để gửi tới AI.
 * Mỗi prompt được thiết kế để nhận output có cấu trúc dễ parse.
 * 
 * @author Nguyễn Thành Huy
 * @version 1.0
 */
public class PromptBuilder {
    
    // ===== 1. PHÂN TÍCH ĐỀ BÀI =====
    
    /**
     * Prompt phân tích đề bài từ văn bản.
     * 
     * @param rawText văn bản đề bài
     * @return prompt để gửi cho AI
     */
    public static String analyzeProblemFromText(String rawText) {
        return """
Bạn là chuyên gia phân tích đề bài lập trình thi đấu (IOI/ICPC/Codeforces).

Hãy phân tích đề bài sau và trả về JSON theo đúng format dưới đây (KHÔNG có markdown, KHÔNG có backtick):

{
  "title": "Tên bài toán",
  "statement": "Mô tả bài toán đầy đủ",
  "inputFormat": "Định dạng input",
  "outputFormat": "Định dạng output",
  "constraints": "Ràng buộc về giá trị các biến",
  "timeLimit": "Giới hạn thời gian (vd: 1s)",
  "memoryLimit": "Giới hạn bộ nhớ (vd: 256MB)",
  "problemType": "Loại bài (DP, Graph, Greedy, Math, ...)",
  "examples": "Ví dụ input/output từ đề bài (nếu có)"
}

QUAN TRỌNG: Nếu đề bài có ví dụ input/output, hãy trích xuất và đưa vào field "examples".

ĐỀ BÀI:
""" + rawText;
    }
    
    /**
     * Prompt phân tích đề bài từ hình ảnh (chỉ cần phần text prompt, hình ảnh gửi riêng).
     * 
     * @return prompt để gửi cho AI cùng với hình ảnh
     */
    public static String analyzeProblemFromImage() {
        return """
Bạn là chuyên gia phân tích đề bài lập trình thi đấu (IOI/ICPC/Codeforces).

Hãy đọc đề bài trong hình ảnh và trả về JSON theo đúng format sau.

QUAN TRỌNG: Chỉ trả về JSON thuần túy, KHÔNG có markdown (```json hoặc ```), KHÔNG có giải thích.

Format JSON (bắt buộc có đủ tất cả các field):

{
  "title": "Tên bài toán",
  "statement": "Mô tả bài toán đầy đủ (bao gồm cả ví dụ nếu có)",
  "inputFormat": "Định dạng input chi tiết",
  "outputFormat": "Định dạng output chi tiết",
  "constraints": "Ràng buộc về giá trị các biến (vd: 1 ≤ n ≤ 10^6)",
  "timeLimit": "Giới hạn thời gian (vd: 1s, 1000ms)",
  "memoryLimit": "Giới hạn bộ nhớ (vd: 256MB)",
  "problemType": "Loại bài (DP, Graph, Greedy, Math, String, Implementation, ...)",
  "examples": "Ví dụ input/output từ đề bài (nếu có)"
}

Lưu ý:
- Nếu không thấy rõ inputFormat/outputFormat, hãy suy luận từ ví dụ
- Nếu không có timeLimit/memoryLimit, dùng giá trị mặc định: "1s" và "256MB"
- Nếu đề bài có ví dụ input/output, hãy trích xuất và đưa vào field "examples"
- Đảm bảo JSON hợp lệ, không có trailing comma

Bắt đầu ngay bằng dấu { (mở JSON):
""";
    }
    
    // ===== 2. SINH TEST CASE =====
    
    /**
     * Prompt sinh test inputs only (KHÔNG có expected output).
     * Dùng cho verification workflow: AI sinh inputs, sau đó run AC code để lấy expected output.
     * 
     * @param problem đối tượng Problem chứa thông tin đề bài
     * @param count số lượng test inputs cần sinh
     * @return prompt để gửi cho AI
     */
    public static String generateTestInputsOnly(Problem problem, int count) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là chuyên gia tạo test case cho bài lập trình thi đấu.\n\n");
        
        // Thêm random seed để AI sinh test cases khác nhau mỗi lần
        long seed = System.currentTimeMillis();
        prompt.append("Random seed: ").append(seed).append("\n");
        prompt.append("Hãy sinh test inputs ĐA DẠNG và KHÁC NHAU với các lần trước.\n\n");
        
        prompt.append("ĐỀ BÀI:\n").append(problem.getStatement()).append("\n\n");
        prompt.append("INPUT FORMAT: ").append(problem.getInputFormat()).append("\n\n");
        prompt.append("CONSTRAINTS: ").append(problem.getConstraints()).append("\n\n");
        
        // Thêm VÍ DỤ từ đề bài nếu có
        if (problem.getExamples() != null && !problem.getExamples().isBlank()) {
            prompt.append("VÍ DỤ INPUT/OUTPUT TỪ ĐỀ BÀI (HỌC THEO FORMAT INPUT):\n");
            prompt.append(problem.getExamples()).append("\n\n");
            prompt.append("⚠️ QUAN TRỌNG: Sinh test input theo ĐÚNG FORMAT như ví dụ trên!\n\n");
        }
        
        prompt.append("⚠️ NHIỆM VỤ ĐẶC BIỆT: CHỈ SINH TEST INPUTS - KHÔNG SINH EXPECTED OUTPUTS\n\n");
        
        prompt.append("Hãy sinh ").append(count).append(" test inputs đa dạng bao gồm:\n");
        prompt.append("- Test input thông thường (30%)\n");
        prompt.append("- Test input biên (giá trị min/max) (30%)\n");
        prompt.append("- Test input đặc biệt (n=1, mảng rỗng, ...) (20%)\n");
        prompt.append("- Test input stress (giá trị lớn) (20%)\n");
        prompt.append("- Mỗi test phải KHÁC NHAU về input và độ khó\n");
        prompt.append("- KHÔNG sinh test giống nhau\n");
        prompt.append("- Thay đổi giá trị, thứ tự, độ dài để tạo sự đa dạng\n\n");
        
        prompt.append("⚠️ GIỚI HẠN ĐỘ DÀI INPUT (CỰC KỲ QUAN TRỌNG - ĐỌC KỸ):\n");
        prompt.append("- Mỗi test input KHÔNG được quá 500 ký tự\n");
        prompt.append("- Nếu n lớn (vd: n=1000), chỉ sinh 10-20 số đầu tiên làm đại diện\n");
        prompt.append("- VÍ DỤ: Thay vì sinh 1000 số, chỉ sinh: \"1000\\n1 2 3 4 5 6 7 8 9 10 ... (và 990 số khác)\"\n");
        prompt.append("- Hoặc tốt hơn: Giảm n xuống (vd: n=50 thay vì n=1000) để input ngắn gọn\n");
        prompt.append("- Test input stress: n tối đa 50-100, TUYỆT ĐỐI KHÔNG sinh n=1000 hay n=10000\n");
        prompt.append("- Mục tiêu: Input ngắn gọn, dễ đọc, nhưng vẫn đủ test các trường hợp\n");
        prompt.append("- ⚠️ CỰC KỲ QUAN TRỌNG: Input quá dài (>500 chars) sẽ bị REJECT\n\n");
        
        prompt.append("⚠️ VÍ DỤ GIỚI HẠN ĐỘ DÀI:\n");
        prompt.append("✓ ĐÚNG (n=50, input ~100 chars):\n");
        prompt.append("50\n");
        prompt.append("1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50\n\n");
        
        prompt.append("✗ SAI (n=1000, input >3000 chars - QUÁ DÀI):\n");
        prompt.append("1000\n");
        prompt.append("1 2 3 4 5 ... (1000 số) ...\n\n");
        
        prompt.append("⚠️ CỰC KỲ QUAN TRỌNG VỀ INPUT FORMAT:\n");
        prompt.append("- Input PHẢI theo ĐÚNG format mô tả trong INPUT FORMAT ở trên\n");
        prompt.append("- ĐỌC KỸ INPUT FORMAT và làm CHÍNH XÁC theo đó\n");
        prompt.append("- Nếu INPUT FORMAT yêu cầu 'n số nguyên trên một dòng cách nhau bởi dấu cách', hãy viết: 1 2 3 4 5\n");
        prompt.append("- Nếu INPUT FORMAT yêu cầu 'mỗi số trên một dòng', hãy viết mỗi số trên một dòng riêng\n");
        prompt.append("- Nếu INPUT FORMAT yêu cầu format đặc biệt (JSON, XML, v.v.), hãy làm theo đó\n");
        prompt.append("- Input phải giống như người dùng nhập từ bàn phím hoặc file text\n\n");
        
        prompt.append("VÍ DỤ: Nếu INPUT FORMAT là 'Dòng đầu n, dòng thứ hai n số nguyên cách nhau bởi dấu cách':\n");
        prompt.append("✓ ĐÚNG:\n");
        prompt.append("5\n");
        prompt.append("10 20 30 40 50\n\n");
        
        prompt.append("✗ SAI (nếu INPUT FORMAT không yêu cầu JSON):\n");
        prompt.append("[10, 20, 30, 40, 50]\n");
        prompt.append("{n: 5, arr: [10, 20, 30, 40, 50]}\n\n");
        
        prompt.append("⚠️ FORMAT RESPONSE (CỰC KỲ QUAN TRỌNG - ĐỌC KỸ):\n");
        prompt.append("1. Trả về JSON array thuần túy - BẮT ĐẦU NGAY BẰNG DẤU [\n");
        prompt.append("2. TUYỆT ĐỐI KHÔNG có markdown code blocks (```json hoặc ```)\n");
        prompt.append("3. TUYỆT ĐỐI KHÔNG có text giải thích trước JSON\n");
        prompt.append("4. TUYỆT ĐỐI KHÔNG có text giải thích sau JSON\n");
        prompt.append("5. Dòng đầu tiên PHẢI là dấu [ (mở array)\n");
        prompt.append("6. Dòng cuối cùng PHẢI là dấu ] (đóng array)\n");
        prompt.append("7. Đảm bảo JSON hợp lệ (không có trailing comma ở phần tử cuối)\n");
        prompt.append("8. MỖI test input PHẢI có đủ 4 fields: id, input, description, type\n");
        prompt.append("9. ⚠️ TUYỆT ĐỐI KHÔNG có field 'expectedOutput' - CHỈ CÓ INPUT\n\n");
        
        prompt.append("Format mỗi test input:\n");
        prompt.append("{\n");
        prompt.append("  \"id\": 1,\n");
        prompt.append("  \"input\": \"5\\n10 20 30 40 50\",\n");
        prompt.append("  \"description\": \"Test mảng tăng dần\",\n");
        prompt.append("  \"type\": \"NORMAL\"\n");
        prompt.append("}\n\n");
        
        prompt.append("CHÚ Ý: Field 'input' phải là STRING chứa input ĐÚNG FORMAT, dùng \\n cho xuống dòng\n\n");
        
        prompt.append("VÍ DỤ RESPONSE ĐÚNG:\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"id\": 1,\n");
        prompt.append("    \"input\": \"5\\n10 20 30 40 50\",\n");
        prompt.append("    \"description\": \"Test mảng tăng dần\",\n");
        prompt.append("    \"type\": \"NORMAL\"\n");
        prompt.append("  },\n");
        prompt.append("  {\n");
        prompt.append("    \"id\": 2,\n");
        prompt.append("    \"input\": \"3\\n-5 0 3\",\n");
        prompt.append("    \"description\": \"Test có số âm\",\n");
        prompt.append("    \"type\": \"NORMAL\"\n");
        prompt.append("  },\n");
        prompt.append("  {\n");
        prompt.append("    \"id\": 3,\n");
        prompt.append("    \"input\": \"1\\n42\",\n");
        prompt.append("    \"description\": \"Test mảng 1 phần tử\",\n");
        prompt.append("    \"type\": \"EDGE\"\n");
        prompt.append("  }\n");
        prompt.append("]\n\n");
        
        prompt.append("QUAN TRỌNG:\n");
        prompt.append("1. Input phải theo ĐÚNG format mô tả trong INPUT FORMAT\n");
        prompt.append("2. Input KHÔNG được quá 500 ký tự\n");
        prompt.append("3. Đảm bảo JSON hợp lệ (không có trailing comma)\n");
        prompt.append("4. KHÔNG thêm bất kỳ text nào ngoài JSON\n");
        prompt.append("5. MỖI test input PHẢI có đủ 4 fields bắt buộc (id, input, description, type)\n");
        prompt.append("6. ⚠️ TUYỆT ĐỐI KHÔNG có field 'expectedOutput'\n\n");
        
        prompt.append("BẮT ĐẦU NGAY VỚI DẤU [ (KHÔNG CÓ TEXT TRƯỚC ĐÓ):");
        
        return prompt.toString();
    }
    
    /**
     * Prompt sinh test case phong phú.
     * 
     * @param problem đối tượng Problem chứa thông tin đề bài
     * @param count số lượng test case cần sinh
     * @return prompt để gửi cho AI
     */
    public static String generateTestCases(Problem problem, int count) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là chuyên gia tạo test case cho bài lập trình thi đấu.\n\n");
        
        // Thêm random seed để AI sinh test cases khác nhau mỗi lần
        long seed = System.currentTimeMillis();
        prompt.append("Random seed: ").append(seed).append("\n");
        prompt.append("Hãy sinh test cases ĐA DẠNG và KHÁC NHAU với các lần trước.\n\n");
        
        prompt.append("ĐỀ BÀI:\n").append(problem.getStatement()).append("\n\n");
        prompt.append("INPUT FORMAT: ").append(problem.getInputFormat()).append("\n\n");
        prompt.append("OUTPUT FORMAT (⚠️ CHÚ Ý CHỮ HOA/THƯỜNG): ").append(problem.getOutputFormat()).append("\n\n");
        prompt.append("CONSTRAINTS: ").append(problem.getConstraints()).append("\n\n");
        
        // Thêm VÍ DỤ từ đề bài nếu có
        if (problem.getExamples() != null && !problem.getExamples().isBlank()) {
            prompt.append("VÍ DỤ INPUT/OUTPUT TỪ ĐỀ BÀI (HỌC THEO FORMAT NÀY):\n");
            prompt.append(problem.getExamples()).append("\n\n");
            prompt.append("⚠️ QUAN TRỌNG: Sinh test case theo ĐÚNG FORMAT như ví dụ trên!\n\n");
        }
        
        prompt.append("Hãy sinh ").append(count).append(" test case đa dạng bao gồm:\n");
        prompt.append("- Test case thông thường (30%)\n");
        prompt.append("- Test case biên (giá trị min/max) (30%)\n");
        prompt.append("- Test case đặc biệt (n=1, mảng rỗng, ...) (20%)\n");
        prompt.append("- Test case stress (giá trị lớn) (20%)\n");
        prompt.append("- Mỗi test phải KHÁC NHAU về input và độ khó\n");
        prompt.append("- KHÔNG sinh test giống nhau\n");
        prompt.append("- Thay đổi giá trị, thứ tự, độ dài để tạo sự đa dạng\n\n");
        
        prompt.append("⚠️ GIỚI HẠN ĐỘ DÀI INPUT (CỰC KỲ QUAN TRỌNG):\n");
        prompt.append("- Mỗi test case input KHÔNG được quá 500 ký tự\n");
        prompt.append("- Nếu n lớn (vd: n=1000), chỉ sinh 10-20 số đầu tiên làm đại diện\n");
        prompt.append("- VÍ DỤ: Thay vì sinh 1000 số, chỉ sinh: \"1000\\n1 2 3 4 5 6 7 8 9 10 ... (và 990 số khác)\"\n");
        prompt.append("- Hoặc tốt hơn: Giảm n xuống (vd: n=100 thay vì n=1000) để input ngắn gọn\n");
        prompt.append("- Test case stress: n tối đa 100-200, KHÔNG sinh n=10000\n");
        prompt.append("- Mục tiêu: Input ngắn gọn, dễ đọc, nhưng vẫn đủ test các trường hợp\n\n");
        
        prompt.append("⚠️ CỰC KỲ QUAN TRỌNG VỀ INPUT FORMAT:\n");
        prompt.append("- Input PHẢI theo ĐÚNG format mô tả trong INPUT FORMAT ở trên\n");
        prompt.append("- ĐỌC KỸ INPUT FORMAT và làm CHÍNH XÁC theo đó\n");
        prompt.append("- Nếu INPUT FORMAT yêu cầu 'n số nguyên trên một dòng cách nhau bởi dấu cách', hãy viết: 1 2 3 4 5\n");
        prompt.append("- Nếu INPUT FORMAT yêu cầu 'mỗi số trên một dòng', hãy viết mỗi số trên một dòng riêng\n");
        prompt.append("- Nếu INPUT FORMAT yêu cầu format đặc biệt (JSON, XML, v.v.), hãy làm theo đó\n");
        prompt.append("- Input phải giống như người dùng nhập từ bàn phím hoặc file text\n\n");
        
        prompt.append("VÍ DỤ: Nếu INPUT FORMAT là 'Dòng đầu n, dòng thứ hai n số nguyên cách nhau bởi dấu cách':\n");
        prompt.append("✓ ĐÚNG:\n");
        prompt.append("5\n");
        prompt.append("10 20 30 40 50\n\n");
        
        prompt.append("✗ SAI (nếu INPUT FORMAT không yêu cầu JSON):\n");
        prompt.append("[10, 20, 30, 40, 50]\n");
        prompt.append("{n: 5, arr: [10, 20, 30, 40, 50]}\n\n");
        
        prompt.append("⚠️ FORMAT RESPONSE (CỰC KỲ QUAN TRỌNG - ĐỌC KỸ):\n");
        prompt.append("1. Trả về JSON array thuần túy - BẮT ĐẦU NGAY BẰNG DẤU [\n");
        prompt.append("2. TUYỆT ĐỐI KHÔNG có markdown code blocks (```json hoặc ```)\n");
        prompt.append("3. TUYỆT ĐỐI KHÔNG có text giải thích trước JSON\n");
        prompt.append("4. TUYỆT ĐỐI KHÔNG có text giải thích sau JSON\n");
        prompt.append("5. Dòng đầu tiên PHẢI là dấu [ (mở array)\n");
        prompt.append("6. Dòng cuối cùng PHẢI là dấu ] (đóng array)\n");
        prompt.append("7. Đảm bảo JSON hợp lệ (không có trailing comma ở phần tử cuối)\n");
        prompt.append("8. MỖI test case PHẢI có đủ 5 fields: id, input, expectedOutput, description, type\n\n");
        
        prompt.append("Format mỗi test case:\n");
        prompt.append("{\n");
        prompt.append("  \"id\": 1,\n");
        prompt.append("  \"input\": \"5\\n10 20 30 40 50\",\n");
        prompt.append("  \"expectedOutput\": \"50\",\n");
        prompt.append("  \"description\": \"Test mảng tăng dần\",\n");
        prompt.append("  \"type\": \"NORMAL\"\n");
        prompt.append("}\n\n");
        
        prompt.append("CHÚ Ý: Field 'input' phải là STRING chứa input ĐÚNG FORMAT, dùng \\n cho xuống dòng\n\n");
        
        prompt.append("VÍ DỤ RESPONSE ĐÚNG:\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"id\": 1,\n");
        prompt.append("    \"input\": \"5\\n10 20 30 40 50\",\n");
        prompt.append("    \"expectedOutput\": \"50\",\n");
        prompt.append("    \"description\": \"Test mảng tăng dần\",\n");
        prompt.append("    \"type\": \"NORMAL\"\n");
        prompt.append("  },\n");
        prompt.append("  {\n");
        prompt.append("    \"id\": 2,\n");
        prompt.append("    \"input\": \"3\\n-5 0 3\",\n");
        prompt.append("    \"expectedOutput\": \"3\",\n");
        prompt.append("    \"description\": \"Test có số âm\",\n");
        prompt.append("    \"type\": \"NORMAL\"\n");
        prompt.append("  }\n");
        prompt.append("]\n\n");
        
        prompt.append("⚠️ CHÚ Ý VỀ CHỮ HOA/THƯỜNG:\n");
        prompt.append("Nếu OUTPUT FORMAT yêu cầu in \"CHAN\" hoặc \"LE\":\n");
        prompt.append("✓ ĐÚNG: \"expectedOutput\": \"CHAN\"  (chữ HOA)\n");
        prompt.append("✗ SAI:  \"expectedOutput\": \"Chan\"  (chữ thường)\n");
        prompt.append("✗ SAI:  \"expectedOutput\": \"chan\"  (chữ thường)\n\n");
        
        prompt.append("⚠️ CỰC KỲ QUAN TRỌNG VỀ expectedOutput:\n");
        prompt.append("1. Tính toán expectedOutput CHÍNH XÁC theo logic bài toán\n");
        prompt.append("2. expectedOutput PHẢI CHÍNH XÁC về CHỮ HOA/THƯỜNG theo OUTPUT FORMAT\n");
        prompt.append("3. Nếu OUTPUT FORMAT yêu cầu \"CHAN\", KHÔNG được viết \"Chan\" hay \"chan\"\n");
        prompt.append("4. Nếu OUTPUT FORMAT yêu cầu \"LE\", KHÔNG được viết \"Le\" hay \"le\"\n");
        prompt.append("5. ĐỌC KỸ OUTPUT FORMAT và copy CHÍNH XÁC chữ hoa/thường\n\n");
        
        prompt.append("QUAN TRỌNG:\n");
        prompt.append("1. Input phải theo ĐÚNG format mô tả trong INPUT FORMAT\n");
        prompt.append("2. Đảm bảo JSON hợp lệ (không có trailing comma)\n");
        prompt.append("3. KHÔNG thêm bất kỳ text nào ngoài JSON\n");
        prompt.append("4. MỖI test case PHẢI có đủ 5 fields bắt buộc\n\n");
        
        prompt.append("BẮT ĐẦU NGAY VỚI DẤU [ (KHÔNG CÓ TEXT TRƯỚC ĐÓ):");
        
        return prompt.toString();
    }
    
    // ===== 3. SINH CODE MẪU =====
    
    /**
     * Prompt sinh code AC (Accepted - đúng hoàn toàn).
     * 
     * @param problem đối tượng Problem
     * @return prompt để gửi cho AI
     */
    public static String generateACCode(Problem problem) {
        return "Bạn là lập trình viên competitive programming giỏi.\n\n" +
               "🎯 MỤC ĐÍCH: Code AC này dùng để TẠO EXPECTED OUTPUT CHUẨN cho test cases\n" +
               "- Code phải CHÍNH XÁC 100% - không được sai bất kỳ test case nào\n" +
               "- Output của code này sẽ được dùng làm expected output để chấm các code khác\n" +
               "- Code phải xử lý ĐÚNG TẤT CẢ EDGE CASES (n=0, n=1, số âm, mảng rỗng, v.v.)\n\n" +
               "ĐỀ BÀI:\n" + problem.getStatement() + "\n\n" +
               "INPUT FORMAT: " + problem.getInputFormat() + "\n\n" +
               "OUTPUT FORMAT: " + problem.getOutputFormat() + "\n\n" +
               "⚠️ CONSTRAINTS (ĐỌC KỸ - QUAN TRỌNG CHO THUẬT TOÁN): " + problem.getConstraints() + "\n\n" +
               "TIME LIMIT: " + problem.getTimeLimit() + "\n\n" +
               "Viết code C++ CHÍNH XÁC (AC - Accepted) giải bài này.\n\n" +
               "YÊU CẦU BẮT BUỘC:\n" +
               "1. Dùng #include <iostream> và các thư viện cần thiết\n" +
               "2. Phải có hàm: int main()\n" +
               "3. ⚠️ BẮT BUỘC: Thêm 2 dòng tối ưu I/O ngay đầu main():\n" +
               "   ios_base::sync_with_stdio(false);\n" +
               "   cin.tie(NULL);\n" +
               "4. Dùng cin để đọc input từ stdin\n" +
               "5. Dùng cout để in kết quả ra stdout\n" +
               "6. ⚠️ CỰC KỲ QUAN TRỌNG - XỬ LÝ EDGE CASES:\n" +
               "   - BẮT BUỘC kiểm tra n=0 (mảng rỗng, không có phần tử)\n" +
               "   - BẮT BUỘC kiểm tra n=1 (chỉ 1 phần tử)\n" +
               "   - BẮT BUỘC kiểm tra số âm (nếu constraints cho phép)\n" +
               "   - BẮT BUỘC kiểm tra giá trị min/max của constraints\n" +
               "   - BẮT BUỘC kiểm tra trường hợp đặc biệt (vd: tất cả phần tử bằng nhau)\n" +
               "   - Ví dụ: if (n == 0) { cout << 0 << endl; return 0; }\n" +
               "   - Ví dụ: if (n == 1) { cout << arr[0] << endl; return 0; }\n" +
               "7. ⚠️ CHỌN THUẬT TOÁN TỐI ƯU DỰA TRÊN CONSTRAINTS:\n" +
               "   - BẮT BUỘC: Đọc CONSTRAINTS ở trên để biết giá trị n tối đa\n" +
               "   - Nếu n ≤ 100: O(n²) hoặc O(n³) chấp nhận được\n" +
               "   - Nếu n ≤ 10,000: Cần O(n log n) hoặc O(n²)\n" +
               "   - Nếu n ≤ 100,000: Cần O(n) hoặc O(n log n)\n" +
               "   - Nếu n ≤ 1,000,000: BẮT BUỘC O(n) hoặc O(log n)\n" +
               "   - Dùng cấu trúc dữ liệu phù hợp: unordered_map, map, set, vector, queue, stack\n" +
               "   - TUYỆT ĐỐI TRÁNH brute force O(n²) khi n > 10,000\n" +
               "8. Có comment giải thích thuật toán và độ phức tạp (vd: // O(n) using unordered_map)\n" +
               "9. Dùng using namespace std;\n" +
               "10. QUAN TRỌNG: In output CHÍNH XÁC theo OUTPUT FORMAT (chú ý chữ hoa/thường, dấu cách, xuống dòng)\n\n" +
               "VÍ DỤ XỬ LÝ EDGE CASES:\n" +
               "```cpp\n" +
               "// Ví dụ: Tìm max trong mảng\n" +
               "int n;\n" +
               "cin >> n;\n" +
               "\n" +
               "// Edge case: n = 0\n" +
               "if (n == 0) {\n" +
               "    cout << \"Mảng rỗng\" << endl;\n" +
               "    return 0;\n" +
               "}\n" +
               "\n" +
               "vector<int> arr(n);\n" +
               "for (int i = 0; i < n; i++) cin >> arr[i];\n" +
               "\n" +
               "// Edge case: n = 1\n" +
               "if (n == 1) {\n" +
               "    cout << arr[0] << endl;\n" +
               "    return 0;\n" +
               "}\n" +
               "\n" +
               "// Logic chính\n" +
               "int maxVal = arr[0];\n" +
               "for (int i = 1; i < n; i++) {\n" +
               "    maxVal = max(maxVal, arr[i]);\n" +
               "}\n" +
               "cout << maxVal << endl;\n" +
               "```\n\n" +
               "VÍ DỤ THUẬT TOÁN TỐI ƯU:\n" +
               "- Tìm cặp có tổng = K: Dùng unordered_map O(n) thay vì 2 vòng for O(n²)\n" +
               "- Tìm max/min: Dùng 1 vòng for O(n) thay vì sort O(n log n)\n" +
               "- Kiểm tra tồn tại: Dùng set/map O(log n) thay vì tìm kiếm tuyến tính O(n)\n" +
               "- Đếm tần suất: Dùng unordered_map O(n) thay vì đếm từng phần tử O(n²)\n\n" +
               "FORMAT RESPONSE (CỰC KỲ QUAN TRỌNG):\n" +
               "- Dòng đầu tiên PHẢI là: #include <iostream> (hoặc #include khác)\n" +
               "- KHÔNG được có bất kỳ text nào trước code\n" +
               "- KHÔNG được có markdown: ```cpp hoặc ```\n" +
               "- KHÔNG được có giải thích bằng văn bản trước hoặc sau code\n" +
               "- KHÔNG được có dòng trống ở đầu\n\n" +
               "VÍ DỤ FORMAT ĐÚNG:\n" +
               "#include <iostream>\n" +
               "using namespace std;\n\n" +
               "int main() {\n" +
               "    ios_base::sync_with_stdio(false);\n" +
               "    cin.tie(NULL);\n" +
               "    \n" +
               "    int n;\n" +
               "    cin >> n;\n" +
               "    \n" +
               "    // Edge case: n = 0\n" +
               "    if (n == 0) {\n" +
               "        cout << 0 << endl;\n" +
               "        return 0;\n" +
               "    }\n" +
               "    \n" +
               "    // Logic chính\n" +
               "    // CHÚ Ý: In output đúng format (vd: \"CHAN\" không phải \"Even\")\n" +
               "    return 0;\n" +
               "}\n\n" +
               "BẮT ĐẦU NGAY TỪ DÒNG ĐẦU TIÊN VỚI CODE:";
    }
    
    /**
     * Prompt sinh code WA (Wrong Answer - sai một số trường hợp).
     * 
     * @param problem đối tượng Problem
     * @return prompt để gửi cho AI
     */
    public static String generateWACode(Problem problem) {
        return "⚠️ NHIỆM VỤ ĐẶC BIỆT: Viết code SAI CÓ CHỦ Ý (WA - Wrong Answer)\n\n" +
               "Bạn là lập trình viên viết code SAI cho mục đích kiểm thử.\n\n" +
               "🎯 MỤC ĐÍCH: Code WA này dùng để KIỂM TRA TEST CASE CÓ ĐỦ MẠNH KHÔNG\n" +
               "- Nếu test case tốt → Code WA phải bị bắt lỗi (WA verdict)\n" +
               "- Nếu test case yếu → Code WA có thể pass (AC verdict) - test case cần cải thiện\n\n" +
               "ĐỀ BÀI:\n" + problem.getStatement() + "\n\n" +
               "INPUT FORMAT: " + problem.getInputFormat() + "\n\n" +
               "OUTPUT FORMAT: " + problem.getOutputFormat() + "\n\n" +
               "CONSTRAINTS: " + problem.getConstraints() + "\n\n" +
               "⚠️ CỰC KỲ QUAN TRỌNG - ĐỌC KỸ:\n" +
               "- Đây KHÔNG phải code AC (Accepted)\n" +
               "- Đây là code WA (Wrong Answer) - code phải có LỖI LOGIC\n" +
               "- Code phải COMPILE được nhưng cho KẾT QUẢ SAI ở một số test case\n" +
               "- KHÔNG được viết code đúng hoàn toàn\n" +
               "- Lỗi phải là lỗi THƯỜNG GẶP mà học sinh hay mắc phải\n\n" +
               "YÊU CẦU LỖI (BẮT BUỘC PHẢI CÓ LỖI):\n" +
               "1. Lỗi phải RÕ RÀNG và dễ phát hiện bằng test cases TỐT\n" +
               "2. Gợi ý lỗi tốt (lỗi thường gặp):\n" +
               "   - Sai điều kiện biên: if (n > 0) thay vì if (n >= 0)\n" +
               "   - Sai công thức: n % 2 == 1 thay vì n % 2 == 0\n" +
               "   - Quên xét trường hợp đặc biệt: n=0, n=1, mảng rỗng\n" +
               "   - Sai index: bắt đầu từ i=1 thay vì i=0 (bỏ qua phần tử đầu)\n" +
               "   - Sai phép so sánh: > thay vì >=\n" +
               "   - Quên xử lý số âm\n" +
               "3. VÍ DỤ CỤ THỂ:\n" +
               "   - Bài kiểm tra chẵn/lẻ: Viết if (n % 2 == 1) cout << \"CHAN\"; (SAI)\n" +
               "   - Bài tìm max: Bắt đầu từ arr[1] thay vì arr[0] (bỏ qua phần tử đầu)\n" +
               "   - Bài tính tổng: Quên cộng phần tử cuối cùng\n" +
               "   - Bài đếm cặp: Quên xét trường hợp i == j\n\n" +
               "YÊU CẦU BẮT BUỘC:\n" +
               "1. Dùng #include <iostream> và các thư viện cần thiết\n" +
               "2. Phải có hàm: int main()\n" +
               "3. ⚠️ BẮT BUỘC: Thêm 2 dòng tối ưu I/O ngay đầu main():\n" +
               "   ios_base::sync_with_stdio(false);\n" +
               "   cin.tie(NULL);\n" +
               "4. Code phải COMPILE được (không có lỗi syntax)\n" +
               "5. Có comment // WA: giải thích lỗi logic ở đâu\n" +
               "6. Dùng using namespace std;\n" +
               "7. ⚠️ PHẢI CÓ LỖI LOGIC - không được viết code đúng\n\n" +
               "FORMAT RESPONSE (CỰC KỲ QUAN TRỌNG):\n" +
               "- Dòng đầu tiên PHẢI là: #include <iostream> (hoặc #include khác)\n" +
               "- KHÔNG được có bất kỳ text nào trước code\n" +
               "- KHÔNG được có markdown: ```cpp hoặc ```\n" +
               "- KHÔNG được có giải thích bằng văn bản trước hoặc sau code\n" +
               "- KHÔNG được có dòng trống ở đầu\n\n" +
               "VÍ DỤ CODE WA (có lỗi):\n" +
               "#include <iostream>\n" +
               "using namespace std;\n" +
               "int main() {\n" +
               "    int n;\n" +
               "    cin >> n;\n" +
               "    // WA: Sai điều kiện - nên là n % 2 == 0\n" +
               "    if (n % 2 == 1) {\n" +
               "        cout << \"CHAN\" << endl;\n" +
               "    } else {\n" +
               "        cout << \"LE\" << endl;\n" +
               "    }\n" +
               "    return 0;\n" +
               "}\n\n" +
               "BẮT ĐẦU NGAY TỪ DÒNG ĐẦU TIÊN VỚI CODE WA (CÓ LỖI):";
    }
    
    /**
     * Prompt sinh code TLE (Time Limit Exceeded - quá thời gian).
     * 
     * @param problem đối tượng Problem
     * @return prompt để gửi cho AI
     */
    public static String generateTLECode(Problem problem) {
        return "⚠️ NHIỆM VỤ ĐẶC BIỆT: Viết code CHẬM CÓ CHỦ Ý (TLE - Time Limit Exceeded)\n\n" +
               "Bạn là lập trình viên viết code kém hiệu quả cho mục đích kiểm thử.\n\n" +
               "🎯 MỤC ĐÍCH: Code TLE này dùng để KIỂM TRA TEST CASE CÓ ĐỦ LỚN KHÔNG\n" +
               "- Nếu test case đủ lớn → Code TLE phải bị timeout (TLE verdict)\n" +
               "- Nếu test case quá nhỏ → Code TLE có thể pass (AC verdict) - test case cần thêm test lớn\n\n" +
               "ĐỀ BÀI:\n" + problem.getStatement() + "\n\n" +
               "INPUT: " + problem.getInputFormat() + "\n\n" +
               "OUTPUT: " + problem.getOutputFormat() + "\n\n" +
               "CONSTRAINTS: " + problem.getConstraints() + "\n\n" +
               "TIME LIMIT: " + problem.getTimeLimit() + "\n\n" +
               "⚠️ CỰC KỲ QUAN TRỌNG - ĐỌC KỸ:\n" +
               "- Đây KHÔNG phải code AC (Accepted)\n" +
               "- Đây là code TLE (Time Limit Exceeded) - code CHẬM\n" +
               "- Code phải cho KẾT QUẢ ĐÚNG nhưng ĐỘ PHỨC TẠP QUÁ CAO\n" +
               "- Code phải CHẠY QUÁ CHẬM với input lớn (gần max của CONSTRAINTS)\n" +
               "- KHÔNG được viết code tối ưu\n\n" +
               "YÊU CẦU VỀ ĐỘ CHẬM (BẮT BUỘC PHẢI CHẬM):\n" +
               "1. Dùng thuật toán CHẬM, KHÔNG TỐI ƯU:\n" +
               "   - Dùng brute force O(n²) thay vì O(n log n)\n" +
               "   - Dùng O(n³) thay vì O(n²)\n" +
               "   - Dùng đệ quy không tối ưu (không có memoization)\n" +
               "   - Dùng vòng lặp lồng nhau nhiều tầng\n" +
               "2. VÍ DỤ CỤ THỂ:\n" +
               "   - Tìm cặp có tổng = K: Dùng 2 vòng for O(n²) thay vì unordered_map O(n)\n" +
               "   - Tìm max: Dùng 2 vòng for lồng nhau thay vì 1 vòng\n" +
               "   - Sắp xếp: Dùng bubble sort O(n²) thay vì sort() O(n log n)\n" +
               "   - Tìm kiếm: Dùng linear search O(n) thay vì binary search O(log n)\n" +
               "   - Tính tổng: Dùng đệ quy thay vì vòng lặp\n" +
               "3. Code phải ĐÚNG với n nhỏ (n < 100) nhưng CHẬM với n lớn (n > 10000)\n" +
               "4. ⚠️ QUAN TRỌNG: Code phải đủ chậm để BỊ TLE khi n gần MAX của CONSTRAINTS\n\n" +
               "YÊU CẦU BẮT BUỘC:\n" +
               "1. Dùng #include <iostream> và các thư viện cần thiết\n" +
               "2. Phải có hàm: int main()\n" +
               "3. ⚠️ BẮT BUỘC: Thêm 2 dòng tối ưu I/O ngay đầu main():\n" +
               "   ios_base::sync_with_stdio(false);\n" +
               "   cin.tie(NULL);\n" +
               "4. Code phải COMPILE được và cho kết quả ĐÚNG với n nhỏ\n" +
               "5. Có comment // TLE: giải thích tại sao chậm (độ phức tạp)\n" +
               "6. Dùng using namespace std;\n" +
               "7. ⚠️ PHẢI CHẬM - không được viết code tối ưu\n\n" +
               "FORMAT RESPONSE (CỰC KỲ QUAN TRỌNG):\n" +
               "- Dòng đầu tiên PHẢI là: #include <iostream> (hoặc #include khác)\n" +
               "- KHÔNG được có bất kỳ text nào trước code\n" +
               "- KHÔNG được có markdown: ```cpp hoặc ```\n" +
               "- KHÔNG được có giải thích bằng văn bản trước hoặc sau code\n" +
               "- KHÔNG được có dòng trống ở đầu\n\n" +
               "VÍ DỤ CODE TLE (chậm - O(n²)):\n" +
               "#include <iostream>\n" +
               "using namespace std;\n" +
               "int main() {\n" +
               "    ios_base::sync_with_stdio(false);\n" +
               "    cin.tie(NULL);\n" +
               "    \n" +
               "    int n;\n" +
               "    cin >> n;\n" +
               "    int arr[n];\n" +
               "    for (int i = 0; i < n; i++) cin >> arr[i];\n" +
               "    \n" +
               "    // TLE: O(n²) - dùng 2 vòng lặp thay vì 1 vòng\n" +
               "    int maxVal = arr[0];\n" +
               "    for (int i = 0; i < n; i++) {\n" +
               "        for (int j = 0; j < n; j++) {\n" +
               "            if (arr[j] > maxVal) maxVal = arr[j];\n" +
               "        }\n" +
               "    }\n" +
               "    cout << maxVal << endl;\n" +
               "    return 0;\n" +
               "}\n\n" +
               "BẮT ĐẦU NGAY TỪ DÒNG ĐẦU TIÊN VỚI CODE TLE (CHẬM):";
    }
    
    // ===== 4. SINH CHECKER =====
    
    /**
     * Prompt sinh checker (cho bài có nhiều đáp án đúng).
     * 
     * @param problem đối tượng Problem
     * @return prompt để gửi cho AI
     */
    public static String generateChecker(Problem problem) {
        return "Bạn là chuyên gia tạo checker cho bài lập trình thi đấu.\n\n" +
               "ĐỀ BÀI:\n" + problem.getStatement() + "\n\n" +
               "OUTPUT FORMAT: " + problem.getOutputFormat() + "\n\n" +
               "Bài này có thể có NHIỀU đáp án đúng. Viết Java checker để kiểm tra output có hợp lệ không.\n\n" +
               "Format checker:\n" +
               "- Tên class: Checker\n" +
               "- Method: public static boolean check(String input, String output, String answer)\n" +
               "  + input: input của test case\n" +
               "  + output: output của chương trình cần kiểm tra\n" +
               "  + answer: đáp án mẫu (có thể null nếu không có)\n" +
               "  + return true nếu output hợp lệ, false nếu không\n" +
               "- Có main method để test: args[0]=input_file, args[1]=output_file, args[2]=answer_file\n" +
               "  In \"AC\" hoặc \"WA: lý do\"\n\n" +
               "Chỉ trả về CODE JAVA THUẦN (KHÔNG có markdown, KHÔNG có backtick):";
    }
    
    // ===== 5. SINH NHIỀU CODE CÙNG LÚC (BATCH) =====
    
    /**
     * Prompt sinh nhiều loại code cùng lúc (tiết kiệm API calls).
     * 
     * @param problem đối tượng Problem
     * @param includeAC có sinh AC code không
     * @param includeWA có sinh WA code không
     * @param includeTLE có sinh TLE code không
     * @return prompt để gửi cho AI
     */
    public static String generateMultipleCodes(Problem problem, boolean includeAC, boolean includeWA, boolean includeTLE) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là lập trình viên competitive programming giỏi.\n\n");
        prompt.append("ĐỀ BÀI:\n").append(problem.getStatement()).append("\n\n");
        prompt.append("INPUT FORMAT: ").append(problem.getInputFormat()).append("\n\n");
        prompt.append("OUTPUT FORMAT (QUAN TRỌNG - ĐỌC KỸ): ").append(problem.getOutputFormat()).append("\n\n");
        prompt.append("CONSTRAINTS: ").append(problem.getConstraints()).append("\n\n");
        prompt.append("TIME LIMIT: ").append(problem.getTimeLimit()).append("\n\n");
        
        prompt.append("⚠️ LƯU Ý CỰC KỲ QUAN TRỌNG VỀ OUTPUT:\n");
        prompt.append("- Phải in CHÍNH XÁC theo OUTPUT FORMAT ở trên\n");
        prompt.append("- Chú ý CHỮ HOA/THƯỜNG (vd: \"CHAN\" không phải \"Chan\" hay \"chan\")\n");
        prompt.append("- Chú ý DẤU CÁCH và XUỐNG DÒNG\n");
        prompt.append("- KHÔNG được tự ý đổi output sang tiếng Anh (vd: \"Even\", \"Odd\")\n");
        prompt.append("- Đọc kỹ OUTPUT FORMAT và làm ĐÚNG THEO\n\n");
        
        // Thêm yêu cầu về thuật toán tối ưu cho AC code
        if (includeAC) {
            prompt.append("⚠️ YÊU CẦU CHO AC CODE:\n");
            prompt.append("1. THUẬT TOÁN TỐI ƯU:\n");
            prompt.append("   - Phân tích độ phức tạp dựa trên CONSTRAINTS\n");
            prompt.append("   - Nếu n ≤ 100: O(n²) hoặc O(n³) chấp nhận được\n");
            prompt.append("   - Nếu n ≤ 10,000: Cần O(n log n) hoặc O(n²)\n");
            prompt.append("   - Nếu n ≤ 100,000: Cần O(n) hoặc O(n log n)\n");
            prompt.append("   - Nếu n ≤ 1,000,000: Cần O(n) hoặc O(log n)\n");
            prompt.append("   - Dùng cấu trúc dữ liệu phù hợp: unordered_map, map, set, vector\n");
            prompt.append("   - TRÁNH brute force khi n lớn\n");
            prompt.append("   - VÍ DỤ: Tìm cặp có tổng = K → Dùng unordered_map O(n) thay vì 2 vòng for O(n²)\n\n");
            prompt.append("2. XỬ LÝ EDGE CASES (CỰC KỲ QUAN TRỌNG):\n");
            prompt.append("   - BẮT BUỘC kiểm tra n=0 (mảng rỗng)\n");
            prompt.append("   - BẮT BUỘC kiểm tra n=1 (chỉ 1 phần tử)\n");
            prompt.append("   - BẮT BUỘC kiểm tra số âm (nếu constraints cho phép)\n");
            prompt.append("   - BẮT BUỘC kiểm tra giá trị min/max\n");
            prompt.append("   - Ví dụ: if (n == 0) { cout << 0 << endl; return 0; }\n");
            prompt.append("   - Ví dụ: if (n == 1) { cout << arr[0] << endl; return 0; }\n\n");
        }
        
        // Liệt kê các loại code cần sinh
        prompt.append("Hãy sinh các code C++ sau:\n");
        if (includeAC) prompt.append("1. AC (Accepted) - code ĐÚNG, THUẬT TOÁN TỐI ƯU, OUTPUT CHÍNH XÁC\n");
        if (includeWA) prompt.append("2. WA (Wrong Answer) - code có lỗi logic\n");
        if (includeTLE) prompt.append("3. TLE (Time Limit Exceeded) - code chậm\n");
        prompt.append("\n");
        
        prompt.append("YÊU CẦU FORMAT RESPONSE (CỰC KỲ QUAN TRỌNG):\n");
        prompt.append("- Trả về theo format: ===TYPE=== theo sau là code\n");
        prompt.append("- KHÔNG có markdown (```cpp hoặc ```)\n");
        prompt.append("- KHÔNG có text giải thích ngoài code\n");
        prompt.append("- Mỗi code bắt đầu bằng #include\n");
        prompt.append("- ⚠️ BẮT BUỘC: Mỗi code phải có 2 dòng tối ưu I/O ngay đầu main():\n");
        prompt.append("  ios_base::sync_with_stdio(false);\n");
        prompt.append("  cin.tie(NULL);\n\n");
        
        prompt.append("VÍ DỤ FORMAT:\n");
        if (includeAC) {
            prompt.append("===AC===\n");
            prompt.append("#include <iostream>\n");
            prompt.append("using namespace std;\n");
            prompt.append("int main() {\n");
            prompt.append("    ios_base::sync_with_stdio(false);\n");
            prompt.append("    cin.tie(NULL);\n");
            prompt.append("    \n");
            prompt.append("    // Đọc input\n");
            prompt.append("    // Xử lý logic\n");
            prompt.append("    // In output ĐÚNG THEO OUTPUT FORMAT (không tự ý đổi)\n");
            prompt.append("    return 0;\n");
            prompt.append("}\n\n");
        }
        if (includeWA) {
            prompt.append("===WA===\n");
            prompt.append("#include <iostream>\n");
            prompt.append("using namespace std;\n");
            prompt.append("int main() {\n");
            prompt.append("    ios_base::sync_with_stdio(false);\n");
            prompt.append("    cin.tie(NULL);\n");
            prompt.append("    \n");
            prompt.append("    // WA code here (có lỗi logic)\n");
            prompt.append("    return 0;\n");
            prompt.append("}\n\n");
        }
        if (includeTLE) {
            prompt.append("===TLE===\n");
            prompt.append("#include <iostream>\n");
            prompt.append("using namespace std;\n");
            prompt.append("int main() {\n");
            prompt.append("    ios_base::sync_with_stdio(false);\n");
            prompt.append("    cin.tie(NULL);\n");
            prompt.append("    \n");
            prompt.append("    // TLE code here (chậm)\n");
            prompt.append("    return 0;\n");
            prompt.append("}\n\n");
        }
        
        prompt.append("BẮT ĐẦU NGAY VỚI ===AC=== (hoặc ===WA=== hoặc ===TLE===):");
        
        return prompt.toString();
    }
}
