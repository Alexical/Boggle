import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BoggleSolver {

    private static final int[] SCORE = { 0, 0, 0, 1, 1, 2, 3, 5, 11 };

    private final int[] bt;
    private final Map<Integer, String> btWords;

    private int[] tst;
    private String[] tstWords;
    private int root, next;

    public BoggleSolver(String[] dictionary) {
        int dictLen = dictionary.length;
        String[] words = new String[dictLen];

        int dictPos = 0, dictEnd = 0, btEnd = 0;
        while (dictEnd < dictLen) {
            for (dictEnd = dictPos; dictEnd < dictLen; ++dictEnd) {
                String word = dictionary[dictEnd];
                int len = word.length();
                if (len < 3)
                    break;
                boolean isValid = true;
                int d = word.indexOf('Q', 0);
                while (d != -1) {
                    if (d + 1 == len || word.charAt(d + 1) != 'U') {
                        isValid = false;
                        break;
                    } else d = word.indexOf('Q', d + 1);
                }
                if (!isValid)
                    break;
            }
            if (dictEnd > dictPos) {
                int len = dictEnd - dictPos;
                System.arraycopy(dictionary, dictPos, words, btEnd, len);
                dictPos += len;
                btEnd += len;
            } else ++dictPos;
        }

        // int btEnd = 0;
        int btCount = 0;
        // for (int i = 0; i < dictlen; ++i) {
        // String word = dictionary[i];
        // int len = word.length();
        // if (len > 2) {
        // boolean isValid = true;
        // int d = word.indexOf('Q', 0);
        // while (d != -1) {
        // if (d + 1 == len || word.charAt(d + 1) != 'U') {
        // isValid = false;
        // break;
        // } else d = word.indexOf('Q', d + 1);
        // }
        // if (isValid) {
        // System.arraycopy(dictionary, 0, words, 0, 30);
        // words[btEnd++] = word;
        // if (len < 6)
        // ++btCount;
        // }
        // }
        // }

        int[] bt = new int[896807];
        Map<Integer, String> btWords = new HashMap<>(btCount << 1);
        for (int i = 0; i < btEnd; ++i) {
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
        this.bt = bt;
        this.btWords = btWords;

        int tstEnd = 0;
        for (int i = 0; i < btEnd; ++i) {
            String word = words[i];
            if (word.length() > 5)
                words[tstEnd++] = word;
        }

        int cap = Integer.highestOneBit(tstEnd) << 2;
        int[] tst = new int[cap];
        String[] tstWords = new String[cap];
        int[] stack = new int[64];
        stack[1] = tstEnd;
        int root = 0, next = 4, top = 2;
        while (top != 0) {
            int hi = stack[--top];
            int lo = stack[--top];
            if (lo < hi) {
                int mid = lo + hi >> 1;
                char[] word = words[mid].toCharArray();
                int d = 0, x = root;
                while (true) {
                    char c = word[d];
                    if (x == 0) {
                        if (next == cap) {
                            cap <<= 1;
                            tst = Arrays.copyOf(tst, cap);
                            tstWords = Arrays.copyOf(tstWords, cap);
                        }
                        x = next;
                        next += 4;
                        tst[x] = c;
                    }
                    int cmp = c - (char) tst[x];
                    if (cmp < 0) {

                    }
                    break;
                }
                stack[top++] = lo;
                stack[top++] = mid;
                stack[top++] = mid + 1;
                stack[top++] = hi;
            }
        }
        this.tst = tst;
        this.tstWords = tstWords;
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
