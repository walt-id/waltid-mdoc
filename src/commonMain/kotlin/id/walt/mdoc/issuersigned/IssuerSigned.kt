package id.walt.mdoc.issuersigned

import id.walt.mdoc.cose.COSESign1
import id.walt.mdoc.dataelement.EncodedCBORElement
import kotlinx.serialization.Serializable

@Serializable
data class IssuerSigned(
    val nameSpaces: Map<String, List<EncodedCBORElement>>?,
    val issuerAuth: COSESign1
)
