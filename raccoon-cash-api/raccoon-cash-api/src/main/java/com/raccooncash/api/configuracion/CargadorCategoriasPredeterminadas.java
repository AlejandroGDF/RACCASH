package com.raccooncash.api.configuracion;

import com.raccooncash.api.categoria.Categoria;
import com.raccooncash.api.categoria.CategoriaRepositorio;
import com.raccooncash.api.categoria.TipoCategoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(2)
public class CargadorCategoriasPredeterminadas implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CargadorCategoriasPredeterminadas.class);

    private static final List<CategoriaPredeterminada> DEFAULT_CATEGORIES = List.of(
            expense("Comida", "#f97316", "utensils"),
            expense("Comestibles", "#84cc16", "shopping-basket"),
            expense("Transporte", "#0ea5e9", "car"),
            expense("Compras", "#ec4899", "shopping-bag"),
            expense("Entretenimiento", "#a855f7", "gamepad-2"),
            expense("Facturas", "#64748b", "receipt"),
            expense("Alquiler", "#14b8a6", "home"),
            expense("Salud", "#ef4444", "heart-pulse"),
            expense("Medicamentos", "#dc2626", "pill"),
            expense("Educaci\u00f3n", "#3b82f6", "graduation-cap"),
            expense("Viajes", "#06b6d4", "plane"),
            expense("Regalos", "#f43f5e", "gift"),
            expense("Belleza", "#d946ef", "sparkles"),
            expense("Trabajo", "#475569", "briefcase"),
            expense("Hogar", "#22c55e", "house"),
            expense("Tecnolog\u00eda", "#6366f1", "laptop"),
            expense("Suscripciones", "#8b5cf6", "repeat"),
            expense("Deudas", "#b91c1c", "credit-card"),
            expense("Mascotas", "#a16207", "paw-print"),
            expense("Emergencias", "#f59e0b", "siren"),
            expense("Impuestos", "#334155", "landmark"),
            expense("Otros", "#71717a", "circle-ellipsis"),
            expense("Correcci\u00f3n de saldo", "#0891b2", "scale"),
            income("Trabajo", "#16a34a", "briefcase"),
            income("Pagos", "#22c55e", "hand-coins"),
            income("Regalos", "#db2777", "gift"),
            income("Otros", "#52525b", "circle-ellipsis"),
            income("Correcci\u00f3n de saldo", "#0284c7", "scale")
    );

    private final boolean seedDefaultCategories;
    private final CategoriaRepositorio categoryRepository;

    public CargadorCategoriasPredeterminadas(
            @Value("${raccoon-cash.data.seed-default-categories:true}") boolean seedDefaultCategories,
            CategoriaRepositorio categoryRepository) {
        this.seedDefaultCategories = seedDefaultCategories;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedDefaultCategories) {
            return;
        }

        int created = 0;
        for (CategoriaPredeterminada defaultCategory : DEFAULT_CATEGORIES) {
            if (categoryRepository.existsByNameIgnoreCaseAndTypeAndActiveTrue(
                    defaultCategory.name(), defaultCategory.type())) {
                continue;
            }

            Categoria category = new Categoria();
            category.setName(defaultCategory.name());
            category.setType(defaultCategory.type());
            category.setColor(defaultCategory.color());
            category.setIcon(defaultCategory.icon());
            category.setActive(true);
            categoryRepository.save(category);
            created++;
        }

        if (created > 0) {
            LOGGER.info("Created {} default categories.", created);
        }
    }

    private static CategoriaPredeterminada expense(String name, String color, String icon) {
        return new CategoriaPredeterminada(name, TipoCategoria.EXPENSE, color, icon);
    }

    private static CategoriaPredeterminada income(String name, String color, String icon) {
        return new CategoriaPredeterminada(name, TipoCategoria.INCOME, color, icon);
    }

    private record CategoriaPredeterminada(String name, TipoCategoria type, String color, String icon) {
    }
}
