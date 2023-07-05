package id.walt.mdoc.cose

import id.walt.mdoc.dataelement.ListElement

interface COSECryptoProvider {
  fun sign1(payload: ByteArray, keyID: String? = null): ListElement
  fun verify1(coseSign1: ListElement, keyID: String? = null): Boolean
}