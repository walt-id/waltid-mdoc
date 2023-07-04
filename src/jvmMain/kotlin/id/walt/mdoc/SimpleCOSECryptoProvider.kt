package id.walt.mdoc

import COSE.*
import cbor.Cbor
import id.walt.mdoc.cose.COSECryptoProvider
import id.walt.mdoc.dataelement.AnyDataElement
import id.walt.mdoc.dataelement.DataElementSerializer
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.toDE
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray

class SimpleCOSECryptoProvider: COSECryptoProvider {
  @OptIn(ExperimentalSerializationApi::class)
  override fun sign1(payload: ByteArray, keyID: String?): ListElement {
    val sign1Msg = Sign1Message()
    sign1Msg.addAttribute(HeaderKeys.Algorithm, AlgorithmID.ECDSA_256.AsCBOR(), Attribute.PROTECTED)
    sign1Msg.SetContent(payload)
    sign1Msg.sign(OneKey.generateKey(AlgorithmID.ECDSA_256))
    val cborObj = sign1Msg.EncodeToCBORObject()
    return Cbor.decodeFromByteArray<AnyDataElement>(DataElementSerializer, cborObj.EncodeToBytes()) as ListElement
  }
}