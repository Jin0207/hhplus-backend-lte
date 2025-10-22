package io.hhplus.tdd.point;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

@Component
public class UserLockManager {
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public ReentrantLock getLock(long userId) {
        return lockMap.computeIfAbsent(userId, k -> new ReentrantLock());
    }
}
