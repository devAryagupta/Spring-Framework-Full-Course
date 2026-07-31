package in.strikes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * THEORY (§1): Lecture 06/How-AppConfig-Gets-Registered.md
 *
 * @Configuration includes @Component (marker), but this class is NOT discovered by scan.
 * Main registers it explicitly:
 *   new AnnotationConfigApplicationContext(AppConfig.class)
 *
 * Then Spring reads @ComponentScan HERE and finds OTHER components in the package.
 */
@Configuration
@ComponentScan
public class AppConfig {

    @Bean
    public OrderService getOrder() {
        return new OrderService();
    }

    @Bean
    public OrderService getOrder2() {
        return new OrderService();
    }
}
