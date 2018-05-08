package EmpiresMod.exceptions.Protection;

public class SegmentException extends RuntimeException {

	public SegmentException(String message) {
		super(message);
	}

	public SegmentException(String message, Throwable cause) {
		super(message, cause);
	}
}
