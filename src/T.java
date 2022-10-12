package logic;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class T extends Thread{

	private int id; 
	private int espera;
	
	/*
	 * tipoEnvio: True = Activo
	 * tipoEnvio: False = Pasivo
	 */
	private boolean tipoEnvio;
	private static CyclicBarrier barrera = new CyclicBarrier(4);
	/*
	 * tipoRecepcion: True = Activo
	 * tipoRecepcion: False = Pasivo
	 */
	private boolean tipoRecepcion;
	private Buzon buzonEnvio;
	private Buzon buzonRecepcion;
	private int numMensajes;
	
	public T(int id, int espera, boolean tipoEnvio, boolean tipoRecepcion, Buzon buzonEnvio, Buzon buzonRecepcion)
	{
		this.id = id;
		this.espera = espera;
		this.tipoEnvio = tipoEnvio;
		this.tipoRecepcion = tipoRecepcion;
		this.buzonEnvio = buzonEnvio;
		this.buzonRecepcion = buzonRecepcion;
	}
	
	public void setNumMensajes(int mensajes) {
		this.numMensajes = mensajes;
	}
	@Override
	public void run() {
		if (id == 1) {
			int enviados = 0;
			while (enviados < numMensajes) {
				String mensaje = "ID"+(enviados+1)+":";
				String mod = modificarMensaje(mensaje);
				try {
					sleep(espera);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(tipoEnvio) {
					buzonEnvio.almacenarActivo(mod,this);
				}else {
					buzonEnvio.almacenarPasivo(mod);
				}
				
				enviados++;
			}
			if(tipoEnvio) {
				buzonEnvio.almacenarActivo("FIN",this);
			}else {
				buzonEnvio.almacenarPasivo("FIN");
			}
			
			int recibidos = 0;
			while(recibidos<numMensajes) {
				String msgRecibido;
				if(tipoRecepcion)
					msgRecibido = buzonRecepcion.retirarActivo(this);
				else
					msgRecibido = buzonRecepcion.retirarPasivo();
				System.out.println("Mensaje recibido en el origen: "+msgRecibido);
				recibidos++;
			}
			try {
				barrera.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Finalizó el proceso: " +id);
		}else {
			while (true) {
				String msg;
				if(tipoRecepcion)
					msg = buzonRecepcion.retirarActivo(this);
				else
					msg = buzonRecepcion.retirarPasivo();
				msg = modificarMensaje(msg);
				try {
					sleep(espera);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Mensaje pasó por el proceso "+id+": "+msg);
				if(tipoEnvio)
					buzonEnvio.almacenarActivo(msg,this);
				else
					buzonEnvio.almacenarPasivo(msg);
				if(msg.equals("FIN")) break;
			}
			try {
				barrera.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Finalizó el proceso: " +id);
		}
	}
	
	public String modificarMensaje(String msg) {
		if (msg == "FIN") return msg;
		String r,e;
		if (tipoRecepcion) {
			r = "A";
		}else {
			r = "P";
		}
		
		if (tipoEnvio) {
			e = "A";
		}else {
			e = "P";
		}
		return msg+id+r+e+"-";
	}
	
}
