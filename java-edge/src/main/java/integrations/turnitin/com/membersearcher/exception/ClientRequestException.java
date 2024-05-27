package integrations.turnitin.com.membersearcher.exception;

public class ClientRequestException extends RuntimeException {

	public ClientRequestException() {
	}

	public ClientRequestException(final String message) {
		super(message);
	}

	public ClientRequestException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ClientRequestException(final Throwable cause) {
		super(cause);
	}

	public ClientRequestException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
