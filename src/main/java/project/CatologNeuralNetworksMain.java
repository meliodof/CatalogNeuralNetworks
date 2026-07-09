package project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "project")
@EnableJpaRepositories(basePackages = "project.Repository")
@EntityScan(basePackages = "project.Entity")
public class CatologNeuralNetworksMain {
    public static void main(String[] args) {
        SpringApplication.run(CatologNeuralNetworksMain.class, args);
    }
}