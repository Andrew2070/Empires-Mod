package EmpiresMod.exceptions.Protection;

/**
 * Exception thrown when getting information from a getter
 */
public class GetterException extends Exception {
	public GetterException(String message) {
		super(message);
	}

	public GetterException(String message, Exception ex) {
		super(message, ex);
	}
}