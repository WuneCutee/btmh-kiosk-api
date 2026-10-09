package vn.baotinmanhhai.kioskapi.ratelimit;

import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.common.ErrorCodes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Gioi han goi theo may (deviceCode) + endpoint, trong-tien-trinh bang ConcurrentHashMap, dung
 * triet ly "khong them Redis" da chot cho du an nay (single-instance). Dung cua so truot don gian
 * (fixed 60-second bucket theo epoch minute) - du chinh xac cho muc dich chong lam dung, khong
 * can thuat toan sliding-window chinh xac tuyet doi.
 *
 * <p>Han che da biet: state mat khi restart, khong chia se giua nhieu instance. Xem README.
 */
@Service
public class RateLimiterService {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Kiem va tang bo dem; vuot gioi han -> ApiException 429 kem Retry-After.
     *
     * @param key        thuong la deviceCode + ":" + ten endpoint
     * @param limitPerMinute gioi han so lan goi / phut
     */
    public void checkAndIncrement(String key, int limitPerMinute) {
        long nowMinute = System.currentTimeMillis() / 60_000L;
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket());
        synchronized (bucket) {
            if (bucket.minute.get() != nowMinute) {
                bucket.minute.set(nowMinute);
                bucket.count.set(0);
            }
            long count = bucket.count.incrementAndGet();
            if (count > limitPerMinute) {
                long secondsLeftInMinute = 60 - (System.currentTimeMillis() / 1000 % 60);
                throw ErrorCodes.tooManyRequests((int) Math.max(1, secondsLeftInMinute));
            }
        }
    }

    private static class Bucket {
        private final AtomicLong minute = new AtomicLong(-1);
        private final AtomicLong count = new AtomicLong(0);
    }
}
