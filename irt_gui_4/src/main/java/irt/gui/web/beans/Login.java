package irt.gui.web.beans;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public class Login {

	private final String username;
	private final String password;
}
