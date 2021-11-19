package irt.data.value;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class JSonValueMapper {

	private ScriptEngine engine;

	public JSonValueMapper() {
        ScriptEngineManager sem = new ScriptEngineManager();
        engine = sem.getEngineByName("javascript");
	}

	public List<SonValue> toSonValue(String sonString) throws ScriptException{

		if(sonString==null || sonString.isEmpty())
			return new ArrayList<>();

		String script = "Java.asJSONCompatible(" + sonString.replace(";", "") + ")";
		ScriptObjectMirror result = (ScriptObjectMirror) engine.eval(script);
		return result.entrySet().parallelStream().map(SonValue::new).collect(Collectors.toList());
	}
}
