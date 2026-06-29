package org.linphone

import org.junit.Assert.fail
import org.junit.Test
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * KID-262 — Branding-guard regression test.
 *
 * Scans every res/values*/strings.xml file and asserts that no string *value*
 * contains upstream Linphone / Belledonne branding text.
 *
 * Allowlisted string IDs (legitimate exceptions):
 *   - help_about_open_source_licenses_subtitle  — GPL-3.0 attribution; must
 *     retain "Belledonne Communications" per project ruling #6 / KID-268.
 */
class BrandingGuardTest {

    companion object {
        /** Patterns that must NOT appear in any non-allowlisted string value. */
        private val BANNED_PATTERNS = listOf(
            Regex("Belledonne", RegexOption.IGNORE_CASE),
            Regex("Linphone", RegexOption.IGNORE_CASE),
            Regex("linphone\\.org", RegexOption.IGNORE_CASE)
        )

        /**
         * String resource IDs whose values are explicitly permitted to contain
         * upstream branding (e.g. GPL attribution notices).
         */
        private val ALLOWLISTED_IDS = setOf(
            "help_about_open_source_licenses_subtitle"
        )
    }

    @Test
    fun `no non-allowlisted string value contains upstream Linphone or Belledonne branding`() {
        // Locate the module root relative to the working directory used by
        // Gradle unit tests (project root when run via ./gradlew test).
        val moduleRoot = File("app/src/main/res")
            .takeIf { it.isDirectory }
            ?: File("src/main/res")

        check(moduleRoot.isDirectory) {
            "Cannot locate res/ directory from working dir: ${File(".").absolutePath}"
        }

        val stringsFiles = moduleRoot.walkTopDown()
            .filter { it.name == "strings.xml" && it.parentFile.name.startsWith("values") }
            .toList()

        check(stringsFiles.isNotEmpty()) {
            "No strings.xml files found under $moduleRoot"
        }

        val violations = mutableListOf<String>()

        val dbf = DocumentBuilderFactory.newInstance()
        for (file in stringsFiles) {
            val doc = dbf.newDocumentBuilder().parse(file)
            doc.documentElement.normalize()

            val strings: NodeList = doc.getElementsByTagName("string")
            for (i in 0 until strings.length) {
                val element = strings.item(i) as Element
                val id = element.getAttribute("name")
                val value = element.textContent

                if (id in ALLOWLISTED_IDS) continue

                for (pattern in BANNED_PATTERNS) {
                    if (pattern.containsMatchIn(value)) {
                        violations += "[${file.relativeTo(moduleRoot.parentFile.parentFile)}] " +
                            "id=\"$id\" contains banned pattern /${pattern.pattern}/: \"$value\""
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            fail(
                "Upstream branding found in string resources (${violations.size} violation(s)):\n" +
                    violations.joinToString("\n") { "  • $it" }
            )
        }
    }
}
