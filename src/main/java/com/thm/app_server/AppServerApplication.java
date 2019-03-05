package com.thm.app_server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EntityScan(basePackageClasses = {
        AppServerApplication.class,
        Jsr310JpaConverters.class
})
public class AppServerApplication implements CommandLineRunner {
//
//    @Autowired
//    UserRepository userRepository;
//
//    @Autowired
//    ParkingLotRepository parkingLotRepository;

    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(AppServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        User u = userRepository.findById(3L).orElse(null);
//        ParkingLot p = parkingLotRepository.findById(1L).orElse(null);
//        u.setProperty(p);
//        userRepository.save(u);
    }
}

