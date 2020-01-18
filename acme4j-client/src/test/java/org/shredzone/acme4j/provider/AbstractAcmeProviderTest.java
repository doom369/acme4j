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
package org.shredzone.acme4j.provider;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.shredzone.acme4j.toolbox.TestUtils.getJSON;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import org.shredzone.acme4j.challenge.TokenChallenge;
import org.shredzone.acme4j.connector.Connection;
import org.shredzone.acme4j.connector.DefaultConnection;
import org.shredzone.acme4j.connector.HttpConnector;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.exception.AcmeProtocolException;
import org.shredzone.acme4j.toolbox.JSON;
import org.shredzone.acme4j.toolbox.JSONBuilder;
import org.shredzone.acme4j.toolbox.TestUtils;

/**
 * Unit tests for {@link AbstractAcmeProvider}.
 */
public class AbstractAcmeProviderTest {

    /**
     * Test that connect returns a connection.
     */
    @Test
    public void testConnect() {
        final URI testServerUri = URI.create("http://example.com/acme");

        final AtomicBoolean invoked = new AtomicBoolean();

        AbstractAcmeProvider provider = new AbstractAcmeProvider() {
            @Override
            public boolean accepts(URI serverUri) {
                throw new UnsupportedOperationException();
            }

            @Override
            public URL resolve(URI serverUri) {
                throw new UnsupportedOperationException();
            }

            @Override
            protected HttpConnector createHttpConnector() {
                invoked.set(true);
                return super.createHttpConnector();
            }
        };

        Connection connection = provider.connect(testServerUri);
        assertThat(connection, not(nullValue()));
        assertThat(connection, instanceOf(DefaultConnection.class));
        assertThat(invoked.get(), is(true));
    }

    /**
     * Verify that the resources directory is read.
     */
    @Test
    public void testResources() throws AcmeException {
        final URI testServerUri = URI.create("http://example.com/acme");
        final URL testResolvedUrl = TestUtils.url("http://example.com/acme/directory");
        final Connection connection = mock(Connection.class);
        final Session session = mock(Session.class);

        when(connection.readJsonResponse()).thenReturn(getJSON("directory"));

        AbstractAcmeProvider provider = new AbstractAcmeProvider() {
            @Override
            public Connection connect(URI serverUri) {
                assertThat(serverUri, is(testServerUri));
                return connection;
            }

            @Override
            public boolean accepts(URI serverUri) {
                assertThat(serverUri, is(testServerUri));
                return true;
            }

            @Override
            public URL resolve(URI serverUri) {
                assertThat(serverUri, is(testServerUri));
                return testResolvedUrl;
            }
        };

        JSON map = provider.directory(session, testServerUri);
        assertThat(map.toString(), sameJSONAs(TestUtils.getJSON("directory").toString()));

        verify(connection).sendRequest(testResolvedUrl, session);
        verify(connection).getNonce();
        verify(connection).readJsonResponse();
        verify(connection).close();
        verifyNoMoreInteractions(connection);
    }

    /**
     * Test that challenges are generated properly.
     */
    @Test
    public void testCreateChallenge() {
        Login login = mock(Login.class);

        AbstractAcmeProvider provider = new AbstractAcmeProvider() {
            @Override
            public boolean accepts(URI serverUri) {
                throw new UnsupportedOperationException();
            }

            @Override
            public URL resolve(URI serverUri) {
                throw new UnsupportedOperationException();
            }
        };

        Challenge c1 = provider.createChallenge(login, getJSON("httpChallenge"));
        assertThat(c1, not(nullValue()));
        assertThat(c1, instanceOf(Http01Challenge.class));

        Challenge c2 = provider.createChallenge(login, getJSON("httpChallenge"));
        assertThat(c2, not(sameInstance(c1)));

        Challenge c3 = provider.createChallenge(login, getJSON("dnsChallenge"));
        assertThat(c3, not(nullValue()));
        assertThat(c3, instanceOf(Dns01Challenge.class));

        Challenge c4 = provider.createChallenge(login, getJSON("tlsAlpnChallenge"));
        assertThat(c4, not(nullValue()));
        assertThat(c4, instanceOf(TlsAlpn01Challenge.class));

        JSON json6 = new JSONBuilder()
                    .put("type", "foobar-01")
                    .put("url", "https://example.com/some/challenge")
                    .toJSON();
        Challenge c6 = provider.createChallenge(login, json6);
        assertThat(c6, not(nullValue()));
        assertThat(c6, instanceOf(Challenge.class));

        JSON json7 = new JSONBuilder()
                        .put("type", "foobar-01")
                        .put("token", "abc123")
                        .put("url", "https://example.com/some/challenge")
                        .toJSON();
        Challenge c7 = provider.createChallenge(login, json7);
        assertThat(c7, not(nullValue()));
        assertThat(c7, instanceOf(TokenChallenge.class));

        try {
            JSON json8 = new JSONBuilder()
                        .put("url", "https://example.com/some/challenge")
                        .toJSON();
            provider.createChallenge(login, json8);
            fail("Challenge without type was accepted");
        } catch (AcmeProtocolException ex) {
            // expected
        }

        try {
            provider.createChallenge(login, null);
            fail("null was accepted");
        } catch (NullPointerException ex) {
            // expected
        }
    }

}
