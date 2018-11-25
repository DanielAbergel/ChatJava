import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ServerTest {

	Server S ; 
	Client C ;




	@Test
	void testServerInt() {
		try {
			S = new Server(1500);
		}
		catch (Exception e) {
			fail("The server fail to connect");
		}
		S.stop();
	}

	@Test
	public void ClientTest()
	{
		try {
			C = new Client("localhost",1500,"DNL");
		}
		catch (Exception e) {
			fail("The Client fail to connect");
		}
	}
	@Test
	public void ClientNameTest()
	{
		try {
			C = new Client("localhost",1500,"DNL");
		}
		catch (Exception e) {
			fail("The Client fail to connect");
		}
		if(!(C.username == "DNL")) fail("the constructor does not working well");
	}

}
