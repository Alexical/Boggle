import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BoggleSolver {

    private static final int[] SCORE = { 0, 0, 0, 1, 1, 2, 3, 5, 11 };

    private final int[] bt;
    private final HashMap<Integer, String> btWords;

    private int[] tst;
    private final String[] tstWords;
   
    public BoggleSolver(String[] dictionary) {
        String[] words = new String[dictionary.length + 1];
        System.arraycopy(dictionary, 0, words, 1, dictionary.length);
        int n = removeIfShorterThan3OrInvalid(words, dictionary.length);
        int cap = 3 * (countIfShorterThan6(words, n) >> 1);
        bt = new int[896807];
        btWords = new HashMap<>(cap);
        btAdd(bt, btWords, words, n);
        n = removeIfShorterThan6(words, n);
        cap = Integer.highestOneBit(n) << 5;
        tst = new int[cap];
        tstWords = words;
        tstAdd(tst, words, n);
    }
    
    private static int removeIfShorterThan3OrInvalid(String[] words, int n) {
        int j = 1;
        for (int i = 1; i < n; ++i) {
            String word = words[i];
            if (word.length() >= 3 && isValid(word))
                words[j++] = word;
        }
        return j;
    }
    
    private static int removeIfShorterThan6(String[] words, int n) {
        int j = 1;
        for (int i = 1; i < n; ++i) {
            String word = words[i];
            if (word.length() >= 6)
                words[j++] = word;
        }
        return j;
    }
    
    private static int countIfShorterThan6(String[] words, int n) {
        int count = 0;
        for (int i = 1; i < n; ++i)
            if (words[i].length() < 6)
                ++count;
        return count;
    }
    
    private static boolean isValid(String word) {
        int i = 0, last = word.length() - 1;
        while (i < last)
            if (word.charAt(i++) == 'Q')
                if (i < last && word.charAt(i++) != 'U')
                    return false;
        return true;
    }
    
    private static void btAdd
    (int[] bt, HashMap<Integer, String> btWords, String[] words, int n) {
        for (int i = 1; i < n; ++i) {
            String word = words[i];
            int d = 0, x = 0, len = word.length();
            for (int j = 0; j < 5 && d < len; ++j) {
                char c = word.charAt(d);
                x = 27 * x + (c & 0x1f);
                bt[x >> 4] |= (d < len ? 1 : 2) << (x << 1);
                d += c == 'Q' ? 2 : 1;
            }
            if (d == len)
                btWords.put(x, word);
        }
    }
    
    private static void tstAdd(int[] tst, String[] words, int n) {
        if (n <= 1) return;
        int root = (1 + n >> 1);
        int next = tstAdd(tst, words, 0, root, 4);
        int[] stack = new int[64];
        stack[0] = n;
        stack[1] = root + 1;
        stack[2] = root;
        stack[3] = 1;
        int top = 4;
        while (top != 0) {
            int lo = stack[--top];
            int hi = stack[--top];
            if (lo < hi) {
                int mid = (lo + hi >> 1);
                
                next = tstAdd(tst, words, 4, mid, next);
                stack[top++] = hi;
                stack[top++] = mid + 1;
                stack[top++] = mid;
                stack[top++] = lo;
            }
        }
        assert Boolean.TRUE;
    }
    
    private static int tstAdd
    (int[] tst, String[] words, int x, int mid, int next) {
        String word = words[mid];
        int len = word.length();
        int d = 0, p = 0;
        while (x != 0) {
            char c = word.charAt(d);
            int cmp = c - (tst[x] & 0xff);
            p = x | Integer.signum(cmp) + 2;
            if (cmp == 0)
                d += c == 'Q' ? 2 : 1;
            if (d < len)
                x = tst[p];
            else break;
        }
        while (d < len) {
            char c = word.charAt(d);
            x = next;
            tst[p] = x;
            tst[x] = c;
            p = next | 2;
            d += c == 'Q' ? 2 : 1;
            next += 4;
        }
        tst[x] |= mid << 8;
        return next;
    }
    
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        return new WordFinder(board).getAllValidWords();
    }

    public int scoreOf(String word) {
        int len = word.length();
        if (len > 2) // TODO
            return SCORE[Math.min(len, 8)];
        return 0;
    }

    private class WordFinder {

        private final long[] tray;
        private final Set<String> words;

        private WordFinder(BoggleBoard board) {
            int rows = board.rows();
            int cols = board.cols();
            tray = new long[rows * cols];
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < cols; ++j) {
                    long slot = board.getLetter(i, j);
                    for (int di = -1, k = 8; di < 2; ++di) {
                        int ai = i + di;
                        if (ai < 0 || ai >= rows)
                            continue;
                        for (int dj = -1; dj < 2; ++dj) {
                            if (di == 0 && dj == 0)
                                continue;
                            int aj = j + dj;
                            if (aj < 0 || aj >= cols)
                                continue;
                            slot |= (ai * cols + aj & 0x7fL) << k;
                            k += 7;
                        }
                    }
                    tray[i * cols + j] = slot;
                }
            }
            words = new HashSet<>();
            for (int v = 0; v < tray.length; ++v)
                findWords(v, new CharStack());
        }

        private void findWords(int v, CharStack prefix) {
            long slot = tray[v];
            char c = (char) (slot & 0x7f);
            tray[v] |= 1L << 7;
            prefix.push(c);
            String value = "";
            if (value != null) {
                if (value != "")
                    words.add(value);
                for (slot >>>= 8; slot != 0; slot >>>= 7) {
                    int w = (int) slot & 0x7f;
                    if ((tray[w] & 1L << 7) == 0)
                        findWords(w, new CharStack(prefix));
                }
            }
            tray[v] &= ~(1L << 7);
        }

        private Iterable<String> getAllValidWords() {
            return words;
        }

    }

}
