package com.github.mike10004.twitter.organic;

import com.twitter.sdk.organic.core.SessionManager;
import com.twitter.sdk.organic.core.TwitterSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class TwitterSessions {

    private TwitterSessions() {}

    public static <S extends TwitterSession> SessionManager<S> inMemorySessionManager() {
        return new InMemorySessionManager<>();
    }

    static class InMemorySessionManager<S extends com.twitter.sdk.organic.core.Session> implements SessionManager<S> {

        private final AtomicReference<S> active = new AtomicReference<>();
        private final Map<Long, S> sessionsMap = new ConcurrentHashMap<>();

        @Override
        public S getActiveSession() {
            return active.get();
        }

        @Override
        public void setActiveSession(S session) {
            active.set(session);
        }

        @Override
        public void clearActiveSession() {
            active.set(null);
        }

        @Override
        public S getSession(long id) {
            return sessionsMap.get(id);
        }

        @Override
        public void setSession(long id, S session) {
            sessionsMap.put(id, session);
        }

        @Override
        public void clearSession(long id) {
            sessionsMap.remove(id);
        }

        @Override
        public Map<Long, S> getSessionMap() {
            return sessionsMap;
        }
    }

}
