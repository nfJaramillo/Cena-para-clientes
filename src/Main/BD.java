package Main;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.log4j.Logger;


/**
 * Esta clase conetiene la lectura del archivo de propiedades, la conexion a la base de datos y metodos para la ejecucion de querries.
 */
public class BD {

	//-----------------------------------------------------------------------------------
	// Atributos
	//-----------------------------------------------------------------------------------

	/** La cosntante del driver para mariadb de jdbc */
	private static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";

	/** La url de conexion a la db. */
	private String Db_url;

	/** El usuario de conexion a la bd */
	private String User;

	/** La contraseña de conexion a la bd */
	private String Pass;

	/** La conexion a la bd */
	private Connection conn;

	/** Atributo que denota el logger de la clase */
	private  final Logger LOG = Logger.getLogger(this.getClass().getName());

	//-----------------------------------------------------------------------------------
	// Constructor
	//-----------------------------------------------------------------------------------

	/**
	 * Instancia una nueva conexion a la bd
	 *
	 * @param filePath: Ruta al archivo de propiedades con los datos de conexion
	 */
	public BD(Properties prop ) {


		Db_url = prop.getProperty("db.url");
		User = prop.getProperty("db.user");
		Pass = prop.getProperty("db.password");

		try {

			//Registra el driver de jdbc
			Class.forName(JDBC_DRIVER);

			// Abre la nueva conexion
			LOG.info("Conectado a la bd...");
			conn = DriverManager.getConnection(Db_url, User, Pass);
			LOG.info("Conectado exitosamente a la bd");
		} catch (SQLException se) {
			//Maneja errores de sql
			LOG.error("Fallo al conectar a la bd", se);
			LOG.info("Programa terminado");
			System.exit(1);

		} catch (Exception e) {
			//Maneja errores adicionales 
			e.printStackTrace();
			LOG.error("Fallo al conectar a la bd", e);
			LOG.info("Programa terminado");
			System.exit(1);


		}
	}

	//-----------------------------------------------------------------------------------
	// Metodos
	//-----------------------------------------------------------------------------------

	/**
	 * Este metodo recibe un string con un querry de select para ejecutar sobre la db
	 *
	 * @param pSql String con el querry
	 */
	public ResultSet executeSelect(String pSql)
	{
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(pSql);
			return rs;

		} catch (SQLException se) {
			LOG.error("Fallo al ejecutar el querry: " + pSql, se);
			return null;

		} catch (Exception e) {
			LOG.error("Fallo al ejecutar el querry: " + pSql, e);
			return null;
		}
	}
	
	/**
	 * Este metodo recibe un string con un querry  para ejecutar sobre la db
	 *
	 * @param pSql String con el querry
	 */
	public void execute(String pSql)
	{
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.execute(pSql);

		} catch (SQLException se) {
			LOG.error("Fallo al ejecutar el querry: " + pSql, se);

		} catch (Exception e) {
			LOG.error("Fallo al ejecutar el querry: " + pSql, e);


		}
	}


	/**
	 * Este metodo imprime en consola los resultados del select
	 * Actualmente no esta en uso, pero es util para efectos de debug
	 *
	 * @param pResultSet Conjunto de resultados del select 
	 * @throws SQLException 
	 */
	public void printSelect(ResultSet pResultSet) throws SQLException 
	{
		ResultSetMetaData rsmd =  pResultSet.getMetaData();
		int columns = rsmd.getColumnCount();
		while (pResultSet.next()) {
			for (int i = 1; i <= columns; i++) {
				if (i > 1) System.out.print(",  ");
				String columnValue = pResultSet.getString(i);
				System.out.print(columnValue);
			}
			System.out.println("");
		}


	}



	/**
	 * Este metodo se encargar de cortar la conexion con la bd
	 */
	public void terminarConexion()
	{
		try {
			conn.close();
		} catch (SQLException se) {
			LOG.error("Fallo al terminar la conexion con la bd", se);
			LOG.info("Programa terminado");
			System.exit(1);

		} catch (Exception e) {
			LOG.error("Fallo al terminar la conexion con la bd", e);
			LOG.info("Programa terminado");
			System.exit(1);

		}
	}




}
