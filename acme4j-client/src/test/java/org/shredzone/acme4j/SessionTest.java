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
package org.shredzone.acme4j;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.shredzone.acme4j.toolbox.TestUtils.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URL;
import java.security.KeyPair;
import java.time.Instant;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.shredzone.acme4j.connector.Resource;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.provider.AcmeProvider;
import org.shredzone.acme4j.toolbox.TestUtils;

/**
 * Unit test for {@link Session}.
 */
public class SessionTest {

    /**
     * Test constructor
     */
    @Test
    public void testConstructor() {
        URI serverUri = URI.create(TestUtils.ACME_SERVER_URI);

        try {
            new Session((URI) null);
            fail("accepted null parameters in constructor");
        } catch (NullPointerException ex) {
            // expected
        }

        Session session = new Session(serverUri);
        assertThat(session, not(nullValue()));
        assertThat(session.getServerUri(), is(serverUri));

        Session session2 = new Session(TestUtils.ACME_SERVER_URI);
        assertThat(session2, not(nullValue()));
        assertThat(session2.getServerUri(), is(serverUri));

        try {
            new Session("#*aBaDuRi*#");
            fail("accepted bad URI in constructor");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    /**
     * Test getters and setters.
     */
    @Test
    public void testGettersAndSetters() {
        URI serverUri = URI.create(TestUtils.ACME_SERVER_URI);

        Session session = new Session(serverUri);

        assertThat(session.getNonce(), is(nullValue()));
        session.setNonce(DUMMY_NONCE);
        assertThat(session.getNonce(), is(equalTo(DUMMY_NONCE)));

        assertThat(session.getProxy(), is(Proxy.NO_PROXY));
        Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress("10.0.0.1", 8080));
        session.setProxy(proxy);
        assertThat(session.getProxy(), is(proxy));
        session.setProxy(null);
        assertThat(session.getProxy(), is(Proxy.NO_PROXY));

        assertThat(session.getServerUri(), is(serverUri));
    }

    /**
     * Test login methods.
     */
    @Test
    public void testLogin() throws IOException {
        URI serverUri = URI.create(TestUtils.ACME_SERVER_URI);
        URL accountLocation = url(TestUtils.ACCOUNT_URL);
        KeyPair accountKeyPair = TestUtils.createKeyPair();

        Session session = new Session(serverUri);

        Login login = session.login(accountLocation, accountKeyPair);
        assertThat(login, is(notNullValue()));
        assertThat(login.getSession(), is(session));
        assertThat(login.getAccountLocation(), is(accountLocation));
        assertThat(login.getKeyPair(), is(accountKeyPair));
    }

    /**
     * Test that the directory is properly read and cached.
     */
    @Test
    public void testDirectory() throws AcmeException, IOException {
        URI serverUri = URI.create(TestUtils.ACME_SERVER_URI);

        final AcmeProvider mockProvider = mock(AcmeProvider.class);
        when(mockProvider.directory(
                        ArgumentMatchers.any(Session.class),
                        ArgumentMatchers.eq(serverUri)))
                .thenReturn(getJSON("directory"));

        Session session = new Session(serverUri) {
            @Override
            public AcmeProvider provider() {
                return mockProvider;
            };
        };

        assertSession(session);

        // Make sure directory is only read once!
        verify(mockProvider, times(1)).directory(
                        ArgumentMatchers.any(Session.class),
                        ArgumentMatchers.any(URI.class));

        // Simulate a cache expiry
        session.directoryCacheExpiry = Instant.now();

        // Make sure directory is read once again
        assertSession(session);
        verify(mockProvider, times(2)).directory(
                        ArgumentMatchers.any(Session.class),
                        ArgumentMatchers.any(URI.class));
    }

    /**
     * Test that the directory is properly read even if there are no metadata.
     */
    @Test
    public void testNoMeta() throws AcmeException, IOException {
        URI serverUri = URI.create(TestUtils.ACME_SERVER_URI);

        final AcmeProvider mockProvider = mock(AcmeProvider.class);
        when(mockProvider.directory(
                        ArgumentMatchers.any(Session.class),
                        ArgumentMatchers.eq(serverUri)))
                .thenReturn(getJSON("directoryNoMeta"));

        Session session = new Session(serverUri) {
            @Override
            public AcmeProvider provider() {
                return mockProvider;
            };
        };

        assertThat(session.resourceUrl(Resource.NEW_ACCOUNT),
                        is(new URL("https://example.com/acme/new-account")));
        assertThat(session.resourceUrl(Resource.NEW_AUTHZ),
                        is(new URL("https://example.com/acme/new-authz")));
        assertThat(session.resourceUrl(Resource.NEW_ORDER),
                        is(new URL("https://example.com/acme/new-order")));

        Metadata meta = session.getMetadata();
        assertThat(meta, not(nullValue()));
        assertThat(meta.getTermsOfService(), is(nullValue()));
        assertThat(meta.getWebsite(), is(nullValue()));
        assertThat(meta.getCaaIdentities(), is(empty()));
    }

    /**
     * Asserts that the {@link Session} returns correct
     * {@link Session#resourceUri(Resource)} and {@link Session#getMetadata()}.
     *
     * @param session
     *            {@link Session} to assert
     */
    private void assertSession(Session session) throws AcmeException, IOException {
        assertThat(session.resourceUrl(Resource.NEW_ACCOUNT),
                        is(new URL("https://example.com/acme/new-account")));
        assertThat(session.resourceUrl(Resource.NEW_AUTHZ),
                        is(new URL("https://example.com/acme/new-authz")));
        assertThat(session.resourceUrl(Resource.NEW_ORDER),
                        is(new URL("https://example.com/acme/new-order")));

        try {
            session.resourceUrl(Resource.REVOKE_CERT);
            fail("Did not fail to get an unsupported resource URL");
        } catch (AcmeException ex) {
            // Expected
        }

        Metadata meta = session.getMetadata();
        assertThat(meta, not(nullValue()));
        assertThat(meta.getTermsOfService(), is(URI.create("https://example.com/acme/terms")));
        assertThat(meta.getWebsite(), is(url("https://www.example.com/")));
        assertThat(meta.getCaaIdentities(), containsInAnyOrder("example.com"));
        assertThat(meta.isExternalAccountRequired(), is(true));
        assertThat(meta.getJSON(), is(notNullValue()));
    }

}
