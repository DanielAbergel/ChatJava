import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Scanner;
class ServerTest {
	
	static Server TEST;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception 
	{
		int portNumber = 1500; 
		switch(args.length) { 
		case 1: 
			try { 
				portNumber = Integer.parseInt(args[0]); 
			} 
			catch(Exception e) { 
				System.out.println("Invalid port number."); 
				System.out.println("Usage is: > java Server [portNumber]"); 
				return; 
			} 
		case 0: 
			break; 
		default: 
			System.out.println("Usage is: > java Server [portNumber]"); 
			return; 

		} 
		// create a server object and start it 
		Server server = new Server(portNumber); 
		server.start(); 
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception
	{
		
	}

	

}
