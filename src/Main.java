package logic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main {
		
		private static Buzon[] buzones;
		private static T[] threads;
		public static void main(String[] args)
		{	
			buzones = new Buzon[4];
			threads = new T[4];
			 try {
		            FileReader reader = new FileReader(".\\Data\\test.txt");
		            BufferedReader bufferedReader = new BufferedReader(reader);
		 
		            String line;
		            for(int i = 0; i < 4; i++)
		            {
		            	line = bufferedReader.readLine();
		            	String[] dataB = line.split(" ");
		            	buzones[i] = new Buzon(dataB[0], Integer.parseInt(dataB[1]));
		            }
		            for(int j = 0; j < 4; j++)
		            {
		            	line = bufferedReader.readLine();
		            	String[] dataT = line.split(" ");
		            	threads[j] = new T(Integer.parseInt(dataT[0]), Integer.parseInt(dataT[1]), Boolean.parseBoolean(dataT[2]), Boolean.parseBoolean(dataT[3]), buzones[j], buzones[(j+3)%4]);
		            }
		            	
		     
		            reader.close();
		 
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
			 
			 Scanner scanner = new Scanner(System.in);
			 System.out.println("Ingrese el numero de mensajes que desea enviar: ");
			 int mensajes = scanner.nextInt();
			 System.out.println(mensajes);
			 
			 threads[0].setNumMensajes(mensajes);
			 
			 for (int i = 0; i < 4; i++)
				 threads[i].start();
			 for (int i = 0; i < 4; i++)
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		}

}
