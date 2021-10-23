package Main;


import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;  
import org.apache.log4j.rolling.RollingFileAppender;
import org.apache.log4j.rolling.SizeBasedTriggeringPolicy;
import org.apache.log4j.rolling.RollingPolicy;

import Objetos.Mesa;

/**
 * Esta es la clase principal, la cual tiene el metodo main y funciona como orquestrador.
 */

@SuppressWarnings("unused")
public class Main {

	//-----------------------------------------------------------------------------------
	// Atributos
	//-----------------------------------------------------------------------------------

	/** Atributo que denota la ruta relativa al archivo de propiedades */
	private static final String filePath = "./files/config.properties";
	
	/** Atributo que denota el logger de la clase */
	private static final Logger LOG = Logger.getLogger(Main.class.getName());


	/**
	 * El metodo main
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		// Configura el lugar para escribir los logs
		PropertyConfigurator.configure(filePath);
		LOG.info("El programa comienza");
		
		// Lee el archivo de propiedades
		Reader rd = new Reader(filePath);

		// Crea la conexion con la bd
		BD db = new BD (rd.getProp());

		// Lee el archivo de entrada
		rd.inputReader();
		
		// Crea un archivo para escribir la respuesta
		rd.crearArchivoRespuesta();

		// Para cada mesa calcula los comensales y los escribe en el archivo
		for(Mesa m: rd.getMesas())
		{
			m.comensales(db, rd.getProp().getProperty("encrypt.link"));
			m.igualdad(db,rd);
		}
		
		// Cierra la conexion con la bd
		db.terminarConexion();
		LOG.info("Programa terminado");
	}
}
