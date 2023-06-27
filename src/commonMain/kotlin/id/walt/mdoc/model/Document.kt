package id.walt.mdoc.model

import kotlinx.serialization.Serializable

@Serializable
data class Document (
  val docType: String,
  val issuerSigned: IssuerSigned
)