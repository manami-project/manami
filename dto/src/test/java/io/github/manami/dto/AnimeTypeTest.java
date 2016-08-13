package io.github.manami.dto;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class AnimeTypeTest {

    @Test(groups = "unitTest")
    public void testUnknown() {
        // given
        final String defaultString = "akjbfJKdsd";
        final String upperCase = defaultString.toUpperCase();
        final String lowerCase = defaultString.toLowerCase();

        // when
        final AnimeType defaultStringType = AnimeType.findByName(defaultString);
        final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
        final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

        // then
        assertEquals(defaultStringType, null);
        assertEquals(upperCaseStringType, null);
        assertEquals(lowerCaseStringType, null);
    }


    @Test(groups = "unitTest")
    public void testTv() {
        // given
        final String defaultString = "Tv";
        final String upperCase = defaultString.toUpperCase();
        final String lowerCase = defaultString.toLowerCase();

        // when
        final AnimeType defaultStringType = AnimeType.findByName(defaultString);
        final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
        final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

        // then
        assertEquals(defaultStringType, AnimeType.TV);
        assertEquals(upperCaseStringType, AnimeType.TV);
        assertEquals(lowerCaseStringType, AnimeType.TV);
    }


    @Test(groups = "unitTest")
    public void testMovie() {
        // given
        final String defaultString = "Movie";
        final String upperCase = defaultString.toUpperCase();
        final String lowerCase = defaultString.toLowerCase();

        // when
        final AnimeType defaultStringType = AnimeType.findByName(defaultString);
        final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
        final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

        // then
        assertEquals(defaultStringType, AnimeType.MOVIE);
        assertEquals(upperCaseStringType, AnimeType.MOVIE);
        assertEquals(lowerCaseStringType, AnimeType.MOVIE);
    }


    @Test(groups = "unitTest")
    public void testOva() {
        // given
        final String defaultString = "Ova";
        final String upperCase = defaultString.toUpperCase();
        final String lowerCase = defaultString.toLowerCase();

        // when
        final AnimeType defaultStringType = AnimeType.findByName(defaultString);
        final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
        final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

        // then
        assertEquals(defaultStringType, AnimeType.OVA);
        assertEquals(upperCaseStringType, AnimeType.OVA);
        assertEquals(lowerCaseStringType, AnimeType.OVA);
    }


    @Test(groups = "unitTest")
    public void testSpecial() {
        // given
        final String defaultString = "Special";
        final String upperCase = defaultString.toUpperCase();
        final String lowerCase = defaultString.toLowerCase();

        // when
        final AnimeType defaultStringType = AnimeType.findByName(defaultString);
        final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
        final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

        // then
        assertEquals(defaultStringType, AnimeType.SPECIAL);
        assertEquals(upperCaseStringType, AnimeType.SPECIAL);
        assertEquals(lowerCaseStringType, AnimeType.SPECIAL);
    }


    @Test(groups = "unitTest")
    public void testOna() {
        // given
        final String defaultString = "Ona";
        final String upperCase = defaultString.toUpperCase();
        final String lowerCase = defaultString.toLowerCase();

        // when
        final AnimeType defaultStringType = AnimeType.findByName(defaultString);
        final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
        final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

        // then
        assertEquals(defaultStringType, AnimeType.ONA);
        assertEquals(upperCaseStringType, AnimeType.ONA);
        assertEquals(lowerCaseStringType, AnimeType.ONA);
    }


    @Test(groups = "unitTest")
    public void testMusic() {
        // given
        final String defaultString = "Music";
        final String upperCase = defaultString.toUpperCase();
        final String lowerCase = defaultString.toLowerCase();

        // when
        final AnimeType defaultStringType = AnimeType.findByName(defaultString);
        final AnimeType upperCaseStringType = AnimeType.findByName(upperCase);
        final AnimeType lowerCaseStringType = AnimeType.findByName(lowerCase);

        // then
        assertEquals(defaultStringType, AnimeType.MUSIC);
        assertEquals(upperCaseStringType, AnimeType.MUSIC);
        assertEquals(lowerCaseStringType, AnimeType.MUSIC);
    }
}
