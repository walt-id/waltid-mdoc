package id.walt.mdoc.model

import kotlinx.serialization.Serializable

@Serializable
data class IssuerSigned(
    val nameSpaces: Map<String, List<EncodedDataElementValue>>?
    // val issuerAuth: IssuerAuth
)
