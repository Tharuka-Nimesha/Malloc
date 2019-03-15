/* * * Explanation * * */
/*
** 25000 Byte array is used as the RAM
** Details about processes are stored using a linked list
** Each node of the linked list represents a process
** When a new process is inserted, using myAlloc() it creates a new node in the linked list and fills the RAM array
** When a process is removed, using myFree() it removes the node from the linked list and clears the RAM array
** Size of the linked list nodes will be reduced from the 25000 usable bytes
*/
/* * * End of explanation * * */

class LinkedList{ 
    Block head=null;  // head of list 
    Block tail=null;  // tail of list 
  
    /* Block inner class */
    class Block{ 
    	String processNo;
		int processSize;
		boolean holeExists=false;
		Block next;
		Block previous;
		public int begin;
		public int end;
        
        Block(String pNo, int pSize) //Constructor to create a new block 
		{
			this.processNo=pNo;
			this.processSize=pSize;
			this.next=null; //This is the last node, so next of it is null
		}

		long getSize(){
			return jdk.nashorn.internal.ir.debug.ObjectSizeCalculator.getObjectSize(this); //to get the size of the object
		}
    }
    /* End of block inner class */

    Block allocate(String pNo, int pSize){
	    Block block = new Block(pNo, pSize);

	    if (head == null) //If the Linked List is empty, allocate new node as head
	    { 
	        head = block;
	        block.begin=0;
	        block.end=pSize-1;
	        tail = block;
	        return block; 
	    } 
	  
	    tail.next = block; //Else add tail.next
	    block.begin=tail.end+1;
	    block.end=block.begin+pSize-1;
	    block.previous=tail;
	    tail=block;
	    return block; 
	}

	Block allocateHole(Block selected, String pNo, int pSize){
	  	Block newprocess=new Block(pNo,pSize); //adding a new process
	  	newprocess.begin=selected.begin; //adding a block by seperating the selected block to two
	  	newprocess.end=newprocess.begin+pSize-1;

	  	newprocess.next=selected;
	  	newprocess.previous=selected.previous;
	  	selected.previous.next=newprocess;
	  	selected.previous=newprocess;
		
		newprocess.processSize=pSize;
		newprocess.holeExists=false;

		selected.begin=newprocess.end+1;
		selected.processSize-=newprocess.processSize;

	    return newprocess; 
	}

	Block remove(String pNo){ 
        if (head == null){ //If linked list is empty 
        	System.out.println("No processes found.");
            return null; 
        }

        Block temp = head; 
  
        if (temp.processNo==pNo ) //If head needs to be removed 
        { 
            head = temp.next; // Change head 
            return temp; 
        } 
  
        while(temp!=null && temp.next.processNo!=pNo) // Find previous block of the block to be deleted 
            temp = temp.next; 
  
        if (temp == null){ // If process number is not found
        	System.out.println("No processes found.");
            return null; 
        }

        Block block = temp.next;
        block.holeExists=true;
		block.processNo=null;

        while (block.previous!=null && block.previous.holeExists==true){ //if the previous block is a hole, combine them
        	block.begin=block.previous.begin;
        	block.previous=block.previous.previous;
        	block.previous.next=block; //combine holes together
        }

        while (block.next!=null && block.next.holeExists==true){ //if the next block is a hole, combine them
        	block.end=block.next.end;
        	block.next=block.next.next;
        	block.next.previous=block; //combine holes together
        }

        block.processSize=block.end-block.begin;
        return block;
    }
}

class Memory{
	byte ram [] = new byte[25000];
	int used = 0;
	LinkedList memList = new LinkedList();

	public void MyMalloc(String processNo,int processSize){
		LinkedList.Block block;
		if(used>=25000){
			System.out.println("Not enough space.");
		}

		else if(used==0){ //if no block is used, allocating the head of th elinked list
			block=memList.allocate(processNo, processSize);
			used+=block.getSize();
		}

		else{ //if the linked list is not empty
			LinkedList.Block cur = memList.head;
			LinkedList.Block selected=cur;

			while(cur!=null){ //traversing to find a best fitting hole
				if(cur.holeExists && (processSize<=cur.processSize)){
					if(cur.processSize<=selected.processSize){
						selected=cur;
					}
				}
				cur=cur.next;
			}

			if(selected==memList.head && selected.holeExists==false){ //no holes have been found
				block=memList.allocate(processNo, processSize);
				used+=block.getSize();
			}
			else{ //a hole has been found
				block=memList.allocateHole(selected, processNo, processSize);
				used+=block.getSize();
			}

			for(int i=block.begin; i<=block.end; i++){
				ram[i]=1; //making the ram partitions one.
			}
		}
		used+=processSize;
	}

	public void MyFree(String processNo){
		LinkedList.Block block=memList.remove(processNo);
		if(block!=null){
			used-=block.processSize;
			used-=block.getSize();

			for(int i=block.begin; i<=block.end; i++){
				ram[i]=0; //making the ram partitions zero.
			}
		}
	}

	public void print(){
		LinkedList.Block current=memList.head;

		while(current!=null)
		{
			if(current.holeExists==true)
				System.out.println("Free space "+current.begin+" - "+current.end);
			else
				System.out.println(current.processNo+" "+current.begin+" - "+current.end);
			current=current.next;
		}

		System.out.println("Total space used "+used+" Bytes");
	}
}

class mymalloc{

	public static void main(String args[]){
		Memory m=new Memory();
		//use m.MyMalloc("Process Number", Process Size) too allocate a process
		//m.MyMalloc("P1",10);

		//use m.MyFree("Process Number") too remove a process
		//m.MyFree("P1");
		
		//use m.print() to view the memory status
		//m.print();		
	}
}