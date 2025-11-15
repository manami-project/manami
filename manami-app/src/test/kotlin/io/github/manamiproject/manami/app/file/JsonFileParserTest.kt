package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.LinkEntry
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.versioning.SemanticVersion
import io.github.manamiproject.manami.app.versioning.TestVersionProvider
import io.github.manamiproject.manami.app.versioning.VersionProvider
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.FALL
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeType
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.anime.Episodes
import io.github.manamiproject.modb.core.anime.Title
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createFile

internal class JsonFileParserTest {

    @Test
    fun `handles JSON files`() {
        // given
        val parser = JsonFileParser(
            cache = TestAnimeCache,
            versionProvider = TestVersionProvider,
        )

        // when
        val result = parser.handlesSuffix()

        // then
        assertThat(result).isEqualTo("json")
    }

    @Test
    fun `throws exception if the given path is a directory`() {
        tempDirectory {
            // given
            val parser = JsonFileParser(
                cache = TestAnimeCache,
                versionProvider = TestVersionProvider,
            )

            // when
            val result = assertThrows<IllegalArgumentException> {
                parser.parse(tempDir)
            }

            // then
            assertThat(result).hasMessage("Given path [${tempDir.toAbsolutePath()}] is either not a file or doesn't exist.")
        }
    }

    @Test
    fun `throws exception if the given path does not exist`() {
        tempDirectory {
            // given
            val parser = JsonFileParser(
                cache = TestAnimeCache,
                versionProvider = TestVersionProvider,
            )

            val file = tempDir.resolve("test.json")

            // when
            val result = assertThrows<IllegalArgumentException> {
                parser.parse(file)
            }

            // then
            assertThat(result).hasMessage("Given path [$file] is either not a file or doesn't exist.")
        }
    }

    @Test
    fun `throws exception if the given suffix is not supported`() {
        tempDirectory {
            // given
            val parser = JsonFileParser(
                cache = TestAnimeCache,
                versionProvider = TestVersionProvider,
            )

            val file = tempDir.resolve("test.xml").createFile()

            // when
            val result = assertThrows<IllegalArgumentException> {
                parser.parse(file)
            }

            // then
            assertThat(result).hasMessage("Parser doesn't support given file suffix.")
        }
    }

    @Test
    fun `throws exception if you try to open a file created with a newer version of manami in an older version`() {
        tempDirectory {
            // given
            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.0.0")
            }

            val parser = JsonFileParser(
                cache = TestAnimeCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "4.1.0",
                    "animeListEntries": [],
                    "watchListEntries": [],
                    "ignoreListEntries": []
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = assertThrows<IllegalArgumentException> {
                parser.parse(file)
            }

            // then
            assertThat(result).hasMessage("Cannot open a file created with version [4.1.0] in manami [4.0.0]")
        }
    }

    @Test
    fun `throws exception if you try to open a file that has been created with a manami version prior to v3`() {
        tempDirectory {
            // given
            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.0.0")
            }

            val parser = JsonFileParser(
                cache = TestAnimeCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "2.5.4",
                    "animeListEntries": [],
                    "watchListEntries": [],
                    "ignoreListEntries": []
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = assertThrows<IllegalArgumentException> {
                parser.parse(file)
            }

            // then
            assertThat(result).hasMessage("Unable to parse manami file older than 3.0.0")
        }
    }

    @Test
    fun `correctly parses empty file with same version`() {
        tempDirectory {
            // given
            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.0.0")
            }

            val parser = JsonFileParser(
                cache = TestAnimeCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "4.0.0",
                    "animeListEntries": [],
                    "watchListEntries": [],
                    "ignoreListEntries": []
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = parser.parse(file)

            // then
            assertThat(result.version).isEqualTo(SemanticVersion("4.0.0"))
            assertThat(result.animeListEntries).isEmpty()
            assertThat(result.watchListEntries).isEmpty()
            assertThat(result.ignoreListEntries).isEmpty()
        }
    }

    @Test
    fun `correctly parses empty file with where the tool has a newer version than the file`() {
        tempDirectory {
            // given
            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.1.0")
            }

            val parser = JsonFileParser(
                cache = TestAnimeCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "4.0.0",
                    "animeListEntries": [],
                    "watchListEntries": [],
                    "ignoreListEntries": []
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = parser.parse(file)

            // then
            assertThat(result.version).isEqualTo(SemanticVersion("4.0.0"))
            assertThat(result.animeListEntries).isEmpty()
            assertThat(result.watchListEntries).isEmpty()
            assertThat(result.ignoreListEntries).isEmpty()
        }
    }

    @Test
    fun `removes dead entries from watchList`() {
        tempDirectory {
            // given
            val testCache = object: AnimeCache by TestAnimeCache {
                override suspend fun fetch(key: URI): CacheEntry<Anime> = DeadEntry()
            }

            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.0.0")
            }

            val parser = JsonFileParser(
                cache = testCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "4.0.0",
                    "animeListEntries": [],
                    "watchListEntries": [
                        "https://myanimelist.com/anime/10001"
                    ],
                    "ignoreListEntries": []
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = parser.parse(file)

            // then
            assertThat(result.version).isEqualTo(SemanticVersion("4.0.0"))
            assertThat(result.animeListEntries).isEmpty()
            assertThat(result.watchListEntries).isEmpty()
            assertThat(result.ignoreListEntries).isEmpty()
        }
    }

    @Test
    fun `removes dead entries from ignoreList`() {
        tempDirectory {
            // given
            val testCache = object: AnimeCache by TestAnimeCache {
                override suspend fun fetch(key: URI): CacheEntry<Anime> = DeadEntry()
            }

            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.0.0")
            }

            val parser = JsonFileParser(
                cache = testCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "4.0.0",
                    "animeListEntries": [],
                    "watchListEntries": [],
                    "ignoreListEntries": [
                        "https://myanimelist.com/anime/10001"
                    ]
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = parser.parse(file)

            // then
            assertThat(result.version).isEqualTo(SemanticVersion("4.0.0"))
            assertThat(result.animeListEntries).isEmpty()
            assertThat(result.watchListEntries).isEmpty()
            assertThat(result.ignoreListEntries).isEmpty()
        }
    }

    @Test
    fun `retrieves all data for the URI in watchListEntries and therefore updates the entries`() {
        tempDirectory {
            // given
            val anime = Anime(
                title = "Death Note",
                type = TV,
                episodes = 37,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2006,
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/9/9453.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg"),
                sources = hashSetOf(
                    URI("https://anidb.net/anime/4563"),
                    URI("https://anilist.co/anime/1535"),
                    URI("https://anime-planet.com/anime/death-note"),
                    URI("https://kitsu.app/anime/1376"),
                    URI("https://myanimelist.net/anime/1535"),
                    URI("https://notify.moe/anime/0-A-5Fimg"),
                ),
                synonyms = hashSetOf(
                    "DEATH NOTE",
                    "DN",
                    "Death Note - A halállista",
                    "Death Note - Carnetul morţii",
                    "Death Note - Zápisník smrti",
                    "Notatnik śmierci",
                    "Τετράδιο Θανάτου",
                    "Бележник на Смъртта",
                    "Тетрадь cмерти",
                    "Үхлийн Тэмдэглэл",
                    "دفترچه یادداشت مرگ",
                    "كـتـاب الـموت",
                    "डेथ नोट",
                    "ですのーと",
                    "デスノート",
                    "死亡笔记",
                    "데스노트",
                ),
                tags = hashSetOf(
                    "alternative present",
                    "amnesia",
                    "anti-hero",
                    "asia",
                    "based on a manga",
                    "contemporary fantasy",
                    "cops",
                    "crime",
                    "criminals",
                    "demons",
                    "detective",
                    "detectives",
                    "drama",
                    "earth",
                    "espionage",
                    "gods",
                    "japan",
                    "male protagonist",
                    "manga",
                    "mind games",
                    "mystery",
                    "overpowered main characters",
                    "philosophy",
                    "plot continuity",
                    "police",
                    "present",
                    "primarily adult cast",
                    "primarily male cast",
                    "psychological",
                    "psychopaths",
                    "revenge",
                    "rivalries",
                    "secret identity",
                    "serial killers",
                    "shinigami",
                    "shounen",
                    "supernatural",
                    "thriller",
                    "time skip",
                    "tragedy",
                    "urban",
                    "urban fantasy",
                    "vigilantes",
                    "work",
                ),
            )

            val testWatchListEntry = WatchListEntry(anime)

            val testCache = object: AnimeCache by TestAnimeCache {
                override suspend fun fetch(key: URI): CacheEntry<Anime> {
                    return when(key) {
                        anime.sources.first() -> PresentValue(anime)
                        else -> shouldNotBeInvoked()
                    }
                }
            }

            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.0.0")
            }

            val parser = JsonFileParser(
                cache = testCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "4.0.0",
                    "animeListEntries": [],
                    "watchListEntries": [
                        "${anime.sources.first()}"
                    ],
                    "ignoreListEntries": []
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = parser.parse(file)

            // then
            assertThat(result.version).isEqualTo(SemanticVersion("4.0.0"))
            assertThat(result.animeListEntries).isEmpty()
            assertThat(result.watchListEntries).containsExactly(
                testWatchListEntry,
            )
            assertThat(result.ignoreListEntries).isEmpty()
        }
    }

    @Test
    fun `retrieves all data for the URI in ignoreListEntries and therefore updates the entries`() {
        tempDirectory {
            // given
            val anime = Anime(
                title = "Death Note",
                type = TV,
                episodes = 37,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2006,
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/9/9453.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg"),
                sources = hashSetOf(
                    URI("https://anidb.net/anime/4563"),
                    URI("https://anilist.co/anime/1535"),
                    URI("https://anime-planet.com/anime/death-note"),
                    URI("https://kitsu.app/anime/1376"),
                    URI("https://myanimelist.net/anime/1535"),
                    URI("https://notify.moe/anime/0-A-5Fimg"),
                ),
                synonyms = hashSetOf(
                    "DEATH NOTE",
                    "DN",
                    "Death Note - A halállista",
                    "Death Note - Carnetul morţii",
                    "Death Note - Zápisník smrti",
                    "Notatnik śmierci",
                    "Τετράδιο Θανάτου",
                    "Бележник на Смъртта",
                    "Тетрадь cмерти",
                    "Үхлийн Тэмдэглэл",
                    "دفترچه یادداشت مرگ",
                    "كـتـاب الـموت",
                    "डेथ नोट",
                    "ですのーと",
                    "デスノート",
                    "死亡笔记",
                    "데스노트",
                ),
                tags = hashSetOf(
                    "alternative present",
                    "amnesia",
                    "anti-hero",
                    "asia",
                    "based on a manga",
                    "contemporary fantasy",
                    "cops",
                    "crime",
                    "criminals",
                    "demons",
                    "detective",
                    "detectives",
                    "drama",
                    "earth",
                    "espionage",
                    "gods",
                    "japan",
                    "male protagonist",
                    "manga",
                    "mind games",
                    "mystery",
                    "overpowered main characters",
                    "philosophy",
                    "plot continuity",
                    "police",
                    "present",
                    "primarily adult cast",
                    "primarily male cast",
                    "psychological",
                    "psychopaths",
                    "revenge",
                    "rivalries",
                    "secret identity",
                    "serial killers",
                    "shinigami",
                    "shounen",
                    "supernatural",
                    "thriller",
                    "time skip",
                    "tragedy",
                    "urban",
                    "urban fantasy",
                    "vigilantes",
                    "work",
                ),
            )

            val testIgnoreListEntry = IgnoreListEntry(anime)

            val testCache = object: AnimeCache by TestAnimeCache {
                override suspend fun fetch(key: URI): CacheEntry<Anime> {
                    return when(key) {
                        anime.sources.first() -> PresentValue(anime)
                        else -> shouldNotBeInvoked()
                    }
                }
            }

            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.0.0")
            }

            val parser = JsonFileParser(
                cache = testCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "4.0.0",
                    "animeListEntries": [],
                    "watchListEntries": [],
                    "ignoreListEntries": [
                        "${anime.sources.first()}"
                    ]
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = parser.parse(file)

            // then
            assertThat(result.version).isEqualTo(SemanticVersion("4.0.0"))
            assertThat(result.animeListEntries).isEmpty()
            assertThat(result.watchListEntries).isEmpty()
            assertThat(result.ignoreListEntries).containsExactly(
                testIgnoreListEntry,
            )
        }
    }

    @Test
    fun `migrates NO_PICTURE_THUMBNAIL to NO_PICTURE for animeListEntries if they don't have a link`() {
        tempDirectory {
            // given
            val testAnimeListEntry = AnimeListEntry(
                link = NoLink,
                title = "Death Note",
                thumbnail = NO_PICTURE,
                episodes = 37,
                type = TV,
                location = Path("./anime"),
            )

            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.0.0")
            }

            val parser = JsonFileParser(
                cache = TestAnimeCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "4.0.0",
                    "animeListEntries": [
                        {
                            "title": "Death Note",
                            "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                            "episodes": 37,
                            "type": "TV",
                            "location": "./anime"
                        }
                    ],
                    "watchListEntries": [],
                    "ignoreListEntries": []
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = parser.parse(file)

            // then
            assertThat(result.version).isEqualTo(SemanticVersion("4.0.0"))
            assertThat(result.animeListEntries).containsExactly(
                testAnimeListEntry,
            )
            assertThat(result.watchListEntries).isEmpty()
            assertThat(result.ignoreListEntries).isEmpty()
        }
    }

    @Test
    fun `doesn't migrate images for animeListEntries if they don't have a link and the image is neither NO_PICTURE nor NO_PICTURE_THUMBNAIL`() {
        tempDirectory {
            // given
            val testAnimeListEntry = AnimeListEntry(
                link = NoLink,
                title = "Death Note",
                thumbnail = URI("https://cdn.example.org/images/1.png"),
                episodes = 37,
                type = TV,
                location = Path("./anime"),
            )

            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.0.0")
            }

            val parser = JsonFileParser(
                cache = TestAnimeCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "4.0.0",
                    "animeListEntries": [
                        {
                            "title": "Death Note",
                            "thumbnail": "https://cdn.example.org/images/1.png",
                            "episodes": 37,
                            "type": "TV",
                            "location": "./anime"
                        }
                    ],
                    "watchListEntries": [],
                    "ignoreListEntries": []
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = parser.parse(file)

            // then
            assertThat(result.version).isEqualTo(SemanticVersion("4.0.0"))
            assertThat(result.animeListEntries).containsExactly(
                testAnimeListEntry,
            )
            assertThat(result.watchListEntries).isEmpty()
            assertThat(result.ignoreListEntries).isEmpty()
        }
    }

    @Test
    fun `migrates the thumbnail of every animeListEntries with a link to the respective picture from the cache of an entry is present`() {
        tempDirectory {
            // given
            val anime = Anime(
                title = "Death Note",
                type = TV,
                episodes = 37,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2006,
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/9/9453.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg"),
                sources = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                ),
                synonyms = hashSetOf(
                    "DEATH NOTE",
                    "DN",
                    "Death Note - A halállista",
                    "Death Note - Carnetul morţii",
                    "Death Note - Zápisník smrti",
                    "Notatnik śmierci",
                    "Τετράδιο Θανάτου",
                    "Бележник на Смъртта",
                    "Тетрадь cмерти",
                    "Үхлийн Тэмдэглэл",
                    "دفترچه یادداشت مرگ",
                    "كـتـاب الـموت",
                    "डेथ नोट",
                    "ですのーと",
                    "デスノート",
                    "死亡笔记",
                    "데스노트",
                ),
                tags = hashSetOf(
                    "alternative present",
                    "amnesia",
                    "anti-hero",
                    "asia",
                    "based on a manga",
                    "contemporary fantasy",
                    "cops",
                    "crime",
                    "criminals",
                    "demons",
                    "detective",
                    "detectives",
                    "drama",
                    "earth",
                    "espionage",
                    "gods",
                    "japan",
                    "male protagonist",
                    "manga",
                    "mind games",
                    "mystery",
                    "overpowered main characters",
                    "philosophy",
                    "plot continuity",
                    "police",
                    "present",
                    "primarily adult cast",
                    "primarily male cast",
                    "psychological",
                    "psychopaths",
                    "revenge",
                    "rivalries",
                    "secret identity",
                    "serial killers",
                    "shinigami",
                    "shounen",
                    "supernatural",
                    "thriller",
                    "time skip",
                    "tragedy",
                    "urban",
                    "urban fantasy",
                    "vigilantes",
                    "work",
                ),
            )

            val testAnimeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453.jpg"),
                episodes = 37,
                type = TV,
                location = Path("./anime"),
            )

            val testCache = object: AnimeCache by TestAnimeCache {
                override suspend fun fetch(key: URI): CacheEntry<Anime> {
                    return when(key) {
                        anime.sources.first() -> PresentValue(anime)
                        else -> shouldNotBeInvoked()
                    }
                }
            }

            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.0.0")
            }

            val parser = JsonFileParser(
                cache = testCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "4.0.0",
                    "animeListEntries": [
                        {
                            "link": "https://myanimelist.net/anime/1535",
                            "title": "Death Note",
                            "thumbnail": "https://cdn.example.org/images/1.png",
                            "episodes": 37,
                            "type": "TV",
                            "location": "./anime"
                        }
                    ],
                    "watchListEntries": [],
                    "ignoreListEntries": []
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = parser.parse(file)

            // then
            assertThat(result.version).isEqualTo(SemanticVersion("4.0.0"))
            assertThat(result.animeListEntries).containsExactly(
                testAnimeListEntry,
            )
            assertThat(result.watchListEntries).isEmpty()
            assertThat(result.ignoreListEntries).isEmpty()
        }
    }

    @Test
    fun `migrates the thumbnail of every animeListEntries with a link to NO_PICTURE of the respective entry in the cache is a DeadEntry`() {
        tempDirectory {
            // given
            val anime = Anime(
                title = "Death Note",
                type = TV,
                episodes = 37,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2006,
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/9/9453.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg"),
                sources = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                ),
                synonyms = hashSetOf(
                    "DEATH NOTE",
                    "DN",
                    "Death Note - A halállista",
                    "Death Note - Carnetul morţii",
                    "Death Note - Zápisník smrti",
                    "Notatnik śmierci",
                    "Τετράδιο Θανάτου",
                    "Бележник на Смъртта",
                    "Тетрадь cмерти",
                    "Үхлийн Тэмдэглэл",
                    "دفترچه یادداشت مرگ",
                    "كـتـاب الـموت",
                    "डेथ नोट",
                    "ですのーと",
                    "デスノート",
                    "死亡笔记",
                    "데스노트",
                ),
                tags = hashSetOf(
                    "alternative present",
                    "amnesia",
                    "anti-hero",
                    "asia",
                    "based on a manga",
                    "contemporary fantasy",
                    "cops",
                    "crime",
                    "criminals",
                    "demons",
                    "detective",
                    "detectives",
                    "drama",
                    "earth",
                    "espionage",
                    "gods",
                    "japan",
                    "male protagonist",
                    "manga",
                    "mind games",
                    "mystery",
                    "overpowered main characters",
                    "philosophy",
                    "plot continuity",
                    "police",
                    "present",
                    "primarily adult cast",
                    "primarily male cast",
                    "psychological",
                    "psychopaths",
                    "revenge",
                    "rivalries",
                    "secret identity",
                    "serial killers",
                    "shinigami",
                    "shounen",
                    "supernatural",
                    "thriller",
                    "time skip",
                    "tragedy",
                    "urban",
                    "urban fantasy",
                    "vigilantes",
                    "work",
                ),
            )

            val testAnimeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = NO_PICTURE,
                episodes = 37,
                type = TV,
                location = Path("./anime"),
            )

            val testCache = object: AnimeCache by TestAnimeCache {
                override suspend fun fetch(key: URI): CacheEntry<Anime> {
                    return when(key) {
                        anime.sources.first() -> DeadEntry()
                        else -> shouldNotBeInvoked()
                    }
                }
            }

            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.0.0")
            }

            val parser = JsonFileParser(
                cache = testCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "4.0.0",
                    "animeListEntries": [
                        {
                            "link": "https://myanimelist.net/anime/1535",
                            "title": "Death Note",
                            "thumbnail": "https://cdn.example.org/images/1.png",
                            "episodes": 37,
                            "type": "TV",
                            "location": "./anime"
                        }
                    ],
                    "watchListEntries": [],
                    "ignoreListEntries": []
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = parser.parse(file)

            // then
            assertThat(result.version).isEqualTo(SemanticVersion("4.0.0"))
            assertThat(result.animeListEntries).containsExactly(
                testAnimeListEntry,
            )
            assertThat(result.watchListEntries).isEmpty()
            assertThat(result.ignoreListEntries).isEmpty()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["other", "-1"])
    fun `throws exception if the value is not a valid Int or negative`(value: String) {
        tempDirectory {
            // given
            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("4.0.0")
            }

            val parser = JsonFileParser(
                cache = TestAnimeCache,
                versionProvider = testVersionProvider,
            )

            val file = tempDir.resolve("test.json").createFile()
            """
                {
                    "version": "4.0.0",
                    "animeListEntries": [
                        {
                            "title": "Death Note",
                            "thumbnail": "https://cdn.example.org/images/1.png",
                            "episodes": "$value",
                            "type": "TV",
                            "location": "./anime"
                        }
                    ],
                    "watchListEntries": [],
                    "ignoreListEntries": []
                }
            """.trimIndent().writeToFile(file)

            // when
            val result = exceptionExpected<IllegalStateException> {
                parser.parse(file)
            }

            // then
            assertThat(result).hasMessage("Episodes value [$value] for [Death Note - null] is either not numeric or invalid.")
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = JsonFileParser.instance

            // when
            val result = JsonFileParser.instance

            // then
            assertThat(result).isExactlyInstanceOf(JsonFileParser::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}