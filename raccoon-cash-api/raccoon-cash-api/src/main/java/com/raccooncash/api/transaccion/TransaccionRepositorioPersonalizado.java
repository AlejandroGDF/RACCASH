package com.raccooncash.api.transaccion;

import java.time.LocalDateTime;
import java.util.List;

public interface TransaccionRepositorioPersonalizado {
    List<Transaccion> findWithFilters(Long accountId,
                                      Long categoryId,
                                      TipoTransaccion type,
                                      LocalDateTime fromDate,
                                      LocalDateTime toDate);
}
