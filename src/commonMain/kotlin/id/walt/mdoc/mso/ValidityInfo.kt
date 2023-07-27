package id.walt.mdoc.mso

import id.walt.mdoc.dataelement.TDateElement
import id.walt.mdoc.dataelement.toDE
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class ValidityInfo private constructor(
  val signed: TDateElement,
  val validFrom: TDateElement,
  val validUntil: TDateElement,
  val expectedUpdate: TDateElement? = null
) {

  constructor(signed: Instant, validFrom: Instant, validUntil: Instant, expectedUpdate: Instant? = null)
    : this(TDateElement(signed), TDateElement(validFrom), TDateElement(validUntil), expectedUpdate?.let { TDateElement(it) })

  /**
   * Convert to CBOR map element
   */
  fun toMapElement() = buildMap {
    put("signed", signed)
    put("validFrom", validFrom)
    put("validUntil", validUntil)
    expectedUpdate?.let { put("expectedUpdate", it) }
  }.toDE()
}
