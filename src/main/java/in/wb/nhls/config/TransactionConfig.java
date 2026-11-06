package in.wb.nhls.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// Proxy-র ভেতরের ক্রম: AuditAspect -> @Transactional -> NotifyAspect -> আসল method
// NotifyAspect transaction-এর ভেতরে থাকে, তাই outbox row business data-র সাথেই commit হয়
@Configuration
@EnableTransactionManagement(proxyTargetClass = true, order = TransactionConfig.TRANSACTION_ORDER)
public class TransactionConfig {

    public static final int TRANSACTION_ORDER = Ordered.LOWEST_PRECEDENCE - 100;
}
