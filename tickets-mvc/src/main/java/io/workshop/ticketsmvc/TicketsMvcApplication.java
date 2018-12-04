package io.workshop.ticketsmvc;

import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.strategy.FixedSegmentNamingStrategy;
import com.ryanair.aws.xray.mvc.XRayTracingRestTemplateInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import javax.servlet.Filter;

@EnableHystrix
@SpringBootApplication
public class TicketsMvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketsMvcApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new XRayTracingRestTemplateInterceptor());
        return restTemplate;
    }

    @Bean
    public Filter tracingFilter() {
        return new AWSXRayServletFilter(new FixedSegmentNamingStrategy("Tickets MVC"));
    }
}
