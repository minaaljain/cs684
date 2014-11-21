package smartac;

//import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MyClient {

    public static void callRpi(int msg) throws IOException {
    
        //public static void main(String args[])throws IOException{
        //String serverAddress = "127.0.0.1";
        
        String serverAddress = "10.110.200.206";
        Socket s = new Socket(serverAddress, 9090);
        OutputStream outstream = s.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(outstream, "US-ASCII");
        PrintWriter out = new PrintWriter(osw);
        out.println(msg);
        //out.println(0);
        out.flush();
    }
}
