package EmpiresMod.API.Commands.Command;

/**
 * This is what is returned after a command finished its processing.
 */
public enum CommandResponse {

	/**
	 * Command finished processing successfully, no post-processing needed.
	 */
	DONE,

	/**
	 * Command is not supposed to be called, post-processing by sending the help
	 * message.
	 */
	SEND_HELP_MESSAGE,

	/**
	 * Command is supposed to be called but the arguments given are invalid or
	 * insufficient
	 */
	SEND_SYNTAX
}