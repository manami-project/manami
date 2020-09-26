package io.github.manamiproject.manami.app.fileimport

import io.github.manamiproject.manami.app.fileimport.parser.Parser
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.extensions.createFile
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Paths

internal class DefaultImportHandlerTest {

    @Nested
    inner class ConstructorTests {

        @Test
        fun `throws exception if the list of parsers is empty`() {
            // when
            val result = assertThrows<IllegalArgumentException> {
                DefaultImportHandler(emptyList())
            }

            // then
            assertThat(result).hasMessage("List of parser must not be empty")
        }

        @Test
        fun `throws exception if there is more than one parser handling a file suffix`() {
            // given
            val json1 = object: Parser by TestParser {
                override fun handlesSuffix(): FileSuffix = "json"
            }
            val json2 = object: Parser by TestParser {
                override fun handlesSuffix(): FileSuffix = "json"
            }

            // when
            val result = assertThrows<IllegalArgumentException> {
                DefaultImportHandler(listOf(json1, json2))
            }

            // then
            assertThat(result).hasMessage("Only one parser per file suffix")
        }
    }

    @Nested
    inner class ImportTests {

        @Test
        fun `throws exception if the given file doesn't exist`() {
            // given
            val testParser = object: Parser by TestParser {
                override fun handlesSuffix(): FileSuffix = "json"
            }

            val nonExistentFile = Paths.get("non-existent-file.json")
            val importHandler = DefaultImportHandler(listOf(testParser))

            // when
            val result = assertThrows<IllegalArgumentException> {
                importHandler.import(nonExistentFile)
            }

            // then
            assertThat(result).hasMessage("Given path doesn't exist or is not a file [${nonExistentFile.toAbsolutePath()}]")
        }

        @Test
        fun `throws exception if the given path is not a file`() {
            tempDirectory {
                // given
                val testParser = object: Parser by TestParser {
                    override fun handlesSuffix(): FileSuffix = "json"
                }

                val importHandler = DefaultImportHandler(listOf(testParser))

                // when
                val result = assertThrows<IllegalArgumentException> {
                    importHandler.import(tempDir)
                }

                // then
                assertThat(result).hasMessage("Given path doesn't exist or is not a file [${tempDir.toAbsolutePath()}]")
            }
        }

        @Test
        fun `throws exception if the file's suffix is not supported`() {
            tempDirectory {
                // given
                val testParser = object: Parser by TestParser {
                    override fun handlesSuffix(): FileSuffix = "json"
                }

                val fileWithUnsupportedFileSuffix = tempDir.resolve("test.abc").createFile()
                val importHandler = DefaultImportHandler(listOf(testParser))

                // when
                val result = assertThrows<IllegalArgumentException> {
                    importHandler.import(fileWithUnsupportedFileSuffix)
                }

                // then
                assertThat(result).hasMessage("No suitable parser for file type [abc]")
            }
        }
    }
}