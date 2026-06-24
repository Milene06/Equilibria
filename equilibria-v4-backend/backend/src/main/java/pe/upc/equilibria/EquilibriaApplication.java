package pe.upc.equilibria;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication @EnableScheduling
public class EquilibriaApplication {
    public static void main(String[] args) { SpringApplication.run(EquilibriaApplication.class, args); }
}
