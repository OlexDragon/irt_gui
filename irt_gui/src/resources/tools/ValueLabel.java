package resources.tools;

public class ValueLabel {

	private String value;
	private String label;
	public String getValue() {
		return value;
	}
	public String getLabel() {
		return label;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	@Override
	public String toString() {
		return label;
	}
	@Override
	public boolean equals(Object obj) {
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}
	@Override
	public int hashCode() {
		return value!=null ? value.hashCode() : super.hashCode();
	}
}
