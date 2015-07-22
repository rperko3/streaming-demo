package gov.pnnl.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan("gov.pnnl.streaming")
@EnableAutoConfiguration
@EnableAsync

@Configuration
// set -Dapp.home=/opt/test/foo/ on startup
// example -jar -Dapp.home="/opt/test/" test-services-0.1.0.jar
@PropertySource("file:${app.home}/streaming-simulator.properties")
//@PropertySource("classpath:/test-services.properties")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
