package com.issuetracker.controller;

import com.issuetracker.monitoring.ErrorTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for monitoring and diagnostics endpoints
 */
@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

    private final ErrorTrackingService errorTrackingService;

    public MonitoringController(ErrorTrackingService errorTrackingService) {
        this.errorTrackingService = errorTrackingService;
    }

    /**
     * Get application metrics summary
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Memory metrics
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        Map<String, Object> memory = new HashMap<>();
        memory.put("heapUsed", formatBytes(heapUsage.getUsed()));
        memory.put("heapMax", formatBytes(heapUsage.getMax()));
        memory.put("heapUsagePercent", String.format("%.2f%%", (double) heapUsage.getUsed() / heapUsage.getMax() * 100));
        memory.put("nonHeapUsed", formatBytes(nonHeapUsage.getUsed()));
        metrics.put("memory", memory);
        
        // CPU metrics
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> cpu = new HashMap<>();
        cpu.put("availableProcessors", osBean.getAvailableProcessors());
        cpu.put("systemLoadAverage", osBean.getSystemLoadAverage());
        metrics.put("cpu", cpu);
        
        // Runtime metrics
        Map<String, Object> runtime = new HashMap<>();
        runtime.put("uptime", formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()));
        runtime.put("startTime", ManagementFactory.getRuntimeMXBean().getStartTime());
        metrics.put("runtime", runtime);
        
        // Thread metrics
        Map<String, Object> threads = new HashMap<>();
        threads.put("threadCount", ManagementFactory.getThreadMXBean().getThreadCount());
        threads.put("peakThreadCount", ManagementFactory.getThreadMXBean().getPeakThreadCount());
        threads.put("daemonThreadCount", ManagementFactory.getThreadMXBean().getDaemonThreadCount());
        metrics.put("threads", threads);
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get error statistics
     */
    @GetMapping("/errors")
    public ResponseEntity<Map<String, ErrorTrackingService.ErrorStats>> getErrorStats() {
        return ResponseEntity.ok(errorTrackingService.getErrorStats());
    }

    /**
     * Simple health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(health);
    }

    /**
     * Get system information
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> system = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("javaVendor", System.getProperty("java.vendor"));
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("osArch", System.getProperty("os.arch"));
        system.put("availableProcessors", runtime.availableProcessors());
        system.put("totalMemory", formatBytes(runtime.totalMemory()));
        system.put("freeMemory", formatBytes(runtime.freeMemory()));
        system.put("maxMemory", formatBytes(runtime.maxMemory()));
        
        return ResponseEntity.ok(system);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
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
}
