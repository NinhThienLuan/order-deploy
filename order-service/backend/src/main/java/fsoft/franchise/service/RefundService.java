package fsoft.franchise.service;

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
}
