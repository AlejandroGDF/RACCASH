package ni.edu.uam.raccooncash.data.model

data class PresupuestoRespuesta(
    val id: Long,
    val nombre: String,
    val monto: Double,
    val tipoPeriodo: TipoPeriodoPresupuesto,
    val valorPeriodo: Int,
    val fechaInicio: String,
    val color: String,
    val esGasto: Boolean,
    val incluirTodasLasTransacciones: Boolean,
    val montoActual: Double = 0.0,
    val moneda: String? = "C$"
)
