// package com.example;

// import org.apache.hadoop.io.IntWritable;
// import org.apache.hadoop.io.Text;
// import org.apache.hadoop.mapreduce.Mapper;
// import java.io.IOException;
// import java.util.StringTokenizer;

// public class WordMapper extends Mapper<Object, Text, Text, Text> {
//     private final Text word = new Text();
//     private final Text doc = new Text();

//     public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
//         String document = value.toString().trim();
//         String[] documentSplit = document.split("\\s+");
//         String documentID = documentSplit[0];
//         doc.set(documentID);
//         java.util.HashSet<String> seen = new java.util.HashSet<>();
//         for (int i = 1; i < documentSplit.length; i++) {
//             String documentWord = documentSplit[i].toLowerCase().replaceAll("[^a-z0-9]", ""); // normalize
//             if (seen.add(documentWord)) {                  
//                 word.set(documentWord);
//                 context.write(word, doc);
//             }
//         }
//     }
// }

package com.example;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;
import java.util.HashSet;

public class  DocumentSimilarityMapper extends Mapper<Object, Text, Text, Text> {
    private final Text outKey = new Text();
    private final Text outVal = new Text();

    @Override
    protected void map(Object key, Text value, Context ctx) throws IOException, InterruptedException {
        String line = value.toString().trim();
        if (line.isEmpty()) return;

        String[] parts = line.split("\\s+");
        String docId = parts[0];

        HashSet<String> seen = new HashSet<>();
        for (int i = 1; i < parts.length; i++) {
            String w = parts[i].toLowerCase().replaceAll("[^a-z0-9]", "");
            if (w.isEmpty()) continue;
            if (seen.add(w)) {
                outKey.set("W\t" + w);
                outVal.set(docId);
                ctx.write(outKey, outVal); // (word -> docId)
            }
        }

        // Emit doc size once
        outKey.set("D\t" + docId);
        outVal.set(Integer.toString(seen.size()));
        ctx.write(outKey, outVal);       // (doc -> size)
    }
}
