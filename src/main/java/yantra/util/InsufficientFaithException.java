package yantra.util;

/** ข้อยกเว้นที่สร้างเอง: เกิดเมื่อแรงศรัทธาไม่พอจ่าย */
public class InsufficientFaithException extends Exception {
    private static final long serialVersionUID = 1L;

    private final int required;
    private final int available;

    public InsufficientFaithException(int required, int available) {
        super(String.format("ต้องใช้ %d ศรัทธา แต่มีเพียง %d", required, available));
        this.required = required;
        this.available = available;
    }

    public int getRequired()  { return required; }
    public int getAvailable() { return available; }
}
