package com.github.vinicius.fixclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

@QuarkusMain
public class ClientMain {
	
	private static Initiator initiator = null;
    
    public static void main(String[] args) throws Exception {
        System.out.println("Running main method");
        try (InputStream inputStream = getSettingsInputStream(args)){
            SessionSettings settings = new SessionSettings(inputStream);

            boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "false"));

            MyApplication application = new MyApplication();
            MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
            LogFactory logFactory = new ScreenLogFactory(true, true, true, logHeartbeats);
            MessageFactory messageFactory = new DefaultMessageFactory();

            initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory, messageFactory);
            
            initiator.start();
            System.out.println("Sessions Size: "+initiator.getSessions().size());
            for (SessionID sessionId : initiator.getSessions()) {
                Session.lookupSession(sessionId).logon();
            }
            System.out.println("Logged On?? "+initiator.isLoggedOn());
            Quarkus.run(args); 
            Quarkus.waitForExit();
            logout();
        }
        
    }
    
    public static void logout() {
        for (SessionID sessionId : initiator.getSessions()) {
            Session.lookupSession(sessionId).logout("user requested");
        }
    }
    
    private static InputStream getSettingsInputStream(String[] args) throws FileNotFoundException {
    	InputStream inputStream = null;
		System.out.println(Arrays.toString(args));
		if(args.length == 1) {
			System.out.println("Arquito de conf: "+args[0]);
			inputStream = new FileInputStream(new File(args[0]));
		}else {
			inputStream = ClientMain.class.getResourceAsStream("settings.cfg");	
		}
        if (inputStream == null) {
            System.out.println("usage: " + ClientMain.class.getName() + " [configFile].");
            System.exit(1);
        }else {
        	System.out.println("Arquivo de conf do FIX encontrado com sucesso!");
        }
        return inputStream;
    }

}