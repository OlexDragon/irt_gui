
package irt.data.profile;
class SequenceChecker{

	private Double value;
	private Boolean sequence; // null - no result,  true - increase, false - decrease. 

	public boolean add(Double value) {

		if(value == null)
			return false;

		// First time call
		if(this.value == null){
			this.value = value;
			return true;
		}

		final boolean seq = this.value.compareTo(value) > 0;
		this.value = value;

		// Define type of sequence (decrease or increase)
		if(sequence == null){
			sequence = seq; 
			return true;
		}

		// Check type of the sequence
		return sequence == seq;
	}
	
}