package com.thm.app_server;

import com.thm.app_server.model.ParkingLot;
import com.thm.app_server.model.Role;
import com.thm.app_server.model.RoleName;
import com.thm.app_server.model.User;
import com.thm.app_server.repository.RoleRepository;
import com.thm.app_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.TimeZone;

@SpringBootApplication
@EntityScan(basePackageClasses = {
        AppServerApplication.class,
        Jsr310JpaConverters.class
})
@EnableAsync
public class AppServerApplication implements CommandLineRunner {
//
//    @Autowired
//    UserRepository userRepository;
//
//    @Autowired
//    PasswordEncoder encoder;
//
//    @Autowired
//    RoleRepository roleRepository;
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
//        User u = new User("admin", "admin@gmail.com", encoder.encode("nothing"));
//        Role role = roleRepository.findByName(RoleName.ROLE_ADMIN).orElse(null);
//        u.setRoles(Collections.singleton(role));
//        userRepository.save(u);
    }
}

