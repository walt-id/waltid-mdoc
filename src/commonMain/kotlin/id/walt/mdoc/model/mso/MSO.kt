package id.walt.mdoc.model.mso

import id.walt.mdoc.model.MapKey
import id.walt.mdoc.model.dataelement.ByteStringElement
import id.walt.mdoc.model.dataelement.MapElement
import id.walt.mdoc.model.dataelement.StringElement
import kotlinx.serialization.Serializable

@Serializable
class MSO (
  val version: StringElement,
  val digestAlgorithm: StringElement,
  val valueDigests: MapElement,
  val deviceKeyInfo: DeviceKeyInfo,
  val docType: StringElement,
  val validityInfo: ValidityInfo
) {
  fun getValueDigestsFor(nameSpace: String): Map<Int, ByteArray> {
    val nameSpaceElement = valueDigests.value[MapKey(nameSpace)] ?: return mapOf()
    return (nameSpaceElement as MapElement).value.map { entry ->
      Pair(entry.key.int, (entry.value as ByteStringElement).value)
    }.toMap()
  }

  val nameSpaces
    get() = valueDigests.value.keys.map { it.str }
}