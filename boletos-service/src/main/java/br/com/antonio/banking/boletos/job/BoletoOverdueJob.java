package br.com.antonio.banking.boletos.job;

import br.com.antonio.banking.boletos.domain.enums.BoletoStatus;
import br.com.antonio.banking.boletos.repository.BoletoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Scheduled job: marks overdue boletos daily at midnight.
 *
 * Runs at 00:05 every day (5 min after midnight to avoid race conditions).
 * Finds all PENDING boletos with dueDate before today and marks as OVERDUE.
 *
 * In production, this would also:
 * - Notify payers via email/push (event-driven)
 * - Apply fine and interest rates (juros e multa)
 * - Integrate with CIP/FEBRABAN for official status update
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoletoOverdueJob {

    private final BoletoRepository boletoRepository;

    @Scheduled(cron = "0 5 0 * * *")  // every day at 00:05
    @Transactional
    public void markOverdueBoletos() {
        var yesterday = LocalDate.now().minusDays(1);
        var overdue = boletoRepository.findByStatusAndDueDateBefore(
                BoletoStatus.PENDING, yesterday
        );

        if (overdue.isEmpty()) {
            log.debug("Overdue job: no boletos to update");
            return;
        }

        overdue.forEach(b -> b.setStatus(BoletoStatus.OVERDUE));
        boletoRepository.saveAll(overdue);
        log.info("Overdue job: marked {} boleto(s) as OVERDUE (due before {})",
                overdue.size(), yesterday);
    }
}