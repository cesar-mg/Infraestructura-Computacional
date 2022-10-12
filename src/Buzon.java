package logic;


import java.util.ArrayList;

public class Buzon {
	
	private String id;
	private int size;
	private ArrayList<String> buzon;
	
	public Buzon(String id, int size) 
	{
		this.id = id;
		this.size = size;
		buzon = new ArrayList<String>(size);
	}
	
	public void almacenarActivo(String msg,T thread) {
		while (buzon.size() == size) {
			thread.yield();
		}
		synchronized (thread) {
			buzon.add(msg);
		}
		
	}
	
	public synchronized void almacenarPasivo(String msg) {
		while (buzon.size() == size) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		buzon.add(msg);
		notifyAll();
	}
	
	public String retirarActivo(T thread) {
		while (buzon.size() == 0) {
			thread.yield();
		}
		String msg;
		synchronized (thread) {
			msg = buzon.remove(0);
		}
		return msg;
	}
	
	public synchronized String retirarPasivo() {
		while (buzon.size() == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String msg = buzon.remove(0);
		notifyAll();
		return msg;
	}

}
