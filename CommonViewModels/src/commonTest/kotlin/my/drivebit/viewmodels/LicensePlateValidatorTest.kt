package my.drivebit.viewmodels

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LicensePlateValidatorTest {
    // ===== isValid tests =====

    @Test
    fun `valid plate with 2-digit region`() {
        assertTrue(LicensePlateValidator.isValid("А123ВЕ12"))
    }

    @Test
    fun `valid plate with 3-digit region`() {
        assertTrue(LicensePlateValidator.isValid("А123ВЕ123"))
    }

    @Test
    fun `valid plate with all allowed first letters`() {
        assertTrue(LicensePlateValidator.isValid("А111АА11"))
        assertTrue(LicensePlateValidator.isValid("В222ВВ22"))
        assertTrue(LicensePlateValidator.isValid("Е333ЕЕ33"))
        assertTrue(LicensePlateValidator.isValid("К444КК44"))
        assertTrue(LicensePlateValidator.isValid("М555ММ55"))
        assertTrue(LicensePlateValidator.isValid("Н666НН66"))
        assertTrue(LicensePlateValidator.isValid("О777ОО77"))
        assertTrue(LicensePlateValidator.isValid("Р888РР88"))
        assertTrue(LicensePlateValidator.isValid("С999СС99"))
        assertTrue(LicensePlateValidator.isValid("Т100ТТ10"))
        assertTrue(LicensePlateValidator.isValid("У200УУ20"))
        assertTrue(LicensePlateValidator.isValid("Х300ХХ30"))
    }

    @Test
    fun `valid plate with lowercase letters`() {
        assertTrue(LicensePlateValidator.isValid("а123ве12"))
    }

    @Test
    fun `valid plate with mixed case`() {
        assertTrue(LicensePlateValidator.isValid("А123ве12"))
    }

    @Test
    fun `valid real-world plates`() {
        assertTrue(LicensePlateValidator.isValid("К128СТ78"))
        assertTrue(LicensePlateValidator.isValid("О001ОО177"))
        assertTrue(LicensePlateValidator.isValid("Е555КХ199"))
        assertTrue(LicensePlateValidator.isValid("М777ММ97"))
    }

    @Test
    fun `invalid - Latin letters instead of Cyrillic`() {
        assertFalse(LicensePlateValidator.isValid("A123BE12"))
    }

    @Test
    fun `invalid - disallowed Cyrillic letters`() {
        assertFalse(LicensePlateValidator.isValid("Б123АА12"))
        assertFalse(LicensePlateValidator.isValid("А123ГА12"))
        assertFalse(LicensePlateValidator.isValid("А123АЯ12"))
    }

    @Test
    fun `invalid - too few digits in number`() {
        assertFalse(LicensePlateValidator.isValid("А12ВЕ12"))
    }

    @Test
    fun `invalid - too many digits in number`() {
        assertFalse(LicensePlateValidator.isValid("А1234ВЕ12"))
    }

    @Test
    fun `invalid - 1-digit region`() {
        assertFalse(LicensePlateValidator.isValid("А123ВЕ1"))
    }

    @Test
    fun `invalid - 4-digit region`() {
        assertFalse(LicensePlateValidator.isValid("А123ВЕ1234"))
    }

    @Test
    fun `invalid - one letter after digits`() {
        assertFalse(LicensePlateValidator.isValid("А123В12"))
    }

    @Test
    fun `invalid - three letters after digits`() {
        assertFalse(LicensePlateValidator.isValid("А123ВЕК12"))
    }

    @Test
    fun `invalid - missing first letter`() {
        assertFalse(LicensePlateValidator.isValid("123ВЕ12"))
    }

    @Test
    fun `invalid - empty string`() {
        assertFalse(LicensePlateValidator.isValid(""))
    }

    @Test
    fun `invalid - blank string`() {
        assertFalse(LicensePlateValidator.isValid("   "))
    }

    @Test
    fun `invalid - spaces in plate`() {
        assertFalse(LicensePlateValidator.isValid("А 123 ВЕ 12"))
    }

    @Test
    fun `invalid - special characters`() {
        assertFalse(LicensePlateValidator.isValid("А123-ВЕ12"))
    }

    @Test
    fun `invalid - random text`() {
        assertFalse(LicensePlateValidator.isValid("some random text"))
    }

    // ===== filterInput tests =====

    @Test
    fun `filterInput removes disallowed characters`() {
        assertEquals("А123ВЕ12", LicensePlateValidator.filterInput("А123ВЕ12"))
    }

    @Test
    fun `filterInput converts lowercase to uppercase`() {
        assertEquals("А123ВЕ12", LicensePlateValidator.filterInput("а123ве12"))
    }

    @Test
    fun `filterInput removes Latin characters`() {
        assertEquals("", LicensePlateValidator.filterInput("ABC"))
    }

    @Test
    fun `filterInput removes disallowed Cyrillic`() {
        assertEquals("А", LicensePlateValidator.filterInput("АБГ"))
    }

    @Test
    fun `filterInput keeps digits`() {
        assertEquals("123", LicensePlateValidator.filterInput("123"))
    }

    @Test
    fun `filterInput removes spaces and special chars`() {
        assertEquals("А123ВЕ12", LicensePlateValidator.filterInput("А 123-ВЕ.12"))
    }

    @Test
    fun `filterInput truncates to max length 9`() {
        assertEquals("А123ВЕ123", LicensePlateValidator.filterInput("А123ВЕ1234567"))
    }

    @Test
    fun `filterInput handles empty string`() {
        assertEquals("", LicensePlateValidator.filterInput(""))
    }

    @Test
    fun `filterInput mixed valid and invalid`() {
        assertEquals("К128СТ78", LicensePlateValidator.filterInput("К1Z2L8СТ78"))
    }

    // ===== errorMessage test =====

    @Test
    fun `error message is not empty`() {
        assertTrue(LicensePlateValidator.ERROR_MESSAGE.isNotBlank())
    }

    // ===== MAX_LENGTH test =====

    @Test
    fun `max length is 9`() {
        assertEquals(9, LicensePlateValidator.MAX_LENGTH)
    }
}
