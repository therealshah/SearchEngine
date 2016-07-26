
************************************************************************
Web Seach Engines
Search Engine - Course Project 
SHAHZAIB JAVED
************************************************************************

Built a full actual search engine


************************************************************************

Phase 1 - Reads and parses the files and makes inverted lists for each words

	Data Sructures:

	Lexicon: Store the mapping between a word(string) and wordId(int)
	URLTable: Store the mapping between document url and docId
	Inverted List: basically a dictionary, where key is word id and value is a list of wordId 
	
	Reads and parses the input files. First, we check if the given document has been seen (from it's URL) using the URLTable. The URLTable basically maps the document url to an int, which is better for compression. SO the everytime a new url is encountered, it is inserted in the URLTable and the counter is incremented. So the URLTable essentially gives each url a docId, which is unique. 
		Next, it parses the document using the Parser.java code, which parses the html and javascript out and 
	gets each word. Similar to how we mapped a document url to a docId, we map each word to a wordId, for similar reasons. Basically, we keep an inverted list for each word id, where the key is the word id and each word id has a list of docIds, along with the frequency. So when we encounter a word ( which we parsed from a document with a unique docID), we basically check if this docId already exists for the given wordId, if so, increment the freq. If not, insert it with freq 1. The list is sorted in ascending order by docId
		Here's the key, the structures Lexicon and URLTable can fit in main memory, whereas the inverted index
	will not. So we flush the inverted index, once main memory gets full. Notice, each flush is to a new file and different files may have the same wordIds, which will be taken care off in the next two phases

	Ex: inverted index:
		wordId: 	<docId,freq> ..... 
		15 : 		<10,15> , <15, 2> ... 




************************************************************************
Phase 2
	
	In the last stage we ended up with n inverted lists, in n different files. In this phase we will merge
	the n inverted lists, into one file.
		We will use a n-way merge. We have n input buffers and one output buffer. We push the first entry from 
	each file and push on to a min heap. We extract the min and push it to the output buffer, and take the next value the file this value corrosponded with. We keep doing this until we have finished all the files. 

************************************************************************

Phase 3 
	
	In this phase, we have merged the lists into one huge file, but a particular wordId may have mulitple (concurrent) entries, since we didn't merge the different lists. In this last step, we merge the separate lists into one and make the final structure for our search engine, the inverted index.

		We read in entries from the inverted list until our mem is full ( merging each separate list into one). 
	Now we are guaranteed each wordId appears only once. Now we proceed to making the inverted index. The inverted index, similar to the inverted list, is a dictionary and the key is the wordId and now the values are a list containing 3 things: 
		position in file where the inverted list is located, number of documents this word appears in, total freq of this word
 	For each wordId, we add an entry to the inverted index (in main mem) and write the corrosponding inverted list onto the hardrive on a file. But we get where exactly on the file we stored this inverted list and store it in the inverted list, along with document count and total freq. We store the file location, so we can use random access to quickly load the inverted list of a particular word into main mem during for query processor, rather than reading it sequentially. We will do this until we have finished all the files. The reason we do it this way is because the inverted index is small enough to fit in main mem where as inverted list is not.
 		At the end we have 4 main structures:

 		1.Lexicon : holds mapping between wordID and word
 		2.UrlTable : holds mapping between documentUrl and documentId
 		3.invertedIndex: holds mapping between wordId and its location on where its stores, freq and # of documents it appears in
 		4. inverted list: For each wordId, it holds a list of all documents along with the freq in that document that the wordID appeared in

************************************************************************

QueryProcessor
	
	Now we have the backend of the search engine, this is sort of the front end. The user sends a query and this 	returns the top n results. The lexicon, urltable and inverted index are loaded into main mem.

	The top n results is determined as follows:
		1. Maps word into wordId using lexicon
		2. Checked if a docId exists with all three words( more on this later)
		3. For every docId, computes BM25 score (huge function that use freq, document occurances etc and outputs a score) and push onto heap. 
		4. Take top n docIds and converted them into a url using urlTable
		5. Outputs them to the user, ranking determined by Bm25 (highest ranking == 1st)

	How to determine if all words exist in a document? 
		Using Document at a time (DAAT) algo

	DAAT

	It basically zips through the documents in a efficent way:
		1. Get inverted list of all wordIds and sort them in ascending order by length.
		2. Set smallest = -1
		3. Get the smallest docId in the first list that's >= smallest. Set smallest to that
		4. Now get the smallest docId in the next list that's >= smallest. Set smallest to that. Now if this equaled the value of previous lists, continue and do the same for next list. If it was diff( meaning greater), go back to step 3, with new smallest value. 
		5. We keep  moving down the list everytime we hit the same docId, and keep reseting to start if we fail. Eventually we'll find a docId common to all words and we are done.


	Ex:

	3 ( shahzaib is awesome) lists with given docIds

	List 1(is): 5,8,10,15

	List 2(shahzaib): 1,3,5,6,8,11,15

	List 3(awesome): 1,2,3,7,8,10,11,12,13,15

	First: Smallest is set to -1
	2. list 1 sets smallest to 5
	3. list 2 sets smallest to 5, and is equal. Go to list 3
	4. List 3 sets smallest to 7 ( different) so go back to list 1, with smallest = 7
	5. List 1 sets smallest to 8.
	6. List 2 sets smallest to 8 (same) go to list 3
	7. List 3 also sets smallest to 8 (same) we have a match (thats one). set smallest to smallest +1 (9)
	8. List 1 sets smallest = 10. 
	9. List 2 sets smallest = 11 ( difff). 
	10. List 1 sets smallest = 15
	11. List 2 sets smallest = 15 (same) Go to list 3
	12. List 3 sets smallest = 15. Output document (smallest = 16 now)
	13. List 1 has no more values ST >= smallest, hence stop.

************************************************************************
