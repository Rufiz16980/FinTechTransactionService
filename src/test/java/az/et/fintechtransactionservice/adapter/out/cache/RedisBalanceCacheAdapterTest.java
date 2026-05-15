package az.et.fintechtransactionservice.adapter.out.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisBalanceCacheAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("Should return cached balance when Redis contains value")
    void shouldReturnCachedBalance() {
        WalletId walletId = WalletId.from("wallet-001");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("wallet:balance:wallet-001")).willReturn("42.50|AZN");

        var result = adapter().getBalance(walletId);

        assertThat(result).isPresent();
        assertThat(result.get().amount()).isEqualByComparingTo("42.50");
        assertThat(result.get().currency()).isEqualTo("AZN");
    }

    @Test
    @DisplayName("Should return empty when Redis key is missing")
    void shouldReturnEmptyForMissingKey() {
        WalletId walletId = WalletId.from("wallet-001");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("wallet:balance:wallet-001")).willReturn(null);

        assertThat(adapter().getBalance(walletId)).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when Redis get fails")
    void shouldReturnEmptyWhenGetFails() {
        WalletId walletId = WalletId.from("wallet-001");
        given(redisTemplate.opsForValue()).willThrow(new IllegalStateException("redis down"));

        assertThat(adapter().getBalance(walletId)).isEmpty();
    }

    @Test
    @DisplayName("Should store serialized balance with TTL")
    void shouldStoreBalanceWithTtl() {
        WalletId walletId = WalletId.from("wallet-001");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        adapter().putBalance(walletId, Money.of("42.50", "AZN"));

        then(valueOperations).should(times(1))
                .set("wallet:balance:wallet-001", "42.50|AZN", Duration.ofSeconds(300));
    }

    @Test
    @DisplayName("Should swallow Redis put failures")
    void shouldSwallowPutFailures() {
        WalletId walletId = WalletId.from("wallet-001");
        given(redisTemplate.opsForValue()).willThrow(new IllegalStateException("redis down"));

        assertThatCode(() -> adapter().putBalance(walletId, Money.of("42.50", "AZN"))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should evict cached balance")
    void shouldEvictBalance() {
        WalletId walletId = WalletId.from("wallet-001");

        adapter().evictBalance(walletId);

        then(redisTemplate).should(times(1)).delete("wallet:balance:wallet-001");
    }

    @Test
    @DisplayName("Should swallow Redis eviction failures")
    void shouldSwallowEvictionFailures() {
        WalletId walletId = WalletId.from("wallet-001");
        given(redisTemplate.delete("wallet:balance:wallet-001")).willThrow(new IllegalStateException("redis down"));

        assertThatCode(() -> adapter().evictBalance(walletId)).doesNotThrowAnyException();
    }

    private RedisBalanceCacheAdapter adapter() {
        return new RedisBalanceCacheAdapter(redisTemplate, 300);
    }
}

