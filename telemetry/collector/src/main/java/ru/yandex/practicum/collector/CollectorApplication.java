package ru.yandex.practicum.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CollectorApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(CollectorApplication.class, args);
	}
}