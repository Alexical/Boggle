import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class BoggleClient {

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        for (String word : solver.getAllValidWords(board)) {
            StdOut.println(word);
            score += solver.scoreOf(word);
        }
        StdOut.println("Score = " + score);
    }

}

//int count = 0;
//final long start = System.nanoTime();
//while (System.nanoTime() - start < 5L*1000L*1000L*1000L) {
//    solver.getAllValidWords(new BoggleBoard());
//    ++count;
//}
//StdOut.println(count);
