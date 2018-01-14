import java.util.Arrays;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdStats;

public class BoggleClient {

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int n = 1000;
        int[] scores = new int[n];
        for (int i = 0; i < n; ++i) {
            int score = 0;
            for (String word : solver.getAllValidWords(new BoggleBoard()))
                ++score;
            scores[i] = score;
        }
        Arrays.sort(scores);
        StdOut.println(Arrays.toString(scores));
        StdOut.printf("max: %d\n", StdStats.max(scores));
        StdOut.printf("mean: %f\n", StdStats.mean(scores));
    }

}

//int count = 0;
//final long start = System.nanoTime();
//while (System.nanoTime() - start < 5L*1000L*1000L*1000L) {
//    solver.getAllValidWords(new BoggleBoard());
//    ++count;
//}
//StdOut.println(count);
