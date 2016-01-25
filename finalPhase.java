import java.util.*;
import java.io.*;



public class finalPhase{

	private CustomMap<String,Pointer> finalLexicon  = new CustomMap<String,Pointer>(); // used to hold the final lexion
	public static CustomMap<Integer,String> conversionTable  = new CustomMap<Integer,String>(); // this is used for the conversion of the table
	private final String directory = "Phase2/index.txt";
	private final String lexiconDirectory = "tempStructures/lexicon.txt";
	private final String outputDir = "Phase3/";
	public static void main(String[]args) throws IOException
	{
		finalPhase Program = new finalPhase();
		Program.run();
	}

	private void run() throws IOException
	{
		readLexicon();
		System.out.println("Going here");
		readData(); // reads the inverted list from the file
	}

	private void readData() throws IOException
	{
		File file = new File(directory);
		Scanner in = new Scanner(file);
		RandomAccessFile raf = new RandomAccessFile(outputDir + "finalIndex.txt","rw");
		ArrayList<Integer> temp;
		//ArrayList<Integer> temp2 = new ArrayList<Integer>(); // intermediate arrays for the merging
		int previousId = -1; // used to ck if the documents are the same
		ArrayList<Integer> result = null; // used to hold the result
		while (in.hasNextLine())
		{
			 String line = in.nextLine();
		
			if (result != null) // meaning we have merged previously or read data before, ck if the data read in matches the result's Id
			{

				String [] index = line.split("\\s+");
				if (!index[0].isEmpty())
				{
					int id = Integer.parseInt(index[0].trim());
					temp = new ArrayList<Integer>();
					//System.out.println(index[0]);
					String [] occurances = index[1].split(":"); // split all the ints and freqency for the merging
					for (int i = 0; i < occurances.length; ++i)
					{
						//System.out.println(occurances[i]);
						temp.add(Integer.parseInt(occurances[i].trim())); // adds the docId and Frequency in the list. DocId followed by the frequency
					}
					// they are the same ID, so merge the two postings
					if (id == previousId) 
					{
						//System.out.println("INSIDE MERGRING" + id + " === " + previousId);
						result = merge(result,temp);
						//System.out.println("AFTER MERGE:" + result);
					}
					// if they are different wordIds, then just write to the file
					else 
					{
						// put the document ID and the pointer and number of documents into the lexicon
						System.out.println("Testing " + raf.getFilePointer());
						finalLexicon.put(conversionTable.get(previousId),new Pointer(raf.getFilePointer(),result.size()/2));
						// write out the list to the file
						writeToFile(raf,result,previousId);

						// just swap the two lists
						ArrayList<Integer> pointer = result;
						result = temp;
						temp = pointer; // swap the value
						previousId = id;
						//System.out.println(in.nextInt());
					}

					temp.clear();
				}// end of if
			}
			else
			{
				// just read the data first
				String [] index = line.split("\\s+");
				previousId = Integer.parseInt(index[0].trim());

				// create a new List to hold the values
				result = new ArrayList<Integer>(); 
				//System.out.println(index[1]);
				String [] occurances = index[1].split(":"); // split all the ints and freqency for the merging
				for (int i = 0; i < occurances.length; ++i)
				{
					//System.out.println(occurances[i]);
					result.add(Integer.parseInt(occurances[i].trim())); // adds the docId and Frequency in the list. DocId followed by the frequency
				}
			}
	
		} // end of while loop
		finalLexicon.put(conversionTable.get(previousId),new Pointer(raf.getFilePointer(),result.size()));
		// System.out.println(result.toString());
		writeToFile(raf,result,previousId);
		in.close();
		raf.close();
		raf = new RandomAccessFile(outputDir + "finalLexicon.txt","rw");
		finalLexicon.writeToFile(raf); //write the final lexicon
		raf.close();
	}

/**
* Merges the two lists and returns the new list
*/
	private ArrayList<Integer> merge(ArrayList<Integer> list1, ArrayList<Integer> list2)
	{
		ArrayList<Integer> temp = new ArrayList<Integer>(); // used for the merging of the two lists
		int i,j;
		i = j =0;
		Integer temp1;
		Integer temp2;
		// System.out.println("list1 = " + list1);
		// System.out.println("list2 = " + list2);
		// temp1 = t.get(i);
		// temp2 = t.get(j);
		for (; i<list1.size()-1 && j < list2.size()-1;)
		{
			temp1 = list1.get(i);
			temp2 = list2.get(j);
			if (temp1 < temp2)
			{
				temp.add(temp1);
				temp.add(list1.get(++i)); // get the freq
				++i;
				
			}
			else
			{
				temp.add(temp2);
				temp.add(list2.get(++j)); // get the freq
				++j;
				
			}
		}
		//System.out.println("Intermeddiate:" + temp + "   " + j);
		// copy over the remaining elements
		if (i < list1.size())
		{
			// append to the end
			while(i<list1.size())
			{
				temp.add(list1.get(i++));
				//++i;
			}
		}
		else if (j < list2.size())
		{
			while (j < list2.size())
			{
				temp.add(list2.get(j++));

			}
				
		}
		//System.out.println("AFTER MERGE:" + temp);
		return temp;

	} // end of function

	private <T> void writeToFile(RandomAccessFile raf, ArrayList<T> list,int id) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < list.size()-1; ++i)
		{
			builder.append(list.get(i) + " " + list.get(++i)+" ");
		}
		builder.append("\n");
		raf.writeBytes(builder.toString());
	}

	private void readLexicon() throws IOException
	{
		// this will read the lexicon thats on the disk from the previous stage
		File file = new File(lexiconDirectory);
		//Scanner in = new Scanner(file);
		RandomAccessFile raf = new RandomAccessFile(file,"rw");
		String line;
		while ((line = raf.readLine()) != null)
		{
			// String line = in.nextLine();
			// String [] 
			//System.out.println(line);
			String [] values = line.split("\\s+");

			// int x = in.nextInt();
			conversionTable.put(Integer.parseInt(values[1].trim()),values[0]);
			//System.out.println(in.next() + " " + in.nextInt());
		}
		raf.close();
	}
}

// used to hold the lexicon of the inverted list
class CustomMap<K,V> extends TreeMap<K,V>
{
	  public void writeToFile(RandomAccessFile raf) throws IOException
    {
    	//raf.writeUTF(dictionary.toString());
    	for (Map.Entry<K, V> entry : super.entrySet())
 		{
		 //((List)(entry.getValue())).setOffset(raf.getFilePointer()); // get the offset to this position
		 raf.writeBytes(entry.getKey() + " " + entry.getValue().toString() + "\n" );
		 //raf.writeBytes(entry.getValue().toString()+"\n" );
		 // clear the list
		 //((List)entry.getValue()) .clearList();   
 			
 		}	
    }
}


class Pointer{
	private long fp = 0 ; //file pointer
	private int documents = 0; // contains the number of documents this word occurs in

	public Pointer(long fp,int count)
	{
		this.fp = fp;
		this.documents = count;
	}



	public void addPointer(long fp)
	{
		this.fp = fp;
	}
	public void AddDocumentCount(int count)
	{
		this.documents = count;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("FilePointer " + fp + " Documents " + documents);
		return builder.toString();
	}
}
