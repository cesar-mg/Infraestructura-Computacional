package Cliente;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Random;

import logic.Logica;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Cliente extends Thread{

	/**
	 * Socket para manejar comunicacion con el servidor
	 */
	private Socket servidor;

	/**
	 * Identificador del cliente
	 */
	private int id;

	/**
	 * Nombre del usuario
	 */
	private String nombre;
	/**
	 * Llave publica del servidor, leida del archivo LlavePublicaServidor.txt
	 */
	private PublicKey pkServidor;

	/**
	 * Llave simetrica comunicada al servidor
	 */
	private SecretKey llaveSimetrica;

	/**
	 * Constructor
	 * @param id. Id del cliente.
	 * @param nombres. Nombre del 
	 */
	public Cliente(int id, String nombre)
	{
		this.id = id;
		this.nombre = nombre;
		leerLlave();
	}
	
	/**
	 * Funcion para leer la llave del servidor
	 */
	public void leerLlave()
	{
		String path = "llavePublicaServidor.txt";
		try {

			ObjectInputStream oos = new ObjectInputStream( new FileInputStream(path));
			pkServidor = (PublicKey) oos.readObject();
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * Funcion para generar la conexion con el servidor
	 */
	public void generarConexion()
	{
		try 
		{
			servidor = new Socket("localhost",9999);
			manejarConexion();

		}
		catch(IOException e)
		{
			System.out.println("Error al generar una conexion");
		}
		finally
		{
			try 
			{
				servidor.close();	
			}
			catch (IOException e) 
			{
				System.out.println("Error cerrando el ss");
			}
		}
	}
	
	/**
	 * Funcion para la generacion de la llave simetrica
	 */
	public void generarLlaveSimetrica()
	{
		try {
			KeyGenerator keygen = KeyGenerator.getInstance(Logica.SYMETRIC_ALGORITHM);
			keygen.init(256);
			llaveSimetrica = keygen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/**
	 * Funcion para el manejo de la conexion con el servidor
	 */
	private void manejarConexion()
	{
		try 
		{
			PrintStream out = new PrintStream( servidor.getOutputStream() );
			BufferedReader in = new BufferedReader( new InputStreamReader( servidor.getInputStream() ) );	
			out.println("INICIO");
			out.println(String.valueOf(id));
			if(in.readLine().equals("ACK"))
			{
				String num = "";
				for(int i = 0; i < 24; i++) 
					num += String.valueOf((int) Math.floor(Math.random() * 10));

				out.println(num);
				String numeroRetoCifrado = in.readLine();
				byte[] numeroDecifrado = Logica.descifradoASimetrico(pkServidor, Logica.str2byte(numeroRetoCifrado));
				String numRecibido = new String(numeroDecifrado);

				if(numRecibido.equals(num))
				{
					System.out.println("Comunicacion Aprobada - Reto Correcto");
					generarLlaveSimetrica();
					String simetricaString = new String (Base64.getEncoder().encode(llaveSimetrica.getEncoded()));
					out.println(Logica.byte2str(Logica.cifradoASimetrico(pkServidor, simetricaString)));

					if(in.readLine().equals("ACK"))
					{

						byte[] nombreC = Logica.cifradoASimetrico(pkServidor, nombre);
						out.println(Logica.byte2str(nombreC));

						if(in.readLine().equals("ACK"))
						{
							Random rand = new Random();
							String idPkg ="" + rand.nextInt(32);
							byte[] idPkgC = Logica.cifradoSimetrico(llaveSimetrica, idPkg);
							out.println(Logica.byte2str(idPkgC));

							String respuestaC = in.readLine();
							String respuestaD = new String (Logica.descifradoSimetrico(llaveSimetrica, Logica.str2byte(respuestaC)));

							out.println("ACK");						
							String digest = in.readLine();

							if(digest.equals(Logica.byte2str(Logica.getHMACSHA256(respuestaD,llaveSimetrica))))
							{
								System.out.println("El estado del paquete " + idPkg + " es: " + respuestaD);
								out.println("TERMINAR");
							}
							else
							{
								System.out.println("ALERTA - El contenido ha sido adulterado");
							}
						}
						else
							System.out.println("Comunicacion Terminada - Error: Nombre no encontrado");

					}
					else
						System.out.println("Comunicacion Terminada - Error al comunicar la llave simetrica");

				}
				else
					System.out.println("Comunicacion Terminada - Reto Incorrecto");

			}	
			in.close();
			out.close();
			servidor.close();
			System.out.println("Comunicacion finalizada exitosamente");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	/**
	 * Run para los clientes delegados
	 */
	public void run()
	{
		generarConexion();
	}
	
	/**
	 * Main de la clase
	 */
	public static void main(String[] args)
	{
		String[] nombres = new String[] {"Felipe", "Ricardo","Pepe"};
		Random rand = new Random();
		if(Logica.CONFIG)
		{
			Cliente cliente = new Cliente(1, nombres[rand.nextInt(3)]);
			for(int i = 0; i < Logica.NUMERO_CONSULTAS; i++)
				cliente.generarConexion();
			
		}
		else
		{
			for(int i = 0; i < Logica.NUMERO_CONSULTAS; i++)
			{
				Cliente cliente = new Cliente(i + 1, nombres[rand.nextInt(3)]);
				cliente.start();
			}

		}
	}
}
