package tools.jackson.core.unittest;

import org.junit.jupiter.api.Test;

import tools.jackson.core.Base64Variant;
import tools.jackson.core.Base64Variants;
import tools.jackson.core.util.BufferRecycler;
import tools.jackson.core.util.ByteArrayBuilder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for class {@link Base64Variant}.
 *
 * @see Base64Variant
 */
@SuppressWarnings("resource")
public class Base64VariantTest extends JacksonCoreTestBase
{
    @Test
    public void testDecodeTaking2ArgumentsThrowsIllegalArgumentException() {
        Base64Variant base64Variant = new Base64Variant("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
                false,
                'x',
                'x');

        assertEquals(120, base64Variant.getMaxLineLength());
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", base64Variant.toString());
        assertFalse(base64Variant.usesPadding());
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", base64Variant.getName());
        assertEquals((byte) 120, base64Variant.getPaddingByte());
        assertEquals('x', base64Variant.getPaddingChar());

        ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();

        try {
            base64Variant.decode("-%8en$9m=>$m", byteArrayBuilder);
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(Base64Variant.class.getName(), e.getStackTrace()[0].getClassName());
        }
    }

    @Test
    public void test_reportInvalidBase64ThrowsIllegalArgumentException() {
        Base64Variant base64Variant = new Base64Variant("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
                false,
                'L',
                3);

        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", base64Variant.getName());
        assertFalse(base64Variant.usesPadding());
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", base64Variant.toString());
        assertEquals((byte) 76, base64Variant.getPaddingByte());
        assertEquals(3, base64Variant.getMaxLineLength());
        assertEquals('L', base64Variant.getPaddingChar());
    }

    @Test
    public void testEquals() {
        Base64Variant base64Variant = new Base64Variant("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
                true,
                ':',
                ':');

        Object bogus = new BufferRecycler();
        assertFalse(base64Variant.equals(bogus));
        assertTrue(base64Variant.equals(base64Variant));

    }

    @Test
    public void testDecodeTaking2ArgumentsOne() {
        Base64Variant base64Variant = new Base64Variant("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
                false,
                'R',
                4);
        BufferRecycler bufferRecycler = new BufferRecycler();
        ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder(bufferRecycler);

        base64Variant.decode("PEM", byteArrayBuilder);

        assertFalse(base64Variant.usesPadding());
        assertEquals('R', base64Variant.getPaddingChar());
        assertEquals(4, base64Variant.getMaxLineLength());
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", base64Variant.toString());
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", base64Variant.getName());
        assertEquals((byte) 82, base64Variant.getPaddingByte());
    }

    @Test
    public void testEncodeTaking2ArgumentsWithTrue() {
        Base64Variant base64Variant = new Base64Variant("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
                false,
                'L',
                3);
        byte[] byteArray = new byte[9];
        String encoded = base64Variant.encode(byteArray, true);

        assertArrayEquals(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, byteArray);
        assertEquals("\"AAAA\\nAAAA\\nAAAA\\n\"", encoded);
    }

    @Test
    public void testDecodeCustomPaddingDerivedVariant() {
        /*
         * This test uses custom Base64Variant derived from padding-less 'MODIFIED_FOR_URL' variant.
         * Since a custom padding char is set in the constructor - decoding should accept this custom padding.
         */
        Base64Variant variant = new Base64Variant(Base64Variants.MODIFIED_FOR_URL, "CUSTOM-PADDING",
                true, '!', Integer.MAX_VALUE).withPaddingRequired();
        byte[] data = new byte[1]; // 1 byte of data should produce 2 padding chars

        String encoded = variant.encode(data);
        assertTrue(encoded.endsWith("!!"));

        byte[] decoded = variant.decode(encoded);
        assertArrayEquals(data, decoded);
    }

    @Test
    public void testDecodeDerivedVariantIllegalPaddingCharThrows() {
        /*
         * This test uses custom Base64Variant derived from 'MIME_NO_LINEFEEDS'.
         * Since a custom padding char is set in the constructor - decoding should not allow standard '=' padding.
         */
        Base64Variant variant = new Base64Variant(Base64Variants.MIME_NO_LINEFEEDS, "CUSTOM-PADDING",
                true, '!', Integer.MAX_VALUE);

        assertThrows(IllegalArgumentException.class, () -> variant.decode("AA=="));
    }
}
