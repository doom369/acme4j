/*
 * acme4j - Java ACME client
 *
 * Copyright (C) 2015 Richard "Shred" Körber
 *   http://acme4j.shredzone.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.acme4j.connector;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for {@link HttpConnector}.
 */
public class HttpConnectorTest {

    /**
     * Test if a HTTP connection can be opened.
     * <p>
     * This is just a mock to check that the parameters are properly set.
     */
    @Test
    public void testMockOpenConnection() throws IOException {
        NetworkSettings settings = new NetworkSettings();
        settings.setTimeout(Duration.ofSeconds(50));

        HttpURLConnection conn = mock(HttpURLConnection.class);

        HttpConnector connector = new HttpConnector();
        connector.configure(conn, settings);

        verify(conn).setConnectTimeout(50000);
        verify(conn).setReadTimeout(50000);
        verify(conn).setUseCaches(false);
        verify(conn).setRequestProperty("User-Agent", HttpConnector.defaultUserAgent());
    }

    /**
     * Test if a HTTP connection can be opened.
     * <p>
     * This test requires a network connection. It should be excluded from automated
     * builds.
     */
    @Test
    @Category(HttpURLConnection.class)
    public void testOpenConnection() throws IOException {
        NetworkSettings settings = new NetworkSettings();
        HttpConnector connector = new HttpConnector();
        HttpURLConnection conn = connector.openConnection(new URL("http://example.com"), settings);
        assertThat(conn, not(nullValue()));
        conn.connect();
        assertThat(conn.getResponseCode(), is(HttpURLConnection.HTTP_OK));
    }

    /**
     * Tests that the user agent is correct.
     */
    @Test
    public void testUserAgent() {
        String userAgent = HttpConnector.defaultUserAgent();
        assertThat(userAgent.contains("acme4j/"), is(true));
        assertThat(userAgent.contains("Java/"), is(true));
    }

}
