package id.walt.mdoc.mso

import cbor.Cbor
import id.walt.mdoc.dataelement.*
import id.walt.mdoc.issuersigned.IssuerSignedItem
import korlibs.crypto.HasherFactory
import korlibs.crypto.SHA256
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray

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

  fun toMapElement() = mapOf(
    "version" to version,
    "digestAlgorithm" to digestAlgorithm,
    "valueDigests" to valueDigests,
    "deviceKeyInfo" to deviceKeyInfo.toMapElement(),
    "docType" to docType,
    "validityInfo" to validityInfo.toMapElement()
  ).toDE()

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    fun createFor(nameSpaces: Map<String, List<IssuerSignedItem>>,
                  deviceKeyInfo: DeviceKeyInfo,
                  docType: String,
                  validityInfo: ValidityInfo,
                  digestAlgorithm: HasherFactory = SHA256): MSO {
      return MSO(
        "1.0".toDE(),
        digestAlgorithm.name.toDE(),
        nameSpaces.mapValues { entry ->
          entry.value.map { item ->
            Pair(
              item.digestID.value.toInt(),
              ByteStringElement(
                digestAlgorithm.digest(Cbor.encodeToByteArray(EncodedCBORElement(item.toMapElement()))).bytes
              )
            )
          }.toMap().toDE()
        }.toDE(),
        deviceKeyInfo,
        docType.toDE(),
        validityInfo
      )
    }
  }
}