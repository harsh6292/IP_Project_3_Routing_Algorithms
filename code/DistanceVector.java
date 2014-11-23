import java.io.*;


public class DistanceVector {
    
    float [][][]cost_metric_AllNodes;
    char neighbor[][];          //to track neighbor for each node
    int reachable_via[][];      //to track next-hop for each node
    int received_DV[];          //to track if a node received DV from neighbor
    int line_count=0;
    int no_of_nodes=0;
    int initial_node;
    
    int node1;
    int node2;
    boolean first_time[];
    
    public static void main (String args[]) throws Exception
    {
       
        DistanceVector distObj=new DistanceVector();
      
        File input_file=new File(args[1]);
        FileReader fstream=new FileReader(input_file);
        BufferedReader br_file=new BufferedReader(fstream);
        
        distObj.initial_node=Integer.parseInt(args[0]);
        distObj.node1=Integer.parseInt(args[2]);
        distObj.node2=Integer.parseInt(args[3]);
        
        distObj.no_of_nodes = Integer.parseInt(br_file.readLine());
        
        System.out.println("Number of nodes : "+distObj.no_of_nodes);//+" Root node :"+ distObj.root);
        
        distObj.cost_metric_AllNodes = new float[distObj.no_of_nodes+1][distObj.no_of_nodes+1][distObj.no_of_nodes+1];
        distObj.neighbor = new char[distObj.no_of_nodes+1][distObj.no_of_nodes+1];
        distObj.reachable_via = new int[distObj.no_of_nodes+1][distObj.no_of_nodes+1];
        distObj.received_DV = new int[distObj.no_of_nodes+1];
        distObj.first_time = new boolean[distObj.no_of_nodes+1];
        
        for(int i=1;i<=distObj.no_of_nodes; i++)
        {
            
            for(int j=1; j<=distObj.no_of_nodes; j++)
            {
                distObj.neighbor[i][j]='N';
            }
        }
        
        
        String read_file;
        String[] eachValue;
        while((read_file=br_file.readLine())!=null)
        {
            eachValue=read_file.split(" ");
            int i = Integer.parseInt(eachValue[0]);
            int j = Integer.parseInt(eachValue[1]);
            float value = Float.parseFloat(eachValue[2]);
            distObj.cost_metric_AllNodes[i][i][j]=value;
            distObj.cost_metric_AllNodes[j][j][i]=value;
            
            distObj.neighbor[i][j]='Y';
            distObj.neighbor[j][i]='Y';
            distObj.reachable_via[i][j]=j;
            distObj.reachable_via[j][i]=i;
        }

        
        distObj.initialize_root();
        
        //Initial node sending
        distObj.initial_send(distObj.initial_node);
          
          
        distObj.calculatePath();
      
        System.out.println("\nRouting Table of Node : "+distObj.node1);
        System.out.println("To Node    :   Next-Hop    :   Total Cost");
        System.out.println("=========================================");
        for(int k=1;k<=distObj.no_of_nodes;k++)
        {
            if(k!=(distObj.node1))
                System.out.println("   "+(k)+"       :       "+((int)(distObj.reachable_via[distObj.node1][k]))+"       :       "+distObj.cost_metric_AllNodes[distObj.node1][distObj.node1][k]);
        }
            
            
        System.out.println("\nRouting Table of Node : "+distObj.node2);
        System.out.println("To Node    :   Next-Hop    :   Total Cost");
        System.out.println("=========================================");
        for(int k=1;k<=distObj.no_of_nodes;k++)
        {
            if(k!=(distObj.node2))
                System.out.println("   "+(k)+"       :       "+((int)(distObj.reachable_via[distObj.node2][k]))+"       :       "+distObj.cost_metric_AllNodes[distObj.node2][distObj.node2][k]);
        }
          
        System.out.println("\nCost of Shortest Path between Node "+(distObj.node1)+" and Node "+(distObj.node2)+" is  "+distObj.cost_metric_AllNodes[distObj.node1][distObj.node1][distObj.node2]);
          
    }

    
    
    public void initial_send(int init_node)
    {
        for(int m=1;m<=no_of_nodes;m++)
        {
            if(init_node!=m)
            {
                for(int k=1;k<=no_of_nodes;k++)
                {
                    if(neighbor[init_node][m]=='Y')
                    {
                        cost_metric_AllNodes[m][init_node][k]=cost_metric_AllNodes[init_node][init_node][k];
                        received_DV[m]=1;
                    }
                    
                }
            }
        }
    }
    
    
    
    public void initialize_root()
    {
            
        for (int i=1;i<=no_of_nodes;i++)    //for all nodes
        {
            first_time[i]=true;
            for(int j=1;j<=no_of_nodes;j++)
            {
                if(i==j)
                {
                    for(int k=1;k<=no_of_nodes;k++)
                      {
                          if(k!=j)
                            {
                                if(neighbor[j][k]!='Y')
                                  cost_metric_AllNodes[i][j][k]=Float.MAX_VALUE;
                            }
                          else
                              cost_metric_AllNodes[i][j][k]=0;
                      }
                }
                else
                {
                     for(int k=1;k<=no_of_nodes;k++)
                         cost_metric_AllNodes[i][j][k]=Float.MAX_VALUE;
                }

            }
                
        }
    }
    
       
    
    public void calculatePath()
    {
        boolean terminate=false;
        boolean change=false;
        float min=0;
        
        while(terminate==false)
        {
            terminate=true;
            for(int i=1;i<=no_of_nodes;i++)                 //any node who has changed its DV
            {
                if(received_DV[i]==1)                   
                {
                    received_DV[i]=0;
                    for(int j=1;j<=no_of_nodes;j++) //for each node in x table own row
                    {
                        float old_val=cost_metric_AllNodes[i][i][j];            // cost from i to j
                                
                        if(i!=j)
                        {
                            min = old_val;
                            
                            for(int k=1; k<=no_of_nodes; k++)       //for each neighbour of x formula Di(j)=Di(k)+Dk(j)
                            {
                                if(neighbor[i][k]=='Y')
                                {
                                    
                                    if(min > (cost_metric_AllNodes[i][i][k]+cost_metric_AllNodes[i][k][j]))
                                    {
                                        
                                        min=cost_metric_AllNodes[i][i][k]+cost_metric_AllNodes[i][k][j];
                                        
                                        reachable_via[i][j]=k;
                                        terminate=false;
                                        change=true;
                                    }
                                    
                                }
                            }
                            if(old_val!=min)
                            {
                                change=true;
                                terminate=false;
                            }
                            cost_metric_AllNodes[i][i][j]=min;
                        }
                         
                    }
                    
                    if(change==true || first_time[i]==true)
                    {
                        first_time[i]=false;
                        change=false;
                        initial_send(i);
                    }
                }
                

            }
            
        
            for(int i=0;i<=no_of_nodes;i++)
            {
                if(received_DV[i]==1)
                {
                    terminate=false;
                    break;
                }
                else
                    terminate=true;
            }
        }
    }
}