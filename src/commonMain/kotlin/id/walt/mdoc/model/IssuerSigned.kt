package id.walt.mdoc.model

import id.walt.mdoc.model.dataelement.EncodedCBORElement
import kotlinx.serialization.Serializable

@Serializable
data class IssuerSigned(
    val nameSpaces: Map<String, List<EncodedCBORElement>>?,
    val issuerAuth: IssuerAuth
)
