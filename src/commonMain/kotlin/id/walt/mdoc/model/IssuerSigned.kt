package id.walt.mdoc.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import cbor.ByteString

@Serializable
data class IssuerSigned(
    val nameSpaces: Map<String, List<EmbeddedCBORDataItem>>?
    // val issuerAuth: IssuerAuth
)
