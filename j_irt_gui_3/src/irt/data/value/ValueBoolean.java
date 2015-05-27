package irt.data.value;

public class ValueBoolean extends Value{

	protected String falseValue;

	public ValueBoolean(){
		super(0, 0, 1, 0);
	}

	public ValueBoolean(String falseValue, String trueValue){
		this();
		this.falseValue = falseValue;
		prefix = trueValue;
	}

	protected ValueBoolean(int value, int minValue, int maxValue, int precision) {
		super(value, minValue, maxValue, precision);
	}

	@Override
	public String toString(long value) {
		String str;
		if(value==0){
			if(falseValue==null)
				str = Long.toString(value);
			else
				str = falseValue;
		}else{
			if(falseValue==null)
				str = Long.toString(value);
			else
				str = prefix;
		}
		return str;
	}
}
