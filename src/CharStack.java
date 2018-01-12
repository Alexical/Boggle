import java.util.Arrays;
import java.util.NoSuchElementException;

public class CharStack implements CharSequence {

    private char[] value;
    private int length;
    private int hash;

    public CharStack()
    { value = new char[8]; }

    public CharStack(CharStack other) {
        value = other.value;
        length = other.length;
        hash = other.hash;
    }

    public CharStack(String other) {
        value = other.toCharArray();
        length = other.length();
        hash = other.hashCode();
    }

    public boolean isEmpty()
    { return length == 0; }

    public int size()
    { return length; }

    public void push(char c) {
        if (length == value.length)
            value = Arrays.copyOf(value, length << 1);
        value[length++] = c;
        hash = 31 * hash + c;
    }

    public char pop() {
        if (length != 0)
            return value[--length];
        throw new NoSuchElementException();
    }

    public char peek() {
        if (length != 0)
            return value[length - 1];
        throw new NoSuchElementException();
    }

    public void clear()
    { length = 0; }

    @Override
    public int length()
    { return length; }

    @Override
    public char charAt(int index) {
        if (index < length)
            return value[index];
        throw new IndexOutOfBoundsException();
    }

    @Override
    public CharSequence subSequence(int start, int end)
    { throw new UnsupportedOperationException(); }

    @Override
    public int hashCode()
    { return hash; }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj.getClass() == this.getClass()) {
            CharStack other = (CharStack) obj;
            if (length != other.length)
                return false;
            for (int i = 0; i < length; ++i)
                if (value[i] != other.value[i])
                    return false;
            return true;
        }
        return false;
    }

    @Override
    public String toString()
    { return String.valueOf(value, 0, length); }

}
