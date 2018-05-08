package EmpiresMod.Datasource.Bridge;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.mysql.jdbc.Driver;

import EmpiresMod.Empires;
import EmpiresMod.Configuration.ConfigProperty;
import EmpiresMod.Configuration.ConfigTemplate;

public class BridgeMySQL extends BridgeSQL {

	public ConfigProperty<String> username = new ConfigProperty<String>("username", "datasource",
			"Username to use when connecting", "");

	public ConfigProperty<String> password = new ConfigProperty<String>("password", "datasource",
			"Password to use when connecting", "");

	public ConfigProperty<String> host = new ConfigProperty<String>("host", "datasource",
			"Hostname (format: 'host:port') to use when connecting", "localhost");

	public ConfigProperty<String> database = new ConfigProperty<String>("database", "datasource", "The database name",
			"Empires");

	public BridgeMySQL(ConfigTemplate config) {
		config.addBinding(username);
		config.addBinding(password);
		config.addBinding(host);
		config.addBinding(database, true);

		initProperties();
		initConnection();
	}

	@Override
	protected void initProperties() {
		autoIncrement = "AUTO_INCREMENT";

		properties.put("autoReconnect", "true");
		properties.put("user", username.get());
		properties.put("password", password.get());
		properties.put("relaxAutoCommit", "true");
	}

	@Override
	protected void initConnection() {
		this.dsn = "jdbc:mysql://" + host.get() + "/" + database.get();

		try {
			DriverManager.registerDriver(new Driver());
		} catch (SQLException ex) {
			Empires.instance.LOG.error("Failed to register driver for MySQL database.", ex);
			Empires.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
		}

		try {
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException ex) {
				} // Ignore since we are just closing an old connection
				conn = null;
			}

			conn = DriverManager.getConnection(dsn, properties);
		} catch (SQLException ex) {
			Empires.instance.LOG.error("Failed to get SQL connection! {}", dsn);
			Empires.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
		}
	}
}