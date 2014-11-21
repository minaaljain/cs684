/*
 * -------------------------------------------------------
 * + SmartAC : The Semestar Project For Embedded Systems +
 * -------------------------------------------------------
 */

package smartac;

// Packages used in the Project
import java.net.UnknownHostException;
import java.sql.*;
import java.io.*;
import gnu.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * @author Asus
 */

public class SmartAC implements SerialPortEventListener  {
    
/*---------------------------------Variables Definitions Start---------------------------------*/
        
        // Variables Used For SQL Query Execution
        static Connection con = null;
        static PreparedStatement ps;
        static ResultSet rs;

        // Variables Used Within The Java Code
        static String ID = null, NAME = null, predict_para = null, fetch_predict_para = null, var=null;
        static Time PRED_ARR_TIME = null, ARR_TIME = null, PRED_LEAVE_TIME, LEAVE_TIME;
        static int already_arrived, valid_user;
        static Date dt = null;
        static Time tm = null;
        static int dw, dy, set_on_ac = 1, noofusers = 4, i=-1;
        static java.util.ArrayList<java.sql.Time> al = new java.util.ArrayList<java.sql.Time>();
        static java.util.ArrayList<Integer> a2 = new java.util.ArrayList<Integer>();
        static String userid = null;
        static Boolean rec_flag = false;
        static java.util.Date curdate;
        static java.sql.Time dbprediction, curt;
        static long curtime, diff;
        static boolean acstatus[] = new boolean[noofusers];
        
        // Varibles for RxTx Communication
        private SerialPort serialPort ;                 //defining serial port object
        private CommPortIdentifier portId  = null;      //my COM port
        private static final int TIME_OUT = 2000;       //time in milliseconds
        private static final int BAUD_RATE = 9600;      //baud rate to 9600bps
        private BufferedReader input;                   //declaring my input buffer
        private OutputStream output;                    //declaring output stream
        private String name;                            //user input name string
        Scanner inputName;                              //user input name
        String inputLine ="";
        String port="COM4";                                 //COM PORT in use
        
/*-----------------------------------Variables Definitions Ends--------------------------------------*/
        
/*---------------------------------------Main Method Starts-------------------------------------------*/

    public static void main(String[] args) {
        

    // Setting Up Connection With The Zigbee Hardware
            SmartAC myTest = new SmartAC();  //creates an object of the class
            myTest.initialize();
            myTest.portConnect();
            System.out.println("Started");
        
    // Setting Up Connection With The Database
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                con=DriverManager.getConnection("jdbc:mysql://localhost:3306/test?","root","root");
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.out.println("Error Occured While Connecting TO DataBase");
            }
           
            
    // Sensing By The Sysytem Starts Here And Actions Are Taken Accordingly
            //int j=0;
            //while(true)
            {
                /*j++;
                // LED Blinking Demo
                  for (; j<5; j++)
                {
                    try{MyClient.callRpi(0);Thread.sleep(5000);}
                    catch(Exception e){e.printStackTrace();System.out.println("Error in callRpi()");}
                }*/
                  
              // Predicting the Arrival Time for Next Day, if Current System Time is 23:55:00 HRS 
                predict_para = (new java.util.Date()).toString() ;
                if(predict_para.substring(11,19).equalsIgnoreCase(""))
                {
                    predict();
                    //try{Thread.sleep(500);}catch(Exception e){}
                }
              
                
               // Checking if The System Time is Equal To Any of the Predicted Time
              //  System.out.println("again"+j);
                //fetch_prediction();
                if (set_on_ac != 0 && !acstatus[set_on_ac-1])
                {
                    // Turn The AC ON
                //predict_para = (new java.util.Date()).toString() ;
                if(predict_para.substring(11,19).equalsIgnoreCase("15:53:40"))
                {
                    System.out.println("AC - Turned ON...");
                    try
                    {
                        set_on_ac=2;
                        MyClient.callRpi(set_on_ac);
                        acstatus[set_on_ac-1] = true;
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        System.out.println("Error in callRpi()");
                    }
                    set_on_ac = 0;
                }
                
                }
                
                
            // Updating the DataBase if a User has Arrived
               
                if (rec_flag)
                {
                    ID = userid;
                    
                    logging_database();
                    
                    try
                    {
                        ps = con.prepareStatement(" select name from data where id = ? ");
                        ps.setString(1, ID);
                        rs = ps.executeQuery();
                        rs.first();
                        var = rs.getString(1);
                        System.out.println(ID+" "+var+" Arrived");
                    }
                    catch(Exception e)
                    {
                        //e.printStackTrace();
                        System.out.println("Error in Updating in DataBase");
                    }
                    rec_flag = false;
                }
                
                
                //System.out.println("Mayank Wins");
                
                
                // Turns OFF all the ACs at the Office Closing Time
                curdate = (new java.util.Date());
                if(curdate.toString().substring(11,19).equalsIgnoreCase("15:13:00"))
                {
                    //System.out.println("Gauri Loose");
                    try
                    {
                        MyClient.callRpi(0);
                        //Arrays.fill(acstatus,Boolean.FALSE);
                        //Thread.sleep(1000);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        System.out.println("Error in callRpi()");
                    }
                }
            }
    }
    
/*-----------------------------------------Main Method Ends-------------------------------------------*/

/*-----------------------------Function for Updating DataBase Starts----------------------------------*/

public static void logging_database()
{
        // Executing Various SQL Queries
        try{

            // checking if user is authorised or not
            ps = con.prepareStatement(" select count(*) from (select name from persons where id = ?) as t ");
            ps.setString(1, ID);
            rs = ps.executeQuery();
            rs.first();
            valid_user = rs.getInt(1);
            
            if(valid_user == 0)
            {
                System.out.println("UNAUTHORISED USER");
            }
            else
            {
            // checking if user has already arrived or not
            ps = con.prepareStatement(" SELECT COUNT(*) FROM (SELECT * FROM DATA WHERE EXISTS (SELECT id FROM DATA WHERE id = ? and date = CURDATE())) AS d ");
            ps.setString(1, ID);
            rs = ps.executeQuery();
            rs.first();
            already_arrived = rs.getInt(1);
            
            if(already_arrived != 0)    // if user as already arrived
            {
                // fetching its all details from the database in all variables
                
                // feeding the time into the database as leaving time
                ps = con.prepareStatement(" update data set leaving_time=CURTIME() where id = ? and date = CURDATE()");
                ps.setString(1, ID);
                ps.executeUpdate();
                
                /*//storing the leaving time into a variable
                ps = con.prepareStatement(" select leaving_time from data where id = ? and date = CURDATE() ");
                ps.setString(1, ID);
                rs = ps.executeQuery();
                rs.first();
                LEAVE_TIME = rs.getTime(1);*/
            }
            else                        // if the user came for the first time on that day
            {
                // fetching the name of the person given its id
                ps = con.prepareStatement(" select name from persons where id = ? ");
                ps.setString(1, ID);
                rs = ps.executeQuery();
                rs.first();
                NAME = rs.getString(1);

                // feeding the time into the database as arrival time
                ps = con.prepareStatement(" insert into data values (?,?,CURDATE(),DAYOFWEEK(CURDATE()),DAYOFYEAR(CURDATE()),null,CURTIME(),null,null) ");
                ps.setString(1, ID);
                ps.setString(2, NAME);
                ps.executeUpdate();
                
                /*//storing the arrival time into a variable
                ps = con.prepareStatement(" select arrival_time from data where id = ? and date = CURDATE() ");
                ps.setString(1, ID);
                rs = ps.executeQuery();
                rs.first();
                ARR_TIME = rs.getTime(1);*/
            }
            }
        }
        
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("Error in SQL Execution");
        }
        
}
/*-----------------------------Function for Updating DataBase Ends---------------------------------------*/

/*---------------------------Function for Fetching Prediction Starts-------------------------------------*/
    public static void fetch_prediction()
    {
        try
        {
                ps = con.prepareStatement(" SELECT d.predicted_arrival_time, p.ac FROM DATA d JOIN persons p ON d.id = p.id WHERE d.date = CURDATE() - INTERVAL 7 DAY");
                rs = ps.executeQuery();
                rs.first();
                do
                {
                    al.add(rs.getTime(1));
                    a2.add(rs.getInt(2));
                }while(rs.next());
                i++;
                //System.out.println(al.size());
                for(; i<al.size() ;i++)
                {
                    curdate = (new java.util.Date());
                    curtime = curdate.getTime();
                    dbprediction = (al.get(i));
                    curt = java.sql.Time.valueOf(curdate.toString().substring(11,19));
                    diff = dbprediction.getTime() - curt.getTime();             
                    diff = diff/60000; //Convert milliseconds to minutes 
                    //System.out.println("difference: between"+dbprediction+" and "+curt+" is "+diff);
                    if(diff < 16 && diff>0)
                    {
                        set_on_ac = a2.get(i);
                        if (i == al.size()-1)
                        {   
                            i = -1;
                            //System.out.println("REseted i");
                        }
                        break;
                    }//System.out.println(i);
                }
                if(i>=al.size())
                {
                    i=-1;
                }
                al.clear();
                a2.clear();
                Thread.sleep(3000);
        }
        catch(Exception e)
        {
            System.out.println("Error in Fetching Predicted Values");
            e.printStackTrace();
        }
        
    }
    
/*---------------------------Function for Fetching Prediction Ends---------------------------------------*/

/*--------------------------------Function for Prediction Starts-----------------------------------------*/    

    public static void predict()
    {
        try
        {
            System.out.println("Let's predict..");
            ps = con.prepareStatement("select distinct id from data");            
            rs = ps.executeQuery();
            rs.first();
            do
            {
                ID = rs.getString(1);
                System.out.println(ID);
                PreparedStatement ps1; 
                ps1 = con.prepareStatement("SELECT DATE,arrival_time, day_of_week AS dow, HOUR(arrival_time) AS hr,COUNT(*) AS c,SEC_TO_TIME(AVG(TIME_TO_SEC(arrival_time))) AS avg1 FROM DATA WHERE id=? GROUP BY dow,hr ORDER BY dow,c DESC;");
                ps1.setString(1, ID);
                ResultSet rs1;
                rs1 = ps1.executeQuery();
                rs1.first();
                int dow;
                java.sql.Date dtt= null;
                int dow1 = -1;
                do
                {                    
                    dow = rs1.getInt(3);
                    java.sql.Date dt1 = rs1.getDate(1);
                    if(dow!=dow1 )//|| dt1!=dtt)
                    {                        
                        dow1 = dow;                        
                        java.sql.Time t1 = rs1.getTime(6);                        
                        ps1 = con.prepareStatement("update data set predicted_arrival_time = ? where id = ? and day_of_week=? and date> CURDATE()-INTERVAL 7 DAY");
                        ps1.setTime(1, t1);
                        ps1.setString(2, ID);
                        ps1.setInt(3, dow);
                        //ps1.setDate(4, dt1);
                        System.out.println(ID+" "+t1+" "+dow+" " + dt1);
                        int r = ps1.executeUpdate();
                        System.out.println("Updated "+r+" rows");
                    }
                }while(rs1.next());
            }while(rs.next());
        }
        catch(Exception e)
        {
            System.out.println("Error in Predict");
            e.printStackTrace();
        }
    }
    
/*--------------------------------Function for Prediction Ends----------------------------------------------*/        
    
/*---------------------------Functions for Port Communication Starts----------------------------------------*/    
   
        //method initialize
    private void initialize(){
        CommPortIdentifier ports = null;      //to browse through each port identified
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers(); //store all available ports
        while(portEnum.hasMoreElements()){  //browse through available ports
                ports = (CommPortIdentifier)portEnum.nextElement();
             //following line checks whether there is the port i am looking for and whether it is serial
               if(ports.getPortType() == CommPortIdentifier.PORT_SERIAL&&ports.getName().equals(port)){ 

                System.out.println("COM port found:"+port);
                portId = ports;                  //initialize my port
                break;                                                                                     }
           
            }
       //if serial port am looking for is not found
        if(portId==null){
            System.out.println("COM port not found");
            System.exit(1);
        }
        
                            }
    
    //end of initialize method
    
    //connect method
   
    private void portConnect(){
        //connect to port
        try{
         serialPort = (SerialPort)portId.open(this.getClass().getName(),TIME_OUT);   //down cast the comm port to serial port
                                                                                     //give the name of the application
                                                                                     //time to wait
        System.out.println("Port open succesful: "+port); 
        
        //set serial port parameters
serialPort.setSerialPortParams(BAUD_RATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
        
        

        }
        catch(PortInUseException e){
            System.out.println("Port already in use");
            System.exit(1);
        }
        catch(NullPointerException e2){
            System.out.println("COM port maybe disconnected");
        }
        catch(UnsupportedCommOperationException e3){
            System.out.println(e3.toString());
        }
       
        //input and output channels
        try{
      //defining reader and output stream
       input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        output =  serialPort.getOutputStream();
        //adding listeners to input and output streams
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);
        serialPort.notifyOnOutputEmpty(true);
        }
        catch(Exception e){
            System.out.println(e.toString());
                            }
        
    }
    //end of portConncet method
    
    //readWrite method
   
    public synchronized void serialEvent(SerialPortEvent oEvent) {
    	 if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
    	    try {
    	        char[] cbuf = new char [15];
 //               int[] in = new int[10];
                int i;
     	        if (input.ready()) {
    	            int ret = input.read(cbuf);// .readLine();
                    for(i=0;i<ret;i++)
                    {
                        //count ++;
                        if (cbuf[i] == 'q')
                        {
                            //System.out.print(inputLine);
                            userid = inputLine;
                            rec_flag = true;
                            inputLine = "";
                            break;
                        }
                        inputLine+=cbuf[i];
                    }
                }
    	    } catch (Exception e) {
    	        System.err.println(e.toString());
    	    }
    	 }
    	// Ignore all the other eventTypes, but you should consider the other ones.
    	}
    //end of serialEvent method
    
    //closePort method
    private void close(){
        if(serialPort!=null){
            serialPort.close(); //close serial port
        }
        input = null;        //close input and output streams
        output = null;
    }
   
/*--------------------------------Functions for Port Communication Ends-------------------------------------*/    
    
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////PROJECT ENDS/////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
}
