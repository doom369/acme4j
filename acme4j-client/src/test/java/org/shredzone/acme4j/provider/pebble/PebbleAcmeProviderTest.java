/*
 * acme4j - Java ACME client
 *
 * Copyright (C) 2017 Richard "Shred" Körber
 *   http://acme4j.shredzone.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.acme4j.provider.pebble;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.shredzone.acme4j.toolbox.TestUtils.url;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

/**
 * Unit tests for {@link PebbleAcmeProvider}.
 */
public class PebbleAcmeProviderTest {

    /**
     * Tests if the provider accepts the correct URIs.
     */
    @Test
    public void testAccepts() throws URISyntaxException {
        PebbleAcmeProvider provider = new PebbleAcmeProvider();

        assertThat(provider.accepts(new URI("acme://pebble")), is(true));
        assertThat(provider.accepts(new URI("acme://pebble/")), is(true));
        assertThat(provider.accepts(new URI("acme://pebble/some-host.example.com")), is(true));
        assertThat(provider.accepts(new URI("acme://pebble/some-host.example.com:12345")), is(true));
        assertThat(provider.accepts(new URI("acme://example.com")), is(false));
        assertThat(provider.accepts(new URI("http://example.com/acme")), is(false));
        assertThat(provider.accepts(new URI("https://example.com/acme")), is(false));
    }

    /**
     * Test if acme URIs are properly resolved.
     */
    @Test
    public void testResolve() throws URISyntaxException {
        PebbleAcmeProvider provider = new PebbleAcmeProvider();

        assertThat(provider.resolve(new URI("acme://pebble")),
                        is(url("https://localhost:14000/dir")));
        assertThat(provider.resolve(new URI("acme://pebble/")),
                        is(url("https://localhost:14000/dir")));
        assertThat(provider.resolve(new URI("acme://pebble/pebble.example.com")),
                        is(url("https://pebble.example.com:14000/dir")));
        assertThat(provider.resolve(new URI("acme://pebble/pebble.example.com:12345")),
                        is(url("https://pebble.example.com:12345/dir")));
        assertThat(provider.resolve(new URI("acme://pebble/pebble.example.com:12345/")),
                        is(url("https://pebble.example.com:12345/dir")));

        try {
            provider.resolve(new URI("acme://pebble/bad.example.com:port"));
            fail("accepted bad port");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            provider.resolve(new URI("acme://pebble/bad.example.com:1234/foo"));
            fail("accepted invalid path");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

}
