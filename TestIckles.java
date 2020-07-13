package gad.bfs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// A test that tickles you!
public class TestIckles {
    // FLAGS
    private static final int TIMEOUT = 1000, SEED = 10, LEN = 10000, NUM_OF_EDGES_TRIALS = 11000;
    private static final String yourResultFile = "myResult.txt", friendResult = null;

    // Not FLAGS
    private static BFS bfs;
    private static Graph artemisGraph, bigOlGraph;
    private static Random random;
    private static Result result;
    private static ConnectedComponents cc;
    protected static Path file;
    private static List<String> toBePrinted;

    static final class ResultButItWritesFile implements Result {
        @Override
        public void visit(int node) {
            System.out.println("Visited node " + node);
            toBePrinted.add("Visited node " + node);
        }
    }

    public static void make() {
        random = new Random(SEED);
        artemisGraph = new Graph();
        bigOlGraph = new Graph();
        result = new ResultButItWritesFile();
        bfs = new BFS(result);
        cc = new ConnectedComponents();
        toBePrinted = new LinkedList<>();

        // Writing Header
        try {
            file = Files.createFile(Path.of("src", yourResultFile));
            Files.write(file, List.of("INFO! Seed: " + SEED + " Max: " + LEN));
        } catch (FileAlreadyExistsException e) {
            try {
                Files.delete(Path.of("src", yourResultFile));
                file = Files.createFile(Path.of("src", yourResultFile));
                Files.write(file, List.of("INFO! Seed: " + SEED + " Max: " + LEN));
            } catch (IOException f) {
                f.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Injecting with Reflection
        try {
            Field bfsField = cc.getClass().getDeclaredField("search");
            bfsField.setAccessible(true);
            bfsField.set(cc, bfs);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed injecting custom bfs!");
        }

        // Making artemis Graph
        for (int i = 0; i <= 7; i++) {
            artemisGraph.addNode();
        }
        Map<Integer, List<Integer>> abcon = new HashMap<>();
        abcon.put(0, List.of(1, 2, 3));
        abcon.put(1, List.of(3, 4, 5));
        abcon.put(3, List.of(4, 5, 6));
        abcon.put(2, List.of(5, 6));
        abcon.put(4, List.of(5, 6, 7));
        abcon.put(7, List.of(5, 6));
        abcon.entrySet()
                .stream()
                .forEach(entry ->
                        entry.getValue()
                                .stream()
                                .forEach(b -> artemisGraph.addEdge(entry.getKey(), b)
                                )
                );

        // Making graph of the devil
        for (int i = 0; i <= LEN; i++) {
            bigOlGraph.addNode();
        }
        // Ever wonder why it shall not do anything instead of throwing stuff?
        for (int i = 0; i <= NUM_OF_EDGES_TRIALS; i++) {
            bigOlGraph.addEdge(random.nextInt(LEN + 1), random.nextInt(LEN + 1));
        }
    }

    private static void print(String printed) {
        System.out.println(printed);
        toBePrinted.add(printed);
    }

    private static void done() {
        try {
            Files.write(file, Stream.concat(Files.lines(file), toBePrinted.stream()).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.err.println("!!! DONUT PUSH OR YOU GET BIG PROBLEMS WITH THE HOLY SHEEPS (and penguin, too 🐧) !!!");

        long timeBeforeMaking = System.currentTimeMillis();
        make();
        long timeAfterMaking = System.currentTimeMillis();
        System.out.println("Make time: " + (timeAfterMaking - timeBeforeMaking) + " ms\n");

        System.out.println("----------- TEST PARAMETERS -----------");
        System.out.println("Seed: " + SEED);
        System.out.println("Nodes in Devil Test: " + LEN);
        System.out.println("Timeout: " + TIMEOUT + " ms");
        System.out.println("Your file: " + new File(file.toString()).getAbsolutePath());
        if (friendResult != null) System.out.println("Your friend's file: " + new File(friendResult).getAbsolutePath());

        System.out.println("\n-------------- TESTING --------------");

        print("Artemis Graph Testing");
        long artemisStart = System.currentTimeMillis();
        int resArtemis = cc.countConnectedComponents(artemisGraph);
        long artemisEnd = System.currentTimeMillis();
        System.out.println("Your Artemis Result: " + resArtemis);
        toBePrinted.add("Your Artemis Result: " + resArtemis);
        System.out.println("Time: " + (artemisEnd - artemisStart));

        print("Devil Graph Testing");
        long devilStart = System.currentTimeMillis();
        int resDevil = cc.countConnectedComponents(bigOlGraph);
        long devilEnd = System.currentTimeMillis();
        print("Your Devil Result: " + resDevil);
        System.out.println("Time: " + (devilEnd - devilStart));

        done();
        System.out.println("\n-------------- DONE --------------");
    }
}
