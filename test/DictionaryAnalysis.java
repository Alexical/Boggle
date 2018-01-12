import java.util.HashMap;
import java.util.Map;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;

public class DictionaryAnalysis {

    private static class Count {
        public int nWords;
        public int nLetters;
    }

    public static void main(String[] args) {
        // int R = Integer.parseInt(args[1]);
        for (int R = 2; R < 6; ++R) {
            In in = new In(args[0]);
            Map<String, Count> counts = new HashMap<>();
            while (in.hasNextLine()) {
                String word = in.readLine();
                if (word.length() > R) {
                    String prefix = word.substring(0, R);
                    Count count = counts.getOrDefault(prefix, new Count());
                    ++count.nWords;
                    count.nLetters += word.length() - R;
                    counts.put(prefix, count);
                }
            }
            String ext = String.format("_%d.csv", R);
            Out out = new Out(args[0].replaceAll("\\.txt", ext));
            for (Map.Entry<String, Count> entry : counts.entrySet()) {
                String prefix = entry.getKey();
                Count count = entry.getValue();
                out.print(String.format("%s\t%d\t%d\n", prefix,
                                        count.nWords, count.nLetters));
            }
        }
    }

}
