package my.drivebit.utils

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class PublicOfferFileTest {
    @Test
    fun projectOfferTxtIsValid() {
        val offerText = locateOfferText()
        assertTrue(isPublicOfferValid(offerText), validatePublicOffer(offerText).joinToString())
        assertTrue(offerText.contains(PublicOfferMetadata.REVISION_DATE))
    }

    private fun locateOfferText(): String {
        val moduleDir = File(System.getProperty("user.dir"))
        val candidates =
            listOf(
                moduleDir.resolve("composeApp/src/jsMain/resources/offer.txt"),
                moduleDir.resolve("../composeApp/src/jsMain/resources/offer.txt"),
                moduleDir.resolve("../../composeApp/src/jsMain/resources/offer.txt"),
                moduleDir.parentFile?.resolve("composeApp/src/jsMain/resources/offer.txt"),
            ).filterNotNull()

        for (file in candidates) {
            if (file.isFile) {
                return file.readText()
            }
        }
        error("offer.txt not found; checked: ${candidates.joinToString { it.path }}")
    }
}
