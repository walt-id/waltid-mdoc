package id.walt.mdoc

import id.walt.mdoc.model.MDoc
import id.walt.mdoc.model.MDocResponseStatus
import id.walt.mdoc.model.dataelement.NumberElement
import id.walt.mdoc.model.dataelement.StringElement
import id.walt.mdoc.model.dataelement.toDE
import kotlinx.serialization.Serializable

@Serializable
class MDocResponse(
    val version: StringElement,
    val documents: List<MDoc>,
    val status: NumberElement = MDocResponseStatus.OK.status.toDE()
)