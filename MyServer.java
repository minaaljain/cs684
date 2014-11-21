
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
/*import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;*/
//import java.util.Date;

/**
 * A TCP server that runs on port 9090.  When a client connects, it
 * sends the client the current date and time, then closes the
 * connection with that client.  Arguably just about the simplest
 * server you can write.
 */
public class MyServer {



// create gpio controller
/*final static  GpioController gpio = GpioFactory.getInstance();
        
// provision gpio pin #01 as an output pin and turn on
final static  GpioPinDigitalOutput ac1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "AC1", PinState.LOW);
final static GpioPinDigitalOutput ac2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "AC2", PinState.LOW);
final static  GpioPinDigitalOutput ac3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13, "AC3", PinState.LOW);
final static GpioPinDigitalOutput ac4 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_19, "AC4", PinState.LOW);
*/
    /**
     * Runs the server.
     */
    public static void main(String[] args) throws IOException {
System.out.println("Server Started");

        ServerSocket listener = new ServerSocket(9090);
        try {
            while (true) {

                Socket socket = listener.accept();
                try {
                    PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                InputStream is = socket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(is));              
                int line;
                line = Integer.parseInt(in.readLine());
                System.out.println(line);
			if (line == 1)
			{ //ac1.high();
			System.out.println("AC1 Started...");}
			else if (line == 2)
			{ //ac2.high();
			System.out.println("AC2 Started...");}
			else if (line == 3)
			{ //ac3.high();
			System.out.println("AC3 Started...");}
			else if (line == 4)
			{ //ac4.high();
			System.out.println("AC4 Started...");}			
			else if (line == 0) 
			{	//ac1.low();			
				//ac2.low();			
				//ac3.low();			
				//ac4.low();
				System.out.println("All AC's turned off...");}
                        else{}
                } finally {
                   
                }
            }
        }
        finally {
            
        }
    }
}
