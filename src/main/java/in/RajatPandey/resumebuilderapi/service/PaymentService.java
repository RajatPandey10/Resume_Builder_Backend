package in.RajatPandey.resumebuilderapi.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import in.RajatPandey.resumebuilderapi.Documents.Payment;
import in.RajatPandey.resumebuilderapi.Documents.User;
import in.RajatPandey.resumebuilderapi.dto.AuthResponse;
import in.RajatPandey.resumebuilderapi.repository.PaymentRepository;
import in.RajatPandey.resumebuilderapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static in.RajatPandey.resumebuilderapi.utils.AppConstants.PREMIUM;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public Payment createOrder(Object principal, String planType) throws RazorpayException {

        log.info("Creating Razorpay order for plan: {}", planType);
        AuthResponse authResponse = authService.getProfile(principal);
        log.debug("Fetched user profile for order creation: userId={}", authResponse.getId());


        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId,razorpayKeySecret);
        int amount = 9900; // amount in paise

        String currency = "INR";
        String receipt = PREMIUM+"_"+ UUID.randomUUID().toString().substring(0,8);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount",amount);
        orderRequest.put("currency",currency);
        orderRequest.put("receipt",receipt);

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);


        Payment newpayment = Payment.builder()
                .userId(authResponse.getId())
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(amount)
                .currency(currency)
                .planType(planType)
                .status("created")
                .receipt(receipt)
                .build();

        log.debug("Saving payment record to database for userId={}", authResponse.getId());
        return paymentRepository.save(newpayment);
    }

    public boolean verifyPayment(String razorpayOrderId,
                                 String razorpayPaymentId,
                                 String razorpaySignature) throws RazorpayException {
        log.info("Verifying payment for orderId={}, paymentId={}", razorpayOrderId, razorpayPaymentId);

        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id",razorpayOrderId);
            attributes.put("razorpay_payment_id",razorpayPaymentId);
            attributes.put("razorpay_signature",razorpaySignature);

            boolean isValidSignature= Utils.verifyPaymentSignature(attributes,razorpayKeySecret);

            if(isValidSignature){
                Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                        .orElseThrow(()-> new RuntimeException("Payment not found"));
                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setRazorpaySignature(razorpaySignature);
                payment.setStatus("paid");

                paymentRepository.save(payment);

                upgradeUserSubscription(payment.getUserId(),payment.getPlanType());
                return true;
            }
            return false;
        }catch (Exception e){
            log.error("Error verifying the payment: ",e);
            return false;
        }
    }

    private void upgradeUserSubscription(String userId, String planType) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));
        existingUser.setSubscriptionPlan(planType);
        userRepository.save(existingUser);
        log.info("User {} upgraded to {} plan",userId,planType);

    }

    public List<Payment> getUserPayments(Object principal) {
        AuthResponse response = authService.getProfile(principal);
        log.info("Fetching payments for user {}", response.getId());
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(response.getId());
    }

    public Payment getPaymentDetails(String orderId) {
        log.info("Fetching payment details for Razorpay order {}", orderId);

        return paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(()-> new RuntimeException("Payment not found"));

    }
}
