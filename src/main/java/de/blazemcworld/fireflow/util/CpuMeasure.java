package de.blazemcworld.fireflow.util;

public class CpuMeasure {

    private final long start = System.nanoTime();
    private long stop = -1;

    public void finish() {
        stop = System.nanoTime();
    }

    public boolean isRecent() {
        if (stop == -1) return true;
        return System.nanoTime() - stop < 1_000_000_000L; // 1s
    }

    public long getTimeNs() {
        if (stop == -1) return System.nanoTime() - start;
        return stop - start;
    }

}
