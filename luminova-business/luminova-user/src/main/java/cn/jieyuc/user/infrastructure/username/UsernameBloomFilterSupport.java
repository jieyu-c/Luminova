package cn.jieyuc.user.infrastructure.username;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
public class UsernameBloomFilterSupport {

    static final String BLOOM_FILTER_NAME = "user:username:bloom";
    static final long EXPECTED_INSERTIONS = 1_000_000L;
    static final double FALSE_POSITIVE_RATE = 0.01D;

    private final RBloomFilter<String> bloomFilter;

    public UsernameBloomFilterSupport(RedissonClient redissonClient) {
        this.bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_NAME);
    }

    public boolean mightContain(String username) {
        return bloomFilter.contains(username);
    }

    public void add(String username) {
        bloomFilter.add(username);
    }

    /**
     * @return true if bloom filter was newly created and needs DB preload
     */
    public boolean initIfAbsent() {
        if (bloomFilter.isExists()) {
            return false;
        }
        bloomFilter.tryInit(EXPECTED_INSERTIONS, FALSE_POSITIVE_RATE);
        return true;
    }
}
