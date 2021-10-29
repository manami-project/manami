package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.versioning.SemanticVersion
import io.github.manamiproject.modb.test.testResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import java.nio.file.Paths
import javax.xml.parsers.SAXParserFactory
import kotlin.io.path.inputStream

internal class ManamiVersionHandlerTest {

    @Test
    fun `successfully pase version from manami file`() {
        // given
        val versionHandler = ManamiVersionHandler()
        val file = testResource("file/FileParser/correctly_parse_entries.xml")

        val saxParser = SAXParserFactory.newInstance().apply { isValidating = true }.newSAXParser()

        val entityResolver = EntityResolver { _, systemId ->
            val fileName = Paths.get(systemId).fileName
            InputSource(file.parent.resolve(fileName).toString())
        }
        versionHandler.entityResolver = entityResolver

        // when
        saxParser.parse(file.inputStream(), versionHandler)

        // then
        assertThat(versionHandler.version).isEqualTo(SemanticVersion("3.0.0"))
    }
}