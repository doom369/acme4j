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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.LoggerFactory;

/**
 * A generic HTTP connector. It connects to the given URL with a 10 seconds connection and
 * read timeout.
 * <p>
 * Subclasses may reconfigure the {@link HttpURLConnection} and pin it to a concrete SSL
 * certificate.
 */
@ParametersAreNonnullByDefault
@ThreadSafe
public class HttpConnector {

    private static final String USER_AGENT;

    static {
        StringBuilder agent = new StringBuilder("acme4j");

        try (InputStream in = HttpConnector.class.getResourceAsStream("/org/shredzone/acme4j/version.properties")) {
            Properties prop = new Properties();
            prop.load(in);
            agent.append('/').append(prop.getProperty("version"));
        } catch (Exception ex) {
            // Ignore, just don't use a version
            LoggerFactory.getLogger(HttpConnector.class).warn("Could not read library version", ex);
        }

        agent.append(" Java/").append(System.getProperty("java.version"));
        USER_AGENT = agent.toString();
    }

    /**
     * Returns the default User-Agent to be used.
     *
     * @return User-Agent
     */
    public static String defaultUserAgent() {
        return USER_AGENT;
    }

    /**
     * Opens a {@link HttpURLConnection} to the given {@link URL}.
     *
     * @param url
     *            {@link URL} to connect to
     * @param settings
     *            {@link NetworkSettings} to be used
     * @return {@link HttpURLConnection} connected to the {@link URL}
     */
    public HttpURLConnection openConnection(URL url, NetworkSettings settings) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(settings.getProxy());
        configure(conn, settings);
        return conn;
    }

    /**
     * Configures the new {@link HttpURLConnection}.
     * <p>
     * The {@link HttpURLConnection} is already preconfigured with a reasonable timeout,
     * disabled caches and a User-Agent header. Subclasses can override this method to
     * change the configuration.
     *
     * @param conn
     *         {@link HttpURLConnection} to configure.
     * @param settings
     *         {@link NetworkSettings} with settings to be used
     */
    @OverridingMethodsMustInvokeSuper
    protected void configure(HttpURLConnection conn, NetworkSettings settings) {
        int timeout = (int) settings.getTimeout().toMillis();
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setUseCaches(false);
        conn.setRequestProperty("User-Agent", USER_AGENT);
    }

}
