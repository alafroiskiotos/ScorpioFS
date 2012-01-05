package unipi.p2p.chord.util.console;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Nodes{
	private String ipAddr;
	private int port;
	private int proxyPort;
	public Nodes(String ipAddr, int port, int proxyPort){
		this.ipAddr = ipAddr;
		this.port = port;
		this.proxyPort = proxyPort;
	}
	public String getIpAddr(){
		return ipAddr;
	}
	public int getPort(){
		return port;
	}
	public int getProxyPort(){
		return proxyPort;
	}
}
public class ConsoleClient {
	static InetAddress iAddr = null;
	static Socket socket = null;
	static PrintWriter pw = null;
	
	private static void connect(String ipAddress, int port){
		try{
			iAddr = InetAddress.getByName(ipAddress);
			socket = new Socket(iAddr, port);
			pw = new PrintWriter(socket.getOutputStream(), true);
		}catch (UnknownHostException e0){
			e0.printStackTrace();
		}catch (IOException e1){
			e1.printStackTrace();
		}
	}
	
	private static void disconnect(){
		if(pw != null)
			pw.close();
		try{
			if(socket != null)
				socket.close();
		}catch (IOException e0){
			e0.printStackTrace();
		}
	}
	public static void main(String[] args) {
		boolean consoleUp = true;

		System.out.println("Welcome to ScorpioFS administration console");
		Scanner in = new Scanner(System.in);
		LinkedList<Nodes> nodesList = new LinkedList<Nodes>();
		ExecutorService exec = Executors.newCachedThreadPool();
		ConsoleClientReceiver consoleRec = new ConsoleClientReceiver(nodesList);
		exec.execute(consoleRec);
		
		while(consoleUp){
			System.out.print("$>");
			String command = in.nextLine();
			String[] tokens = command.split("[ ]");

			//Chord node block
			if(tokens[0].equals("node")){
				if(tokens[1].equals("create")){
					if(tokens.length < 3){
						System.err.println("Usage: node create IP_ADDR[:port]" +
								" -chordport PORT -config CONFIG");
					}else{
						int chordPort = -1;
						String chordConfig = null;
						int i;
						int chPortIndex = -1;
						int configIndex = -1;
						for(i = 0; i < tokens.length; i++){
							if(tokens[i].equals("-chordport"))
								chPortIndex = i;
							if(tokens[i].equals("-config"))
								configIndex = i;
						}
						
						//Create chord node on a non default port
						if(chPortIndex != -1){
							chordPort = Integer.parseInt(tokens[chPortIndex + 1]);
						}else{
							chordPort = ConsoleProtocol.CHORD_PORT;
						}
						
						//Explicit define chord properties file
						if(configIndex != -1){
							chordConfig = tokens[configIndex + 1];
						}else{
							chordConfig = "config/chord.properties";
						}
						
						//Proxy running on a non default port
						if(tokens[2].contains(":")){
							String ipPort[] = tokens[2].split(":");
							connect(ipPort[0], Integer.parseInt(ipPort[1]));
							pw.println(ConsoleProtocol.NODE_CREATE);
							pw.println(chordPort);
							pw.println(chordConfig);
						}else{
							//Proxy running on default port
							connect(tokens[2], ConsoleProtocol.PROXY_PORT);
							pw.println(ConsoleProtocol.NODE_CREATE);
							pw.println(chordPort);
							pw.println(chordConfig);
						}
						disconnect();
					}
				}else if(tokens[1].equals("stop")){
					if(tokens.length < 3){
						System.err.println("Usage: node stop IP_ADDR[:port]"+
								" -chordport PORT");
					}else{
						String config = null;
						int chordPort = -1;
						int chPortIndex = -1;
						int i;
						for(i = 0; i < tokens.length; i++){
							if(tokens[i].equals("-chordport"))
								chPortIndex = i;
						}
						
						if(chPortIndex != -1){
							chordPort = Integer.parseInt(tokens[chPortIndex + 1]);
						}else{
							chordPort = ConsoleProtocol.CHORD_PORT;
						}
						
						//Proxy running on a non default port
						if(tokens[2].contains(":")){
							String ipPort[] = tokens[2].split(":");
							connect(ipPort[0], Integer.parseInt(ipPort[1]));
							pw.println(ConsoleProtocol.NODE_STOP);
							pw.println(chordPort);
							pw.println(config);
						}else{
							//Proxy running on default port
							connect(tokens[2], ConsoleProtocol.PROXY_PORT);
							pw.println(ConsoleProtocol.NODE_STOP);
							pw.println(chordPort);
							pw.println(config);
						}
						disconnect();
					}
				}else if(tokens[1].equals("stat")){
					pw.println(ConsoleProtocol.NODE_STAT);
				}else if(tokens[1].equals("alive")){
					pw.println(ConsoleProtocol.NODE_ALIVE);
				}else if(tokens[1].equals("list")){
					if(nodesList.size() == 0){
						System.out.println("There are no working chord nodes");
						System.out.print("$>");
					}else{
						Nodes tmpNode = null;
						Iterator<Nodes> nodesIt = nodesList.iterator();
						while(nodesIt.hasNext()){
							tmpNode = nodesIt.next();
							System.out.println("Chord node on ip "+tmpNode
									.getIpAddr()+" and on port "+tmpNode
									.getPort()+" is up");
							System.out.print("$>");
						}
					}
				}else if(tokens.length < 2){
					System.out.println("Command not found!");
				}else{
					System.out.println("Command not found!");
				}
				//ScorpioFS block
			}else if(tokens[0].equals("scorpiofs")){
				if(tokens[1].equals("mount")){
					//Call ScorpioFS
				}else if(tokens[1].equals("unmount")){
					//System process (fusermount -u)
				}else if(tokens.length < 2){
					System.out.println("Command not found!");
				}else{
					System.out.println("Command not found!");
				}
				//help
			}else if(tokens[0].equals("help")){
				System.out.println("help!");
				//exit
			}else if(tokens[0].equals("exit")){
				disconnect();
				consoleRec.stopRunning();
				consoleUp = false;
			}else{
				System.out.println("Command not found!");
			}
		}
		System.out.println("GoodBye!");
		System.exit(1);
	}

}
