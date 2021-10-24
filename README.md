# Cena-para-clientes
Diseñado para la Codigotón de Bancolombia 2021, este código java/sql resuelve el problema planteado.

Mediante un código Java, SQL y librerías Maven, se inicia leyendo un archivo de propiedades, el cual posee datos para la conexión a la DB, la ruta para el archivo de entrada, el Link para desencriptar códigos, entre otros. Al leer el archivo de entrada los datos de cada mesa se guardan en una clase Mesa con un nombre y un HashMap para cada uno de los criterios de esta. 

Posteriormente se realiza la conexión a la base de datos, y en ella se crea una tabla temporal en donde los clientes, se filtran por los criterios específicos de la entrada, se calcula su monto total a raíz de las cuentas, se agrupa por empresa y se ordena por monto total (Esto mediante la generación de un Querry a partir de los criterios de cada mesa). Luego basado en los datos de la tabla temporal ya creada, se organizan los candidatos por balance total y código. Adicionalmente se agrupan por igualdad de género en las mesas, si no es posible se saca al candidato del género predominante con menos balance y se reemplaza por el siguiente candidato. Así sucesivamente hasta completar la mesa darla por cancelada.

Finalmente se guardan los datos en un archivo y se cierra la conexión a la DB. Cabe resaltar que el programa cuenta con un sistema de logs, en el cual deja registrado todo lo que ocurrió en cada ejecución y adicionalmente que para cada método existe un manejo adecuado de las excepciones guardadas en los archivos de logs.

Si se desea ejecutar el código, no se debería necesitar ninguna configuración adicional más allá de que Maven instale las librerías. Para cambiar la conexión a la base de datos o alguna otra configuración dirigirse a: files/config.properties. Para modificar los datos de entrada modificar file/entrada.txt. Para revisar la respuesta de la ejecución dirigirse a files/salida.txt. Para revisar los logs de ejecución dirigirse al directorio logs.
