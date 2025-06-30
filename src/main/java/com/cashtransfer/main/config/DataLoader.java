// src/main/java/com/cashtransfer/main/config/DataLoader.java
package com.cashtransfer.main.config;

import com.cashtransfer.main.model.Account;
import com.cashtransfer.main.model.User;
import com.cashtransfer.main.repository.AccountRepository;
import com.cashtransfer.main.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * CommandLineRunner to load mock data into the database on application startup. This
 * runner is activated only when the "dev" or "postgres" Spring profile is active.
 */
@Component
@Profile({ "dev", "postgres", "h2" }) // Activate this loader for 'dev', 'postgres', or 'h2' profiles
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor for DataLoader.
     * @param userRepository The repository for User entities.
     * @param accountRepository The repository for Account entities.
     * @param passwordEncoder The password encoder used for hashing passwords.
     */
    public DataLoader(UserRepository userRepository, AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * This method runs automatically after the Spring ApplicationContext is loaded. It
     * checks if data already exists and, if not, creates mock users and accounts.
     * @param args Command line arguments (not used here).
     * @throws Exception if an error occurs during data loading.
     */
    @Override
    public void run(String... args) throws Exception {
        logger.info("Checking if mock data needs to be loaded...");

        // Only load data if no users exist to prevent duplicates on restarts
        if (userRepository.count() == 0) {
            logger.info("No existing users found. Loading mock data...");

            // Create and save Users
            // The @AllArgsConstructor in User model expects all fields, including
            // 'account'.
            // Since account is a MappedCollection and will be created separately, pass
            // null here.
            User alice = new User(null, "alice_smith", passwordEncoder.encode("password123"), "USER", null);
            User bob = new User(null, "bob_johnson", passwordEncoder.encode("securepass"), "USER", null);
            User admin = new User(null, "admin_user", passwordEncoder.encode("adminpass"), "ADMIN", null);
            User charlie = new User(null, "charlie_brown", passwordEncoder.encode("charliepass"), "USER", null);
            User diana = new User(null, "diana_prince", passwordEncoder.encode("wonderwoman"), "USER", null);
            User eve = new User(null, "eve_adams", passwordEncoder.encode("secretpass"), "USER", null);


            alice = userRepository.save(alice);
            bob = userRepository.save(bob);
            admin = userRepository.save(admin);
            charlie = userRepository.save(charlie);
            diana = userRepository.save(diana);
            eve = userRepository.save(eve);


            logger.info("Created users: {}, {}, {}, {}, {}, {}", alice, bob, admin, charlie, diana, eve);

            // Create and save Accounts linked to Users
            // Ensure that the user_id corresponds to the saved user's actual ID
            // The @AllArgsConstructor in Account model expects the 'id' field as the
            // first argument.
            accountRepository.save(new Account(null, "ACC001A", new BigDecimal("1500.75"), "SAVINGS", alice.getId()));
            accountRepository.save(new Account(null, "ACC002A", new BigDecimal("25000.00"), "SAVINGS", bob.getId()));
            accountRepository.save(new Account(null, "ACC003A", new BigDecimal("10000.50"), "BUSINESS", admin.getId()));
            accountRepository.save(new Account(null, "ACC004C", new BigDecimal("500.25"), "CHECKING", charlie.getId()));
            accountRepository.save(new Account(null, "ACC006D", new BigDecimal("3000.00"), "CHECKING", diana.getId()));
            accountRepository.save(new Account(null, "ACC007E", new BigDecimal("750.00"), "SAVINGS", eve.getId()));


            logger.info("Mock data loaded successfully. Total users: {}, Total accounts: {}", userRepository.count(),
                    accountRepository.count());
        }
        else {
            logger.info("Existing users found. Skipping mock data loading.");
        }
    }

}
