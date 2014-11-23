
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class UDPDistanceVector 
{
    DatagramSocket client_socket = null;
    DatagramSocket send_sock[]=null;
    DatagramPacket data_packet[] = null;
       
    BufferedReader file_reader = null;
    
    //static int port = 65500;
    int server_port_list[]=null;
    int reachable_via[][];      //to track next-hop for each node
    int no_of_nodes=0;
    int node1;
    int node2;
    int mynode;
    
    final float [][]cost_metric;
    
    char neighbor[][];          //to track neighbor for each node
    
    String server_list[]=null;
    
    
    AtomicBoolean change = new AtomicBoolean(true);
    AtomicBoolean converge = new AtomicBoolean(false); 
    static AtomicBoolean[] ack_flag=new AtomicBoolean[10] ;
       
    
    public UDPDistanceVector(int num, BufferedReader br_file)
    {
        this.no_of_nodes = num;
        this.cost_metric = new float[num+1][num+1];
        this.file_reader = br_file;
    }
       
               
    public void initizalize(String arguments[]) throws Exception
    {
       mynode = Integer.parseInt(arguments[1]);

        //INITIALIZATION PHASE
        neighbor = new char[no_of_nodes+1][no_of_nodes+1];
        reachable_via = new int[no_of_nodes+1][no_of_nodes+1];

        for(int i=1;i<=no_of_nodes; i++)
        {
            for(int j=1; j<=no_of_nodes; j++)
            {
                neighbor[i][j]='N';
            }
        }

        
        server_list=new String[no_of_nodes+1];
        server_port_list=new int[no_of_nodes+1];
        data_packet=new DatagramPacket[no_of_nodes+1];
        send_sock=new DatagramSocket[no_of_nodes+1];


        server_list[mynode]="localhost";
        

/*        Scanner sc = new Scanner(System.in);
        
        System.out.println("Enter this("+mynode+") node's port number : ");
        server_port_list[mynode]=sc.nextInt();
        port = server_port_list[mynode];
        
        for(int i=1;i<=no_of_nodes;i++)
        {
            if(i!=mynode)
            {
                System.out.println("Enter node "+i+" IP address : ");
                server_list[i]=sc.next();
                System.out.println("Enter node "+i+" port number : ");
                server_port_list[i]=sc.nextInt();
            }
        }
*/

        
        
	server_list[1]="localhost";//10.137.46.153	Use this IP for testing (Harsh IP)
        server_list[2]="localhost";//10.137.46.153
        server_list[3]="10.137.47.221";			//(Vishal IP)
        server_list[4]="10.137.47.221";
      //  server_port_list[5]=65500;



      //System.out.println("port argument : "+server_port_list[1]+"\n argu :"+arguments[(arguments.length-1)]);
        server_port_list[1]=65100;
        server_port_list[2]=65200;
        server_port_list[3]=65300;
        server_port_list[4]=65400;
      //  server_port_list[5]=65500;

        for(int count=1;count<=no_of_nodes;count++)
        {
            ack_flag[count]=new AtomicBoolean(false);
            send_sock[count]=new DatagramSocket();
        }


        initialize_root(mynode);

        String read_file;
        String[] eachValue;
        while((read_file=file_reader.readLine())!=null)
        {
            eachValue=read_file.split(" ");
            int i = Integer.parseInt(eachValue[0]);
            int j = Integer.parseInt(eachValue[1]);
            float value = Float.parseFloat(eachValue[2]);
            if(i==mynode)
            {
                cost_metric[i][j]=value;
                neighbor[i][j]='Y';
                reachable_via[i][j]=j;
            }

            if(j==mynode)
            {
                cost_metric[j][i]=value;
                neighbor[j][i]='Y';
                reachable_via[j][i]=i;
            }
        }


        /*System.out.println("Cost matrix");
        for(int i=1;i<=no_of_nodes;i++)
        {
            for(int j=1;j<=no_of_nodes;j++)
            {
                System.out.print(cost_metric[i][j]+"  ");
            }
            System.out.println();
        }*/
    }
        
        
        
    public void initialize_root(int myn)
    {
        for (int i=1;i<=no_of_nodes;i++)
            for(int j=1;j<=no_of_nodes;j++)
                cost_metric[i][j]=1000;
        cost_metric[myn][myn]=0;
    }
   
    
    
    public static void main(String args[]) throws Exception
    {
        File input_file=new File(args[0]);
        FileReader fstream=new FileReader(input_file);
        BufferedReader br_file=new BufferedReader(fstream);

        int num = Integer.parseInt(br_file.readLine());
        System.out.println("Number of nodes : "+num);

        UDPDistanceVector obj = new UDPDistanceVector(num, br_file);
        obj.client_socket = new DatagramSocket();
        obj.initizalize(args);
        obj.callerFunction();
    }

    
    public void callerFunction() throws Exception
    {
        Thread thread=new Thread(new ClientSender(this));
        thread.start();

        Thread recv_thread = new Thread(new ClientReceiver(this));
        recv_thread.start();
        recv_thread.join();
    }
    

    public class ClientSender implements Runnable 
    {
        
        UDPDistanceVector obj;
        boolean proceed_sending=true;

        public ClientSender(UDPDistanceVector client_obj)
        {
            obj=client_obj;
        }
        
        @Override
        public void run() 
        {
            try
            {
                while(obj.converge.get()==false)
                {
                    while(obj.change.get()==true)
                    {
                        obj.change.set(false);
                        for(int count=1;count<=obj.no_of_nodes;count++)
                        {
                            if(count!=obj.mynode)
                                ack_flag[count].set(false);
                        }

                        Thread th_send=new Thread(new CallRdt(obj));
                        th_send.start();
                        th_send.join();

                        long start = System.currentTimeMillis();
                        while(System.currentTimeMillis()<=(start+3000))
                        {
                            //Keep looping till 100 milliseconds
                        }
                        

                        try
                        {
                            for(int i=1;i<=obj.no_of_nodes;i++)
                            {
                                if(i!=obj.mynode && obj.neighbor[obj.mynode][i]=='Y')
                                {
                                    proceed_sending=ack_flag[i].get();
                                    if(proceed_sending==false)
                                        break;
                                }
                                
                            }

                            while(proceed_sending==false)
                            {
                                CallRdt resend=new CallRdt(obj);
                                Thread resendth=new Thread(resend);
                                resendth.start();
                                resendth.join();

                                long start2 = System.currentTimeMillis();
                                while(System.currentTimeMillis()<=(start2+3000))
                                {
                                    //Keep looping till 100 milliseconds
                                }
                                
                                for(int i=1;i<=obj.no_of_nodes;i++)
                                {
                                    if(i!=obj.mynode && obj.neighbor[obj.mynode][i]=='Y')
                                    {
                                        proceed_sending=ack_flag[i].get();
                                        if(proceed_sending==false)
                                            break;
                                    }

                                }
                            }
                        }

                        catch(Exception e)
                        {
                            System.out.println(e);
                        }
                    }
                }
            }
            
            catch(Exception e)
            {
                System.out.println(e);
            }
            
            
            System.out.println("\nRouting Table of this Node : "+obj.mynode);
            System.out.println("To Node    :   Next-Hop    :   Total Cost");
            System.out.println("=========================================");
            for(int k=1;k<=obj.no_of_nodes;k++)
            {
                if(k!=(obj.mynode))
                    System.out.println("   "+(k)+"       :       "+((int)(obj.reachable_via[obj.mynode][k]))+"       :       "+obj.cost_metric[obj.mynode][k]);
            }
            
            
        }

        
                
        //=====================CLASS TO CALL THE RDT FUNCTION=======================================================
        
        public class CallRdt implements Runnable
        {
            UDPDistanceVector udp_obj=null;
            
            public void CallRdt()
            {

            }
            
            public CallRdt(UDPDistanceVector obj)
            {
                this.udp_obj = obj;
            }
            
            public String getCostMatrixString()
            {
                String costMatrxInString=String.valueOf(udp_obj.mynode);
                costMatrxInString+=" ";
                {
                    for(int j=1;j<=udp_obj.no_of_nodes;j++)
                    {
                        costMatrxInString+=String.valueOf(udp_obj.cost_metric[udp_obj.mynode][j]);
                        costMatrxInString+=" ";
                    }    
                }
                
                return costMatrxInString;
            }
            
            
            public void run()
            {
                try
                {
                    String dataToBeSent = getCostMatrixString();
                    byte[] send_data=dataToBeSent.getBytes();

                    for(int i=1;i<=udp_obj.no_of_nodes;i++)
                    {
                        if(i!=udp_obj.mynode)
                        {
                            if(ack_flag[i].get()==false)
                            {
                                if(udp_obj.neighbor[udp_obj.mynode][i]=='Y')
                                {
                                    InetAddress inetAddr=InetAddress.getByName(udp_obj.server_list[i]);
                                    data_packet[i]=new DatagramPacket(send_data,send_data.length,inetAddr, udp_obj.server_port_list[i]);
                                    send_sock[i].send(data_packet[i]);
                                }
                            }
                        }
                    }
                }
                
                catch(Exception e)
                {
                    System.out.println(e);
                }
            }
        }
    }

    
    //===================RECEIVER CLASS TO ACCEPT ACKNOWLEDGEMENTS====================================
    
    public class ClientReceiver implements Runnable
    {
        DatagramSocket datagram_sock;   
        DatagramPacket recv_pkt;
        
        UDPDistanceVector obj_recv;
        
        int recv_ack_count=0;
        int token;
        
        AtomicBoolean  received_response=new AtomicBoolean();    //Individual received responses from each server
        AtomicBoolean timer_expired=new AtomicBoolean();    //Individual received responses from each server
        
        byte[] ack_recv=new byte[256];
        byte[] send_ack = new byte[256];
        
        
        public ClientReceiver() 
        {

        }

        
        public ClientReceiver(UDPDistanceVector c_obj)
        {
            this.obj_recv=c_obj;
            try
            {
                this.datagram_sock = new DatagramSocket(obj_recv.server_port_list[obj_recv.mynode]);
                this.datagram_sock.setSoTimeout(15000);  //wait for 15 seconds to see if something is received on socket
            }
            catch(Exception e)
            {
                System.out.println("Receiver server socket error!");
            }
        }
        
        
        public void calculatePath()
        {
            float min=0;
            {
                for(int i=1;i<=obj_recv.no_of_nodes;i++)     
                {
                    if(obj_recv.mynode!=i)
                    {
                        float old_val=obj_recv.cost_metric[obj_recv.mynode][i];  
                        min = old_val;
                        
                        for(int k=1; k<=no_of_nodes; k++)       //for each neighbour of x formula Di(j)=Di(k)+Dk(j)
                        {
                            if(obj_recv.neighbor[obj_recv.mynode][k]=='Y')
                            {
                                if(min > (obj_recv.cost_metric[obj_recv.mynode][k]+obj_recv.cost_metric[k][i]))
                                {
                                    min = (obj_recv.cost_metric[obj_recv.mynode][k]+obj_recv.cost_metric[k][i]);
                                    obj_recv.reachable_via[obj_recv.mynode][i]=k;
                                    obj_recv.change.set(true);
                                    //System.out.println("min aaya : "+obj_recv.change.get());
                                }
                            }
                        }
                        
                        if(old_val!=min)
                        {
                            obj_recv.change.set(true);
                        }
                        
                        obj_recv.cost_metric[obj_recv.mynode][i]=min;
                    }
                }
            }
        }
        
        
        public void getCostMatrix(String data)
        {
            String cost_data[] = data.split(" ");
            
            int getNode = Integer.parseInt(cost_data[0]);
            {
                for(int i=1;i<=obj_recv.no_of_nodes;i++)
                    obj_recv.cost_metric[getNode][i]=Float.parseFloat(cost_data[i]);
            }
        }
        
        @Override
        public void run()
        {
            try
            {
                while(obj_recv.converge.get()==false)
                {
                    recv_pkt=new DatagramPacket(ack_recv, 255);
                    datagram_sock.receive(recv_pkt);

                    byte[] received_data=recv_pkt.getData();
                    String received_data_string=new String(received_data);

                    String actual_data_string=received_data_string.substring(0,recv_pkt.getLength());

                    System.out.println("Received packet : "+actual_data_string);
                    
                    int node = Integer.parseInt(actual_data_string.substring(0,1));
                        
                    if((actual_data_string.substring(2,5)).equalsIgnoreCase("ACK"))
                    {
                        ack_flag[node].set(true);
                    }
                    
                    else
                    {
                        String sendACK = String.valueOf(obj_recv.mynode);
                        sendACK+=" ACK";
                        send_ack = sendACK.getBytes();
                        InetAddress inetAddr=InetAddress.getByName(obj_recv.server_list[node]);
                        obj_recv.data_packet[node]=new DatagramPacket(send_ack,send_ack.length,inetAddr, obj_recv.server_port_list[node]);

                        obj_recv.send_sock[node].send(data_packet[node]);

                        getCostMatrix(actual_data_string);
                        
                        /*System.out.println(" NEW Cost matrix");
                        for(int i=1;i<=no_of_nodes;i++)
                        {
                            for(int j=1;j<=no_of_nodes;j++)
                            {
                                System.out.print(obj_recv.cost_metric[i][j]+"  ");

                            }
                            System.out.println();
                        }*/
                        
                        calculatePath();
                    }
                }
            }
            
            catch(Exception e)
            {
                System.out.println("Convergence Achieved ");
                obj_recv.converge.set(true);
            }
        }
    }
}