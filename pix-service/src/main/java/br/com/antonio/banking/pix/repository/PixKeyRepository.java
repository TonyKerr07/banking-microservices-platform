package br.com.antonio.banking.pix.repository;

import br.com.antonio.banking.pix.domain.entity.PixKey;
import br.com.antonio.banking.pix.domain.enums.PixKeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PixKeyRepository extends JpaRepository<PixKey, UUID> {

    List<PixKey> findByAccountIdAndStatus(UUID accountId, PixKeyStatus status);

    Optional<PixKey> findByKeyValueAndStatus(String keyValue, PixKeyStatus status);

    boolean existsByKeyValueAndStatus(String keyValue, PixKeyStatus status);

    long countByAccountIdAndStatus(UUID accountId, PixKeyStatus status);
}