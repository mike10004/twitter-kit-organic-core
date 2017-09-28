package com.twitter.sdk.organic.core;

import com.github.mike10004.twitter.organic.Uri;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class UriCodecTest {
    @Test
    public void decode() throws Exception {
        String encodedValue = "this%2Fis%7Ea%25%21test";
        String expected = "this/is~a%!test";
        String actual = Uri.UriCodec.decode(encodedValue, true, StandardCharsets.UTF_8, true);
        assertEquals("decoded value", expected, actual);
    }

}