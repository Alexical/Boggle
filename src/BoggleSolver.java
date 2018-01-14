import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BoggleSolver {

    private static final int[] SCORE = { 0, 0, 0, 1, 1, 2, 3, 5, 11 };
    private static final int[] TRAY4x4 = computeTray(4, 4);
    private static final Object PRESENT = new Object();
    
    private final String[] words;
    private final HashMap<String, Object> wordMap;
    private final int[] tst;

    public BoggleSolver(String[] dictionary) {
        int n = dictionary.length;
        words = new String[n + 1];
        n = copyIfValid(dictionary, words, n);
        wordMap = initializeWordMap(words, n);
        tst = initializeTst(words, n);
    }
    
    private static int copyIfValid(String[] src, String[] dest, int n) {
        int j = 1;
        for (int i = 1; i < n; ++i) {
            String word = src[i];
            if (word.length() >= 3 && isValid(word))
                dest[j++] = word;
        }
        return j;
    }
    
    private static boolean isValid(String word) {
        int i = 0, last = word.length() - 1;
        while (i < last)
            if (word.charAt(i++) == 'Q')
                if (i < last && word.charAt(i++) != 'U')
                    return false;
        return true;
    }
    
    private static HashMap<String, Object>
    initializeWordMap(String[] words, int n) {
        HashMap<String, Object> map = new HashMap<>(3 * (n >> 1));
        for (int i = 1; i < n; ++i)
            map.put(words[i], PRESENT);
        return map;
    }
    
    private static int[] initializeTst(String[] words, int n) {
        int[] tst = new int[Integer.highestOneBit(n) << 5];
        if (n > 1) {
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
        }
        return tst;
    }
    
    private static int tstAdd
    (int[] tst, String[] words, int x, int mid, int next) {
        String word = words[mid];
        int len = word.length();
        int d = 0, p = 2;
        while (x != 0) {
            int c = word.charAt(d); // TODO
            int cmp = c - (tst[x] & 0xff);
            p = x | Integer.signum(cmp) + 2;
            if (cmp == 0)
                d += c == 'Q' ? 2 : 1;
            if (d < len)
                x = tst[p];
            else break;
        }
        while (d < len) {
            int c = word.charAt(d);
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
    
    private int get(int x, int c) {
        x = tst[x | 2];
        while (x != 0) {
            int cmp = c - (tst[x] & 0xff);
            if (cmp != 0)
                x = tst[x | Integer.signum(cmp) + 2];
            else break;
        }
        return x;
    }
    
    private static int[] computeTray(int rows, int cols) {
        int[] tray = new int[rows * cols << 4];
        Arrays.fill(tray, -1);
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                int base = i * cols + j << 4;
                for (int di = -1, k = 0; di < 2; ++di) {
                    int ai = i + di;
                    if (ai < 0 || ai >= rows)
                        continue;
                    for (int dj = -1; dj < 2; ++dj) {
                        if (di == 0 && dj == 0)
                            continue;
                        int aj = j + dj;
                        if (aj < 0 || aj >= cols)
                            continue;
                        tray[base + k++] = ai * cols + aj;
                    }
                }
            }
        }
        return tray;
    }
    
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        int rows = board.rows();
        int cols = board.cols();
        int[] tray = TRAY4x4;
        if (rows != 4 || cols != 4)
            tray = computeTray(rows, cols);
        int[] letters = new int[rows * cols];
        for (int i = 0, k = 0; i < rows; ++i)
            for (int j = 0; j < cols; ++j, ++k)
                letters[k] = board.getLetter(i, j);
        return findWords(tray, letters);
    }
    
    private Iterable<String> findWords(int[] tray, int[] letters) {
        ArrayList<String> list = new ArrayList<>(1 << 9);
        HashMap<String, Object> map = new HashMap<>(1 << 9);
        int n = letters.length;
        boolean[] marked = new boolean[n];
        int[] stack = new int[n << 2];
        for (int i = 0; i < n; ++i) {
            int c = letters[i];
            int x = get(0, c);
            if (x != 0) {
                int top = 0;
                stack[0] = i << 4;
                stack[1] = i;
                stack[2] = c;
                stack[3] = x;
                marked[i] = true;
                while (top >= 0) {
                    int w = tray[stack[top]];
                    if (w != -1) {
                        ++stack[top | 0];
                        if (!marked[w]) {
                            c = letters[w];
                            x = get(stack[top | 3], c);
                            if (x != 0) {
                                int q = tst[x] >>> 8;
                                if (q != 0) {
                                    String word = words[q];
                                    if (map.put(word, PRESENT) == null)
                                        list.add(word);
                                }
                                top += 4;
                                stack[top | 0] = w << 4;
                                stack[top | 1] = w;
                                stack[top | 2] = c;
                                stack[top | 3] = x;
                                marked[w] = true;
                            }
                        }
                    } else {
                        marked[stack[top | 1]] = false;
                        stack[top | 3] = 0; // TODO
                        stack[top | 2] = 0; // TODO
                        stack[top | 1] = 0; // TODO
                        stack[top | 0] = 0; // TODO
                        top -= 4;
                    }
                }
            }
        }
        return list;
    }

    public int scoreOf(String word) {
        if (wordMap.containsKey(word))
            return SCORE[Math.min(word.length(), 8)];
        return 0;
    }

}
