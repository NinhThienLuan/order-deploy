package fsoft.franchise.service;

import fsoft.franchise.dto.payments.OrderRefundResponse;
import fsoft.franchise.dto.payments.RefundRequest;
import fsoft.franchise.dto.payments.RefundResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface RefundService {
    RefundResponse createRefundRequest(RefundRequest requestDTO, UUID customerId);

    List<RefundResponse> getAllPendingRefunds();

    RefundResponse approveRefund(UUID refundId);

    RefundResponse declineRefund(UUID refundId, String declineReason);

    /**
     * Thực hiện hoàn tiền trực tiếp cho đơn hàng (Admin/Store Manager).
     * POST /v1/orders/{order_id}/refund — không cần tạo request, approve ngay.
     *
     * @param orderId    ID đơn hàng
     * @param performedBy ID user thực hiện (ADMIN/MANAGER)
     * @return Thông tin kết quả refund (orderId, status REFUNDED, refundAmount, refundTime)
     */
    OrderRefundResponse processOrderRefund(UUID orderId, UUID performedBy);
}
