// package com.example;

// public class DocumentSimilarityReducer {
    
// }
package com.example;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

public class DocumentSimilarityReducer extends Reducer<Text, Text, Text, DoubleWritable> {
    // Global (for this single reducer task)
    private final Map<String,Integer> docSizes = new HashMap<>();
    private final Map<String,Integer> pairIntersections = new HashMap<>();

    private final Text outKey = new Text();
    private final DoubleWritable outVal = new DoubleWritable();

    @Override
    protected void reduce(Text key, Iterable<Text> vals, Context ctx) throws IOException, InterruptedException {
        String k = key.toString();

        if (k.startsWith("D\t")) {
            // Key: "D\t<docId>", Values: ["<size>"]
            String docId = k.substring(2);
            for (Text v : vals) { // usually one
                try {
                    docSizes.put(docId, Integer.parseInt(v.toString()));
                } catch (NumberFormatException ignored) {}
            }
            return;
        }

        if (k.startsWith("W\t")) {
            // Key: "W\t<word>", Values: [docId...]
            HashSet<String> uniqDocs = new HashSet<>();
            for (Text v : vals) uniqDocs.add(v.toString());

            if (uniqDocs.size() > 1) {
                List<String> docs = new ArrayList<>(uniqDocs);
                Collections.sort(docs);
                for (int i = 0; i < docs.size(); i++) {
                    for (int j = i + 1; j < docs.size(); j++) {
                        String pair = docs.get(i) + "," + docs.get(j);
                        pairIntersections.merge(pair, 1, Integer::sum);
                    }
                }
            }
        }
    }

    // @Override
    // protected void cleanup(Context ctx) throws IOException, InterruptedException {
    //     for (Map.Entry<String,Integer> e : pairIntersections.entrySet()) {
    //         String pair = e.getKey();
    //         int inter = e.getValue();

    //         int comma = pair.indexOf(',');
    //         String A = pair.substring(0, comma);
    //         String B = pair.substring(comma + 1);

    //         Integer aSize = docSizes.get(A);
    //         Integer bSize = docSizes.get(B);
    //         if (aSize == null || bSize == null) continue;

    //         int union = aSize + bSize - inter;
    //         double j = (union == 0) ? 0.0 : (inter * 1.0) / union;

    //         outKey.set(pair);
    //         outVal.set(j);
    //         ctx.write(outKey, outVal); // "A,B" \t jaccard
    //     }
    // }

    @Override
    protected void cleanup(Context ctx) throws IOException, InterruptedException {
        // Build a sorted list of ALL documents we saw sizes for
        List<String> docs = new ArrayList<>(docSizes.keySet());
        Collections.sort(docs);

        for (int i = 0; i < docs.size(); i++) {
            for (int j = i + 1; j < docs.size(); j++) {
                String A = docs.get(i), B = docs.get(j);
                String pairKey = A + "," + B; // matches how you stored intersections
                int inter = pairIntersections.getOrDefault(pairKey, 0);

                int aSize = docSizes.getOrDefault(A, 0);
                int bSize = docSizes.getOrDefault(B, 0);
                int union = aSize + bSize - inter;
                double k = (union == 0) ? 0.0 : (double) inter / union;

                outKey.set(pairKey);
                outVal.set(k);
                ctx.write(outKey, outVal);
            }
        }
    }
}
