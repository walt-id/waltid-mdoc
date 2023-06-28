package id.walt.mdoc

import id.walt.mdoc.model.MDoc
import kotlinx.serialization.Serializable

@Serializable
class MDocResponse(
    val version: String,
    val documents: List<MDoc>
)