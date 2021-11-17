package org.apiaddicts.apitools.openapi2soapui.error;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationError implements Error {
    private int errorCode;
    private String message;
    private String solution;
    
	public ValidationError(String message, String solution) {
		setCodeAndMessage(message);
        this.solution = solution;
	}

	public ValidationError(String message) {
		setCodeAndMessage(message);
    }
	
	private void setCodeAndMessage(String message) {
		String[] messageParts = message.split("\\|");
		if (messageParts.length > 1) {
			this.errorCode = Integer.parseInt(messageParts[0]);
	        this.message = messageParts[1];
		} else {
			this.errorCode = 0;
	        this.message = message;
		}
	}
}
