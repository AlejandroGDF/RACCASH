package ni.edu.uam.raccooncash.data.model

data class CategoryRequest(
    val name: String,
    val icon: String?,
    val color: String?,
    val type: String, // INCOME, EXPENSE
    val parentCategoryId: Long? = null
)
