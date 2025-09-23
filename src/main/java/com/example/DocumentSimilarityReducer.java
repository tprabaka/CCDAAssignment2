package com.example;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

public class DocumentSimilarityReducer extends Reducer<Text, Text, Text, DoubleWritable> {
    private final Map<String,Integer> docSizes = new HashMap<>();
    private final Map<String,Integer> pairIntersections = new HashMap<>();

    private final Text outKey = new Text();
    private final DoubleWritable outVal = new DoubleWritable();

    @Override
    protected void reduce(Text key, Iterable<Text> vals, Context ctx) throws IOException, InterruptedException {
        String k = key.toString();

        if (k.startsWith("D\t")) {
            String docId = k.substring(2);
            for (Text v : vals) {
                try {
                    docSizes.put(docId, Integer.parseInt(v.toString()));
                } catch (NumberFormatException ignored) {}
            }
            return;
        }

        if (k.startsWith("W\t")) {
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

    @Override
    protected void cleanup(Context ctx) throws IOException, InterruptedException {
        List<String> docs = new ArrayList<>(docSizes.keySet());
        Collections.sort(docs);

        for (int i = 0; i < docs.size(); i++) {
            for (int j = i + 1; j < docs.size(); j++) {
                String A = docs.get(i), B = docs.get(j);
                String pairKey = A + "," + B;
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
