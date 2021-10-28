package irt.data.value;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class SonValue {

	private final String name;
	private final Object value;

	public SonValue(Entry<String, Object> entry) {

		name = entry.getKey();
		final Object v = entry.getValue();

		value = Optional.of(v).filter(ScriptObjectMirror.class::isInstance)
				.map(ScriptObjectMirror.class::cast)
				.map(Map::entrySet)
				.map(Set::parallelStream)
				.map(stream->stream.map(SonValue::new).collect(Collectors.toList()))
				.map(Object.class::cast)
				.orElse(v);
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "SonValue [name=" + name + ", values=" + value + "]";
	}
}
