
package capitec.fraudengine.config;

import capitec.fraudengine.iso.IsoMessageHandler;
import capitec.fraudengine.iso.IsoPackagerConfig;
import capitec.fraudengine.iso.IsoServer;
import capitec.fraudengine.repository.TransactionRepository;
import capitec.fraudengine.service.FraudDetectionService;
import org.jpos.iso.ISOPackager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IsoListenerConfig {

    @Bean
    public ISOPackager isoPackager() {
        return IsoPackagerConfig.defaultPackager();
    }

    /**
     * ISO message handler: maps ISOMsg → TransactionEntity,
     * runs fraud rules via FraudDetectionService, persists and builds response.
     */
    @Bean
    public IsoMessageHandler isoMessageHandler(TransactionRepository repo,
            FraudDetectionService fraudService) {
        return new IsoMessageHandler(repo, fraudService);
    }

    /**
     * ISO server: listens on iso8583.listener.port and delegates to handler.
     * Starts automatically on app startup and shuts down on context close.
     */
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public IsoServer isoServer(@Value("${iso8583.listener.port:8037}") int port,
            ISOPackager packager,
            IsoMessageHandler handler) {
        return new IsoServer(port, packager, handler);
    }
}
