package EmpiresMod.Datasource.Schematics;

import java.util.ArrayList;
import java.util.List;

import EmpiresMod.Datasource.Bridge.BridgeSQL;

/**
 * Retains information about the changes that have occurred in the database to
 * support backwards compatibility. Extend this and add to it all the DBUpdates
 * you want. This has been isolated because of the amount of lines the updates
 * can have.
 */
public abstract class BaseSchematic {

	protected List<DBUpdate> updates = new ArrayList<DBUpdate>();

	public abstract void initializeUpdates(BridgeSQL bridge);

	public class DBUpdate {
		/**
		 * Formatted mm.dd.yyyy.e where e increments by 1 for every update
		 * released on the same date
		 */
		public final String id;
		public final String desc;
		public final String statement;

		public DBUpdate(String id, String desc, String statement) {
			this.id = id;
			this.desc = desc;
			this.statement = statement;
		}
	}
}