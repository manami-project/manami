package io.github.manami.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class AnimeTypeTest {

    @Test
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
        assertThat(defaultStringType, equalTo(null));
        assertThat(upperCaseStringType, equalTo(null));
        assertThat(lowerCaseStringType, equalTo(null));
    }


    @Test
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
        assertThat(defaultStringType, equalTo(AnimeType.TV));
        assertThat(upperCaseStringType, equalTo(AnimeType.TV));
        assertThat(lowerCaseStringType, equalTo(AnimeType.TV));
    }


    @Test
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
        assertThat(defaultStringType, equalTo(AnimeType.MOVIE));
        assertThat(upperCaseStringType, equalTo(AnimeType.MOVIE));
        assertThat(lowerCaseStringType, equalTo(AnimeType.MOVIE));
    }


    @Test
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
        assertThat(defaultStringType, equalTo(AnimeType.OVA));
        assertThat(upperCaseStringType, equalTo(AnimeType.OVA));
        assertThat(lowerCaseStringType, equalTo(AnimeType.OVA));
    }


    @Test
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
        assertThat(defaultStringType, equalTo(AnimeType.SPECIAL));
        assertThat(upperCaseStringType, equalTo(AnimeType.SPECIAL));
        assertThat(lowerCaseStringType, equalTo(AnimeType.SPECIAL));
    }


    @Test
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
        assertThat(defaultStringType, equalTo(AnimeType.ONA));
        assertThat(upperCaseStringType, equalTo(AnimeType.ONA));
        assertThat(lowerCaseStringType, equalTo(AnimeType.ONA));
    }


    @Test
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
        assertThat(defaultStringType, equalTo(AnimeType.MUSIC));
        assertThat(upperCaseStringType, equalTo(AnimeType.MUSIC));
        assertThat(lowerCaseStringType, equalTo(AnimeType.MUSIC));
    }
}
