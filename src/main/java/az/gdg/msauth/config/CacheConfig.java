package az.gdg.msauth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    private final CacheManager cacheManager;

    public CacheConfig(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(cron = "0 0 12 * * ?")  // at 12 PM every day
    public void clearCacheSchedule() {
        logger.info("Config.clearCacheSchedule.start : date {}", LocalDateTime.now());
        for (String name : cacheManager.getCacheNames()) {
            cacheManager.getCache(name).clear();
        }

        logger.info("Config.clearCacheSchedule.stop.success : date {}", LocalDateTime.now());

    }
}
