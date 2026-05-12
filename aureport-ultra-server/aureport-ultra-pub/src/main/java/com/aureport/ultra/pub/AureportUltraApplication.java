package com.aureport.ultra.pub;

import com.aureport.ultra.web.config.AureportUltraMainConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * @author luck
 */
@SpringBootApplication(scanBasePackages = {"com.aureport.ultra"})
@Import(AureportUltraMainConfig.class)
public class AureportUltraApplication {

    public static void main(String[] args) {
        SpringApplication.run(AureportUltraApplication.class, args);
        System.out.println("Aureport Ultra 后台启动成功！");
    }

}
