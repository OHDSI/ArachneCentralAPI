package com.odysseusinc.arachne.portal.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class PortalAuthMethodConfig {

	private final Map<String, Provider> provider = new HashMap<>();

	public Map<String, Provider> getProvider() {
		return this.provider;
	}


	public static class Provider {
		private String text;
		private String image;

		public void setText(String text) {
			this.text = text;
		}

		public void setImage(String image) {
			this.image = image;
		}

		public String getText() {
			return text;
		}

		public String getImage() {
			return image;
		}
	}

}
