package ni.edu.uam.raccooncash.data.model

data class PresupuestoSolicitud(
    val nombre: String,
    val monto: Double,
    val tipoPeriodo: TipoPeriodoPresupuesto,
    val valorPeriodo: Int,
    val fechaInicio: String,
    val color: String,
    val esGasto: Boolean,
    val incluirTodasLasTransacciones: Boolean
)
