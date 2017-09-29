package com.twitter.sdk.organic.core.test;

import org.bitcoinj.core.Base58;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Function;

public class TokenGenerator implements Iterator<String> {

    private static final Random DEFAULT_RANDOM = new Random(TokenGenerator.class.hashCode());

    private final Random random;
    private final int width;
    private final Function<byte[], String> stringifier;

    public TokenGenerator(Random random, int width, Function<byte[], String> stringifier) {
        this.random = random;
        this.width = width;
        this.stringifier = stringifier;
    }

    public TokenGenerator() {
        this(DEFAULT_RANDOM, 16, Base58::encode);
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public String next() {
        byte[] bytes = new byte[width];
        random.nextBytes(bytes);
        return stringifier.apply(bytes);
    }
}
