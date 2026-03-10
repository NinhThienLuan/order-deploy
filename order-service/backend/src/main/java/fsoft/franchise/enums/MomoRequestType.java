package fsoft.franchise.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MoMo AIO v2 request types (payment channels).
 * Each value maps to the {@code requestType} field sent to the MoMo API.
 */
@Getter
@RequiredArgsConstructor
public enum MomoRequestType {

    CAPTURE_WALLET("captureWallet", "QR / Ví MoMo", "Quét mã QR hoặc mở app MoMo"),
    PAY_WITH_ATM("payWithATM", "Thẻ ATM nội địa", "Vietcombank, BIDV, MB, Techcombank..."),
    PAY_WITH_CC("payWithCC", "Thẻ quốc tế", "Visa, Mastercard, JCB");

    /** Value sent to MoMo API as {@code requestType}. */
    private final String momoCode;

    /** Human-readable display name. */
    private final String displayName;

    /** Short description for FE/docs. */
    private final String description;

    /**
     * Resolve a raw string (e.g. from request body) to a MomoRequestType.
     * Returns {@code null} if no match.
     */
    public static MomoRequestType fromMomoCode(String code) {
        if (code == null)
            return null;
        for (MomoRequestType type : values()) {
            if (type.momoCode.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /** Default type used when none specified. */
    public static MomoRequestType defaultType() {
        return CAPTURE_WALLET;
    }
}
