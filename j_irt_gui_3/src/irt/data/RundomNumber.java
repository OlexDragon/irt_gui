package irt.data;

public class RundomNumber {

	int number = (int)(Math.random() * (9999 + 1));

	@Override
	public String toString() {
		return "" + number;
	}
}
