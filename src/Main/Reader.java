package Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import Objetos.Mesa;



/**
 * Clase que se encarga de leer y alamcenar temporalmente las propiedades leidas.
 */
public class Reader {

	//-----------------------------------------------------------------------------------
	// Atributos
	//-----------------------------------------------------------------------------------

	/**  Atributo que guarda las propeidades leidas. */
	private Properties prop;

	/**  Atributo que denota la ruta relativa al archivo de propiedades que contiene los datos de conexion a la bd. */
	private String filePath;


	/**  Atributo que denota el logger de la clase. */
	private  final Logger LOG = Logger.getLogger(this.getClass().getName());

	/**  Atributo que denota las mesas registradas. */
	private ArrayList<Mesa> mesas;



	//-----------------------------------------------------------------------------------
	// Constructor
	//-----------------------------------------------------------------------------------

	/**
	 * Instancia un nuevo Reades.
	 *
	 * @param pFilePath La ruta al archivo de propiedades
	 */
	public Reader(String pFilePath) {
		filePath = pFilePath;
		prop = fileReader(filePath);
		mesas = new ArrayList<Mesa>();
	}




	//-----------------------------------------------------------------------------------
	// Metodos
	//-----------------------------------------------------------------------------------




	/**
	 * Este metodo lee las propiedades de conexion del archivo config.properties
	 *
	 * @param pPath Ruta al archivo de propiedades
	 * @return El grupo de propiedades leidas del archivo
	 */
	private  Properties fileReader(String pPath)
	{
		LOG.info("Leyendo el archivo de propiedades...");
		try (InputStream input = new FileInputStream(pPath)) {

			Properties prop = new Properties();

			prop.load(input);

			LOG.info("Termino de leer el archivo de propiedades");
			return prop;

		} catch (IOException ex) {
			ex.printStackTrace();
			LOG.error("Fallo al leer el archivo de propiedades de la db", ex);
			LOG.info("Programa terminado");
			System.exit(1);
			return null;
		}
	}


	/**
	 * Returna las propiedades .
	 *
	 * @return the prop
	 */
	public Properties getProp() {
		return prop;
	}

	/**
	 * Metodo que se encarga de leer el archivo de entrada
	 * Adicionalmente registra las mesas y sus criterios
	 * Usa la ruta escrita en el archivo de propiedades para leer el archivo de entrada.
	 */
	public void inputReader()
	{
		try {
			LOG.info("Leyendo el archivo de datos de entrada...");
			File myObj = new File(prop.getProperty("input.path"));
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				if(data.contains("<"))
				{
					mesas.add(new Mesa(StringUtils.substringBetween(data, "<", ">")));
				}
				else
				{
					String[] criterio = data.split(":");
					mesas.get(mesas.size()-1).agregarCriterio(criterio[0], Double.valueOf(criterio[1]));
				}
			}
			myReader.close();
			LOG.info("Termino de leer el archivo de datos de entrada");
		} catch (Exception e) {
			LOG.error("Fallo al leer el archivo de datos de entrada", e);
			LOG.info("Programa terminado");
			System.exit(1);
		}

	}

	/**
	 * Retorna las mesas.
	 *
	 * @return las mesas
	 */
	public ArrayList<Mesa> getMesas() {
		return mesas;
	}

	/**
	 * Crear archivo respuesta.
	 */
	public void crearArchivoRespuesta()
	{
		try {
			File file = new File(prop.getProperty("output.path"));
			if (file.createNewFile()) {
				LOG.info("Archivo de respuesta creado: " + file.getName());
			} else {
				file.delete();
				file.createNewFile();
				LOG.info("Archivo de respuesta creado: " + file.getName());
			}
		} catch (IOException e) {
			LOG.error("No se pudo crear el archivo de respuesta", e);
			LOG.info("Programa terminado");
			System.exit(1);
		}
	}

	/**
	 * Escribir respuesta con comensales.
	 * Escribe los datos en el archivo de respuesta, el cual se configura en el properties.
	 *
	 * @param pFinalistas the finalistas
	 * @param pMesa the mesa
	 */
	public void escribirRespuestaConComensales(ResultSet pFinalistas, Mesa pMesa)
	{
		try {
			FileWriter myWriter = new FileWriter(prop.getProperty("output.path"),true);
			myWriter.append("<" + pMesa.getNombre() + ">" + '\n');
			while(pFinalistas.next())
			{
				String code = pFinalistas.getString(1);
				
				if(!pFinalistas.isLast())
				{
					myWriter.append(code + ",");
				}
				else
				{
					myWriter.append(code + '\n');
				}
				
			}
			myWriter.close();
		}
		catch (Exception e) {
			LOG.error("No se pudo guardar la respuesta en el archivo", e);
			LOG.info("Programa terminado");
			System.exit(1);
		}

	}

	/**
	 * Escribir respuesta cancelada.
	 * Escribe los datos en el archivo de respuesta, el cual se configura en el properties.
	 *
	 * @param pMesa the mesa
	 */
	public void escribirRespuestaCancelada(Mesa pMesa)
	{
		try {
			FileWriter myWriter = new FileWriter(prop.getProperty("output.path"),true);
			myWriter.append("<" + pMesa.getNombre() + ">" + '\n');
			myWriter.append("CANCELADA" + '\n');
			
			myWriter.close();
		}
		catch (Exception e) {
			LOG.error("No se pudo guardar la respuesta en el archivo", e);
			LOG.info("Programa terminado");
			System.exit(1);
		}

	}

}
