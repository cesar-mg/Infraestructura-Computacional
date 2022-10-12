package Servidor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import logic.Logica;

public class Servidor extends Thread{

	/**
	 * Ss donde los clientes solicitan conexión
	 */
	private ServerSocket ss;

	/**
	 * TH que maneja los paquetes
	 */
	private Hashtable<Integer, String> paquetes;

	/**
	 *  TH que maneja los nombres y los paquetes que tiene asociado cada nombre.
	 */
	private Hashtable<String,ArrayList<Integer>> nombres;

	/**
	 * Lista de estados posibles de un paquete
	 */
	private String[] estados;

	private String[] names;

	/**
	 * Llave publica del servidor
	 */
	private PublicKey llavePublica;

	/**
	 * Llave privada del servidor
	 */
	private PrivateKey llavePrivada;

	/**
	 * Llave simetrica a utilizar
	 */
	private SecretKey llaveSimetrica;
	
	/**
	 * Socket que representa el cliente que va a manejar el servidor delegado
	 */
	private Socket cliente;
	
	/**
	 * Constructor
	 */
	public Servidor()
	{

	}

	/**
	 * Constructor para los threads
	 */

	public Servidor(Hashtable<Integer, String> paquetes,  Hashtable<String, ArrayList<Integer>> nombres, PublicKey llaveP, PrivateKey llavePriv, Socket cliente)
	{
		this.paquetes = paquetes;
		this.nombres = nombres;
		llavePublica = llaveP;
		llavePrivada = llavePriv;
		this.cliente = cliente;
	}
	public void inicializar()
	{
		paquetes = new Hashtable<Integer, String>(40);
		nombres = new Hashtable<String,ArrayList<Integer>>();
		estados = new String[]{"PKT_EN_OFICINA", "PKT_RECOGIDO", "PKT_EN_CLASIFICACION", "PKT_DESPACHADO", "PKT_EN_ENTREGA", "PKT_ENTREGADO", "PKT_DESCONOCIDO"};
		names = new String[] {"Felipe", "Ricardo", "Pepe"};
		nombres.put("Ricardo", new ArrayList<Integer>());
		nombres.put("Felipe", new ArrayList<Integer>());
		for(int i = 0; i < 32; i++)
		{
			Random rand = new Random();
			paquetes.put(i, estados[rand.nextInt(6)]);
			nombres.get(names[rand.nextInt(2)]).add(i);
		}

		generarLlavesAsimetricas();
	}
	
	/**
	 * Funcion para la generacion de la llave asimetrica
	 */
	public void generarLlavesAsimetricas()
	{
		try {
			KeyPairGenerator keygen = KeyPairGenerator.getInstance(Logica.ASYMETRIC_ALGORITHM);
			keygen.initialize(1024);
			KeyPair keyPair = keygen.generateKeyPair();
			llavePublica = keyPair.getPublic();
			llavePrivada = keyPair.getPrivate();
			generarArchivo();

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * Funcion para generar el archivo que contiene la llave publica del servidor
	 */
	public void generarArchivo()
	{
		String path = "llavePublicaServidor.txt";
		File f = new File(path);
		f.delete();
		try {

			ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream(path));
			oos.writeObject(llavePublica);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/**
	 * Funcion que representa el manejo de la comunicacion, dependiendo de la configuracion elegida
	 */
	public void aceptarConexiones()
	{
		try 
		{
			inicializar();
			ss = new ServerSocket(9999);
			if(Logica.CONFIG)
			{
				while(true)
				{
					Socket nuevoCliente = ss.accept();	
					manejarCliente(nuevoCliente);
				}
			}
			else
			{
				while(true)
				{
					Socket nuevoCliente = ss.accept();
					Servidor n = new Servidor(paquetes, nombres, llavePublica, llavePrivada, nuevoCliente);
					n.start();
				}
			}
		}
		catch(IOException e)
		{
			System.out.println("Error al recibir conexiones");
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				if(ss != null)
					ss.close();
			}
			catch (NullPointerException | IOException e) 
			{
				System.out.println("Error cerrando el ss");
			}
		}
	}
	
	/**
	 * Run para los servidores delegados
	 */
	@Override
	public void run()
	{
		manejarCliente(cliente);
	}
	
	/**
	 * Funcion que maneja la comunicacion
	 * @param nuevoCliente. Socket que representa el cliente a manejar.
	 */
	private void manejarCliente(Socket nuevoCliente)
	{
		try 
		{
			PrintStream out = new PrintStream( nuevoCliente.getOutputStream() );
			BufferedReader in = new BufferedReader( new InputStreamReader( nuevoCliente.getInputStream() ) );
			String id = "";
			String nombre = "";
			if(in.readLine().equals("INICIO"))
			{
				id = in.readLine();
				System.out.println("Comunicacion iniciada con el cliente: " + id + " ");
				out.println("ACK");
				String numReto = in.readLine();
				byte[] numRetoCifrado = Logica.cifradoASimetrico(llavePrivada, numReto);
				out.println(Logica.byte2str(numRetoCifrado));

				byte[] decodedKey = Base64.getDecoder().decode(Logica.descifradoASimetrico(llavePrivada,Logica.str2byte(in.readLine())));
				llaveSimetrica = new SecretKeySpec(decodedKey, 0, decodedKey.length, Logica.SYMETRIC_ALGORITHM); 
				out.println("ACK");

				String nombreC = in.readLine();
				nombre = new String (Logica.descifradoASimetrico(llavePrivada, Logica.str2byte(nombreC)));
				if(nombres.containsKey(nombre))
				{
					out.println("ACK");

					String idPkgC = in.readLine();
					String idPkgD = new String (Logica.descifradoSimetrico(llaveSimetrica, Logica.str2byte(idPkgC)));
					String status = paquetes.get(Integer.parseInt(idPkgD));
					if(nombres.get(nombre).contains(Integer.parseInt(idPkgD)))
					{
						out.println(Logica.byte2str(Logica.cifradoSimetrico(llaveSimetrica, status)));
						if(in.readLine().equals("ACK"))
						{
							//DIGEST
							byte[] digest = Logica.getHMACSHA256(status,llaveSimetrica); 
							out.println(Logica.byte2str(digest));
						}
					}

					else
					{
						out.println(Logica.byte2str(Logica.cifradoSimetrico(llaveSimetrica, "PKT_DESCONOCIDO")));
						if(in.readLine().equals("ACK"))
						{
							//DIGEST
							byte[] digest = Logica.getHMACSHA256("PKT_DESCONOCIDO",llaveSimetrica); 
							out.println(Logica.byte2str(digest));
						}
					}
					if(!in.readLine().equals("TERMINAR"))
						System.out.println("Error: Mensaje de finalizacion no encontrado");


				}
				else
				{
					System.out.println("Comunicacion terminada con el cliente con id: " + id + " Nombre no encontrado");
					out.print("ERROR");
				}
			}
			else
			{
				System.out.println("Comunicacion terminada con el cliente con id: " + id + " - Falta el Mensaje de inicio");
			}
			in.close();
			out.close();
			nuevoCliente.close();
			System.out.println(" Comunicacion con el cliente: " + nombre + " con id: " + id + " finalizada exitosamente");
			System.out.println(" ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ----");
			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Main del servidor
	 */
	public static void main(String[] args)
	{
		Servidor servidor = new Servidor();
		servidor.aceptarConexiones();
	}
}
