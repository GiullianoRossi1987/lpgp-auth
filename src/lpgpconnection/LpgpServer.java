package lpgpconnection;

import java.net.ServerSocket;
import java.io.*;
import datacore.ClientsController;
import datacore.ClientsController.*;
import config.SocketConfig;
import config.SocketConfig.*;
import java.lang.Exception;
import java.lang.Runnable;
import org.jetbrains.annotations.*;

public class LpgpServer extends ServerBase{
	private SocketConfig configurations;
	private ClientsController controller;
	private boolean ready = false;
	
	private static final boolean DEBUG = false;
	
	public static final String HANDSHAKE = "Welcome to the official LPGP authenticator server\nPlease insert the client to be authenticated";
	
	public static class ClientAuthenticationError extends Exception{
		
		public ClientAuthenticationError(String msg){ super("Invalid Client: " + msg); }
	}
	
	public static class ServerNotReadyError extends Exception{
		
		public ServerNotReadyError(){ super("The server isn't configured yet!"); }
	}
	
	public static class ServerAlreadyConfigured extends Exception{
		
		public ServerAlreadyConfigured(){ super("The server's configured already!"); }
	}
	
	public LpgpServer(SocketConfig config, ClientsController controller) throws ServerInternalError, ServerAlreadyConfigured{
		if(this.ready) throw new ServerAlreadyConfigured();
		try{
			this.configurations = config;
			this.controller = controller;
			this.mainServer = new ServerSocket(config.getServerPort());
			this.ready = true;
		}
		catch(Exception e){ throw new ServerInternalError(e.getMessage());}
	}
	
	public LpgpServer(){
		this.configurations = null;
		this.controller = null;
		this.mainServer = null;
		this.ready = false;
	}
	
	public void disableServer() throws ServerNotReadyError, ServerInternalError{
		if(!this.ready) throw new ServerNotReadyError();
		try{
			this.mainServer.close();
			this.configurations.unloadConfig();
			this.ready = false;
		}
		catch(Exception e){
			throw new ServerInternalError(e.getMessage());
		}
		
	}
	
	@Nullable public SocketConfig getConfig(){ return this.configurations;}
	
	@Nullable public ClientsController getController(){ return this.controller; }
	
	@Nullable public ServerSocket getConnection(){ return this.mainServer; }
	
	public boolean isReady(){ return this.ready; }
	
	public String[] getAuth(boolean root) throws ServerNotReadyError, ConfigurationsNotLoaded{
		if(!this.ready) throw new ServerNotReadyError();
		return root ? this.configurations.getRootClient() : this.configurations.getNormalClient();
	}
	
	@Override
	public void run(){
		// TODO
	}
}