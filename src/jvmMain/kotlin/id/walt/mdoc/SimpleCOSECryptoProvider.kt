package id.walt.mdoc

import COSE.*
import cbor.Cbor
import com.upokecenter.cbor.CBORObject
import id.walt.mdoc.cose.COSECryptoProvider
import id.walt.mdoc.cose.COSESign1
import id.walt.mdoc.cose.COSESign1Serializer
import id.walt.mdoc.cose.X5_CHAIN
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.*
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Create simple COSE crypto provider for a given private and public key pair. For verification only, private key can be omitted.
 * @param algorithmID Signing algorithm ID, e.g.: ECDSA_256
 * @param key Public/Private key pair for COSE signing and/or verification
 * @param x5Chain certificate chain, including intermediate and signing key certificates, but excluding root CA certificate!
 * @param trustedCA enforce trusted root CA, if not publicly known, for certificate path validation
 */
class SimpleCOSECryptoProvider(
  val algorithmID: AlgorithmID,
  val key: OneKey,
  val x5Chain: List<X509Certificate>,
  val trustedCA: X509Certificate? = null
): COSECryptoProvider {

  /**
   * Create simple COSE crypto provider for a given private and public key pair. For verification only, private key can be omitted.
   * @param algorithmID Signing algorithm ID, e.g.: ECDSA_256
   * @param publicKey Public key for COSE verification
   * @param privateKey Private key for COSE signing
   * @param x5Chain certificate chain, including intermediate and signing key certificates, but excluding root CA certificate!
   * @param trustedCA enforce trusted root CA, if not publicly known, for certificate path validation
   */
  constructor(algorithmID: AlgorithmID, publicKey: PublicKey?, privateKey: PrivateKey?, x5Chain: List<X509Certificate>, trustedCA: X509Certificate? = null)
      : this(algorithmID, OneKey(publicKey, privateKey), x5Chain, trustedCA)

  @OptIn(ExperimentalSerializationApi::class)
  override fun sign1(payload: ByteArray, keyID: String?): COSESign1 {
    val sign1Msg = Sign1Message()
    sign1Msg.addAttribute(HeaderKeys.Algorithm, algorithmID.AsCBOR(), Attribute.PROTECTED)
    sign1Msg.addAttribute(
      CBORObject.FromObject(X5_CHAIN),
      CBORObject.FromObject(x5Chain.map { it.encoded }.reduce { acc, bytes -> acc + bytes }),
      Attribute.UNPROTECTED)
    sign1Msg.SetContent(payload)
    sign1Msg.sign(key)

    val cborObj = sign1Msg.EncodeToCBORObject()
    return Cbor.decodeFromByteArray(COSESign1Serializer, cborObj.EncodeToBytes())
  }

  @OptIn(ExperimentalSerializationApi::class)
  override fun verify1(coseSign1: COSESign1, keyID: String?): Boolean {
    val sign1Msg = Sign1Message.DecodeFromBytes(coseSign1.toCBOR(), MessageTag.Sign1) as Sign1Message
    return sign1Msg.validate(key)
  }

  private fun findRootCA(cert: X509Certificate, enforceTrustedCA: X509Certificate?): X509Certificate? {
    val tm = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tm.init(null as? KeyStore)
    return tm.trustManagers.filterIsInstance<X509TrustManager>().flatMap { it.acceptedIssuers.toList() }.firstOrNull {
      cert.issuerX500Principal.name.equals(it.subjectX500Principal.name)
    } ?: enforceTrustedCA
  }

  private fun validateCertificateChain(certChain: List<X509Certificate>): Boolean {
    val certPath = CertificateFactory.getInstance("X509").generateCertPath(certChain)
    val cpv = CertPathValidator.getInstance("PKIX")
    val trustAnchorCert = findRootCA(certChain.first(), trustedCA) ?: return false
    cpv.validate(certPath, PKIXParameters(setOf(TrustAnchor(trustAnchorCert, null))).apply {
      isRevocationEnabled = false
    })

    return true
  }

  override fun verifyX5Chain(coseSign1: COSESign1, keyID: String?): Boolean {
    return coseSign1.x5Chain?.let {
      val certChain = CertificateFactory.getInstance("X509").generateCertificates(ByteArrayInputStream(it)).map { it as X509Certificate }
      return certChain.isNotEmpty() && certChain.last().publicKey.encoded.contentEquals(key.AsPublicKey().encoded) &&
          validateCertificateChain(certChain.toList())
    } ?: false
  }


}