import { useState, useEffect } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import styles from "./OrderDetailPage.module.css";
import { getOrderById, getOrderStatuses } from "@/features/orders/services/order.service";
import { STEPPER_STEPS, STATUS_LABELS } from "@/features/orders/services/order.constants";
import CancelOrderButton from "@/features/orders/components/CancelOrderButton";
import Navbar from "@/layouts/Navbar";

// ── Formatters ────────────────────────────────────────────
const formatVND = (n) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(
    n ?? 0,
  );

const formatDateTime = (iso) =>
  iso
    ? new Date(iso).toLocaleString("en-US", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      })
    : "—";

const STATUS_LABEL_MAP = {
  PENDING: "Order Placed",
  PAID: "Payment Confirmed",
  PREPARING: "Preparing",
  READY: "Ready for Pickup",
  COMPLETED: "Completed",
  CANCELED: "Cancelled",
};

// ─────────────────────────────────────────────────────────

export default function OrderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState(null);
  const [apiStatuses, setApiStatuses] = useState([]);

  // Payment States
  const [paymentError, setPaymentError] = useState(
    location.state?.paymentError || null,
  );
  const [paymentMethod, setPaymentMethod] = useState(
    location.state?.preselectedPaymentMethod || "VNPAY"
  );
  const [momoRequestType, setMomoRequestType] = useState(
    location.state?.preselectedMomoType || "captureWallet"
  );
  const [showMethods, setShowMethods] = useState(
    !!location.state?.preselectedPaymentMethod
  );

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [data, statusesData] = await Promise.all([
          getOrderById(id),
          getOrderStatuses().catch(() => ["PENDING", "PAID", "PREPARING", "READY", "COMPLETED"])
        ]);
        setOrder(data);
        setApiStatuses(statusesData.filter((s) => s !== "CANCELED"));
        
        // Chỉ set paymentMethod từ backend nếu không có preselected từ POS
        if (!location.state?.preselectedPaymentMethod && data.payment?.paymentMethod) {
          setPaymentMethod(data.payment.paymentMethod);
        }
      } catch {
        setFetchError("Could not load order.");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  const handleCancelled = () =>
    setOrder((prev) => ({ ...prev, status: "CANCELED" }));

  const handlePayNow = () => {
    setPaymentError(null);
    const orderId = order.orderId || order.id;
    const amount = grandTotal || order.pricing?.totalAmount || order.totalAmount || 0;
    sessionStorage.setItem("pendingOrderId", String(orderId));
    sessionStorage.setItem("pendingPaymentMethod", paymentMethod);
    navigate("/payment/loading", {
      state: {
        orderId,
        paymentMethod,
        amount,
        ...(paymentMethod === "MOMO" && { momoRequestType }),
      },
    });
  };

  if (loading)
    return (
      <>
        <Navbar />
        <div className={styles.pageState}>
          <div className={styles.spinner} />
          <span>Loading order details...</span>
        </div>
      </>
    );

  if (fetchError || !order)
    return (
      <>
        <Navbar />
        <div className={styles.pageState}>
          <span className={styles.errorIcon}>
            <svg
              width="24"
              height="24"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
          </span>
          <span>{fetchError || "Order not found."}</span>
        </div>
      </>
    );

  const subtotal = order.pricing?.subtotal || 0;
  const deliveryFee = order.pricing?.deliveryFee || 0;
  const platformFee = order.pricing?.platformFee || 0;
  const discount = order.pricing?.discount || 0;
  const grandTotal = order.pricing?.totalAmount || 0;
  
  // Dynamic step matching based on API statuses
  const currentStepIdx = apiStatuses.indexOf(order.status);
  
  const totalItems = order.items?.reduce((s, i) => s + i.quantity, 0) || 0;

  const getIconForStatus = (status) => {
    switch (status) {
      case "PENDING":
        return <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>;
      case "PAID":
        return <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>;
      case "PROCESSING":
      case "PREPARING":
        return <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>;
      case "READY":
        return <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" /></svg>;
      case "COMPLETED":
        return <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>;
      default:
        return <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor"><circle cx="12" cy="12" r="10" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></svg>;
    }
  };

  return (
    <>
      <Navbar />
      <div className={styles.pageWrap}>
        {/* ── Page Header ── */}
        <header className={styles.pageHeader}>
          <div>
            <h1 className={styles.pageTitle}>
              Order #{order.orderNumber || order.orderId || order.id || "N/A"}
            </h1>
            <p className={styles.pageSubtitle}>
              Placed on {formatDateTime(order.orderTime || order.createdAt)}
            </p>
          </div>
          {/* Optional Action Button (e.g. Download Invoice) can go here */}
        </header>

        <div className={styles.dashboardGrid}>
          {/* ── Left Column: Timeline, Items, Summary ── */}
          <div className={styles.mainCol}>
            {/* Stepper Card */}
            <div className={styles.card}>
              <h2 className={styles.sectionTitle}>Order Progress</h2>
              <div className={styles.stepperContainer}>
                {apiStatuses.map((statusKey, idx) => {
                  const isDone = currentStepIdx > -1 && idx < currentStepIdx;
                  const isActive = currentStepIdx > -1 && idx === currentStepIdx;

                  return (
                    <div
                      key={statusKey}
                      className={`${styles.step} ${isDone ? styles.stepDone : ""} ${isActive ? styles.stepActive : ""}`}
                    >
                      <div className={styles.stepIndicator}>
                        {isDone ? (
                          <svg
                            width="18"
                            height="18"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="3"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          >
                            <polyline points="20 6 9 17 4 12"></polyline>
                          </svg>
                        ) : isActive ? (
                          <div className={styles.activeDot}></div>
                        ) : (
                          getIconForStatus(statusKey)
                        )}
                      </div>
                      <div className={styles.stepContent}>
                        <span className={styles.stepLabel}>{STATUS_LABELS[statusKey] || statusKey}</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Order Items Card */}
            <div className={styles.card}>
              <h2 className={styles.sectionTitle}>Items ({totalItems})</h2>
              <div className={styles.itemList}>
                {order.items?.map((item, index) => (
                  <div key={item.productId || index} className={styles.itemRow}>
                    <div className={styles.itemThumbWrap}>
                      <img
                        src={
                          item.imageUrl ||
                          item.image ||
                          "https://placehold.co/120x120/EFECE3/231F1E?text=No+Image"
                        }
                        alt={item.productName || item.name}
                        className={styles.itemThumb}
                      />
                    </div>
                    <div className={styles.itemDetails}>
                      <span className={styles.itemCategory}>
                        {item.category || "Beverage"}
                      </span>
                      <span className={styles.itemName}>
                        {item.productName || item.name}
                      </span>
                      {item.note && (
                        <span className={styles.itemNote}>{item.note}</span>
                      )}
                    </div>
                    <div className={styles.itemPricing}>
                      <span className={styles.itemMath}>
                        {item.quantity} ×{" "}
                        {formatVND(item.unitPrice || item.price)}
                      </span>
                      <span className={styles.itemTotal}>
                        {formatVND(
                          item.subtotal ||
                            (item.unitPrice || item.price) * item.quantity,
                        )}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Order Summary Card */}
            <div className={styles.card}>
              <h2 className={styles.sectionTitle}>Financial Summary</h2>
              <div className={styles.summaryList}>
                <div className={styles.summaryRow}>
                  <span className={styles.summaryLabel}>Subtotal</span>
                  <span className={styles.summaryVal}>
                    {formatVND(subtotal)}
                  </span>
                </div>
                <div className={styles.summaryRow}>
                  <span className={styles.summaryLabel}>Delivery Fee</span>
                  <span className={styles.summaryVal}>
                    {deliveryFee === 0 ? "Free" : formatVND(deliveryFee)}
                  </span>
                </div>
                {platformFee > 0 && (
                  <div className={styles.summaryRow}>
                    <span className={styles.summaryLabel}>Platform Fee</span>
                    <span className={styles.summaryVal}>
                      {formatVND(platformFee)}
                    </span>
                  </div>
                )}
                {discount > 0 && (
                  <div
                    className={`${styles.summaryRow} ${styles.summaryDiscount}`}
                  >
                    <span className={styles.summaryLabel}>Discount</span>
                    <span className={styles.summaryVal}>
                      -{formatVND(discount)}
                    </span>
                  </div>
                )}
                <div className={styles.summaryGrandTotal}>
                  <span>Total Amount</span>
                  <span className={styles.grandTotalVal}>
                    {formatVND(grandTotal)}
                  </span>
                </div>
              </div>
            </div>
            {/* Delivery Cards */}
            <div className={styles.card}>
              <h2 className={styles.sectionTitle}>Delivery Details</h2>

              <div className={styles.addressBlock}>
                <span className={styles.addressLabel}>Customer (Buyer)</span>
                <span className={styles.addressName}>
                  {order.customer?.customerName || "Customer"}
                </span>
                <span className={styles.addressText}>
                  {order.customer?.deliveryAddress || "Pick-up at store"}
                </span>
              </div>

              <div className={styles.addressDivider}></div>

              <div className={styles.addressBlock}>
                <span className={styles.addressLabel}>Origin (Seller)</span>
                <span className={styles.addressName}>
                  E-Coffee Flagship Store
                </span>
                <span className={styles.addressText}>
                  Floor 1, 123 Le Loi{"\n"}District 1, HCMC{"\n"}Vietnam
                </span>
              </div>
            </div>
          </div>

          {/* ── Right Column: Context, Contacts, Actions ── */}
          <div className={styles.sideCol}>
            {/* Status Card */}
            <div
              className={`${styles.card} ${styles.statusCard} ${styles[`status_${order.status}`] || ""}`}
            >
              <div className={styles.statusHeader}>
                <div className={styles.statusDot}></div>
                <span>{STATUS_LABEL_MAP[order.status] ?? order.status}</span>
              </div>
            </div>

            {/* Payment Module */}
            <div className={styles.card}>
              <h2 className={styles.sectionTitle}>Payment</h2>

              {/* Paid Status Details */}
              {order.status === "PAID" && order.payment && (
                <div className={styles.paymentDetails}>
                  <div className={styles.payMetaRow}>
                    <span className={styles.payMetaLabel}>Method</span>
                    <span className={styles.payMetaVal}>
                      {order.payment.paymentMethod}
                    </span>
                  </div>
                  <div className={styles.payMetaRow}>
                    <span className={styles.payMetaLabel}>Status</span>
                    <span
                      className={`${styles.payMetaVal} ${styles.paySuccess}`}
                    >
                      {order.payment.paymentStatus}
                    </span>
                  </div>
                  {order.payment.paymentDate && (
                    <div className={styles.payMetaRow}>
                      <span className={styles.payMetaLabel}>Date</span>
                      <span className={styles.payMetaVal}>
                        {formatDateTime(order.payment.paymentDate)}
                      </span>
                    </div>
                  )}
                </div>
              )}

              {/* Unpaid / Pending / Processing Flow */}
              {(order.status === "PENDING" || order.status === "PROCESSING") && (
                <div className={styles.paymentFlow}>
                  {paymentError && (
                    <div className={styles.paymentErrorAlert}>
                      <svg
                        className={styles.errorAlertIcon}
                        width="20"
                        height="20"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                        />
                      </svg>
                      <span>{paymentError}</span>
                    </div>
                  )}

                  {order.status === "PROCESSING" && order.payment?.paymentUrl && !showMethods ? (
                    <div className={styles.processingState}>
                      <p className={styles.paymentInstruction}>
                        You have an active payment session. Please click below to securely return to the payment gateway.
                      </p>
                      <button
                        onClick={handlePayNow}
                        className={styles.payActionBtn}
                      >
                        RETURN TO PAYMENT GATEWAY
                      </button>
                      <button 
                        onClick={() => setShowMethods(true)}
                        className={styles.secondaryActionBtn}
                      >
                        Refresh or Change Payment Method
                      </button>
                    </div>
                  ) : (
                    <>
                      <p className={styles.paymentInstruction}>
                        Select your preferred payment method to finalize your order.
                      </p>
                      <div className={styles.methodSelector}>
                        {["VNPAY", "MOMO"].map((m) => (
                          <button
                            key={m}
                            onClick={() => setPaymentMethod(m)}
                            className={`${styles.methodBtn} ${paymentMethod === m ? styles.methodBtnActive : ""}`}
                          >
                            {m}
                          </button>
                        ))}
                      </div>

                      {paymentMethod === "MOMO" && (
                        <div className={styles.momoSubSelector}>
                          {[
                            { code: "captureWallet", label: "QR/Wallet" },
                            { code: "payWithATM", label: "ATM" },
                            { code: "payWithCC", label: "Credit" },
                          ].map((sub) => (
                            <button
                              key={sub.code}
                              onClick={() => setMomoRequestType(sub.code)}
                              className={`${styles.momoSubBtn} ${momoRequestType === sub.code ? styles.momoSubBtnActive : ""}`}
                            >
                              {sub.label}
                            </button>
                          ))}
                        </div>
                      )}

                      <button
                        onClick={handlePayNow}
                        className={styles.payActionBtn}
                      >
                        PAY NOW ({formatVND(grandTotal)})
                      </button>
                      
                      {order.status === "PROCESSING" && (
                        <button 
                          onClick={() => setShowMethods(false)}
                          className={styles.secondaryActionBtn}
                        >
                          Cancel and show pending link
                        </button>
                      )}
                    </>
                  )}
                </div>
              )}
            </div>

            {/* Order Cancellation */}
            {order.status !== "CANCELED" && order.status !== "COMPLETED" && (
              <div className={`${styles.card} ${styles.dangerCard}`}>
                <CancelOrderButton
                  orderId={order.orderId || order.id}
                  currentStatus={order.status}
                  onCancelled={handleCancelled}
                />
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
