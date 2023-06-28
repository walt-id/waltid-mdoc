package id.walt.mdoc

import kotlinx.serialization.*
import cbor.Cbor
import id.walt.mdoc.model.*
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlin.test.Test

class MDocTest {

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testSerialization() {

        val textItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"family_name", DataElementValue("Doe"))
        val byteStringItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"byte_array", DataElementValue(byteArrayOf(0,1,2)))
        val intItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"age", DataElementValue(35))
        val floatItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"factor_x", DataElementValue(0.5f))
        val booleanItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"is_over_18", DataElementValue(true))
        val listItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"driving_privileges", DataElementValue(listOf(DataElementValue("A"), DataElementValue("B"))))
        val mapItem = IssuerSignedItem(0u,byteArrayOf(1, 2, 3),"attributes", DataElementValue(mapOf("attribute1" to DataElementValue("X"), "attribute2" to DataElementValue("Y"))))
        val nullItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "nothing", DataElementValue(null))

        println(Cbor.encodeToHexString(textItem).uppercase())
        println(Cbor.encodeToHexString(byteStringItem).uppercase())
        println(Cbor.encodeToHexString(intItem).uppercase())
        println(Cbor.encodeToHexString(floatItem).uppercase())
        println(Cbor.encodeToHexString(booleanItem).uppercase())
        println(Cbor.encodeToHexString(listItem).uppercase())
        println(Cbor.encodeToHexString(mapItem).uppercase())
        println(Cbor.encodeToHexString(nullItem).uppercase())

        val mdoc = MDocResponse(
            version = "1.0",
            documents = listOf(
                MDocBuilder("org.iso.18013.5.1.mDL").
                    addIssuerSignedItems(
                        "org.iso.18013.5.1", textItem, byteStringItem, intItem, floatItem, booleanItem, listItem, mapItem, nullItem
                    ).build()
            )
        )
        val mdocHex = Cbor.encodeToHexString(mdoc).uppercase()
        println("SERIALIZED MDOC:")
        println(mdocHex)

        val mdocParsed = Cbor.decodeFromHexString<MDocResponse>(mdocHex)
        mdocParsed.version shouldBe mdoc.version
        mdocParsed.documents shouldHaveSize mdoc.documents.size
        mdocParsed.documents[0].docType shouldBe "org.iso.18013.5.1.mDL"
        mdocParsed.documents[0].issuerSigned.nameSpaces?.shouldContainKey("org.iso.18013.5.1")
        mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!!.size shouldBe 8

        val parsedTextItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![0].data)
        parsedTextItem.elementValue.isTextString shouldBe true
        parsedTextItem.elementValue.textString shouldBe "Doe"

        val parsedByteStringItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![1].data)
        parsedByteStringItem.elementValue.isByteString shouldBe true
        parsedByteStringItem.elementValue.byteString shouldBe byteArrayOf(0,1,2)

        val parsedIntItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![2].data)
        parsedIntItem.elementValue.isNumber shouldBe true
        parsedIntItem.elementValue.number shouldBe 35

        val parsedFloatItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![3].data)
        parsedFloatItem.elementValue.isNumber shouldBe true
        parsedFloatItem.elementValue.number shouldBe 0.5f

        val parsedBooleanItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![4].data)
        parsedBooleanItem.elementValue.isBoolean shouldBe true
        parsedBooleanItem.elementValue.boolean shouldBe true

        val parsedListItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![5].data)
        parsedListItem.elementValue.isList shouldBe true
        parsedListItem.elementValue.list.map { it.textString } shouldContainAll listOf("A", "B")

        val parsedMapItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![6].data)
        parsedMapItem.elementValue.isMap shouldBe true
        parsedMapItem.elementValue.map.keys shouldContainAll listOf("attribute1", "attribute2")
        parsedMapItem.elementValue.map.values.map { it.textString } shouldContainAll listOf("X", "Y")

        val parsedNullItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![7].data)
        parsedNullItem.elementValue.isNull shouldBe true
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testDeserialize() {
        val serializedDoc =
            "A26776657273696F6E63312E3069646F63756D656E747381A267646F6354797065756F72672E69736F2E31383031332E352E312E6D444C6C6973737565725369676E6564A16A6E616D65537061636573A1716F72672E69736F2E31383031332E352E3181D8185863A4686469676573744944006672616E646F6D58208798645B20EA200E19FFABAC92624BEE6AEC63ACEEDECFB1B80077D22BFC20E971656C656D656E744964656E7469666965726B66616D696C795F6E616D656C656C656D656E7456616C756563446F65"
        val mdoc = Cbor.decodeFromHexString<MDocResponse>(serializedDoc)
        println(mdoc)

        val item = Cbor.decodeFromByteArray<IssuerSignedItem>(mdoc.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![0].data)
        item.elementValue.isTextString shouldBe true
        item.elementValue.textString shouldBe "Doe"
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testx() {
        val exampleCborData = byteArrayOf((0xBF).toByte(), 0x1A, 0x12, 0x38, 0x00, 0x00, 0x41, 0x03, (0xFF).toByte())
        println(Cbor.decodeFromByteArray<Map<Int, EmbeddedCBORDataItem>>(exampleCborData))
    }
}
