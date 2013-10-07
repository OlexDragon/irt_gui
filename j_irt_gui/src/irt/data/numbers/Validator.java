package irt.data.numbers;

public class Validator {

	public static Double parseDouble(String text){

		Double value = null;

		if(text!=null && !(text=text.trim()).isEmpty()){
			boolean isNegative =  text.charAt(0)=='-';
			String[] split = text.split("\\.");
			text = "";
			switch(split.length){
			case 2:
				if(!(split[1]=split[1].replaceAll("\\D", "")).isEmpty())
					text = "."+split[1];
			case 1:
				if(!(split[0]=split[0].replaceAll("\\D", "")).isEmpty())
					text += split[0];
			}
			if(!text.isEmpty()){
				if(isNegative)
					text = "-"+text;
				value = Double.parseDouble(text);
			}
		}

		return value;
	}
}
