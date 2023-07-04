package id.walt.mdoc

import id.walt.mdoc.dataelement.NumberElement
import id.walt.mdoc.dataelement.StringElement
import id.walt.mdoc.dataelement.toDE
import kotlinx.serialization.Serializable

@Serializable
class MDocResponse(
    val version: StringElement,
    val documents: List<MDoc>,
    val status: NumberElement = MDocResponseStatus.OK.status.toDE()
)