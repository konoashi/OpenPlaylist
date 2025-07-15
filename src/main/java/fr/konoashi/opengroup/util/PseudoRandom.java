package fr.konoashi.opengroup.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class PseudoRandom {

    // Internal state (two 32-bit integers)
    private int state0;
    private int state1;

    // Linear Congruential Generator (LCG) state
    private final int m = 0x80000000; // 2^31
    private final int a = 1103515245;
    private final int c = 12345;
    private int state;

    private static final double POW36_8 = Math.pow(36, 8);
    private static final double INV_2_32 = 1.0 / 4294967296.0;

    public PseudoRandom(long seed) {
        this.state0 = (int) seed ;
        this.state1 = 0x6e2d786c;

        if (this.state0 == 0) this.state0 = 1;

        this.state = (int) (seed % m);
        if (this.state < 0) this.state += m;

        for (int i = 0; i < 20; i++) {
            this._nextIntInternal();
        }
    }

    /**
     * Internal function implementing the XorShift algorithm.
     */
    private int _nextIntInternal() {
        int s1 = state0;
        int s0 = state1;

        state0 = s0;
        s1 ^= (s1 << 23);
        s1 ^= (s1 >>> 17);
        s1 ^= s0;
        s1 ^= (s0 >>> 26);
        state1 = s1;

        return state0 + state1;
    }

    /**
     * Generates an unsigned 32-bit integer (simulated with long).
     */
    private long _nextUInt32() {
        return Integer.toUnsignedLong(_nextIntInternal());
    }

    /**
     * Generates the next pseudorandom number between 0 (inclusive) and 1 (exclusive).
     */
    public double next() {
        long intVal = _nextUInt32();
        state = (int)(intVal % m);
        return (double) state / m;
    }

    /**
     * Returns a float in [0, 1) without affecting state.
     */
    private double _nextFloat() {
        return _nextUInt32() * INV_2_32;
    }

    /**
     * Generates a random integer between min (inclusive) and max (exclusive).
     */
    public int nextInt(int min, int max) {
        return (int) Math.floor(_nextFloat() * (max - min)) + min;
    }

    /**
     * Generates a random float between min (inclusive) and max (exclusive).
     */
    public double nextFloat(double min, double max) {
        return _nextFloat() * (max - min) + min;
    }

    /**
     * Generates a random alphanumeric ID (8 characters).
     */
    public String nextID() {
        long num = (long) Math.floor(_nextFloat() * POW36_8);
        String base36 = Long.toString(num, 36);
        return String.format("%8s", base36).replace(' ', '0');
    }

    /**
     * Selects a random element from a list.
     */
    public <T> T randElement(List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("array must not be empty");
        }
        int index = (int) Math.floor(_nextFloat() * list.size());
        return list.get(index);
    }

    /**
     * Returns true with probability 1/odds.
     */
    public boolean chance(int odds) {
        return Math.floor(_nextFloat() * odds) == 0;
    }

    /**
     * Returns a shuffled copy of the array using Fisher-Yates algorithm.
     */
    public <T> List<T> shuffleArray(List<T> array) {
        List<T> result = new ArrayList<>(array);
        for (int i = result.size() - 1; i >= 0; i--) {
            int j = (int) Math.floor(_nextFloat() * (i + 1));
            Collections.swap(result, i, j);
        }
        return result;
    }
}
