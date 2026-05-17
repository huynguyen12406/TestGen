package vn.testgen.util;

/**
 * Centralized constants for the TestGen application.
 * Contains all UI text, validation limits, and configuration values.
 */
public class Constants {
    
    // ── Application Info ───────────────────────────────────────────────────
    public static final String APP_NAME = "TestGen";
    public static final String APP_VERSION = "v1.0";
    public static final String APP_TITLE = "TestGen — Hệ thống sinh test tự động cho kỳ thi lập trình";
    
    // ── Validation Limits ──────────────────────────────────────────────────
    public static final int MIN_TIME_LIMIT_MS = 1;
    public static final int MAX_TIME_LIMIT_MS = 60000;
    public static final int DEFAULT_TIME_LIMIT_MS = 1000;
    
    public static final int MIN_MEMORY_LIMIT_MB = 1;
    public static final int MAX_MEMORY_LIMIT_MB = 2048;
    public static final int DEFAULT_MEMORY_LIMIT_MB = 256;
    
    public static final int MIN_TEST_COUNT = 1;
    public static final int MAX_TEST_COUNT = 1000;
    public static final int DEFAULT_TEST_COUNT = 10;
    
    public static final int MAX_TITLE_LENGTH = 200;
    
    // ── Tab Names ──────────────────────────────────────────────────────────
    public static final String TAB_INPUT = "1  NHẬP ĐỀ";
    public static final String TAB_TESTGEN = "2  SINH TEST";
    public static final String TAB_EVAL = "3  ĐÁNH GIÁ";
    public static final String TAB_SETTINGS = "⚙  CÀI ĐẶT";
    
    // ── Panel Titles ───────────────────────────────────────────────────────
    public static final String TITLE_INPUT_PANEL = "⌨  NHẬP ĐỀ BÀI";
    public static final String TITLE_TESTGEN_PANEL = "⚡  SINH TEST CASES";
    public static final String TITLE_EVAL_PANEL = "📊  ĐÁNH GIÁ KẾT QUẢ";
    public static final String TITLE_SETTINGS_PANEL = "⚙  CÀI ĐẶT";
    
    // ── Card Titles ────────────────────────────────────────────────────────
    public static final String CARD_PROBLEM_INFO = "Thông tin đề";
    public static final String CARD_PROBLEM_STATEMENT = "Nội dung đề bài";
    public static final String CARD_TEST_CONFIG = "Cấu hình sinh test";
    public static final String CARD_TEST_LOG = "Log sinh test";
    public static final String CARD_TEST_LIST = "Danh sách test cases";
    public static final String CARD_CODE_SAMPLES = "Code mẫu (AI sinh / tự nhập)";
    public static final String CARD_RUN_RESULTS = "Kết quả chạy thử";
    public static final String CARD_RESULT_DISTRIBUTION = "Phân bố kết quả";
    public static final String CARD_EVAL_DETAIL = "Chi tiết đánh giá";
    
    // ── Labels ─────────────────────────────────────────────────────────────
    public static final String LBL_PROBLEM_TITLE = "Tên bài:";
    public static final String LBL_CONTEST_TYPE = "Loại kỳ thi:";
    public static final String LBL_LANGUAGE = "Ngôn ngữ:";
    public static final String LBL_TIME_LIMIT = "Time limit (ms):";
    public static final String LBL_MEMORY_LIMIT = "Memory (MB):";
    public static final String LBL_UPLOAD_IMAGE = "Tải ảnh đề bài:";
    public static final String LBL_SAMPLE_TESTS = "Sample tests:";
    public static final String LBL_TEST_COUNT = "Số lượng test:";
    public static final String LBL_TEST_TYPES = "Loại test:";
    public static final String LBL_CHECKER = "Checker:";
    public static final String LBL_GENERATE_CODE = "Sinh code mẫu:";
    
    // ── Buttons ────────────────────────────────────────────────────────────
    public static final String BTN_PARSE = "🔍  Phân tích đề";
    public static final String BTN_CLEAR = "🗑  Xóa";
    public static final String BTN_CHOOSE_FILE = "📁  Chọn file";
    public static final String BTN_ADD_SAMPLE = "+ Thêm sample";
    public static final String BTN_GENERATE = "▶  Sinh test";
    public static final String BTN_STOP = "⏹  Dừng";
    public static final String BTN_CLEAR_LOG = "🗑  Xóa log";
    public static final String BTN_RUN_EVAL = "▶  Chạy đánh giá";
    public static final String BTN_EXPORT_REPORT = "💾  Xuất báo cáo";
    public static final String BTN_SAVE_SETTINGS = "💾  Lưu cài đặt";
    public static final String BTN_CLOSE = "Đóng";
    public static final String BTN_CANCEL = "Hủy";
    public static final String BTN_ADD = "Thêm";
    public static final String BTN_REGEN_AC = "↺ AC";
    public static final String BTN_REGEN_WA = "↺ WA";
    public static final String BTN_REGEN_TLE = "↺ TLE";
    
    // ── Placeholders ───────────────────────────────────────────────────────
    public static final String PH_TITLE = "VD: A. Two Sum";
    public static final String PH_TIME_LIMIT = "1000";
    public static final String PH_MEMORY_LIMIT = "256";
    public static final String PH_STATEMENT = "Dán đề bài vào đây...\n\nVí dụ:\n" +
        "Given n integers, find the maximum sum subarray.\n" +
        "Input: First line n (1 ≤ n ≤ 10^5), second line n integers.\n" +
        "Output: Print the maximum sum.";
    public static final String PH_DRAG_DROP = "<html><center>Kéo thả ảnh<br>hoặc PDF vào đây</center></html>";
    public static final String PH_INPUT = "Input...";
    public static final String PH_OUTPUT = "Expected output...";
    
    // ── Hints ──────────────────────────────────────────────────────────────
    public static final String HINT_STATEMENT = "  💡 Dán toàn bộ đề bài vào đây (text, LaTeX, hoặc dùng ảnh bên trái)";
    public static final String HINT_DOUBLE_CLICK = "Nhấp đúp vào test để xem chi tiết";
    public static final String HINT_WORKFLOW = "Nhập đề → Sinh test → Đánh giá  ";
    
    // ── Status Messages ────────────────────────────────────────────────────
    public static final String STATUS_READY = "Sẵn sàng nhận đề bài.";
    public static final String STATUS_PARSING = "⟳ Đang phân tích...";
    public static final String STATUS_CLEARED = "Form đã được xóa.";
    public static final String STATUS_NO_PROBLEM = "Chưa có đề bài. Hãy nhập đề ở Tab 1 trước.";
    public static final String STATUS_WAITING = "Chờ...";
    public static final String STATUS_READY_EVAL = "Sẵn sàng";
    public static final String STATUS_RUNNING = "Đang chạy...";
    public static final String STATUS_COMPLETED = "Hoàn thành";
    public static final String STATUS_ERROR = "Lỗi";
    public static final String STATUS_STOPPED = "Đã dừng";
    
    // ── Success Messages ───────────────────────────────────────────────────
    public static final String MSG_PARSE_SUCCESS = "✓ Phân tích thành công: %s";
    public static final String MSG_FILE_UPLOADED = "✓ Đã tải: %s";
    public static final String MSG_FILE_DROPPED = "✓ Đã kéo thả: %s";
    public static final String MSG_SAMPLE_ADDED = "✓ Sample test đã thêm";
    public static final String MSG_TESTS_GENERATED = "✓ Đã sinh %d tests. Chuyển sang Tab 3 để đánh giá.";
    public static final String MSG_PROBLEM_LOADED = "✓ Đề bài \"%s\" đã được phân tích. Hãy sinh test!";
    public static final String MSG_REPORT_EXPORTED = "✓ Đã xuất báo cáo: %s";
    public static final String MSG_SOLUTION_REGEN = "[AI] ✓ Đã tái tạo %s solution";
    
    // ── Error Messages ─────────────────────────────────────────────────────
    public static final String ERR_EMPTY_STATEMENT = "Vui lòng nhập nội dung đề bài hoặc tải ảnh lên!";
    public static final String ERR_NO_PROBLEM = "Chưa có đề bài! Hãy nhập đề ở Tab 1 trước.";
    public static final String ERR_NO_TESTS = "Chưa có test cases! Hãy sinh test ở Tab 2 trước.";
    public static final String ERR_PARSE_FAILED = "✗ Lỗi: %s";
    public static final String ERR_INVALID_TIME_LIMIT = "✗ Time limit phải từ %dms đến %dms";
    public static final String ERR_INVALID_MEMORY_LIMIT = "✗ Memory limit phải từ %dMB đến %dMB";
    public static final String ERR_INVALID_TEST_COUNT = "✗ Số test phải từ %d đến %d";
    public static final String ERR_FILE_READ = "✗ Lỗi: Không thể đọc file. Vui lòng chọn file ảnh hoặc PDF hợp lệ";
    public static final String ERR_EXPORT_FAILED = "Lỗi khi xuất: %s";
    
    // ── Log Prefixes ───────────────────────────────────────────────────────
    public static final String LOG_INFO = "[INFO]";
    public static final String LOG_ERROR = "[ERR]";
    public static final String LOG_AI = "[AI]";
    public static final String LOG_GEN = "[GEN]";
    public static final String LOG_CHK = "[CHK]";
    public static final String LOG_SOL = "[SOL]";
    public static final String LOG_RUN = "[RUN]";
    public static final String LOG_START = "[START]";
    public static final String LOG_DONE = "[DONE]";
    public static final String LOG_STOP = "[STOP]";
    public static final String LOG_CFG = "[CFG]";
    public static final String LOG_STAT = "[STAT]";
    
    // ── Dialog Titles ──────────────────────────────────────────────────────
    public static final String DLG_ADD_SAMPLE = "Thêm Sample Test";
    public static final String DLG_TEST_DETAIL = "Test #%d [%s]";
    public static final String DLG_ERROR = "Lỗi";
    public static final String DLG_WARNING = "Cảnh báo";
    public static final String DLG_SUCCESS = "Thành công";
    
    // ── Contest Types ──────────────────────────────────────────────────────
    public static final String[] CONTEST_TYPES = {"ICPC", "IOI", "Codeforces", "Custom"};
    
    // ── Languages ──────────────────────────────────────────────────────────
    public static final String[] LANGUAGES = {"C++", "Java", "Python"};
    
    // ── Checker Types ──────────────────────────────────────────────────────
    public static final String[] CHECKER_TYPES = {
        "Token-based (mặc định)", 
        "Custom checker (AI)", 
        "Special judge"
    };
    
    // ── Test Types ─────────────────────────────────────────────────────────
    public static final String TEST_TYPE_SMALL = "Small (n nhỏ)";
    public static final String TEST_TYPE_MEDIUM = "Medium";
    public static final String TEST_TYPE_LARGE = "Large (n lớn)";
    public static final String TEST_TYPE_EDGE = "Edge cases";
    public static final String TEST_TYPE_STRESS = "Stress test";
    
    // ── Solution Types ─────────────────────────────────────────────────────
    public static final String SOL_TYPE_AC = "Sinh AC solution";
    public static final String SOL_TYPE_WA = "Sinh WA solution";
    public static final String SOL_TYPE_TLE = "Sinh TLE solution";
    public static final String SOL_GEN_CHECKER = "AI sinh checker";
    
    // ── Table Columns ──────────────────────────────────────────────────────
    public static final String[] COL_TEST_LIST = {"#", "Loại", "Kích thước", "Trạng thái", "Input (preview)"};
    public static final String[] COL_EVAL_RESULTS = {"#", "Loại test", "Verdict", "Runtime (ms)", "Memory (KB)", "Chi tiết"};
    
    // ── Verdict Labels ─────────────────────────────────────────────────────
    public static final String VERDICT_AC = "AC";
    public static final String VERDICT_WA = "WA";
    public static final String VERDICT_TLE = "TLE";
    public static final String VERDICT_MLE = "MLE";
    public static final String VERDICT_RE = "RE";
    public static final String VERDICT_CE = "CE";
    
    // ── Status Bar ─────────────────────────────────────────────────────────
    public static final String STATUS_BAR_LEFT = "TestGen  |  Java %s";
    public static final String STATUS_BAR_RIGHT = "Nhóm: [Tên nhóm]  |  Deadline: 15/05/2026  ";
    
    // ── Helper Methods ─────────────────────────────────────────────────────
    
    /**
     * Format success message with parameters
     */
    public static String success(String format, Object... args) {
        return String.format(format, args);
    }
    
    /**
     * Format error message with parameters
     */
    public static String error(String format, Object... args) {
        return String.format(format, args);
    }
    
    /**
     * Format log message with prefix
     */
    public static String log(String prefix, String message) {
        return prefix + " " + message;
    }
    
    /**
     * Get time limit validation error message
     */
    public static String getTimeLimitError() {
        return String.format(ERR_INVALID_TIME_LIMIT, MIN_TIME_LIMIT_MS, MAX_TIME_LIMIT_MS);
    }
    
    /**
     * Get memory limit validation error message
     */
    public static String getMemoryLimitError() {
        return String.format(ERR_INVALID_MEMORY_LIMIT, MIN_MEMORY_LIMIT_MB, MAX_MEMORY_LIMIT_MB);
    }
    
    /**
     * Get test count validation error message
     */
    public static String getTestCountError() {
        return String.format(ERR_INVALID_TEST_COUNT, MIN_TEST_COUNT, MAX_TEST_COUNT);
    }
}
