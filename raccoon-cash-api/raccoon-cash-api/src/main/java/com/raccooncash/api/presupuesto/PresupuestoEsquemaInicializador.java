package com.raccooncash.api.presupuesto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PresupuestoEsquemaInicializador {

    private static final Logger LOGGER = LoggerFactory.getLogger(PresupuestoEsquemaInicializador.class);

    @Bean
    ApplicationRunner actualizarRestriccionesPresupuesto(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE budgets DROP CONSTRAINT IF EXISTS budgets_period_type_check");
                LOGGER.info("Restriccion obsoleta budgets_period_type_check eliminada o inexistente");
            } catch (DataAccessException ex) {
                LOGGER.debug("No se pudo ajustar la restriccion budgets_period_type_check", ex);
            }
        };
    }
}
