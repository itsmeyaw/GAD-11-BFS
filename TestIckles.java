package gad.bfs;

import gad.bfs.Graph.Node;

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
    private static final String yourResultFile = "myResult.txt", friendResult = "myResult2.txt";
    private static final boolean printDot = false;

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
            print("Visited node " + node);
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
            Files.write(file, List.of("INFO! Seed: " + SEED + " Max: " + LEN + " Max-Edges: " + NUM_OF_EDGES_TRIALS));
        } catch (FileAlreadyExistsException e) {
            try {
                Files.delete(Path.of("src", yourResultFile));
                file = Files.createFile(Path.of("src", yourResultFile));
                Files.write(file, List.of("INFO! Seed: " + SEED + " Max: " + LEN + " Max-Edges: " + NUM_OF_EDGES_TRIALS));
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

    // Return dot version of the graph
    private static String dot(Graph g, String name) {
        StringBuilder sb = new StringBuilder("digraph " + name + " {\nconcentrate=true");
        g.getAllNodes().stream()
                .forEach(node -> {
                    g.getAllNeighbours(node.getID()).stream().map(Node::getID).forEach(neighbourID -> {
                        sb.append("\n\t")
                                .append(node.getID())
                                .append(" -> ")
                                .append(neighbourID);
                    });
                });
        sb.append("\n}");
        return sb.toString();
    }

    private static void print(String printed) {
        System.out.println(printed);
        toBePrinted.add(printed);
    }

    private static void compare() {
        try {

            Iterator<String> myIter = Files.lines(Path.of("src", yourResultFile)).iterator();
            Iterator<String> friendIter = Files.lines(Path.of("src", friendResult)).iterator();

            // Check Header
            if (!myIter.next().equals(friendIter.next())) throw new RuntimeException("Different header, exiting!");

            int line = 2;

            // Iterating lins
            while (myIter.hasNext() && friendIter.hasNext()) {
                String myString = myIter.next();
                String friendString = friendIter.next();

                if (!myString.equals(friendString)) {
                    System.out.println("Different found on line: " + line);
                    System.out.println("Your: " + myString);
                    System.out.println("Friend's: " + friendString);
                }

                line++;
            }

            int diffCounter = 0;

            if (myIter.hasNext()) {
                while (myIter.hasNext()) {
                    myIter.next();
                    diffCounter++;
                }
                System.out.println("Your file has " + diffCounter + " more lines than your friend's file.");
                return;
            }

            if (friendIter.hasNext()) {
                while (friendIter.hasNext()) {
                    friendIter.next();
                    diffCounter++;
                }
                System.out.println("Your file has " + diffCounter + " less lines than your friend's file.");
                return;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFile() {
        try {
            Files.write(file, Stream.concat(Files.lines(file), toBePrinted.stream()).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.err.println("!!! DONUT PUSH THIS FILE OR YOU GET BIG PROBLEMS WITH THE HOLY SHEEPS (and penguin, too 🐧) !!!");

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
        print("Your Artemis Result: " + resArtemis);
        print("Parents in Artemis Graph");
        List<Node> artemisParent = artemisGraph.getAllNodes();
        for (int i = 0; i <= 7; i++) {
            Node parent = bfs.getParent(artemisParent.get(i));
            if (parent != null) {
                print("Node " + i + ": " + parent.getID());
            } else {
                print("Node " + i + ": no parent");
            }
        }
        System.out.println("Time: " + (artemisEnd - artemisStart));
        if ((artemisEnd - artemisStart) >= TIMEOUT) System.out.println("Time limit exceeded");

        print("Devil Graph Testing");
        long devilStart = System.currentTimeMillis();
        int resDevil = cc.countConnectedComponents(bigOlGraph);
        long devilEnd = System.currentTimeMillis();
        print("Your Devil Result: " + resDevil);
        print("Parents in Devil Graph");
        List<Node> devilParent = bigOlGraph.getAllNodes();
        for (int i = 0; i <= LEN; i++) {
            Node parent = bfs.getParent(devilParent.get(i));
            if (parent != null) {
                print("Node " + i + ": " + parent.getID());
            } else {
                print("Node " + i + ": no parent");
            }
        }
        System.out.println("Time: " + (devilEnd - devilStart));
        if ((devilEnd - devilStart) >= TIMEOUT) System.out.println("Time limit exceeded");

        System.out.println("\n--------- WRITING FILE -----------");
        writeFile();
        System.out.println("Done writing");

        System.out.println("\n----------- COMPARING ------------");
        if (friendResult != null) {
            compare();
            System.out.println("Done comparing");
        } else {
            System.out.println("friendResult is null, skipping...");
        }

        System.out.println("\n--------- PRINTING DOT ----------");
        if (printDot) {
            System.out.println(dot(artemisGraph, "artemisGraph"));
            System.out.println(dot(bigOlGraph, "devilGraph"));
        } else {
            System.out.println("PrintDot is false, skipping...");
        }

        System.out.println("\n-------------- DONE --------------");
    }
}
