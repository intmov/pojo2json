package ink.organics.pojo2json.test.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.openapi.actionSystem.AnAction;
import ink.organics.pojo2json.POJO2JsonDefaultAction;
import ink.organics.pojo2json.test.TestCase;
import testdata.java.EnumTestPOJO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

public class DataTypeTestModel extends TestModel {

    private final BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);


    public DataTypeTestModel(TestCase testCase) {
        super(testCase);
    }

    public void testPrimitiveTestPOJO(String fileName, AnAction action) {

        JsonNode result = testCase.testAction(fileName, action);

        assertEquals(0, result.get("aByte").intValue());
        assertEquals(0, result.get("aShort").shortValue());
        assertEquals(0, result.get("anInt").intValue());
        assertEquals(0L, result.get("aLong").longValue());
        assertEquals(zero, result.get("aFloat").decimalValue());
        assertEquals(zero, result.get("aDouble").decimalValue());
        assertEquals("c", result.get("aChar").textValue());
        assertFalse(result.get("aBoolean").booleanValue());
    }

    public void testPrimitiveArrayTestPOJO(String fileName, AnAction action) {
        JsonNode result = testCase.testAction(fileName, action);

        assertArrayEquals(new Integer[]{0},
                StreamSupport.stream(result.get("bytes").spliterator(), false).map(JsonNode::intValue).toArray(Integer[]::new));
        assertArrayEquals(new Short[]{0},
                StreamSupport.stream(result.get("shorts").spliterator(), false).map(JsonNode::shortValue).toArray(Short[]::new));
        assertArrayEquals(new Integer[]{0},
                StreamSupport.stream(result.get("ints").spliterator(), false).map(JsonNode::intValue).toArray(Integer[]::new));
        assertArrayEquals(new Long[]{0L},
                StreamSupport.stream(result.get("longs").spliterator(), false).map(JsonNode::longValue).toArray(Long[]::new));
        assertArrayEquals(new Float[]{0.00F},
                StreamSupport.stream(result.get("floats").spliterator(), false).map(JsonNode::floatValue).toArray(Float[]::new));
        assertArrayEquals(new Double[]{0.00D},
                StreamSupport.stream(result.get("doubles").spliterator(), false).map(JsonNode::doubleValue).toArray(Double[]::new));
        assertArrayEquals(new String[]{"c"},
                StreamSupport.stream(result.get("chars").spliterator(), false).map(JsonNode::textValue).toArray(String[]::new));
        assertArrayEquals(new Boolean[]{false},
                StreamSupport.stream(result.get("booleans").spliterator(), false).map(JsonNode::booleanValue).toArray(Boolean[]::new));
    }

    public void testEnumTestPOJO(String fileName, AnAction action) {
        JsonNode result = testCase.testAction(fileName, action);

        assertEquals(EnumTestPOJO.Type.TYPE_A.name(), result.get("type").textValue());
    }

    public void testIterableTestPOJO(String fileName, AnAction action) {
        JsonNode result = testCase.testAction(fileName, new POJO2JsonDefaultAction());

        assertTrue(result.get("iterable").isArray());
        assertTrue(result.get("collection").isArray());
        assertTrue(result.get("list").isArray());
        assertTrue(result.get("arrayList").isArray());
        assertTrue(result.get("linkedList").isArray());
        assertTrue(result.get("set").isArray());
        assertTrue(result.get("hashSet").isArray());
        assertTrue(result.get("linkedHashSet").isArray());

        assertTrue(StreamSupport.stream(result.get("iterable").spliterator(), false)
                .map(JsonNode::intValue)
                .collect(Collectors.toList())
                .contains(0));
    }

    public void testGenericTestPOJO(String fileName, AnAction action) {
        JsonNode result = testCase.testAction(fileName, action);

        assertTrue(result.get("list").isArray());
        assertTrue(result.get("listArr").get(0).isArray());
        assertTrue(result.get("listListArr").get(0).get(0).isArray());
        assertTrue(result.get("listEnum").get(0).isTextual());
        assertTrue(result.get("listList").get(0).isArray());
        assertTrue(result.get("listListList").get(0).get(0).isArray());
        assertTrue(result.get("listObject").get(0).isObject());
        assertTrue(result.get("listGenericObject").get(0).isObject());
        assertTrue(result.get("objectGeneric").isObject());
    }

    public void testSpecialObjectTestPOJO(String fileName, AnAction action) {
        JsonNode result = testCase.testAction(fileName, action);

        assertEquals(0, result.get("aByte").intValue());
        assertEquals(0, result.get("aShort").shortValue());
        assertEquals(0, result.get("integer").intValue());
        assertEquals(0L, result.get("aLong").longValue());
        assertEquals(zero, result.get("aFloat").decimalValue());
        assertEquals(zero, result.get("aDouble").decimalValue());
        assertFalse(result.get("aBoolean").booleanValue());
        assertEquals("c", result.get("character").textValue());
        assertTrue(result.get("string").isTextual());
        assertEquals(BigDecimal.valueOf(0).setScale(2, RoundingMode.UNNECESSARY), result.get("bigDecimal").decimalValue());

        DateTimeFormatter ldtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter ldf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter ltf = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter zdtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        DateTimeFormatter ymf = DateTimeFormatter.ofPattern("yyyy-MM");

        assertEquals(result.get("date").textValue(), LocalDateTime.parse(result.get("date").textValue(), ldtf).format(ldtf));
        assertEquals(result.get("temporal").longValue(), Instant.ofEpochMilli(result.get("temporal").longValue()).toEpochMilli());
        assertEquals(result.get("localDateTime").textValue(), LocalDateTime.parse(result.get("localDateTime").textValue(), ldtf).format(ldtf));
        assertEquals(result.get("localDate").textValue(), LocalDate.parse(result.get("localDate").textValue(), ldf).format(ldf));
        assertEquals(result.get("localTime").textValue(), LocalTime.parse(result.get("localTime").textValue(), ltf).format(ltf));
        assertEquals(result.get("zonedDateTime").textValue(), ZonedDateTime.parse(result.get("zonedDateTime").textValue(), zdtf).format(zdtf));
        assertEquals(result.get("yearMonth").textValue(), YearMonth.parse(result.get("yearMonth").textValue(), ymf).format(ymf));

        assertEquals(result.get("uuid").textValue(), UUID.fromString(result.get("uuid").textValue()).toString());
    }

}
