import java.io.*;
import java.lang.*;
import java.lang.Integer;
import java.util.Properties;

class Process extends Thread//implements Runnable
{
private int index;		//process ID
private int n;			//total procs Count
private int opTime;		//opTime
private int sleepTime;	//sleepTime
private int m;			//Times to run
private int sem_lvs;	//Levels in tree structure

private static int execCt=0;	//Tracking of no.of executns
public static long unixTime=0;	//Start Time Tracking; from first start()

public static StringBuffer sb =new StringBuffer();					//Output String
public static StringBuffer sb2=new StringBuffer("\nsequence:\n\n");	//Process critical section Seq

Semaphore S[];														//To store Semaphore references

//Constructors

Process()
{
index=-1;
opTime=0;
sleepTime=0;
n=0;
m=0;
sem_lvs=0;
System.out.println("Default Process Constructor:Index set to -1");
}

Process(int opT,int slpT,int M,int indx,int N,Semaphore s[])
{
opTime=opT;
sleepTime=slpT;

m=M;	index=indx;	
n=N;	execCt+=M;

S=new Semaphore[n-1];
S=s;

sem_lvs=levels(n);

/*// CHECK Init

System.out.println("P"+index+"-->\n");
for(int i=0;i<(n-1);i++)System.out.println("Semaphore "+i+" val::"+S[i].getSem()+"\n");
System.out.println("Reqd Sem-levels="+sem_lvs);

//*/ //this.run();

}

//compute Process Tree Levels

private int levels(int no)		{return (int)(Math.log(no)/Math.log(2));}

//get opTime, Sleeptime,m and n

public int getopT()			{return opTime;}

public int getsleepT()		{return sleepTime;}

public int getM()			{return m;}

public int getN()			{return n;}

////////////////////////////////////////////////////////////////////////////////////0

//overrided run() method of Thread class-Main Thread Actions

public void run()//throws InterruptedException
{
try
{
//Process Endtime
long unixEnd; 

//Sem access seq var(t) and order storage var(s_order) 
//& Treelevels print_spacing

int t=0,	print_spc=0,	s_order[]=new int[sem_lvs]; 

//init sem access seq
for(int g=0;g<sem_lvs;g++) 	s_order[g]=-1;

    t=(index+n-1)/2;

//Compute sem access seq, actl wrt Process ID
for(int h=0;h<sem_lvs;h++){
				s_order[h]=t-1;
				t=(t/2);
			     }

//m accesses to critical section
 for(int i=1;i<=m;i++)
 { 

  print_spc=2;			//   System.out.println("P"+index+",pass:"+i);
  					//s  System.out.println("P"+index+",pass:"+i+",SleepT("+sleepTime+")");
  this.sleep(sleepTime);

//acquire locks
  for(int j=0;j<sem_lvs;j++)
  {					//p  System.out.println("P"+index+",pass:"+i+",Call P on Semaphore #"+(s_order[j]+1));
  S[s_order[j]].p(index,i);
  }					//o  System.out.println("P"+index+",pass:"+i+",OpTime("+opTime+")");

//optime and Endtime calc
  this.sleep(opTime);         
  unixEnd = System.currentTimeMillis();// / 1000L; 
					//_  System.out.println("\n");

//print Semaphore States on screen and to StrBuffer
  for(int l=1;l<n;l++)
  {
          sb.append("(s"+l+","+S[l-1].getSem()+",p["+S[l-1].getCurr()+","+S[l-1].getCurrM()+"]"+",p["+S[l-1].getWaitg()+","+S[l-1].getWaitgM()+"])");

				//track process currently in critical section
			       if(l==1)sb2.append("p["+S[l-1].getCurr()+"-"+S[l-1].getCurrM()+"]:");

   System.out.print("(s"+l+","+S[l-1].getSem()+",p["+S[l-1].getCurr()+","+S[l-1].getCurrM()+"]"+",p["+S[l-1].getWaitg()+","+S[l-1].getWaitgM()+"])");

   //process level print spacing
   if((l+1)%print_spc==0){		sb.append("\n");	
			     //
				System.out.print("\n");	
								print_spc*=2;	}
   else         	    {		sb.append("\t");	
			     //
				System.out.print("\t");			
			    }

  }
//Elapsed UnixTime Calculation and Printg

  	    sb.append("UnixTime="+(unixEnd-unixTime)+"\n");  	
  	   sb2.append((unixEnd-unixTime)+"(UnixTime)"+"\n");  	
//
  System.out.println("UnixTime="+(unixEnd-unixTime));

  	    sb.append("\n");					
//
  System.out.println("\n");

//Release Locks
  for(int k=(sem_lvs-1);k>=0;k--)
  {
					//v  System.out.println("P"+index+",pass:"+i+",Call V on Semaphore #"+(s_order[k]+1));
  S[s_order[k]].v(index,i);
  }

//Executn count trackg and writing final output to file

     execCt--;
  if(execCt==0)     {
			sb.append(sb2);	//System.out.println("----------------------------------\nStringBuffer Contents-->\n"+sb);

			BufferedWriter out=new BufferedWriter(new FileWriter("event.log"));

			out.write(sb.toString());
			out.close();
		      }
 }

  //System.out.println("Index="+index+"m="+(m+1-i));}


}
catch(InterruptedException e){System.out.println("Exception:"+e);}
catch(IOException e){System.out.println("Exception:"+e);}
}
////////////////////////////////////////////////////////////////////////////////////1
}

class Semaphore
{
private int sem;	//sem variable
int index;		//Semaphore index no
int flag;		//current process flag
int waitg,curr;	//waitg & curr prcs id
int waitg_m,curr_m;  //waitg & curr prcs iteration m

//Constructors

Semaphore()
{
sem=1;
index=-1;
 waitg=-1;  curr=-1;
waitg_m=0; curr_m=0;

flag=1;
System.out.println("Default Semaphore Constructor:Index set to -1");
}
Semaphore(int indx)
{
sem=1;
flag=0;
  waitg=0;   curr=0;
waitg_m=0; curr_m=0;
index=indx;
}

////////////////////////////////////////////////////////////////////////////////////0
//Acquire Lock / p()
public synchronized void p(int pindx,int pass) throws InterruptedException
{
// check sem legal range
if((sem<0)||(sem>1)){System.out.println("Sem"+index+": P Error!");
                                                           return;}
while(sem==0)
 {
 if(flag==0) { waitg=pindx; waitg_m=pass; flag=1; }
 wait();
 if(sem==1){break;}
 }
 sem--;
		 curr=pindx;  curr_m=pass;
 notifyAll();
 
}


public synchronized void v(int pindx,int pass) throws InterruptedException
{
long wt=1;

// check sem legal range
if(sem!=0)          {System.out.println("Sem"+index+": V Error!");
                                                           return;}

  waitg=0; waitg_m=0; flag=0;
   curr=0;  curr_m=0;

 sem++;
 notifyAll();
 this.wait(wt);
}

////////////////////////////////////////////////////////////////////////////////////1
//get Current prcs id, iteration, Waitg prcs id, iterations, Sem val and Sem Index
public int getCurr()  {return    curr;}
public int getCurrM() {return  curr_m;}
public int getWaitg() {return   waitg;}
public int getWaitgM(){return waitg_m;}

public int getSem(){return sem;}
public int getIndex(){return index;}
}

class Start
{
public static void main(String args[]) throws FileNotFoundException,IOException,NullPointerException
{
   
try{ 
    int n,m;
    int sleeptime,optime;

//open and load properties file    
    FileInputStream sys= new FileInputStream("system.properties");
    Properties props=new Properties();
	        props.load(sys);

//read m,n from properties file
    n=Integer.parseInt(props.getProperty("n").trim());
    m=Integer.parseInt(props.getProperty("m").trim());sys.close();

    System.out.println("Start iT! :n="+n+",m="+m+"\n");
    
//Create semaphore array of size n-1
    Semaphore s[]=new Semaphore[n-1];

//Init Semaphores
for(int i=0;i<n-1;i++)
    {
    s[i]=new Semaphore((i+1));
    }

//Create process array of size n
    Process p[]=new Process[n];

//Init Processes
for(int i=0;i<n;i++)
    {
      optime=Integer.parseInt(props.getProperty("P"+(i+1)+".opTime").trim());
      sleeptime=Integer.parseInt(props.getProperty("P"+(i+1)+".sleepTime").trim());

      p[i]=new Process(optime,sleeptime,m,(i+1),n,s);
    }

//Put Start UnixTime in Long
    Process.unixTime=System.currentTimeMillis();				  // / 1000L;

//Start threads
    for(int i=0;i<n;i++)    p[i].start();
  }
  catch(FileNotFoundException e) 	{System.out.println("File not found!");}
}
}
