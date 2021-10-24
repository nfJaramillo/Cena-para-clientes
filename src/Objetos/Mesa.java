package Objetos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import Main.BD;
import Main.Reader;




/**
 * Clase que se encarga de registrar las mesas y sus criterios.
 */
public class Mesa {

	/** Atributo que denora el nombre de la mesa, ej. Mesa 1 */
	private String nombre;

	/**  Hash map que realciona el nombre con el valor de cada uno de los criterios. */
	private  HashMap<String, Double> criterios;

	/**  Atributo que denota el logger de la clase. */
	private  final Logger LOG = Logger.getLogger(this.getClass().getName());

	/**
	 * Instacia una nueva mesa con un nombre.
	 *
	 * @param pNombre the nombre
	 */
	public Mesa(String pNombre) {
		LOG.info("Nueva mesa registrada: " + pNombre);
		nombre = pNombre;
		criterios = new HashMap<String, Double>();
	}

	/**
	 * Agregar criterio, metodo por el cual se adiciona un nuevo criterio con un nombre y un valor .
	 *
	 * @param pNombre Nombre del criterio
	 * @param pValor Valor del criterio
	 */
	public void agregarCriterio(String pNombre, Double pValor)
	{
		LOG.info("Nuevo criterio registrado: " + pNombre + " : " + String.valueOf(pValor));
		criterios.put(pNombre, pValor);
	}


	/**
	 * Gets the criterios.
	 *
	 * @return Los criterios
	 */
	public HashMap<String, Double> getCriterios() {
		return criterios;
	}

	/**
	 * Gets the nombre.
	 *
	 * @return El nombre
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Comensales
	 * Este metodo genera una tabla temporal en la base de datos, donde los clientes
	 * ya se filtran por los criterios especificos de la entrada, se calcula su monton total a raiz de las cuentas
	 * se agrupa por empresa y se ordena por monto total.
	 * 
	 * Adicionalmente se desencriptan los codigos necesarios de los candidatos a la mesa y se modifican
	 * unicamente en la tabla temporal.
	 *
	 * @param pBD the bd
	 */
	public void comensales(BD pBD, String pLnkDecrypt){
		try {

			String querry = "CREATE TEMPORARY TABLE IF NOT EXISTS temp AS(\r\n"
					+ "SELECT client_id, code, MAX(balance) AS balance, male, company, ENCRYPT\r\n"
					+ "FROM (\r\n"
					+ "\r\n"
					+ "SELECT client_id, code, SUM(balance) AS balance, male, company, encrypt\r\n"
					+ "FROM client c\r\n"
					+ "INNER JOIN account a\r\n"
					+ "ON c.id = a.client_id\r\n";

			if(criterios.containsKey("TC") && criterios.containsKey("UG"))
			{
				querry += "WHERE TYPE = "+ criterios.get("TC") +" AND location = " + criterios.get("UG") + "\r\n";
			}
			else if(criterios.containsKey("TC"))
			{
				querry += "WHERE TYPE = "+ criterios.get("TC") + "\r\n";
			}
			else if(criterios.containsKey("UG"))
			{
				querry += "WHERE location = "+ criterios.get("UG") + "\r\n";
			}

			querry += "GROUP BY client_id\r\n"
					+ "ORDER BY SUM(balance) DESC\r\n"
					+ ") AS initial \r\n";

			if(criterios.containsKey("RI") && criterios.containsKey("RF"))
			{
				querry += "WHERE balance > "+ criterios.get("RI") +" AND balance < " + criterios.get("RF") + "\r\n";
			}
			else if(criterios.containsKey("RI"))
			{
				querry += "WHERE balance > "+ criterios.get("RI") + "\r\n";
			}
			else if(criterios.containsKey("RF"))
			{
				querry += "WHERE balance < "+ criterios.get("RF") + "\r\n";
			}

			querry += "GROUP BY company\r\n"
					+ "ORDER BY MAX(balance) DESC\r\n"
					+ ");";

			pBD.execute(querry);

			ResultSet encriptados = pBD.executeSelect("SELECT CODE\r\n"
					+ "FROM temp\r\n"
					+ "WHERE ENCRYPT = 1;");


			while(encriptados.next())
			{
				String encripted = encriptados.getString(1);
				URL url = new URL("https://test.evalartapp.com/extapiquest/code_decrypt/" + encripted );
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				con.setRequestMethod("GET");
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String decripted = in.readLine();
				con.disconnect();
				pBD.execute("UPDATE temp\r\n"
						+ "SET CODE = '" + StringUtils.substringBetween(decripted, "\"", "\"") + "'  \r\n"
						+ "WHERE CODE = '" + encripted + "';");
			}




		} catch (Exception e) {
			LOG.error("Fallo intentar ejecutar los filtros sobre la bd", e);
			LOG.info("Programa terminado");
			System.exit(1);
		}
	}

	/**
	 * Igualdad.
	 * Basado en los datos de la tabla temporal ya creada, se organizan los candidatos por balance total y codigo.
	 * Adicionalmente se agrupan por igualdad de genero en las mesas, si no es posible se saca al candidato del genero
	 * predominante con menos balance y se reemplaza por el siguiente candidato. Asi sucesivamente hasta completar la mesa
	 * o darla por cancelada.
	 * 
	 * Adicionalmente aqui se guardan los resultados en el archivo esperado.
	 *
	 * @param pBD the bd
	 * @param pReader the reader
	 */
	public void igualdad(BD pBD, Reader pReader)
	{
		try {
			ResultSet finalistas0 = pBD.executeSelect("SELECT CLIENT_ID\r\n"
					+ "FROM(\r\n"
					+ "SELECT MALE, CLIENT_ID\r\n"
					+ "FROM TEMP\r\n"
					+ "ORDER BY balance DESC, CODE ASC\r\n"
					+ "LIMIT 8\r\n"
					+ ") AS A\r\n"
					+ "WHERE MALE = 0");

			ResultSet finalistas1 = pBD.executeSelect("SELECT CLIENT_ID\r\n"
					+ "FROM(\r\n"
					+ "SELECT MALE, CLIENT_ID\r\n"
					+ "FROM TEMP\r\n"
					+ "ORDER BY balance DESC, CODE ASC\r\n"
					+ "LIMIT 8\r\n"
					+ ") AS A\r\n"
					+ "WHERE MALE = 1");

			finalistas0.last();
			int cant0 = finalistas0.getRow();


			finalistas1.last();
			int cant1 = finalistas1.getRow();

			if(cant0 + cant1 < 4 )
			{
				LOG.info("La " + nombre + " queda CANCELADA");
				pReader.escribirRespuestaCancelada(this);
				pBD.execute("DROP table temp;");
				return;
			}
			else if(cant0 == cant1)
			{
				LOG.info("La " + nombre + " esta LISTA");
				
				ResultSet finalistas = pBD.executeSelect("SELECT  CODE\r\n"
						+ "FROM TEMP\r\n"
						+ "ORDER BY balance DESC, CODE ASC\r\n"
						+ "LIMIT 8");
				
				pReader.escribirRespuestaConComensales(finalistas, this);
				pBD.execute("DROP table temp;");
				return;
			}
			else if(cant0 > cant1)
			{
				pBD.execute("DELETE FROM TEMP\r\n"
						+ "WHERE CLIENT_ID = " + finalistas0.getInt(1));
			}
			else 
			{
				pBD.execute("DELETE FROM TEMP\r\n"
						+ "WHERE CLIENT_ID = " + finalistas1.getInt(1));
			}
			
			igualdad(pBD,pReader);


		} catch (Exception e) {
			LOG.error("Fallo intentar ejecutar el algoritmo de igualdad", e);
			LOG.info("Programa terminado");
			System.exit(1);
		}
	}

}
