package com.takipi.oss.storage;

public class TakipiStorageConfigurationEnvResolver {
	private static final int ENV_STATE_NONE		= -1;
	private static final int ENV_STATE_ESCAPED	= 0;
	private static final int ENV_STATE_IN_NAME	= 1;

	public static String resolveEnv(Object value) {
		if (value == null) {
			return null;
		}
		
		String property = value.toString();
		
		StringBuilder resultBuilder = new StringBuilder(property.length() * 2);
		StringBuilder envNameBuilder = new StringBuilder();
		
		int envState = ENV_STATE_NONE;
		
		for (char c : property.toCharArray()) {
			switch (envState) {
				case ENV_STATE_NONE: {
					if (c == '$')
					{
						envState = ENV_STATE_ESCAPED;
					} else {
						resultBuilder.append(c);
					}
				}
				break;
				
				case ENV_STATE_ESCAPED: {
					if (c == '{') {
						envState = ENV_STATE_IN_NAME;
					} else {
						resultBuilder.append('$');
						
						if (c != '$') {
							resultBuilder.append(c);
							
							envState = ENV_STATE_NONE;
						}
					}
				}
				break;
				
				case ENV_STATE_IN_NAME: {
					if (c != '}') {
						envNameBuilder.append(c);
					} else {
						String envVarValue = System.getenv(envNameBuilder.toString());
						
						if (envVarValue == null) {
							envVarValue = "";
						}
						
						resultBuilder.append(envVarValue);
						
						envNameBuilder = new StringBuilder();
						
						envState = ENV_STATE_NONE;
					}
				}
				break;
			}
		}
		
		if (envState != ENV_STATE_NONE) {
			resultBuilder.append('$');
			
			if (envState == ENV_STATE_IN_NAME) {
				resultBuilder.append('{');
				resultBuilder.append(envNameBuilder);
			}
		}
		
		return resultBuilder.toString();
	}
}
