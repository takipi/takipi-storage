package com.takipi.oss.storage.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class LoggerStorageMetric implements StorageMetric, Runnable {
    private static final Logger logger = LoggerFactory.getLogger(LoggerStorageMetric.class);

    private final int metricFrequencySeconds;
    private final Thread metricThread;

    private UpdaterThreadLocal updater;
    private volatile Sampling sampling;
    private volatile AtomicLong writersCount;
    private AtomicLong liveConnections;

    public LoggerStorageMetric(int metricFrequencySeconds) {
        this.updater = new UpdaterThreadLocal();
        this.sampling = new Sampling();
        this.writersCount = new AtomicLong();
        this.liveConnections = new AtomicLong();
        this.metricFrequencySeconds = metricFrequencySeconds;
        this.metricThread = new Thread(this, "metric-thread");
        this.metricThread.start();
    }

    @Override
    public void run() {
        logger.info("Metric thread started");

        while (true) {
            try {
                Thread.sleep(metricFrequencySeconds * 1000);
            } catch (Exception e) {
                logger.error("Metric thread interrupted", e);
                return;
            }

            if (!log()) {
                logger.error("Error logging, Aborting.");
                return;
            }
        }
    }

    private boolean log() {
        Sampling sampling = changeSampling();

        if (sampling == null) {
            return false;
        }

        logger.info(
                String.format("Server stats: down: %-15d up: %-15d con: %4d/%-15d", sampling.totalDownloadBytes.get(),
                        sampling.totalUploadBytes.get(), liveConnections.get(), sampling.totalConnections.get()));

        return true;
    }

    // Called by one thread only
    //
    private Sampling changeSampling() {
        Sampling sampling = this.sampling;
        this.sampling = new Sampling();

        AtomicLong writersCount = this.writersCount;
        this.writersCount = new AtomicLong();

        while (writersCount.get() > 0) {
            try {
                Thread.currentThread().sleep(1);
            } catch (Exception e) {
                logger.error("Metric thread interrupted", e);
                return null;
            }
        }

        return sampling;
    }

    @Override
    public void getStarted() {
        updater.get().start();

        updater.get().sampling().totalConnections.incrementAndGet();
        liveConnections.incrementAndGet();

        updater.get().end();
    }

    @Override
    public void getDone(long size) {
        updater.get().start();

        liveConnections.decrementAndGet();
        updater.get().sampling().totalDownloadBytes.addAndGet(size);

        updater.get().end();
    }

    @Override
    public void putStarted() {
        updater.get().start();

        updater.get().sampling().totalConnections.incrementAndGet();
        liveConnections.incrementAndGet();

        updater.get().end();
    }

    @Override
    public void putDone(long size) {
        updater.get().start();

        liveConnections.decrementAndGet();
        updater.get().sampling().totalUploadBytes.addAndGet(size);

        updater.get().end();
    }

    @Override
    public void deleteStarted() {
        updater.get().start();

        updater.get().sampling().totalConnections.incrementAndGet();
        liveConnections.incrementAndGet();

        updater.get().end();
    }

    @Override
    public void deleteDone() {
        liveConnections.decrementAndGet();
    }

    @Override
    public void headStarted() {
        updater.get().start();

        updater.get().sampling().totalConnections.incrementAndGet();
        liveConnections.incrementAndGet();

        updater.get().end();
    }

    @Override
    public void headDone() {
        liveConnections.decrementAndGet();
    }

    private class UpdaterThreadLocal extends ThreadLocal<SamplingUpdater> {
        @Override
        protected SamplingUpdater initialValue() {
            return new SamplingUpdater();
        }
    }

    private class SamplingUpdater {
        private Sampling sampling;
        private AtomicLong writersCount;

        public void start() {
            this.writersCount = LoggerStorageMetric.this.writersCount;
            writersCount.incrementAndGet();
            this.sampling = LoggerStorageMetric.this.sampling;
        }

        public void end() {
            writersCount.decrementAndGet();
        }

        private Sampling sampling() {
            return sampling;
        }
    }

    private class Sampling {
        public AtomicLong totalDownloadBytes = new AtomicLong();
        public AtomicLong totalUploadBytes = new AtomicLong();
        public AtomicLong totalConnections = new AtomicLong();
    }
}
