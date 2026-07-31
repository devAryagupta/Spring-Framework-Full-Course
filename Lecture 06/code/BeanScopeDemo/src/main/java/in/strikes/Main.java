package in.strikes;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        // THEORY (§1): AppConfig is registered HERE (not via @ComponentScan).
        // See: Lecture 06/How-AppConfig-Gets-Registered.md
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        OrderService order = context.getBean(OrderService.class);
        OrderService order2 = context.getBean(OrderService.class);

        //OrderService order3 = new OrderService();

        System.out.println(order == order2);
    }
}
