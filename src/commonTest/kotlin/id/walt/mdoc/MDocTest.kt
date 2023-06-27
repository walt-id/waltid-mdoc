package id.walt.mdoc

import id.walt.mdoc.model.Document
import id.walt.mdoc.model.IssuerSigned
import id.walt.mdoc.model.IssuerSignedItem
import id.walt.mdoc.model.IssuerSignedItemBytes
import kotlinx.serialization.*
import cbor.ByteString
import cbor.Cbor
import cbor.Cbor.Default.decodeFromByteArray
import kotlin.jvm.JvmInline
import kotlin.test.Test

class MDocTest {

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testSerialization() {

        val item = IssuerSignedItem(
            0u,
            byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2),
            "family_name",
            "Doe"
        )
        println(Cbor.encodeToHexString(item))

        val mdoc = MDoc(
            documents = listOf(
                Document(
                    "org.iso.18013.5.1.mDL",
                    IssuerSigned.Companion.IssuerSignedBuilder().addIssuerSignedItem(
                        "org.iso.18013.5.1", IssuerSignedItemBytes(Cbor.encodeToByteArray(item))
                    ).build()
                )
            )
        )
        println(Cbor.encodeToHexString(mdoc))
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testDeserialize() {
        val serializedDoc =
            "bf69646f63756d656e74739fbf67646f6354797065756f72672e69736f2e31383031332e352e312e6d444c6c6973737565725369676e6564bf6a6e616d65537061636573bf716f72672e69736f2e31383031332e352e319f9f384018681864186918671865187318741849184400186618721861186e1864186f186d18581820010203040506070809000102030405060708090001020304050607080900010218711865186c1865186d1865186e1874184918641865186e187418691866186918651872186b18661861186d1869186c1879185f186e1861186d1865186c1865186c1865186d1865186e187418561861186c1875186518631844186f186520ffffffffffffff"
        val mdoc = Cbor.decodeFromHexString<MDoc>(serializedDoc)
        println(mdoc)

        val serializedDoc2 =
            "A26776657273696F6E63312E3069646F63756D656E747381A267646F6354797065756F72672E69736F2E31383031332E352E312E6D444C6C6973737565725369676E6564A16A6E616D65537061636573A1716F72672E69736F2E31383031332E352E3181D8185863A4686469676573744944006672616E646F6D58208798645B20EA200E19FFABAC92624BEE6AEC63ACEEDECFB1B80077D22BFC20E971656C656D656E744964656E7469666965726B66616D696C795F6E616D656C656C656D656E7456616C756563446F65"
        val mdoc2 = Cbor.decodeFromHexString<MDoc>(serializedDoc2)
        println(mdoc2)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testx() {
        val exampleCborData = byteArrayOf((0xBF).toByte(), 0x1A, 0x12, 0x38, 0x00, 0x00, 0x41, 0x03, (0xFF).toByte())
        println(Cbor.decodeFromByteArray<Map<Int, IssuerSignedItemBytes>>(exampleCborData))
    }
}
