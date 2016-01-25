import java.util.*;
import java.lang.*;
import java.io.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
// import java.util.zip.ZipEntry;
// import java.util.zip.ZipInputStream;
import java.nio.file.*;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;





public class phase1{

	private int docId = 1; // used to keep track of which document we are in
	public static int wordId = 1; // used to keep track of which word we are at
	private CustomMap<Integer,List> dictionary = new CustomMap<Integer,List>(); // word id to the list
	private CustomMap<String,Integer> lexicon = new CustomMap<String,Integer>(); // used to map between words and word id
	private CustomMap<String,UrlTable> urlTable = new CustomMap<String,UrlTable>(); // used to store the document IDS and URL mapping
	private  ArrayList<String> fileList = new ArrayList<String>();
	//private final String directoryName = "nz2_merged/"; // this the directory name, manully put it
	private final String directoryName = "data/"; // this the directory name, manully put it
	private final String outPutDirectory = "Phase1/"; // this is the out directory
	private final String structureDir = "tempStructures/"; // direcotry where the URL table and lexicon table are stored

	public static void main(String [] arg) throws IOException
	{

		phase1 run = new phase1();
		run.runProgram();
		
	} // end of main

	private void runProgram() throws IOException
	{
		getFiles(); // get the list of files
		double divisor = 20;

		int documentsPerFile = (int)Math.ceil(((double)fileList.size())/divisor);
		//System.out.println(documentsPerFile);
		//System.exit(1);
		int j = 0; // file name
		File file = new File(outPutDirectory + (j++) + ".txt");
		//File lexiconFile = new File(outPutDirectory + "lexicon.txt");
		//RandomAccessFile rafLex = new RandomAccessFile(lexiconFile,"rw");
		RandomAccessFile raf = new RandomAccessFile(file,"rw");
		for (int i = 0; i< fileList.size(); i++)
		{
			String dataFile = fileList.get(i);
			String indexFile = fileList.get(++i);
		
			//GZIPInputStream in = new GZIPInputStream(new FileInputStream(directoryName + indexFile));
			GZIPInputStream data = new GZIPInputStream(new FileInputStream(directoryName+ dataFile)); // used to read the data

	 	// 	// read the index
			// Reader decoder = new InputStreamReader(in);
			// BufferedReader br = new BufferedReader(decoder);

			// read the data file
			Reader decoder2 = new InputStreamReader(data);
			BufferedReader br2 = new BufferedReader(decoder2);
			 
			String line;
			String [] array;
			String words;
			Parser par = new Parser();
			StringBuilder builder = new StringBuilder();
			//PrintWriter file = new PrintWriter(outPutDirectory + (j++) + ".txt");
			// make a new file only if after we have written 9 doucments to a single document
			if (i%documentsPerFile == 0)
			{
				raf.close(); // close current file stream
				file = new File(outPutDirectory + (j++) + ".txt");
				raf = new RandomAccessFile(file,"rw"); // make a new file

			}
			
			while ((line = br.readLine()) != null) 
			{
				
				array = line.split("\\s+");
			    //System.out.println(array[3]);
			    //System.out.println("words");
			   // System.out.println(array[6]);
				int size = Integer.parseInt(array[3]);
				char[] array2 = new char [size]; // hold the whole content of the page	
			    if (array[6].equals("ok")) // ck to see if the pgae is ok
			    {
			    	//System.out.println("test");
			    	//int id = (urlTable.get(array[0]) == null): docId++ : urlTable.get(array[0]); // if the url exists in the urlTable, fetch otherwise assign a new ID and increment
				    if (urlTable.get(array[0]) == null) // if we have not yet encountered this url. Chances are if it's the same exact URL, then the content is also the same, so just skip it

				    {
				    	//urlTable.put(array[0],docId); // insert this to the urlTable	
				    	urlTable.put(array[0],new UrlTable(docId,size));	    
					    br2.read(array2,0,size); // read the data into the char array			
					    words = new String(array2); // convert the char array to a string array for parsing
			        	par.parseDoc(array[0], words, builder,dictionary,lexicon,docId++); // pass in the new docID, and increment to the next one
				    }
				    else
				    {
				    	// we have already encountered this url, no point to reading it again
			    		br2.read(array2,0,Integer.parseInt(array[3])); 
				    }
			    }
			    else
			    {
			    	// read the messed up data in the array so we dont encounter it
			    	br2.read(array2,0,Integer.parseInt(array[3])); // this data is bad (according to the index file and skip it)
			    }
			    
			   
			}
			//System.out.println(dictionary);
			dictionary.writeToFile(raf);
			dictionary.clear(); // clear the index
			in.close();
			data.close();
			decoder.close();
			br.close();
			decoder2.close();
			br2.close();
		}
		PrintWriter writer = new PrintWriter(structureDir + "lexicon.txt");
		lexicon.writeToFile(writer);// write the lexicon to the file
		writer.close();
		writer = new PrintWriter(structureDir + "urlTable.txt"); // create a new file for the urlTable
		urlTable.writeToFile(writer); // write the URL Table to the file
		//System.out.println(lexicon.toString());

		//rafLex.close();
		raf.close();
		writer.close();
	}

	private void getFiles() 
	{
		File [] files = new File(directoryName).listFiles();
		Arrays.sort(files); // so the index and data files are togther.. data comes first then index
		for (File file : files)
		{
			if (file.isFile());
			{
    			try 
    			{	String name = file.getName();
					String ext =  name.substring(name.lastIndexOf(".") + 1);
        			if ((!ext.equals("txt")) && (!ext.equals("inf")))
        			{
        				//System.out.println(ext);
        				fileList.add(name); // add this name only if its a valid extenion. (.zip file)

        			}
    			} 
    			catch (Exception e)
    			{
        				System.out.println("Error in listing the files");
        				System.exit(1);
    			}
				
			}
		}

	}
}

class CustomMap<K,V> extends TreeMap<K,V>
{
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
 		for (Map.Entry<K, V> entry : super.entrySet())
 		{
 			 builder.append(entry.getKey() + " " + entry.getValue() + "\n");     		
 		}
 		 //System.out.println(builder.toString());
 		return builder.toString();
    }

    public void writeToFile(PrintWriter writer) throws IOException
    {
    	for (Map.Entry<K,V> entry: super.entrySet())
    	{
    		//System.out.println(entry.getValue().toString() + "\n");
    		writer.write(entry.getKey() + " " + entry.getValue().toString()+"\n" );
    	}
    }

    public void writeToFile(RandomAccessFile raf) throws IOException
    {
    	//raf.writeUTF(dictionary.toString());
    	for (Map.Entry<K, V> entry : super.entrySet())
 		{
		 
		 raf.writeBytes(entry.getKey() + " " + entry.getValue().toString() + "\n");
		 //raf.writeBytes(entry.getValue().toString()+"\n" );
		 // clear the list
		 ((List)entry.getValue()) .clearList();   
 			
 		}	

    }
	
}

// class used to group all the information of the list togther ( start and end of the inverted list, number of occurances etc)
class List{
	private ArrayList<Data> list;
	//private long fp; // file pointer within the file to allow us to randomly access the file
	//private int wordID =0;

	public List(int docId)
	{
		list = new ArrayList<Data>(); // initialze to a new list
		list.add(new Data(docId));
	}

	
	public void clearList() 
	{
		// used to delete the list so 
		list.clear();
	}

	public void addDocId(int docId)
	{
		list.add(new Data(docId)); // we found a new occurance in a different document
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		//builder.append("\n");
		builder.append(list.get(0).getDocId()+ ":" + list.get(0).getFreq());
		for (int i = 1; i < list.size(); ++i)
		{
			// docId and freq are separated by space. Different postings separated by comma
			builder.append(":" + list.get(i).getDocId() + ":" + list.get(i).getFreq());

		}
		//builder.append("\n");

		//
	
		return builder.toString();
	}

// Tells us if the list has the document ID
	public boolean contains(int docId)
	{
		return list.contains(docId); 
	}

	public int getLastDocId()
	{
		// used to return the last document id, so we dont insert the same docId for a given word twice!
		return list.get(list.size() - 1).getDocId();
	}
	public void increaseFreq()
	{
		 list.get(list.size() - 1).increaseFreq(); // increase the freq of the word
	}
}

// used to hold the different data for each occurance
class Data{

	private int docID = 0; // which document it occurred in
	private int frequency = 1; // how many times did the word appear?
	//private int position; // which position the word occured in

	public Data(int id)
	{
		this.docID = id; 
	}

	public void increaseFreq()
	{
		frequency++; // increase the freq
	}
	public int getFreq()
	{
		return frequency;
	}

	public int getDocId()
	{
		return docID;
	}
}

class UrlTable{
	private int docId;
	private int pageLength;

	public UrlTable(int docId,int pageLength)
	{
		this.docId = docId;
		this.pageLength = pageLength;
	}

	public String toString()
	{
		return docId + " " + pageLength;

	}
}