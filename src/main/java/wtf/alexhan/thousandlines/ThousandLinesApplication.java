package wtf.alexhan.thousandlines;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class ThousandLinesApplication {
	public static void main(String[] args) {
		SpringApplication.run(ThousandLinesApplication.class, args);
	}
}