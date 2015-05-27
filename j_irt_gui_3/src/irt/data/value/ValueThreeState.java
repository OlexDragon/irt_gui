package irt.data.value;

public class ValueThreeState extends ValueBoolean{

	private String thirdValue;

	public ValueThreeState(){
		super(0, 0, 2, 0);
	}

	public ValueThreeState(String firstValue, String secondValue, String thirdValue){
		this();
		falseValue = firstValue;
		prefix = secondValue;
		this.thirdValue = thirdValue;
	}

	@Override
	public String toString(long value) {
		String str;

		if(value < 2)
			str = super.toString(value);
		else if(value == 2){
			if(thirdValue==null)
				str = Long.toString(value);
			else
				str = thirdValue;
		}else
			if(falseValue==null)
				str = Long.toString(value);
			else
				str = falseValue;

		return str;		
	}
}
