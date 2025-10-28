package irt.gui.web;

import java.util.prefs.Preferences;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class Gui4Config implements WebMvcConfigurer {

	@Value("${irt.message}")
	private String message;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/webjars/**").addResourceLocations("/webjars/");
	}

	@Bean("prefs")
	public Preferences prefs() {
		return Preferences.userRoot().node(Gui4.class.getName());
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(
				requests -> requests.requestMatchers(
						new AntPathRequestMatcher("/"),
						new AntPathRequestMatcher("/css/**"),
						new AntPathRequestMatcher("/qr-code/**"),
						new AntPathRequestMatcher("/images/**"),
						new AntPathRequestMatcher("/webjars/**"),
						new AntPathRequestMatcher("/js/**"),
						new AntPathRequestMatcher("/fragment/**"),
						new AntPathRequestMatcher("/connection/**"),
						new AntPathRequestMatcher("/r-login"),
						new AntPathRequestMatcher("/exit"),
						new AntPathRequestMatcher("/serial/**")).permitAll().anyRequest().authenticated())
				.formLogin((form) -> form.loginPage("/login").permitAll()).logout((logout) -> logout.permitAll())
				.csrf().disable();
		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		final String p = message.replaceAll("\\s+", "");
		final String[] split = message.split("\\s");
		final String p2 = split[0] + split[split.length-1];
		// The builder will ensure the passwords are encoded before saving in memory
		@SuppressWarnings("deprecation")
		UserBuilder users = User.withDefaultPasswordEncoder();
		UserDetails user = users
			.username("user")
			.password(p)
			.roles("USER")
			.build();
		UserDetails admin = users
			.username("admin")
			.password(p2)
			.roles("USER", "ADMIN")
			.build();
		return new InMemoryUserDetailsManager(user, admin);
	}
}