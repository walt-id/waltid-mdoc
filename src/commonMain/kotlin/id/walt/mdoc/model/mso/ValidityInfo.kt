package id.walt.mdoc.model.mso

import id.walt.mdoc.model.dataelement.TDateElement
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
}
