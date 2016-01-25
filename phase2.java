import java.util.*;
import java.io.*;





public class phase2
{
	public static int Memsize = 100000000; // 3 GB for sorting 

	private  ArrayList<String> fileList = new ArrayList<String>(); // used to store the files
	//private ArrayList<byte []> byteList = new ArrayList<byte []>();
	private ArrayList<DataBuffer> bufferList = new ArrayList<DataBuffer>();
	
	private int size; // used to hold the int size for the array



    public static final String directoryName = "Phase1/"; // this the directory name, manully put it
    public static final String outputDirectory = "Phase2/";
	//public static final String directoryName = "test/"; // this the directory name, manully put it

	public static void main(String [] args) throws IOException
	{
		phase2 Program = new phase2();
		Program.run();
	}

	private void run() throws IOException
	{
		getFiles();
		//readLexicon(); // get the lexicon thats in main mem
		//System.out.println(fileList.size());

		int totalFiles = fileList.size()+ 1; // one for the output file
		System.out.println(totalFiles);
		size = (int)(Memsize/totalFiles);
		//System.out.println(totalFiles);
		//create the input buffers
		for (int i = 0; i < totalFiles-1;++i)
		{
			//byteList.add(new byte [size]); // insert a new byte array to each one 
			bufferList.add(new DataBuffer(i,fileList.get(i++),size));
			//break;
		}
		//System.out.println(bufferList.size());

		OutputBuffer outBuffer = new OutputBuffer(size); // create a output buffer for the data
	

		for (int i = 0; i < bufferList.size();++i)
		{
			
			bufferList.get(i).readFile(); // reads the file and gets us the string array
			//System.out.println(bufferList.get(i));
			//break;
		}
		//System.out.println(bufferList.get(9));

		// create my Queue
		PriorityQueue<DataBuffer> heap = new PriorityQueue<DataBuffer>(new DataComparator());
		for (int i = 0; i < bufferList.size(); ++i)
		{
			heap.add(bufferList.get(i));// add the databuffer to the lisr
			//System.out.println(bufferList.get(i).tOString());

		}

		
		DataBuffer temp;
		while (heap.size()!=0)
		{
			//System.out.println("Testing");

			temp = heap.remove(); // remove the data

			outBuffer.addData(temp.getData(),temp.getOccurances());
			if (temp.incrementOffset())
			{

				heap.add(temp);
				//System.out.println(temp.getData());
				//System.out.println("===========testing");
			}
			else
			{
				//System.out.println("=========SKIPPPING" + temp.arrayIndex);
				//temp.arrayIndex;
			}

		}
		// flush the last bit of data
		outBuffer.flushBuffer();

		for (int i = 0; i < bufferList.size(); ++i)
		{
			bufferList.get(i).close();
		}
		outBuffer.close(); // close the stream
	}

	// gets the list of all the files from the given directory
	private void getFiles() 
	{
		File [] files = new File(directoryName).listFiles();
		Arrays.sort(files); // so the index and data files are togther.. data comes first then index
		for (File file : files)
		{
			if (file.isFile());
			{
    			try 
    			{
					//System.out.println(ext);
					fileList.add(file.getName()); // add this name only if its a valid extenion. (.zip file)
					System.out.println(file.getName());
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

/**  this class is used to keep everything togther, meaning the value we left of at, string[] of data 
      It will also read the data from the file and fill the buffer when the data runs out

*/
class DataBuffer{

	//private String [] data;
	private ArrayList<String> data;
	private int offset = 0; // where we left of from the array
	public int arrayIndex; // used to keep track of which array buffer we came from
	private RandomAccessFile raf = null; // used to read the file
	private int size = 0; // size of the memory buffer
	private boolean done = false; // are we done reading the file yet?



	public DataBuffer(int index,String fileName, int size ) throws IOException
	{
		
		this.arrayIndex = index;
		raf = new RandomAccessFile(phase2.directoryName + fileName,"r"); // open a new stream for reading the file
		this.size = size;
		data = new ArrayList<String>();
		
		// index 0 would be wordid
		// index 1 would be the actual documents and freq
	}

	/***
	Reads upto byteSize of data in the byte array
	// this will split all the strings of all the file 
			// basically it splits in groups of two. The first is the word ID and the second is all the freqeuency and occurrances. 
			// so [0] -- > Will denote the wordID
			//    [1] --> Will denote all the documents this word comes in, along with the Frequency
	*/
	public boolean readFile() throws IOException
	{
		// meaning we are done
		if (done)
			return false;

		// keep reading until we have hit the capacity limit or we have reached the end of the file		
		int temp = 0; // count the times we have read the file
		String line;
		while (((line = raf.readLine()) != null) && temp < size)
		{
			// get the total number of bytes this string has
			// remember that each char is 2 bytes
			temp += line.length() *2 ;

			// add the data to the arrayList
			// index 0 will the docId
			// index 1 will be the actual occurances 

			// split the string using spaces
			String [] values;
			values = line.split("\\s+");
			data.add(values[0]);
			data.add(values[1]);
		}

		// meaning we have reached the end of the file
		// so next time just exit the method
		if (temp >= size || line == null)
			done = true;
		return true; // meaning we have reached the end of the file
	}

	public int getData()
	{
		// returns the DocId
		return Integer.parseInt(data.get(offset)); // get the WordID of the current position

	}
	public String getOccurances()
	{
		return data.get(offset+1); // this returns all the occurances for the given wordId
	}

	public boolean incrementOffset() throws IOException
	{
		offset+=2; // increment the offset to the next wordID, remember wordID's are off by every 2!
		if (offset >= data.size() -1)
		{
			
			// meaning we have read all the data from the byte, read more data
			offset = 0; // reset the offset
			//System.out.println("TEST");
			return readFile(); // reads the data from the file, if the data has run out, then we have no more data left
		}
		return true; // meaning we still have data, so we can add this to the heap again
	}
	/***
	Closes the stream
	*/
	public void close() throws IOException
	{
		raf.close();
	}

	protected void finalize() throws Throwable
	{
		raf.close();
	}



	//@Override
	public String tOString() throws IOException
	{
		StringBuilder builder = new StringBuilder();
		//System.out.println(data.length);
		//return data[offset]; // testing purpose, just return the wordID for now
		builder.append(getData()+"\n");
		// for (int i = 0; i < data.length - 1; i+=2)
		// {
		// 	//System.out.println(data[i] + data[i+1]);
		// 	builder.append(getData()+"\n");

		// }
		while (incrementOffset())
		{
			builder.append(getData() + "\n");
		}
		return builder.toString();
		//return Integer.toString(data.length);
	}


}

class OutputBuffer{

	private int memSize; // the size of the outputBuffer
	private int currentSize = 0; // used to keep track of the current size
	private StringBuilder string; //this is used to store all the data. Remeber, each char is 2bytes in java
	//private RandomAccessFile raf;
	private int flushNumber = 0;
	private PrintWriter raf;

	public OutputBuffer(int memSize) throws IOException
	{
		this.memSize = memSize;
		raf = new PrintWriter(phase2.outputDirectory + "index.txt"); // creates a new file for the databuffer
		string = new StringBuilder("");
	}

	public void addData(int wordId,String occurrances) throws IOException
	{
		//String word = merge.conversionTable.get(wordId);
		string.append(wordId+ " " + occurrances +"\n" );
		//System.out.println(string.toString());
		currentSize = string.length(); // get the length of the current string
		// each char takes 2 bytes
		if (2*currentSize >= (.9)*memSize)
			flushBuffer(); // flushes the data

	}

	/**
	* We have reached the capicity of the buffer, so we will flush the data to the output file, and reset all the values
	*/
	public void flushBuffer() throws IOException
	{
		System.out.println("Flushing " + flushNumber++); // debugging purposes
		raf.write(string.toString() + "\n");
		string.setLength(0); // sets the length to 0 and we will reuse the stringBuilder
		currentSize = 0; // set the current size to 0

	}

	public void close() throws IOException
	{
		raf.close(); // closes the stream
	}

	protected void finalize() throws Throwable
	{
		raf.close();
	}


}

class DataComparator implements Comparator<DataBuffer>{

	@Override
	public int compare(DataBuffer data1, DataBuffer data2)
	{
		// if (x.getData() > y.getData())
		// 	return 1;
		// if (x.getData() < y.getData())
		// 	return -1;
		//System.out.println(data1.getData() + " = " + data2.getData());
		
		// int x = Integer.parseInt(data1.getData().trim());
		// int y = Integer.parseInt(data2.getData().trim());
		int x = data1.getData();
		int y = data2.getData();
		//System.out.println(x + "  " + y);
		// return 0; // they are equal
		if( x > y)
			return 1;
		if (x < y)
			return -1;
		return 0;
		//return data1.getData().compareTo(data2.getData());
	}
}

