package br.com.pedrodavi.financesapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
//public class FinancesapiApplication implements WebMvcConfigurer {
public class FinancesapiApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(FinancesapiApplication.class, args);
	}

}
