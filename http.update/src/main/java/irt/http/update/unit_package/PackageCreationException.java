package irt.http.update.unit_package;

import java.io.IOException;

public class PackageCreationException extends RuntimeException {
	private static final long serialVersionUID = 4875959875119986016L;

	public PackageCreationException(String message, IOException exception) {
		super(message, exception);
	}

}
