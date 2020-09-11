package com.sue.generate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 服务启动程序
 *
 * @author : sue
 */
@EnableSwagger2
@MapperScan(basePackages = {"com.sue.generate.mapper"})
@SpringBootApplication
public class Application {

    /**
     * 程序入口
     *
     * @param args 程序输入参数
     */
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).web(WebApplicationType.SERVLET).run(args);
    }
}
