package id.walt.mdoc

import id.walt.mdoc.model.MDoc
import id.walt.mdoc.model.MDocResponseStatus
import kotlinx.serialization.Serializable

@Serializable
class MDocResponse(
    val version: String,
    val documents: List<MDoc>,
    val status: UInt = MDocResponseStatus.OK.status
)