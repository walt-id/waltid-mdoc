package id.walt.mdoc

import COSE.*
import id.walt.mdoc.cose.COSECryptoProvider
import id.walt.mdoc.dataelement.DataElement
import id.walt.mdoc.dataelement.ListElement
import kotlinx.serialization.ExperimentalSerializationApi
import java.security.PrivateKey
import java.security.PublicKey

class SimpleCOSECryptoProvider(
  val algorithmID: AlgorithmID,
  val key: OneKey
): COSECryptoProvider {

  constructor(algorithmID: AlgorithmID, publicKey: PublicKey?, privateKey: PrivateKey?) : this(algorithmID, OneKey(publicKey, privateKey))

  @OptIn(ExperimentalSerializationApi::class)
  override fun sign1(payload: ByteArray, keyID: String?): ListElement {
    val sign1Msg = Sign1Message()
    sign1Msg.addAttribute(HeaderKeys.Algorithm, algorithmID.AsCBOR(), Attribute.PROTECTED)
    sign1Msg.SetContent(payload)
    sign1Msg.sign(key)

    val cborObj = sign1Msg.EncodeToCBORObject()
    return DataElement.fromCBOR<ListElement>(cborObj.EncodeToBytes())
  }

  @OptIn(ExperimentalSerializationApi::class)
  override fun verify1(coseSign1: ListElement, keyID: String?): Boolean {
    val sign1Msg = Sign1Message.DecodeFromBytes(coseSign1.toCBOR(), MessageTag.Sign1) as Sign1Message
    return sign1Msg.validate(key)
  }
}