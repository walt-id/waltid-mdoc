package id.walt.mdoc

import kotlinx.serialization.*
import cbor.Cbor
import id.walt.mdoc.model.*
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
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
        val embeddedCborValue = "The encoded item"
        val cborItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "encoded_cbor", DataElementValue(EncodedDataElementValue(Cbor.encodeToByteArray(embeddedCborValue))))
        val tdateItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "issue_date", DataElementValue(Clock.System.now()))
        val tdateIntItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "issue_date", DataElementValue(Clock.System.now(), DEDateTimeMode.time_int))
        val tdateDblItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "issue_date", DataElementValue(Clock.System.now(), DEDateTimeMode.time_double))
        val fullDateStrItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "birth_date", DataElementValue(LocalDate.parse("1983-07-05"), DEFullDateMode.full_date_str))
        val fullDateIntItem = IssuerSignedItem(0u, byteArrayOf(1,2,3), "expiry_date", DataElementValue(LocalDate.parse("2025-12-31"), DEFullDateMode.full_date_int))

        val mdoc = MDocResponse(
            version = "1.0",
            documents = listOf(
                MDocBuilder("org.iso.18013.5.1.mDL").
                    addIssuerSignedItems(
                        "org.iso.18013.5.1", textItem, byteStringItem, intItem, floatItem, booleanItem, listItem, mapItem, nullItem, cborItem, tdateItem, tdateIntItem, tdateDblItem, fullDateStrItem, fullDateIntItem
                    ).build()
            )
        )
        val mdocHex = Cbor.encodeToHexString(mdoc).uppercase()
        println("SERIALIZED MDOC:")
        println(mdocHex)

        val mdocParsed = Cbor.decodeFromHexString<MDocResponse>(mdocHex)
        mdocParsed.version shouldBe mdoc.version
        mdocParsed.documents shouldHaveSize mdoc.documents.size
        mdocParsed.documents[0].docType shouldBe mdoc.documents[0].docType
        mdocParsed.documents[0].issuerSigned.nameSpaces!!.keys shouldContainAll mdoc.documents[0].issuerSigned.nameSpaces!!.keys
        mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!!.size shouldBe mdoc.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!!.size

        val parsedTextItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![0].data)
        parsedTextItem.elementValue.type shouldBe DEType.textString
        parsedTextItem.elementValue.textString shouldBe textItem.elementValue.textString

        val parsedByteStringItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![1].data)
        parsedByteStringItem.elementValue.type shouldBe DEType.byteString
        parsedByteStringItem.elementValue.byteString shouldBe byteStringItem.elementValue.byteString

        val parsedIntItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![2].data)
        parsedIntItem.elementValue.type shouldBe DEType.number
        parsedIntItem.elementValue.number shouldBe intItem.elementValue.number

        val parsedFloatItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![3].data)
        parsedFloatItem.elementValue.type shouldBe DEType.number
        parsedFloatItem.elementValue.number shouldBe floatItem.elementValue.number

        val parsedBooleanItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![4].data)
        parsedBooleanItem.elementValue.type shouldBe DEType.boolean
        parsedBooleanItem.elementValue.boolean shouldBe booleanItem.elementValue.boolean

        val parsedListItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![5].data)
        parsedListItem.elementValue.type shouldBe DEType.list
        parsedListItem.elementValue.list.map { it.textString } shouldContainAll listItem.elementValue.list.map { it.textString }

        val parsedMapItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![6].data)
        parsedMapItem.elementValue.type shouldBe DEType.map
        parsedMapItem.elementValue.map.keys shouldContainAll mapItem.elementValue.map.keys
        parsedMapItem.elementValue.map.values.map { it.textString } shouldContainAll mapItem.elementValue.map.values.map { it.textString }

        val parsedNullItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![7].data)
        parsedNullItem.elementValue.type shouldBe DEType.nil

        val parsedCborItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![8].data)
        parsedCborItem.elementValue.type shouldBe DEType.encodedCbor
        val parsedEmbeddedCbor = Cbor.decodeFromByteArray<DataElementValue>(parsedCborItem.elementValue.embeddedCBOR.data)
        parsedEmbeddedCbor.type shouldBe DEType.textString
        parsedEmbeddedCbor.textString shouldBe embeddedCborValue

        val parsedTdateItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![9].data)
        parsedTdateItem.elementValue.type shouldBe DEType.dateTime
        parsedTdateItem.elementValue.dateTime shouldBe tdateItem.elementValue.dateTime

        val parsedTdateIntItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![10].data)
        parsedTdateIntItem.elementValue.type shouldBe DEType.dateTime
        parsedTdateIntItem.elementValue.dateTime.epochSeconds shouldBe tdateIntItem.elementValue.dateTime.epochSeconds

        val parsedTdateDblItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![11].data)
        parsedTdateDblItem.elementValue.type shouldBe DEType.dateTime
        parsedTdateDblItem.elementValue.dateTime.toEpochMilliseconds() shouldBe tdateDblItem.elementValue.dateTime.toEpochMilliseconds()

        val parsedfullDateStrItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![12].data)
        parsedfullDateStrItem.elementValue.type shouldBe DEType.fullDate
        parsedfullDateStrItem.elementValue.fullDate.toEpochDays() shouldBe fullDateStrItem.elementValue.fullDate.toEpochDays()

        val parsedfullDateIntItem = Cbor.decodeFromByteArray<IssuerSignedItem>(mdocParsed.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![13].data)
        parsedfullDateIntItem.elementValue.type shouldBe DEType.fullDate
        parsedfullDateIntItem.elementValue.fullDate.toEpochDays() shouldBe fullDateIntItem.elementValue.fullDate.toEpochDays()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testDeserialize() {
        val serializedDoc =
            "A26776657273696F6E63312E3069646F63756D656E747381A267646F6354797065756F72672E69736F2E31383031332E352E312E6D444C6C6973737565725369676E6564A16A6E616D65537061636573A1716F72672E69736F2E31383031332E352E3181D8185863A4686469676573744944006672616E646F6D58208798645B20EA200E19FFABAC92624BEE6AEC63ACEEDECFB1B80077D22BFC20E971656C656D656E744964656E7469666965726B66616D696C795F6E616D656C656C656D656E7456616C756563446F65"
        val mdoc = Cbor.decodeFromHexString<MDocResponse>(serializedDoc)
        println(mdoc)

        val item = Cbor.decodeFromByteArray<IssuerSignedItem>(mdoc.documents[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![0].data)
        item.elementValue.type shouldBe DEType.textString
        item.elementValue.textString shouldBe "Doe"
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testx() {
        val exampleCborData = byteArrayOf((0xBF).toByte(), 0x1A, 0x12, 0x38, 0x00, 0x00, 0x41, 0x03, (0xFF).toByte())
        println(Cbor.decodeFromByteArray<Map<Int, EncodedDataElementValue>>(exampleCborData))
    }
}
