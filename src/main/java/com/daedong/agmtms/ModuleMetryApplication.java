
package com.daedong.agmtms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.daedong.agmtms.metry.dao")
public class ModuleMetryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModuleMetryApplication.class, args);
    }
}
