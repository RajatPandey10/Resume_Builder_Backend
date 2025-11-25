package in.RajatPandey.resumebuilderapi.controller;

import com.razorpay.RazorpayException;
import in.RajatPandey.resumebuilderapi.Documents.Payment;
import in.RajatPandey.resumebuilderapi.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static in.RajatPandey.resumebuilderapi.utils.AppConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping(PAYMENT_CONTROLLER)
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(CREATE_ORDER)
    public ResponseEntity<?> createOrder(@RequestBody Map<String,String> request,
                                         Authentication authentication) throws RazorpayException {

        log.info("Received create order request for user: {}", authentication.getName());

        String planType = request.get("planType");
        if(!PREMIUM.equalsIgnoreCase(planType)){
            log.warn("Invalid plan type received: {}", planType);
            return ResponseEntity.badRequest().body(Map.of("message","Invalid plan type"));
        }

        Payment payment = paymentService.createOrder(authentication.getPrincipal(),planType);

        log.info("Order created successfully with razorpayOrderId: {}", payment.getRazorpayOrderId());


        Map<String, Object> response = Map.of(
                "orderId",payment.getRazorpayOrderId(),
                "amount",payment.getAmount(),
                "currency",payment.getCurrency(),
                "receipt",payment.getReceipt()
        );

        return ResponseEntity.ok(response);

    }

    @PostMapping(VERIFY)
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String,String> request) throws RazorpayException {

        log.info("verify payment: {}",request);

        String razorpayOrderId =  request.get("razorpay_order_id");
        String razorpayPaymentId =  request.get("razorpay_payment_id");
        String razorpaySignature =  request.get("razorpay_signature");

        if(Objects.isNull(razorpayOrderId) ||
                Objects.isNull(razorpayPaymentId) ||
                Objects.isNull(razorpaySignature)){
            log.error("Missing verification parameter. Request: {}", request);
            return ResponseEntity.badRequest().body(Map.of("message","Missing Required parameter."));
        }
        boolean isValid = paymentService.verifyPayment(razorpayOrderId,razorpayPaymentId,razorpaySignature);

        if(isValid){
            log.info("Payment verification successful for orderId: {}", razorpayOrderId);
            return ResponseEntity.ok(Map.of(
                    "message","Payment verified successfully",
                        "status","success"
            ));
        }else {
            log.warn("Payment verification failed for orderId: {}", razorpayOrderId);
            return ResponseEntity.badRequest().body(Map.of("message","Payment verification failed"));
        }

    }

    @GetMapping(HISTORY)
    public ResponseEntity<?> getPaymentHistory(Authentication authentication){
        log.info("Fetching payment history for user: {}", authentication.getName());
        List<Payment> paymentList= paymentService.getUserPayments(authentication.getPrincipal());
        log.info("Payment history fetched. Total entries: {}", paymentList.size());
        return ResponseEntity.ok(paymentList);
    }

    @GetMapping(ORDER_ORDER_ID)
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId){
        log.info("Fetching details for orderId: {}", orderId);

        Payment paymentDetails = paymentService.getPaymentDetails(orderId);
        if (paymentDetails == null) {
            log.warn("No payment found for orderId: {}", orderId);
        } else {
            log.info("Payment details found for orderId: {}", orderId);
        }
        return ResponseEntity.ok(paymentDetails);
    }
}
