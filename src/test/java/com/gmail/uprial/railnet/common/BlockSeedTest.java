package com.gmail.uprial.railnet.common;

import com.gmail.uprial.railnet.populator.ChunkXZ;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.gmail.uprial.railnet.common.BlockSeed.*;
import static org.junit.Assert.*;

public class BlockSeedTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    private static final double INFINITE_DECIMAL = 10000000000000000000000000000000000.200000000000000000000000002;

    private final int TEST_MAP_SIZE = 100;

    // ==== test random-generation alternatives: oneOf() ====

    @Test
    public void testOneOfRangeDistribution() {
        forSeed((final Integer seed) -> {
            for (int size = 1; size < 100; size += 20) {
                final Map<Long, AtomicLong> map = new HashMap<>();
                for (int x = -TEST_MAP_SIZE; x <= TEST_MAP_SIZE; x++) {
                    for (int z = -TEST_MAP_SIZE; z <= TEST_MAP_SIZE; z++) {
                        final long item = new BlockSeed(seed, x, z).oneOf(size);
                        assertTrue(item < size);
                        assertTrue(item < size);

                        map.computeIfAbsent(item,
                                (k) -> new AtomicLong())
                                .incrementAndGet();
                    }
                }
                for(long s = 0; s < size; s++) {
                    assertTrue(String.format("Value %d for size %d exists", s, size),
                            map.containsKey(s));
                }

                final int estimate = (2 * TEST_MAP_SIZE + 1) * (2 * TEST_MAP_SIZE + 1) / size;
                for(final Map.Entry<Long, AtomicLong> entry : map.entrySet()) {
                    final long value = entry.getKey();
                    final long counter = entry.getValue().get();
                    assertTrue(String.format("Values of %d for size %d: %d vs. %d", value, size, counter, estimate),
                            counter < estimate * 2);
                    assertTrue(String.format("Values of %d for size %d: %d vs. %d", value, size, counter, estimate),
                            counter > estimate / 2);
                }
            }
        });
    }

    @Test
    public void testOneOfSet() {
        final Set<String> source = new HashSet<>();
        for(int i = 0; i < 100; i += 7) {
            source.add(String.format("Item #%d", i));
        }
        forSeed((final Integer seed) -> {
            final Set<String> picked = new HashSet<>();
            for (int x = -TEST_MAP_SIZE; x <= TEST_MAP_SIZE; x++) {
                for (int z = -TEST_MAP_SIZE; z <= TEST_MAP_SIZE; z++) {
                    picked.add(new BlockSeed(seed, x, z).oneOf(source));
                }
            }
            assertEquals(source, picked);
        });
    }

    @Test
    public void testOneOfList() {
        final List<Integer> source = new ArrayList<>();
        for(int i = 0; i < 100; i += 7) {
            source.add(i);
        }
        forSeed((final Integer seed) -> {
            final Set<Integer> picked = new HashSet<>();
            for (int x = -TEST_MAP_SIZE; x <= TEST_MAP_SIZE; x++) {
                for (int z = -TEST_MAP_SIZE; z <= TEST_MAP_SIZE; z++) {
                    picked.add(new BlockSeed(seed, x, z).oneOf(source));
                }
            }
            final List<Integer> pickedAndSorted = new ArrayList<>(picked);
            Collections.sort(pickedAndSorted);

            assertEquals(source, pickedAndSorted);
        });
    }

    @Test
    public void testOneOfWithNegativeSeed() throws Exception {
        assertEquals(0, new BlockSeed(-1, 0, 0).oneOf(1));
    }

    @Test
    public void testOneOfWithLongSeed() throws Exception {
        assertEquals(0, new BlockSeed(-1565193744182814265L, 276, -364).oneOf(1));
    }

    @Test
    public void testOneOfWithDifferentRangesForTheSameBlockSeed() throws Exception {
        final BlockSeed bs = new BlockSeed(1, 0, 0);
        assertEquals(0, bs.oneOf(1));
        assertNotEquals(0, bs.oneOf(10_000));
    }

    @Test
    public void testOneOfWithBadRange() throws Exception {
        e.expect(BlockSeed.BlockSeedError.class);
        e.expectMessage("Range is not a natural number: 0");
        new BlockSeed(1, 0, 0).oneOf(0);
    }

    @Test
    public void testOneOfWithBadSeed() throws Exception {
        e.expect(BlockSeed.BlockSeedError.class);
        e.expectMessage("Wrong block seed 0:0:0: / by zero");
        new BlockSeed(0, 0, 0).oneOf(1);
    }

    // ==== test random-generation alternatives: pass() ====

    @Test
    public void testPassDistribution() {
        forSeed((final Integer seed) -> {
            for (int probability = 10; probability < 100; probability += 20) {
                for (int density = 0; density < 3; density += 1) {
                    int numberOfCells = 0;
                    int passedCells = 0;
                    for (int x = -TEST_MAP_SIZE; x <= TEST_MAP_SIZE; x += 10) {
                        for (int z = -TEST_MAP_SIZE; z <= TEST_MAP_SIZE; z += 10) {
                            if (new BlockSeed(seed, x, z).pass(0, probability, density)) {
                                passedCells ++;
                            }
                            numberOfCells ++;
                        }
                    }

                    final int estimate = numberOfCells * Math.min(probability * (1 + density), 100) / 100;
                    assertTrue(String.format("Upper bound of passes of %d/%d for %d cells: %d vs. %d",
                                    probability, density, numberOfCells, passedCells, estimate),
                            passedCells <= estimate * 2);
                    assertTrue(String.format("Bottom bound of passes of %d/%d for %d cells: %d vs. %d",
                                    probability, density, numberOfCells, passedCells, estimate),
                            passedCells >= estimate / 2);
                }
            }
        });
    }

    @Test
    public void testPassWithNegativeSeed() throws Exception {
        assertTrue(new BlockSeed(-1, 0, 0).pass(0, 100, 0));
    }

    @Test
    public void testPassWithLongSeed() throws Exception {
        assertTrue(new BlockSeed(-1565193744182814265L, 276, -364).pass(0, 100, 0));
    }

    @Test
    public void testPassWithDifferentRangesForTheSameBlockSeed() throws Exception {
        final BlockSeed bs = new BlockSeed(1, 0, 0);
        assertFalse(bs.pass(0, 0, 0));
        assertTrue(bs.pass(0, 100, 0));
    }

    @Test
    public void testPassWithDifferentCallIds() throws Exception {
        forSeed((final Integer seed) -> {
            final Map<Long, AtomicLong> passed = new HashMap<>();
            for (long callId = 0; callId < 10; callId++) {
                for (int x = -TEST_MAP_SIZE; x <= TEST_MAP_SIZE; x += 10) {
                    for (int z = -TEST_MAP_SIZE; z <= TEST_MAP_SIZE; z += 10) {
                        if (new BlockSeed(seed, x, z).pass(callId, 50, 0)) {
                            passed.computeIfAbsent(callId, (k) -> new AtomicLong()).incrementAndGet();
                        }
                    }
                }
            }
            for(long callId = 0; callId < 9; callId++) {
                assertNotEquals(String.format("Call id %d vs. %d", callId, callId + 1),
                        passed.get(callId), passed.get(callId + 1));
            }
        });
    }

    @Test
    public void testPassWithBadSeed() throws Exception {
        e.expect(BlockSeed.BlockSeedError.class);
        e.expectMessage("Wrong block seed 0:0:0: / by zero");
        new BlockSeed(0, 0, 0).pass(0, 0.001, 0);
    }

    @Test
    public void testPassWithBadProbability() throws Exception {
        e.expect(BlockSeed.BlockSeedError.class);
        e.expectMessage("Probability has too many digits: 0.0001");
        new BlockSeed(1, 0, 0).pass(0, 0.0001, 0);
    }

    @Test
    public void testPassWithBigProbability() throws Exception {
        e.expect(BlockSeed.BlockSeedError.class);
        e.expectMessage("Probability too big: 200");
        new BlockSeed(1, 0, 0).pass(0, 200, 0);
    }

    // ==== test the main method ====

    @Test
    public void testConsistent() {
        forSeed((final Integer seed) -> {
             assertEquals(getTestMap(seed), getTestMap(seed));
        });
    }

    @Test
    public void testEvenlyDistributed() {
        final int numberOfCells = (2 * TEST_MAP_SIZE + 1) * (2 * TEST_MAP_SIZE + 1);

        forSeed((final Integer seed) -> {
            assertEquals(numberOfCells, getTestMap(seed).size());
        });
    }

    @Test
    public void testAsymmetric() {
        forSeed((final Integer seed) -> {

            final Map<ChunkXZ, Long> map = getTestMap(seed);

            for (final Map.Entry<ChunkXZ, Long> entry : map.entrySet()) {
                final ChunkXZ xz = entry.getKey();
                final long hash = entry.getValue();

                assertEquals(String.format("%s is self-symmetric", xz),
                        hash,
                        (long)map.get(new ChunkXZ(+xz.getX(), +xz.getZ())));

                final Set<ChunkXZ> symmetric = new HashSet<>();
                // Simple sign inversion
                for(int x = -1; x <= +1; x+=2) {
                    for (int z = -1; z <= +1; z+=2) {
                        if ((x != +1) && (z != +1)) {
                            final ChunkXZ _xz = new ChunkXZ(x * xz.getX(), z * xz.getZ());
                            if (!xz.equals(_xz) && (hash == map.get(_xz))) {
                                symmetric.add(_xz);
                            }
                        }
                    }
                }
                // Sign and value inversion
                for(int x = -1; x <= +1; x+=2) {
                    for (int z = -1; z <= +1; z+=2) {
                        final ChunkXZ _xz = new ChunkXZ(x * xz.getZ(), z * xz.getX());
                        if (!xz.equals(_xz) && (hash == map.get(_xz))) {
                            symmetric.add(_xz);
                        }
                    }
                }
                assertTrue(String.format("%s symmetry for %d: %s", xz, seed, symmetric),
                        symmetric.size() < 2);
            }
        });
    }

    // ==== test random-generation alternatives: pass() helpers ====

    @Test
    public void testRound() {
        assertEquals(1.0, round(1.46464, 0), Double.MIN_VALUE);
        assertEquals(1.5, round(1.46464, 1), Double.MIN_VALUE);
        assertEquals(1.46, round(1.46464, 2), Double.MIN_VALUE);
        assertEquals(1.465, round(1.46464, 3), Double.MIN_VALUE);
        assertEquals(1.4646, round(1.46464, 4), Double.MIN_VALUE);
        assertEquals(1.46464, round(1.46464, 5), Double.MIN_VALUE);
        assertEquals(1.46464, round(1.46464, 6), Double.MIN_VALUE);
    }

    @Test
    public void testRightDigits() {
        assertEquals(0,  getRightDigitsSlowly(1.0));
        assertEquals(1,  getRightDigitsSlowly(1.1));
        assertEquals(2,  getRightDigitsSlowly(1.12));
        assertEquals(0,  getRightDigitsSlowly(1.00000000000000003));
        assertEquals(16, getRightDigitsSlowly(1.0000000000000003));
        assertEquals(17, getRightDigitsSlowly(0.00000000000000003));
        assertEquals(16, getRightDigitsSlowly(0.0000000000000003));
        assertEquals(16, getRightDigitsSlowly(1.1000000000000003));
        assertEquals(1,  getRightDigitsSlowly(1.10000000000000003));
        // Found accidentally, I don't know how to reproduce that.
        assertEquals(-Double.MIN_EXPONENT, getRightDigitsSlowly(INFINITE_DECIMAL));
    }

    // This version is slow because of potential loop with 1k iterations.
    private static int getRightDigitsSlowly(double value) {
        return getRightDigits(value, -Double.MIN_EXPONENT);
    }

    // ==== test helpers ====

    private Map<ChunkXZ, Long> getTestMap(final long seed) {
        final Map<ChunkXZ, Long> map = new HashMap<>();
        for (int x = -TEST_MAP_SIZE; x <= TEST_MAP_SIZE; x++) {
            for (int z = -TEST_MAP_SIZE; z <= TEST_MAP_SIZE; z++) {
                map.put(new ChunkXZ(x, z), getHash(seed, x, z));
            }
        }

        return map;
    }

    private void forSeed(final Consumer<Integer> consumer) {
        for (int seed = 19; seed < 1119; seed += 119) {
            consumer.accept(seed);
        }
    }
}