package lpgpconnection;

import java.io.*;
import java.net.Socket;
import config.SocketConfig;
import datacore.ClientsController;
import java.lang.Exception;
import org.jetbrains.annotations.*;
import datacore.MaskedData;

public class LpgpClient extends ClientProc{
	private ClientsController controller;
	private boolean gotController = false;
	public boolean validClient;
	private SocketConfig internalConfig;
	
	public static class ControllerAlreadyLoad extends Exception{
		
		public ControllerAlreadyLoad(){ super("There's a clients controller loaded!"); }
	}
	
	public static class ControllerNotFound extends Exception{
		
		public ControllerNotFound(){ super("There's no client controller loaded");}
	}
	
	public LpgpClient(SocketConfig conf, ClientsController controller, Socket con) throws ClientProc.ClientAlreadyReceived, ControllerAlreadyLoad{
		super(con);
		if(this.gotController) throw new ControllerAlreadyLoad();
		this.controller = controller;
		this.gotController = true;
		this.internalConfig = conf;
	}
	
	public LpgpClient(Socket con) throws ClientAlreadyReceived{
		super(con);
	}
	
	public LpgpClient(){
		// empty
	}
	
	public void setController(ClientsController controller) throws ControllerAlreadyLoad{
		if(this.gotController) throw new ControllerAlreadyLoad();
		this.controller = controller;
		this.gotController = true;
	}
	
	@Nullable public ClientsController getController(){ return this.controller;}
	
	public boolean haveController(){ return this.gotController; }
	
	public void authenticateClient() throws ControllerNotFound, NoSuchClient{
		if(!this.gotController) throw new ControllerNotFound();
		if(!this.gotClient) throw new NoSuchClient();
		try{
			ObjectInputStream receiver = new ObjectInputStream(this.client.getInputStream());
			PrintWriter sender = new PrintWriter(this.client.getOutputStream(), true);
			sender.println(LpgpServer.HANDSHAKE);
			String maskContent = receiver.readUTF();
			this.validClient = this.controller.authClientMask(maskContent);
			MaskedData contentPure = new MaskedData(maskContent);
			if(this.validClient){
				sender.println();
			}
		}
		catch(Exception e){
			this.validClient = false;
			return;
		}
	}
	
	@Override
	public void run(){
	
	}
	
}