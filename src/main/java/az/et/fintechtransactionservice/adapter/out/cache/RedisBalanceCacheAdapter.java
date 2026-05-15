package az.et.fintechtransactionservice.adapter.out.cache;

import az.et.fintechtransactionservice.application.port.out.BalanceCachePort;
import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisBalanceCacheAdapter implements BalanceCachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisBalanceCacheAdapter.class);
    private static final String KEY_PREFIX = "wallet:balance:";
    private static final String DELIMITER = "|";

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    public RedisBalanceCacheAdapter(
            StringRedisTemplate redisTemplate,
            @Value("${fintech.cache.balance-ttl-seconds:300}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    @Override
    public Optional<Money> getBalance(WalletId walletId) {
        try {
            String rawValue = redisTemplate.opsForValue().get(key(walletId));
            if (rawValue == null || rawValue.isBlank()) {
                return Optional.empty();
            }
            String[] parts = rawValue.split("\\|", 2);
            if (parts.length != 2) {
                log.warn("Ignoring malformed cached balance: walletId={}, value={}", walletId.value(), rawValue);
                return Optional.empty();
            }
            return Optional.of(Money.of(parts[0], parts[1]));
        } catch (RuntimeException exception) {
            log.warn("Redis balance get failed; treating as cache miss: walletId={}, reason={}",
                    walletId.value(), exception.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void putBalance(WalletId walletId, Money balance) {
        try {
            redisTemplate.opsForValue().set(key(walletId), serialize(balance), ttl);
            log.debug("Balance cached: walletId={}, ttlSeconds={}", walletId.value(), ttl.toSeconds());
        } catch (RuntimeException exception) {
            log.warn("Redis balance put failed; continuing without cache: walletId={}, reason={}",
                    walletId.value(), exception.getMessage());
        }
    }

    @Override
    public void evictBalance(WalletId walletId) {
        try {
            redisTemplate.delete(key(walletId));
            log.debug("Balance cache evicted: walletId={}", walletId.value());
        } catch (RuntimeException exception) {
            log.warn("Redis balance eviction failed; continuing: walletId={}, reason={}",
                    walletId.value(), exception.getMessage());
        }
    }

    private String key(WalletId walletId) {
        return KEY_PREFIX + walletId.value();
    }

    private String serialize(Money money) {
        return money.amount() + DELIMITER + money.currency();
    }
}

