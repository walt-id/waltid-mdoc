package id.walt.mdoc.cose

import id.walt.mdoc.dataelement.ListElement

interface AsyncCOSECryptoProvider {
  suspend fun sign1(payload: ByteArray, keyID: String? = null): ListElement
}