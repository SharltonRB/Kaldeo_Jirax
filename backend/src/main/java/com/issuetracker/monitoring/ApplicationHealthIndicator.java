package com.issuetracker.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;

/**
 * Custom health indicator for application resources and performance
 */
@Component
public class ApplicationHealthIndicator implements HealthIndicator {

    private static final double MEMORY_THRESHOLD = 0.9; // 90% memory usage threshold
    private static final double CPU_THRESHOLD = 0.9; // 90% CPU usage threshold

    @Override
    public Health health() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            
            // Get memory usage
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            
            long heapUsed = heapUsage.getUsed();
            long heapMax = heapUsage.getMax();
            double heapUsagePercent = (double) heapUsed / heapMax;
            
            // Get CPU load
            double systemCpuLoad = osBean.getSystemLoadAverage();
            int availableProcessors = osBean.getAvailableProcessors();
            
            // Get uptime
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            
            // Determine health status
            Health.Builder builder;
            if (heapUsagePercent > MEMORY_THRESHOLD) {
                builder = Health.down()
                    .withDetail("reason", "High memory usage");
            } else if (systemCpuLoad / availableProcessors > CPU_THRESHOLD) {
                builder = Health.down()
                    .withDetail("reason", "High CPU load");
            } else {
                builder = Health.up();
            }
            
            return builder
                .withDetail("memory", new MemoryDetails(heapUsage, nonHeapUsage))
                .withDetail("cpu", new CpuDetails(systemCpuLoad, availableProcessors))
                .withDetail("uptime", formatUptime(uptime))
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
    
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    private static class MemoryDetails {
        public final String heapUsed;
        public final String heapMax;
        public final String heapUsagePercent;
        public final String nonHeapUsed;
        
        MemoryDetails(MemoryUsage heap, MemoryUsage nonHeap) {
            this.heapUsed = formatBytes(heap.getUsed());
            this.heapMax = formatBytes(heap.getMax());
            this.heapUsagePercent = String.format("%.2f%%", (double) heap.getUsed() / heap.getMax() * 100);
            this.nonHeapUsed = formatBytes(nonHeap.getUsed());
        }
        
        private String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
    
    private static class CpuDetails {
        public final double systemLoadAverage;
        public final int availableProcessors;
        public final String loadPerProcessor;
        
        CpuDetails(double systemLoad, int processors) {
            this.systemLoadAverage = systemLoad;
            this.availableProcessors = processors;
            this.loadPerProcessor = String.format("%.2f%%", (systemLoad / processors) * 100);
        }
    }
}
