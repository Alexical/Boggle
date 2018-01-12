
public class Bytecode {
	
	public int local(int[] arr) {
		int sum = 0;
		int len = arr.length;
		for (int i = 0; i < len; ++i)
			sum += i;
		return sum;
	}
	
	public int inline(int[] arr) {
		int sum = 0;
		for (int i = 0; i < arr.length; ++i)
			sum += i;
		return sum;
	}

}
