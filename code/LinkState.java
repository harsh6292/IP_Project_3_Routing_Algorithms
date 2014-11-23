import java.io.*;

public class LinkState {
    
    int definite_nodes[][];
    float [][]cost_metric_AllNodes;
    float [][][]routing_table;
    float cost_parent[][][];
    int no_of_vertices=0;
    int root;
    int node2;
    
    public static void main (String args[]) throws Exception
    {
        LinkState distObj=new LinkState();
        
        File input_file=new File(args[0]);
        
        FileReader fstream=new FileReader(input_file);
        BufferedReader br_file=new BufferedReader(fstream);
        
        System.out.println("Link State Algorith >");
        
        distObj.root=Integer.parseInt(args[1]);
        distObj.node2=Integer.parseInt(args[2]);
      
        distObj.no_of_vertices = Integer.parseInt(br_file.readLine());
        System.out.println("Number of nodes : "+distObj.no_of_vertices);
        
        distObj.cost_metric_AllNodes=new float[distObj.no_of_vertices+1][distObj.no_of_vertices+1];
        distObj.routing_table=new float[distObj.no_of_vertices+1][distObj.no_of_vertices+1][distObj.no_of_vertices+1];
        distObj.definite_nodes=new int[distObj.no_of_vertices+1][distObj.no_of_vertices+1];
        distObj.cost_parent=new float[distObj.no_of_vertices+1][distObj.no_of_vertices+1][2];
        
        
        String read_file;
        String[] eachValue;
        while((read_file=br_file.readLine())!=null)
        {
            eachValue=read_file.split(" ");
            int i = Integer.parseInt(eachValue[0]);
            int j = Integer.parseInt(eachValue[1]);
            float value = Float.parseFloat(eachValue[2]);
            distObj.cost_metric_AllNodes[i][j]=value;
            distObj.cost_metric_AllNodes[j][i]=value;
        }


               
        
        for(int allVert=1; allVert<=distObj.no_of_vertices; allVert++)
        {
            //long starttime = System.nanoTime();
            distObj.initialize_root(allVert);
            
            distObj.definite_nodes[allVert][allVert]=1;
            
            distObj.calculatePath(allVert);

            distObj.calculateRT(allVert);
            //long end= System.nanoTime();
            
        }
        
        
        
        System.out.println("\nRouting Table of Node : "+distObj.root);
        System.out.println("To Node    :   Next-Hop    :   Total Cost");
        System.out.println("=========================================");
        for(int k=1;k<=distObj.no_of_vertices;k++)
        {
            if(k!=(distObj.root))
                System.out.println("   "+(k)+"       :       "+((int)(distObj.routing_table[distObj.root][distObj.root][k]))+"       :       "+distObj.cost_parent[distObj.root][k][0]);
        }
        
        
        System.out.println("\nRouting Table of Node : "+distObj.node2);
        System.out.println("To Node    :   Next-Hop    :   Total Cost");
        System.out.println("=========================================");
        for(int k=1;k<=distObj.no_of_vertices;k++)
        {
            if(k!=(distObj.node2))
                System.out.println("   "+(k)+"       :       "+((int)(distObj.routing_table[distObj.node2][distObj.node2][k]))+"       :       "+distObj.cost_parent[distObj.node2][k][0]);
        }
        
         
        System.out.println("\nShortest Path between Node "+(distObj.root)+" and Node "+(distObj.node2)+" is  "+distObj.cost_parent[distObj.root][distObj.node2][0]);
        
        
    }
    
    
    
    public void initialize_root(int temp_root)
    {
        for (int i=1;i<=no_of_vertices;i++)
        {
           if((i!=temp_root))
            {
                if(cost_metric_AllNodes[temp_root][i]!=0)
                {
                    cost_parent[temp_root][i][0]=cost_metric_AllNodes[temp_root][i];    //0th pos has cost and 1st pos has root as its parent
                    cost_parent[temp_root][i][1]=temp_root;
                                       
                }
                
                else
                {
                    cost_parent[temp_root][i][0]=Float.MAX_VALUE;                       //0th pos has cost to root as infinte and 1st pos has infinite parent
                    cost_parent[temp_root][i][1]=-1;
                }
            }
        }
        cost_parent[temp_root][temp_root][0]=cost_metric_AllNodes[temp_root][temp_root];    //0th pos has cost and 1st pos has root as its parent
        cost_parent[temp_root][temp_root][1]=temp_root;
    }
    
    
    
    
    public void calculateRT(int temp_root)
    {
        int temp=-1;
        for(int i=1;i<=no_of_vertices;i++)
        {
            if(i!=temp_root)
            {
                temp=i;
                while((cost_parent[temp_root][temp][1])!=(temp_root)&& cost_parent[temp_root][temp][1]!=-1)
                {
                    temp=(int)cost_parent[temp_root][temp][1];
                }
            
                if(cost_parent[temp_root][temp][1]!=-1)
                    routing_table[temp_root][temp_root][i]=temp;
                else
                    routing_table[temp_root][temp_root][i]=-1;
            
            }
        }
    }
    
    
    
    public void calculatePath(int temp_root)
    {
        int least=0;
        boolean allNodesVisited=false;

        {
            int count=1;
            
            while(allNodesVisited==false && count<=no_of_vertices)
            {
                float min = Float.MAX_VALUE;
        
                for(int k=1; k<=no_of_vertices; k++)
                {
                    if(definite_nodes[temp_root][k]==0 && cost_parent[temp_root][k][0]<min)
                    {
                        min=cost_parent[temp_root][k][0];
                        least=k;
                    }
                }

                definite_nodes[temp_root][least]=1;

                for(int i=1;i<=no_of_vertices;i++)
                {
                    if(definite_nodes[temp_root][i]==0&&cost_metric_AllNodes[least][i]!=0)
                    {
                        if((cost_parent[temp_root][least][0]+cost_metric_AllNodes[least][i])<cost_parent[temp_root][i][0])
                        {
                            cost_parent[temp_root][i][0]=cost_parent[temp_root][least][0]+cost_metric_AllNodes[least][i];
                            cost_parent[temp_root][i][1]=least;
                            
                   
                        }
                    }
                }

                for(int m=1;m<=no_of_vertices;m++)
                {
                    if(definite_nodes[temp_root][m]==0)
                    {
                        allNodesVisited=false;
                        break;
                    }
                    else
                        allNodesVisited=true;

                }
                
                count++;
            }
        }
    }

}
