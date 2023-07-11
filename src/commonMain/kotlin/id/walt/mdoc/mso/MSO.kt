package id.walt.mdoc.mso

import id.walt.mdoc.dataelement.*
import id.walt.mdoc.issuersigned.IssuerSignedItem
import kotlinx.serialization.ExperimentalSerializationApi
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

  fun toMapElement() = mapOf(
    "version" to version,
    "digestAlgorithm" to digestAlgorithm,
    "valueDigests" to valueDigests,
    "deviceKeyInfo" to deviceKeyInfo.toMapElement(),
    "docType" to docType,
    "validityInfo" to validityInfo.toMapElement()
  ).toDE()

  fun verifySignedItems(nameSpace: String, items: List<EncodedCBORElement>): Boolean {
    val msoDigests = getValueDigestsFor(nameSpace)
    val algorithm = DigestAlgorithm.values().first { it.value == digestAlgorithm.value }
    return items.all {
      val digestId = it.decode<IssuerSignedItem>().digestID.value.toInt()
      return msoDigests.containsKey(digestId) && msoDigests[digestId]!!.contentEquals(digestItem(it, algorithm))
    }
  }

  companion object {

    fun digestItem(encodedItem: EncodedCBORElement, digestAlgorithm: DigestAlgorithm): ByteArray {
      return digestAlgorithm.getHasher().digest(encodedItem.toCBOR()).bytes
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun createFor(nameSpaces: Map<String, List<IssuerSignedItem>>,
                  deviceKeyInfo: DeviceKeyInfo,
                  docType: String,
                  validityInfo: ValidityInfo,
                  digestAlgorithm: DigestAlgorithm = DigestAlgorithm.SHA256): MSO {
      return MSO(
        "1.0".toDE(),
        digestAlgorithm.value.toDE(),
        nameSpaces.mapValues { entry ->
          entry.value.map { item ->
            Pair(
              item.digestID.value.toInt(),
              ByteStringElement(
                digestItem(EncodedCBORElement(item.toMapElement()), digestAlgorithm)
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