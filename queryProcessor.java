import java.util.*;
import java.io.*;

public class queryProcessor{
	public static void main(String [] args) throws IOException
	{
		queryProcessor Program = new queryProcessor(); // used to run the program
		Program.run();
	}

	//private String inputDirectory = "../shahzaib_javed_hw2/Phase3/"; // directory where the lexicon and inverted list is stored
	//private String inputDirectory = "../shahzaib_javed_hw2/Phase3_nz/";
	//private String urlTablePath = "../shahzaib_javed_hw2/tempStructures/urlTable.txt"; // this the location of the url table from my machine
	private String inputDirectory = "Phase3/";
	private String urlTablePath = "tempStructures/urlTable.txt"; // this the location of the url table from my machine
	private HashMap<String,LexiconValues> lexicon = new HashMap<String,LexiconValues>(); // used to hold the lexicon in main mem
	private HashMap<Integer,UrlTable> urlTable = new HashMap<Integer,UrlTable>(); // creates the URL table
	private ArrayList<MyMap> list = new ArrayList< MyMap >(); // used to hold the list for the queries 
	private PriorityQueue<Pair> heap = new PriorityQueue<Pair>(new DataComparator()); // used to keep the top K documents
	public static int totalDocumentLength = 0; // this basically stores the total document length; will be computed as we insert the documents
	private final int topK  = 10; // top 10 results


/*
* This method is private and is used so the rest of the functions dont need to be static
*/
	private void run() throws IOException
	{	
		System.out.println("Reading lexicon");
		readLexicon();
		System.out.println("reading URl Table");
		readURLTable();

		getQuery();
	
	}

/*
* This method reads the lexicon  from the disk into the main memory
*/
	private void readLexicon()
	{
		try
		{
			File file = new File(inputDirectory+"finalLexicon.txt"); // read the lexicon into main mem
			Scanner in = new Scanner(file);
			String [] values; // used to hold the elements that are read in from the file
			while (in.hasNextLine())
			{
				// splits the string by spaces and stores it in the values array and the indexes are:
				// 0 - word 2- FilePointer 4- Document Occurances
				values = in.nextLine().split("\\s+");					

				// store the file pointer and and the document values
				lexicon.put(values[0],new LexiconValues(Long.parseLong(values[2]),Integer.parseInt(values[4]))); // store the word 


				//System.out.println(in.next());
			}
			in.close();
			//System.out.println(lexicon);
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File not found!");
		}

	}
/*
* This method reads the URL Table  from the disk into the main memory
*/
	private void readURLTable()
	{
		try
		{

			File file = new File(urlTablePath);
			Scanner in  = new Scanner(file); // read the the url Table
			String [] values; // used to splut the input based on white space 
			while (in.hasNextLine())
			{
				//System.out.println(in.next());
				String line = in.nextLine();
				//System.out.println(line);

				values = line.split("\\s+");  // this contains the URL and Document ID 
				
				// 0 index is the url
				// 1 index is the docId
				// 2 index is the length
				urlTable.put(Integer.parseInt(values[1]),new UrlTable(values[0],Integer.parseInt(values[2])));

			}
			//System.out.println(urlTable);

		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found");
		}
	}

	private void getQuery() throws IOException
	{
		// try{
			Scanner in = new Scanner(System.in); // get the user's query
			RandomAccessFile raf = new RandomAccessFile(inputDirectory+ "finalIndex.txt","rw");
			
			while (true){
				String input;
				boolean isResult = true;
				System.out.println("Enter a query: ");
				input = in.nextLine();
				if (input.equals("quit"))
					break;
				boolean disjunctive = input.contains("or"); // if the key word or is in there, then the system is disjunctive
				String [] values = input.split("\\s+"); // split the key words
				//ArrayList<Integer> documentOccurances = new ArrayList<Integer>(); // used to hold the number of documents this term appears in
				for (String s : values)
				{
					LexiconValues documents = lexicon.get(s);
					
					if (!s.equals("or") && !s.equals("and"))
					{
						if (documents != null )
						{
							long filePointer = documents.getFilePointer();
							System.out.print(s + ":");

							// create the list for each file
							openList(raf,filePointer,documents.getDocumentOccurances()); 

							// add the total documents that contain this term
							//documentOccurances.add(documents.getDocumentOccurances()); 
						}
						else
						{
							System.out.println("No results found for the query: " + s);
							isResult = false;
						}
					}
									
				}

				sortArray();
				// this is to execute the nextGEq
				if (disjunctive && isResult) // this is a disjunctive query (or)
				{
					System.out.println("disjunctive query");
					disjunctiveQuery();
				}
				else if(isResult)// conjunctive (and) query
				{
					System.out.println("conjunctiveQuery query");
					conjunctiveQuery();
				}
				//System.out.println(heap);
				while (heap.size() > 0)
				{
					Pair temp = heap.poll(); // get the top element
					System.out.println(urlTable.get(temp.getFirst()).getURL() + ", Score:" + temp.getSecond());
					//UrlTable t = urlTable.get(temp.getFirst());
					//System.out.println(t.getURL());
				}
				// clear the list
				heap.clear();
				list.clear(); 
			} // end of while loop
			raf.close();

		// }
		
		// catch (Exception e)
		// {
		// 	System.out.println(e);
		// 	//raf.close();
		// }


	}

	/*
	* This method sorts the array in increasing list order
	*/
	private void sortArray()
	{

	}

	/*
	* Gets all the documents that intersect with the terms using DISJUNTIVE
	* @param documentOccurances - holds the number of documnets this term appears in
	*/
	private void disjunctiveQuery()
	{
		//int did = 0;
		//int d = 0; // used to store the values of the next lists
		HashMap<Integer,Double> hashTable = new HashMap<Integer,Double>(); // used to look up the hash values // do reverse order so we can look up the terms easily
		// Hash all the smaller ids of the smaller list
		// sort the array

		int N = urlTable.size(); // total number of Documents in the collection
		double k1 = 1.2;
		double b = .75; // constants
		double dAvg = totalDocumentLength/ (double)N; 
		//System.out.println("test");
		for (int i = 0; i < list.size(); ++i)
		{
			int did = 0;
			
			for (int j = 0; j < list.get(i).arraySize();j+=2)
			{

				// did = nextGEQ(0,did); // find the document
				// int freq = getFreq(j,did); // get the freq for this document
				did = list.get(i).getDocument(j); // this is the document ID
				int freq = list.get(i).getDocument(j+1); // this is the frequency of this document
				double bm25Score = 0;
				double ft = list.get(i).getDocumentOccurances(); // get the number of documents this term appears in
				int docLength = urlTable.get(did).getDocumentLength(); // document length
				double K = k1 * ((1-b) + b *(docLength/dAvg));
				bm25Score += (Math.log((N-ft+.5)/(ft+.5)) / Math.log(2)) * ((k1+1)*freq)/(K + freq);
				if (hashTable.get(did)!= null)
				{
					// add to the bm25 score
					//Double value = hashTable.get(did);
					hashTable.put(did,hashTable.get(did) + bm25Score);
				}
				else
				{
					hashTable.put(did,bm25Score);
				}
				
			}
		}
		// now keep adding to the heap until we have out top k results;
		// while (hashTable.size() != 0)
		// {
		// 	if (heap.size() < topK)
		// 	{
		// 		// if we are under just insert

		// 	}
		// }
		// add the elements to get top k results
		for (Map.Entry<Integer,Double> entry : hashTable.entrySet())
		{
			// add in the heap since we don't have the minimum required elements
			if (heap.size() <= topK)
				heap.add(new Pair(entry.getKey(),entry.getValue()));
			else
			{
				if (heap.peek().getSecond() < entry.getValue()) // if the least score is less than the element we are looking for, then remove min and insert this new element
					{
						// we have found a new greater one,, remove the old one and insert the new one
						heap.poll(); // remove head
						heap.add(new Pair(entry.getKey(),entry.getValue()));

					}
			}
		}
		
					
	}


	/*
	* Gets all the documents that intersect with the terms
	* @param documentOccurances - holds the number of documnets this term appears in
	*/
	private void conjunctiveQuery()
	{
		int did = 0;
		int d = 0; // used to store the values of the next lists
		//int maxId = list.get(0).get(list.size()-2); // get the maxDocument Id
		//System.out.println("Before");
		while (did != -1)
		{
			
			did = nextGEQ(0,did); // find the document
			//
			//System.out.println(did);
			for (int i = 1; ((i < list.size()) && ((d=nextGEQ(i,did)) == did)); ++i)
			{
				//System.out.println("inside loop " + d);

			} // get all the documents

			// if the document is greater than the current document or if we havent found it
			//System.out.println(d);
			if ((d > did || d == -1) && list.size() > 1) // only go here if the list size is greater than 1 (i.e more than one term, otherwise it will run infinite times)
			{
				//System.out.println("did = " + did + " d = " + d);
				did = d; // not a intersection
				
			}
			else if (did != -1) // only go here if the did isn't -1
			{
				// lets compute the BM25 score for this document
				// ArrayList<Integer> freq = new ArrayList<Integer>();
				// for (int i = 0; i < list.size();++i)
				// {
				// 	// get the frequency
				// 	freq.add(getFreq(i,did));
				// }
				double bm25Score = 0;
				int N = urlTable.size(); // total number of Documents in the collection
				double k1 = 1.2;
				double b = .75; // constants
				double dAvg = totalDocumentLength/ (double)N; 
				
				for (int i = 0; i < list.size(); ++i)
				{
					int freq = list.get(i).getFreq(did);
					double ft = list.get(i).getDocumentOccurances(); // get the number of documents this term appears in
					int docLength = urlTable.get(did).getDocumentLength(); // document length
					double K = k1 * ((1-b) + b *(docLength/dAvg));
					bm25Score += (Math.log((N-ft+.5)/(ft+.5)) / Math.log(2)) * ((k1+1)*freq)/(K + freq);
				}

				
				//System.out.println("adding to heap");
				if (heap.size() < topK)
				{
					heap.add(new Pair(did,bm25Score)); // insert it into the heap
				}
				else 
				{
					if (heap.peek().getSecond() < bm25Score)
					{
						// we have found a new greater one,, remove the old one and insert the new one
						heap.poll(); // remove head
						heap.add(new Pair(did,bm25Score));

					}
				}

				
				did = did+1; // next document
			}
			
		}
	}

	/*
	* This function finds the next document id greater than or equal to the passed in documnet Id
	* @param documentId: finds the document thats equal to or greater than this
	* @param index: Which arraylist to use
	* @return: Returns the first document ID thats greater than or equal to documentId
	*/
	private int nextGEQ(int index, int documentId)
	{
		if (documentId != -1) // if the document Id isnt -1, then find it
		{
			// //System.out.println(list.get(index).size());
			// for (int i = 0; i< list.get(index).size(); i+=2)
			// {
			// // traverse the array until we find a document atleast documentId
			// if (list.get(index).get(i) >= documentId)
			// 	return list.get(index).get(i);
			// }

			return list.get(index).nextGEQ(documentId);
		}
		

		return -1; // not found // return a document
	}

	private void openList(RandomAccessFile raf, long filePointer, int occurances) throws IOException
	{
			raf.seek(filePointer); // seek to the file location
			String line = raf.readLine(); // read all the documents
			String [] values = line.split("\\s+");
			System.out.println(line); 
			ArrayList<Integer> temp = new ArrayList<Integer>();
			for (String s : values)
			{			
				temp.add(Integer.parseInt(s)); // add the value in the list.. its first the document followed by the frequency
			}
			list.add(new MyMap(temp,occurances)); // add the list in here		
	}

	/*
	* This method will return the frequency for a given document
	* @param Index- This tells us which list to use
	* @param documentId - This is the document that this te
	* @return - returns the frequency of this term for the given document
	*/
	// private int getFreq(int index,int documentId)
	// {
	// 	for (int i = 0; i< list.get(index).size(); i+=2)	
	// 	{
	// 		// traverse the array until we find a document atleast documentId
	// 		if (list.get(index).get(i) == documentId)
	// 			return list.get(index).get(i+1); // return the freq
	// 	}	

	// 	return -1;	
	// }


}

// this is the class that holds the lexicon in main mem
class LexiconValues{
	//private String word;
	private long filePointer; // hold the location of where this word is in the list
	private int documentOccurances; // holds how many documents this word appears in

	public LexiconValues(long filePointer,int documentOccurances)
	{
		//this.word = word;
		this.filePointer = filePointer;
		this.documentOccurances = documentOccurances;
	}
	@Override
	public String toString()
	{
		// StringBuilder builder = new StringBuilder();
		//builder.append("filePointer: " + filePointer + " DocumentOccurances: " + documentOccurances);
		return " filePointer: " + filePointer + " DocumentOccurances: " + documentOccurances;
	}

	public long getFilePointer()
	{
		return filePointer;
	}

	public int getDocumentOccurances()
	{
		return documentOccurances;
	}

}

class UrlTable{
	private String name;
	private int documentLength;


	public UrlTable(String name,int documentLength)
	{
		this.name = name;
		this.documentLength = documentLength;
		queryProcessor.totalDocumentLength+=documentLength; // computes the document length as we go
	}

	public int getDocumentLength()
	{
		return documentLength;
	}

	public String toString()
	{
		return name + " " + documentLength;
	}
	/*
	**  A getter: Returns the name (AKA URL) of the webpage
	*/
	public String getURL()
	{
		return name;
	}

}

class Pair{
	private int documentId;
	private double bm25Score;

	public Pair(int d, Double s)
	{
		documentId = d;
		bm25Score = s;
	}

	public int getFirst()
	{
		return documentId;
	}
	public double getSecond()
	{
		return bm25Score;
	}

	public String toString()
	{
		return documentId + " " + bm25Score;
	}
}

class DataComparator implements Comparator<Pair>
{
	@Override
	public int compare(Pair p1, Pair p2)
	{
		//return p1.getSecond().compareTo(p2.getSecond());
		double x = p1.getSecond();
		double y = p2.getSecond();
		// return x.compareTo(y);
		if (x>y)
			return 1;
		if (x<y)
			return -1;
		return 0;


	}
}

class MyMap
{
	private ArrayList<Integer> occurances;
	private int documentOccurances;

	public MyMap(ArrayList<Integer> temp, int documentOccurances)
	{
		this.occurances = temp;
		this.documentOccurances = documentOccurances; // assign the occurances
	}

	// get the documentID 
	public int getDocument(int i)
	{
		return occurances.get(i);
	}

	public int nextGEQ(int documentId)
	{
		for (int i = 0; i< occurances.size(); i+=2)
		{
		// traverse the array until we find a document atleast documentId
		if (occurances.get(i) >= documentId)
			return occurances.get(i);
		}

		return -1; // not found
	}

	public int getDocumentOccurances()
	{
		return documentOccurances;
	}

	public int arraySize()
	{
		return occurances.size();
	}

	// passed in the document, it basiclly returns this documents freguency
	// which is basically the index after this one
	public int getFreq(int documentId)
	{
		for (int i = 0; i< occurances.size(); i+=2)	
		{
			// traverse the array until we find a document atleast documentId
			if (occurances.get(i) == documentId)
				return occurances.get(i+1); // return the freq
		}	

		return -1;	
	}
}

