package az.et.fintechtransactionservice.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import az.et.fintechtransactionservice.domain.model.CustomerId;
import az.et.fintechtransactionservice.domain.model.Wallet;
import az.et.fintechtransactionservice.domain.model.WalletId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryWalletRepositoryAdapterTest {

    private final InMemoryWalletRepositoryAdapter repository = new InMemoryWalletRepositoryAdapter();

    @Test
    @DisplayName("Should save and find wallet by id")
    void shouldSaveAndFindWallet() {
        Wallet wallet = Wallet.open(new CustomerId("customer-001"), "AZN");

        repository.save(wallet);

        assertThat(repository.findById(wallet.id())).contains(wallet);
    }

    @Test
    @DisplayName("Should return empty for missing wallet")
    void shouldReturnEmptyForMissingWallet() {
        assertThat(repository.findById(WalletId.from("missing-wallet"))).isEmpty();
    }
}

