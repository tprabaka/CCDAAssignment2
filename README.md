# Assignment 2: Document Similarity using MapReduce

**Name: Thejas Prabakaran** 

**Student ID: 8014149982** 

## Approach and Implementation

### Mapper Design
So the mapper class produces two types of key value pair for each text input that comes in. The mapper class recieves each line of the input text file as the text input for example if the input text file is like "Document1 Lorem ipsum ..." , Then input will be Text = "Document1 Lorem ipsum ...". Now this input text is split into list of string by spaces. Then the first item in the list will be "Document1", this is one type of key value pair. Then the list of string is converted into small case and the symbols are removed from the string in the list. Then a Hashset is created out of rest of the strings to get the set of non duplicate strings. The hash set is assigned the document ID which the key will be unique string and the document id we got form input text, this is the another type of key value pair. The first key value pair contains total number of words in the respective document. As the solution I am designing contains only one stage of map reduce, I would like to know the total number of unique words in the document and the words presence in each of the document to compute intersection.

### Reducer Design
The Reducer takes keys of the form "D\t<docId>" with document sizes and "W\t<word>" with lists of documents containing that word. For "D\t", it stores each document’s size. For "W\t", it finds all document pairs sharing the word and increments their intersection count. In cleanup, it computes Jaccard similarity for every document pair using the fomula outputs <docA,docB> with the similarity score

### Overall Data Flow
The input files contain documents with an ID and words. The Mapper emits (W\tword, docId) for each unique word and (D\tdocId, docSize) once per document. In the shuffle/sort phase, all values are grouped by key so the Reducer sees all docs for a word and all sizes for a document. The Reducer records document sizes, counts intersections for document pairs sharing words, and in cleanup computes Jaccard similarity = intersection ÷ union. The final output is each document pair with its similarity score.

---

## Setup and Execution

### 1. **Start the Hadoop Cluster**

Run the following command to start the Hadoop cluster:

```bash
docker compose up -d
```

### 2. **Build the Code**

Build the code using Maven:

```bash
mvn clean package
```

### 4. **Copy JAR to Docker Container**

Copy the JAR file to the Hadoop ResourceManager container:

```bash
docker cp target/DocumentSimilarity-0.0.1-SNAPSHOT.jar resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 5. **Move Dataset to Docker Container**

Copy the dataset to the Hadoop ResourceManager container:

```bash
docker cp shared-folder/input/data/input.txt resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 6. **Connect to Docker Container**

Access the Hadoop ResourceManager container:

```bash
docker exec -it resourcemanager /bin/bash
```

Navigate to the Hadoop directory:

```bash
cd /opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 7. **Set Up HDFS**

Create a folder in HDFS for the input dataset:

```bash
hadoop fs -mkdir -p /input/data
```

Copy the input dataset to the HDFS folder:

```bash
hadoop fs -put ./input.txt /input/data
```

### 8. **Execute the MapReduce Job**

Run your MapReduce job using the following command:

```bash
hadoop jar /opt/hadoop-3.2.1/share/hadoop/mapreduce/DocumentSimilarity-0.0.1-SNAPSHOT.jar com.example.controller.DocumentSimilarityDriver /input/data/input.txt /output
```

### 9. **View the Output**

To view the output of your MapReduce job, use:

```bash
hadoop fs -cat /output1/*
```

### 10. **Copy Output from HDFS to Local OS**

To copy the output from HDFS to your local machine:

1. Use the following command to copy from HDFS:
    ```bash
    hdfs dfs -get /output1 /opt/hadoop-3.2.1/share/hadoop/mapreduce/
    ```

2. use Docker to copy from the container to your local machine:
   ```bash
   exit 
   ```
    ```bash
    docker cp resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/output1/ shared-folder/output/
    ```
3. Commit and push to your repo so that we can able to see your output


---

## Challenges and Solutions

So regarding the challenges while doing this assignment, it started from first to end. First challenge was that the framework to write and understand code was very tough what each argument mean and we are not using Java data types. Then after learning about them, found out these programs do not execute sequentially like not one mapper and one reducer therre will be mutiple instances of them. So the the code we right have to be independant so as to execute the the necessary tasks. Then came desiging the solution, the concept of jaccardian similarity is very easy and straightforward in classical programming but in this case we have carefully design the solution so as to not cause error because of the nature of execution of code in this framework. After finalising on the idea on how to do then came the problems associated with data storage because in my idea I need to have something to store the data that is global so have to find the work around it . Then I executed the code the code was to run but was facing the issues with the output. I got wrong output but the logic was correct, then I found out as the reducer task finish executing in different times the final data received is sometime incomplete to compute the simialrity sdcore then the required changes were made. Finally some challenges while experimenting the different datasets which I made throughafter brushing ioff old OS concepts. I faced no environment issues both while executing in local or in codespaes

---

## Analysis on 3 Data Nodes vs 1 Data nodes

I could not find any big difference in performace while experimenting with my datasets. It may be due to less number of document. But I felt the 3 data nodes executed them a bit faster and easily to complete the program compared to 1 nodes.

---

## Results and Input Dataset Information

I created a dataset generator Python file to create a text file with the N number of words (randomly generated) and the required number of documents.  
The input datasets I used are in the `shared-folder`, and the results I obtained are also stored in the same folder.
I have also a folder created to upload all the screenshots of output from the execution and experimenting. It will be available in the "Screenshots" folder, the file name will denote what dataset where dataset 1 - denote 1000 words dataset, dataset 2- 3000 word dataset and so on.


```text
.
├── shared-folder
│   ├── input
│   │   └── data
│   │       ├── input.txt        // Small sample dataset
│   │       ├── inputTest1.txt   // 1000 words dataset
│   │       ├── inputTest2.txt   // 3000 words dataset
│   │       └── inputTest3.txt   // 5000 words dataset
│   ├── output
│   │   ├── 1_Node               // 1 Node Execution Output
│   │   │   ├── output1_1        // 1000 words dataset
│   │   │   ├── output1_2        // 3000 words dataset
│   │   │   └── output1_3        // 5000 words dataset
│   │   └── 3_Node               // 3 Node Execution Output
│   │       ├── output           // Small sample dataset output
│   │       ├── output1          // 1000 words dataset
│   │       ├── output2          // 3000 words dataset
│   │       └── output3          // 5000 words dataset
└── Screenshots                  // Screenshots of execution results
```

---
## Sample Input

**Input from `small_dataset.txt`**
```
Document1 This is a sample document containing words
Document2 Another document that also has words
Document3 Sample text with different words
```
## Sample Output

**Output from `small_dataset.txt`**
```
"Document1, Document2 Similarity: 0.56"
"Document1, Document3 Similarity: 0.42"
"Document2, Document3 Similarity: 0.50"
```