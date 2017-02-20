package irt.data.value.enumes;
public enum ValueStatus {

	UNKNOWN		("<?>"),
	IN_RANGE	(""),
	UNDER_RANGE	("<"),
	OVER_RANGE	(">"),
	RANGE_SET	(null);

	private final String str;

	private ValueStatus(String str){
		this.str = str;
	}

	@Override
	public String toString(){
		return str;
	}
}
