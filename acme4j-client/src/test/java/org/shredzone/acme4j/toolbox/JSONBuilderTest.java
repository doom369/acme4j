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
package org.shredzone.acme4j.toolbox;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.jose4j.json.JsonUtil;
import org.jose4j.lang.JoseException;
import org.junit.Test;

/**
 * Unit test for {@link JSONBuilder}.
 */
public class JSONBuilderTest {

    /**
     * Test that an empty JSON builder is empty.
     */
    @Test
    public void testEmpty() {
        JSONBuilder cb = new JSONBuilder();
        assertThat(cb.toString(), is("{}"));
    }

    /**
     * Test basic data types. Also test that methods return {@code this}, and that
     * existing keys are replaced.
     */
    @Test
    public void testBasics() {
        JSONBuilder res;

        JSONBuilder cb = new JSONBuilder();
        res = cb.put("fooStr", "String");
        assertThat(res, is(sameInstance(cb)));

        res = cb.put("fooInt", 123);
        assertThat(res, is(sameInstance(cb)));

        res = cb.put("fooInt", 456);
        assertThat(res, is(sameInstance(cb)));

        assertThat(cb.toString(), is("{\"fooStr\":\"String\",\"fooInt\":456}"));

        Map<String, Object> map = cb.toMap();
        assertThat(map.keySet(), hasSize(2));
        assertThat(map, allOf(
                hasEntry("fooInt", (Object) 456),
                hasEntry("fooStr", (Object) "String")
        ));

        JSON json = cb.toJSON();
        assertThat(json.keySet(), hasSize(2));
        assertThat(json.get("fooInt").asInt(), is(456));
        assertThat(json.get("fooStr").asString(), is("String"));
    }

    /**
     * Test date type.
     */
    @Test
    public void testDate() {
        Instant date = ZonedDateTime.of(2016, 6, 1, 5, 13, 46, 0, ZoneId.of("GMT+2")).toInstant();
        Duration duration = Duration.ofMinutes(5);

        JSONBuilder cb = new JSONBuilder();
        cb.put("fooDate", date);
        cb.put("fooDuration", duration);
        cb.put("fooNull", (Object) null);

        assertThat(cb.toString(), is("{\"fooDate\":\"2016-06-01T03:13:46Z\",\"fooDuration\":300,\"fooNull\":null}"));
    }

    /**
     * Test base64 encoding.
     */
    @Test
    public void testBase64() {
        byte[] data = "abc123".getBytes();

        JSONBuilder res;

        JSONBuilder cb = new JSONBuilder();
        res = cb.putBase64("foo", data);
        assertThat(res, is(sameInstance(cb)));
        assertThat(cb.toString(), is("{\"foo\":\"YWJjMTIz\"}"));
    }

    /**
     * Test JWK.
     */
    @Test
    public void testKey() throws IOException, JoseException {
        KeyPair keyPair = TestUtils.createKeyPair();

        JSONBuilder res;

        JSONBuilder cb = new JSONBuilder();
        res = cb.putKey("foo", keyPair.getPublic());
        assertThat(res, is(sameInstance(cb)));

        Map<String, Object> json = JsonUtil.parseJson(cb.toString());
        assertThat(json, hasKey("foo"));

        Map<String, String> jwk = (Map<String, String>) json.get("foo");
        assertThat(jwk.keySet(), hasSize(3));
        assertThat(jwk, hasEntry("n", TestUtils.N));
        assertThat(jwk, hasEntry("e", TestUtils.E));
        assertThat(jwk, hasEntry("kty", TestUtils.KTY));
    }

    /**
     * Test sub claims (objects).
     */
    @Test
    public void testObject() {
        JSONBuilder cb = new JSONBuilder();
        JSONBuilder sub = cb.object("sub");
        assertThat(sub, not(sameInstance(cb)));

        assertThat(cb.toString(), is("{\"sub\":{}}"));

        cb.put("foo", 123);
        sub.put("foo", 456);

        assertThat(cb.toString(), is("{\"sub\":{\"foo\":456},\"foo\":123}"));
    }

    /**
     * Test arrays.
     */
    @Test
    public void testArray() {
        JSONBuilder res;

        JSONBuilder cb1 = new JSONBuilder();
        res = cb1.array("ar", Collections.emptyList());
        assertThat(res, is(sameInstance(cb1)));
        assertThat(cb1.toString(), is("{\"ar\":[]}"));

        JSONBuilder cb2 = new JSONBuilder();
        res = cb2.array("ar", Arrays.asList(123));
        assertThat(res, is(sameInstance(cb2)));
        assertThat(cb2.toString(), is("{\"ar\":[123]}"));

        JSONBuilder cb3 = new JSONBuilder();
        res = cb3.array("ar", Arrays.asList(123, "foo", 456));
        assertThat(res, is(sameInstance(cb3)));
        assertThat(cb3.toString(), is("{\"ar\":[123,\"foo\",456]}"));
    }

}
