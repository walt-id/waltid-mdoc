package id.walt.mdoc.cose

interface COSECryptoProvider {
  fun sign1(payload: ByteArray, keyID: String? = null): COSESign1
  fun verify1(coseSign1: COSESign1, keyID: String? = null): Boolean
  fun verifyX5Chain(coseSign1: COSESign1, keyID: String? = null): Boolean
}