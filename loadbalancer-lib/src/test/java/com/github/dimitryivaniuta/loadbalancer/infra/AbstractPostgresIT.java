package com.github.dimitryivaniuta.loadbalancer.infra;

import com.github.dimitryivaniuta.loadbalancer.api.RegistryScope;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractPostgresIT {

    private static final String TENANT = "tenant-1";
    private static final String GROUP = "payments";

    private static final String TENANT2 = "tenant-2";
    private static final String GROUP2 = "shipping";

    public static final RegistryScope SCOPE1 = new RegistryScope(TENANT, GROUP);
    public static final RegistryScope SCOPE2 = new RegistryScope(TENANT, GROUP2);
    public static final RegistryScope SCOPE3 = new RegistryScope(TENANT2, GROUP);

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("lb")
            .withUsername("lb")
            .withPassword("lb");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);

        //         ensure Flyway runs (if you use validate, keep schema in sync with migrations)
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }


    //    @DynamicPropertySource
//    static void dbProps(DynamicPropertyRegistry r) {
//        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
//        r.add("spring.datasource.username", POSTGRES::getUsername);
//        r.add("spring.datasource.password", POSTGRES::getPassword);
//        r.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
//
//         ensure Flyway runs (if you use validate, keep schema in sync with migrations)
//        r.add("spring.flyway.enabled", () -> "true");
//        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
//    }

}
