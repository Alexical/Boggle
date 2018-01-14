import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class BoggleSolver {

    private static final int[] SCORE = { 0, 0, 0, 1, 1, 2, 3, 5, 11 };
    private static final int[] TRAY4x4 = computeTray(4, 4);
    private static final Object PRESENT = new Object();
    private static final int ABSENT = 0;
    private static final int PREFIX = 1;
    private static final int WORD = 2;
    
    private HashMap<String, Object> wordMap;
    
    private int[] bt;
    private HashMap<Integer, String> btWords;

    private int[] tst;
    private String[] tstWords;

    public BoggleSolver(String[] dictionary) {
        int n = dictionary.length;
        String[] words = new String[n + 1];
        System.arraycopy(dictionary, 0, words, 1, n);
        n = removeIfShorterThan3OrInvalid(words, n);
        initializeWordMap(words, n);
        initializeBt(words, n);
        n = removeIfShorterThan6(words, n);
        initializeTst(words, n);
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
    
    private void initializeWordMap(String[] words, int n) {
        wordMap = new HashMap<>(3 * (n >> 1));
        for (int i = 1; i < n; ++i)
            wordMap.put(words[i], PRESENT);
    }
    
    private void initializeBt(String[] words, int n) {
        bt = new int[896807];
        btWords = new HashMap<>(3 * (countIfShorterThan6(words, n) >> 1));
        for (int i = 1; i < n; ++i) {
            String word = words[i];
            int d = 0, x = 0, len = word.length();
            for (int j = 0; j < 5 && d < len; ++j) {
                int c = word.charAt(d);
                x = 27 * x + (c & 0x1f);
                bt[x >> 4] |= (d < len ? PREFIX : WORD) << (x << 1);
                d += c == 'Q' ? 2 : 1;
            }
            if (d == len)
                btWords.put(x, word);
        }
    }
    
    private void initializeTst(String[] words, int n) {
        tst = new int[Integer.highestOneBit(n) << 5];
        tstWords = words;
        if (n <= 1) return;
        int root = (1 + n >> 1);
        int next = tstAdd(0, root, 4);
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
                next = tstAdd(4, mid, next);
                stack[top++] = hi;
                stack[top++] = mid + 1;
                stack[top++] = mid;
                stack[top++] = lo;
            }
        }
    }
    
    private int tstAdd(int x, int mid, int next) {
        String word = tstWords[mid];
        int len = word.length();
        int d = 0, p = 0;
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
        ArrayList<String> list = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        int n = tray.length;
        boolean[] marked = new boolean[n];
        int[] stack = new int[n << 2];
        for (int i = 0; i < n; ++i) {
            int c = letters[i];
            int x = c & 0x1f;
            int top = 0;
            stack[0] = i << 4;    // j
            stack[1] = i;         // v
            stack[2] = c;         // c
            stack[3] = x;         // x
            marked[i] = true;
            while (top >= 0) {
                int w = tray[stack[top]];
                if (w != -1) {
                    ++stack[top | 0];   // ++j
                    if (!marked[w]) {
                        if (top < 20) {
                            
                        } else if (top == 24) {
                            
                        } else {
                            
                        }
                        c = letters[w];
                        x = 27 * stack[top | 3] + (c & 0x1f);
                        int q = (bt[x >> 4] >> (x << 1)) & 0b11;
                        if (q != ABSENT) {
                            if (q == WORD) {
                                String word = btWords.get(x);
                                if (map.put(word, PRESENT) == null)
                                    list.add(word);
                            }
                            top += 4;
                            stack[top | 0] = w << 4;    // j
                            stack[top | 1] = w;         // v
                            stack[top | 2] = c;         // c
                            stack[top | 3] = x;         // x
                            marked[w] = true;
                        }
                    }
                } else {
                    marked[stack[top | 1]] = false; // marked[v] = false
                    stack[top | 3] = 0; // TODO
                    stack[top | 2] = 0; // TODO
                    stack[top | 1] = 0; // TODO
                    stack[top | 0] = 0; // TODO
                    top -= 4;
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

    private class WordFinder {

        private final long[] tray;
        private final HashSet<String> words;

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
