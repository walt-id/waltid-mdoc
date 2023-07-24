package id.walt.mdoc.cose

import cbor.Cbor
import id.walt.mdoc.dataelement.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException

abstract class COSESimpleBase<T: COSESimpleBase<T>>() {
  abstract val data: List<AnyDataElement>
  val payload: ByteArray?
    get() {
      if (data.size != 4) throw SerializationException("Invalid COSE_Sign1/COSE_Mac0 array")
      return when (data[2].type) {
        DEType.nil -> null
        DEType.byteString -> (data[2] as ByteStringElement).value
        else -> throw SerializationException("Invalid COSE_Sign1 payload")
      }
    }

  val x5Chain: ByteArray?
    get() {
      if (data.size != 4) throw SerializationException("Invalid COSE_Sign1/COSE_Mac0 array")
      val unprotectedHeader = data[1] as? MapElement ?: throw SerializationException("Missing COSE_Sign1 unprotected header")
      val x5Chain = unprotectedHeader.value[MapKey(X5_CHAIN)] as? ByteStringElement
      return x5Chain?.value
    }

  val protectedHeader: ByteArray
    get() {
      if (data.size != 4) throw SerializationException("Invalid COSE_Sign1/COSE_Mac0 array")
      return (data[0] as ByteStringElement).value
    }

  val algorithm: Int
    get() = (DataElement.fromCBOR<MapElement>(protectedHeader).value[MapKey(ALG_LABEL)] as NumberElement).value.toInt()

  val signatureOrTag: ByteArray
    get() {
      if (data.size != 4) throw SerializationException("Invalid COSE_Sign1/COSE_Mac0 array")
      return (data[3] as ByteStringElement).value
    }

  protected fun replacePayload(payloadElement: AnyDataElement): List<AnyDataElement> {
    return data.mapIndexed { idx, el -> when(idx) {
      2 -> payloadElement
      else -> el
    } }
  }

  abstract fun detachPayload(): T
  abstract fun attachPayload(payload: ByteArray): T

  fun toDE() = ListElement(data)
  fun toCBOR() = toDE().toCBOR()
  fun toCBORHex() = toDE().toCBORHex()
}