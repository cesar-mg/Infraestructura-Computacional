package logic;

import java.security.*;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

public class Logica {
	
	// --------------------------------------------------------------------
	// CONSTANTES DE ALGORITMOS
	// --------------------------------------------------------------------
	
	
	public final static String SYMETRIC_ALGORITHM_PADDING = "AES/ECB/PKCS5Padding";
	
	public final static String SYMETRIC_ALGORITHM = "AES";
	
	public final static String HASH_ALGORITHM = "HmacSHA3-256";
	
	public final static String ASYMETRIC_ALGORITHM = "RSA";
	
	// -------------------------------------------------------------------------------
	// VARIABLES PARA LA CONFIGURACION DE LA EJECUCION
	// -------------------------------------------------------------------------------
	
	/**
	 * Variable que maneja el tipo de ejecucion
	 * True -> Iterativa 
	 * False -> Concurrente
	 */
	public final static Boolean CONFIG = true;
	
	/**
	 * Variable que maneja el numero de consutlas a realizar
	 */
	public final static int NUMERO_CONSULTAS = 4;

	// --------------------------------------------------------------------
	// FUNCIONES DE APOYO A LA COMUNICACION
	// --------------------------------------------------------------------	
	
	public static String byte2str( byte[] b )
	{
		// Encapsulamiento con hexadecimales
		String ret = "";
		for (int i = 0 ; i < b.length ; i++) {
			String g = Integer.toHexString(((char)b[i])&0x00ff);
			ret += (g.length()==1?"0":"") + g; }
		return ret;
	}
	
	public static byte[] str2byte( String ss)
	{
		// Encapsulamiento con hexadecimales
		byte[] ret = new byte[ss.length()/2];
		for (int i = 0 ; i < ret.length ; i++)
			ret[i] = (byte) Integer.parseInt(ss.substring(i*2,(i+1)*2), 16);

		return ret;
	}

	// --------------------------------------------------------------------
	// FUNCIONES PARA CIFRADO SIMETRICO
	// --------------------------------------------------------------------
	
	public static byte[] cifradoSimetrico(SecretKey llave, String texto) 
	{
		byte[] textoCifrado;

		try {
			Cipher cifrador = Cipher.getInstance(SYMETRIC_ALGORITHM_PADDING);
			byte[] textoClaro = texto.getBytes();

			cifrador.init(Cipher.ENCRYPT_MODE, llave);
			textoCifrado = cifrador.doFinal(textoClaro);

			return textoCifrado;
		} 
		catch (Exception e) 
		{
			System.out.println("Excepcion" + e.getMessage());
			return null;
		}
	}
	
	public static byte [] descifradoSimetrico(SecretKey llave, byte[] texto)
	{
		byte[] textoClaro;
		try {
			Cipher cifrador = Cipher.getInstance(SYMETRIC_ALGORITHM_PADDING);
			cifrador.init(Cipher.DECRYPT_MODE, llave);
			textoClaro = cifrador.doFinal (texto);

		} catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
			return null;
		}
		return textoClaro;
	}
	
	// --------------------------------------------------------------------
	// FUNCIONES PARA CIFRADO ASIMETRICO
	// --------------------------------------------------------------------
	public static byte[] cifradoASimetrico(Key llave, String texto) 
	{
		byte[] textoCifrado;

		try {
			Cipher cifrador = Cipher.getInstance(ASYMETRIC_ALGORITHM);
			byte[] textoClaro = texto.getBytes();

			cifrador.init(Cipher.ENCRYPT_MODE, llave);
			textoCifrado = cifrador.doFinal(textoClaro);

			return textoCifrado;
		} 
		catch (Exception e) 
		{
			System.out.println("Excepcion" + e.getMessage());
			return null;
		}
	}
	
	public static byte [] descifradoASimetrico(Key llave, byte[] texto)
	{
		byte[] textoClaro;
		try {
			Cipher cifrador = Cipher.getInstance(ASYMETRIC_ALGORITHM);
			cifrador.init(Cipher.DECRYPT_MODE, llave);
			textoClaro = cifrador.doFinal (texto);
			return textoClaro;

		} catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
			return null;
		}
		
	}

	// --------------------------------------------------------------------
	// FUNCIONES PARA HMACSHA256
	// --------------------------------------------------------------------
	
	public static byte[] getHMACSHA256(String msg, SecretKey sk)
	{
		try {
			Mac mac = Mac.getInstance(HASH_ALGORITHM);
			mac.init(sk);
			return mac.doFinal(msg.getBytes());
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] getDigest(String algorithm, byte[] buffer)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.update(buffer);
			return digest.digest();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
