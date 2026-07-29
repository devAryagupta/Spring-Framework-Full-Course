package in.coderarmy;

import in.coderarmy.payment.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

//@Component
// When Main does: context.getBean(OrderService.class)
// OrderService.class is the Class OBJECT that DESCRIBES this class.
// Example of what that Class object knows about OrderService:
//   - name         → "in.coderarmy.OrderService"
//   - fields       → paymentService
//   - constructors → OrderService(PaymentService paymentService)
//   - methods      → setPaymentService(...), placeOrder()
//   - annotations  → @Component (if uncommented above)
// Spring uses that type metadata to locate the matching bean in the container.
public class OrderService {

    private PaymentService paymentService;

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void placeOrder() {

        paymentService.pay();

        System.out.println("Order placed");
    }
}
