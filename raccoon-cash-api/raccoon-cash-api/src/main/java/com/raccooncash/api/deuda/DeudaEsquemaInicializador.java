package com.raccooncash.api.deuda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DeudaEsquemaInicializador {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeudaEsquemaInicializador.class);

    @Bean
    ApplicationRunner permitirDeudasSinCuenta(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE debts ALTER COLUMN account_id DROP NOT NULL");
                LOGGER.info("Columna debts.account_id configurada como opcional");
            } catch (DataAccessException ex) {
                LOGGER.debug("No se pudo ajustar debts.account_id como opcional", ex);
            }
        };
    }
}
