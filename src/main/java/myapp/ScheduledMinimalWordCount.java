package myapp;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.io.TextIO;
import com.google.cloud.dataflow.sdk.options.DataflowPipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.cloud.dataflow.sdk.runners.BlockingDataflowPipelineRunner;
import com.google.cloud.dataflow.sdk.transforms.Count;
import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.MapElements;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.transforms.SimpleFunction;
import com.google.cloud.dataflow.sdk.values.KV;

/**
 * Created by viktor.trako on 13/11/16.
 */
public class ScheduledMinimalWordCount {

    public static void run() {
        // Create a DataflowPipelineOptions object. This object lets us set various execution
        // options for our pipeline, such as the associated Cloud Platform project and the location
        // in Google Cloud Storage to stage files.
        DataflowPipelineOptions options = PipelineOptionsFactory.create()
                .as(DataflowPipelineOptions.class);
        options.setRunner(BlockingDataflowPipelineRunner.class);
        // CHANGE 1/3: Your project ID is required in order to run your pipeline on the Google Cloud.
        options.setProject("cpb100");
        // CHANGE 2/3: Your Google Cloud Storage path is required for staging local files.
        options.setStagingLocation("gs://dataflow-chrome-oven-144308/stagingForScheduledPipeline");

        // Create the Pipeline object with the options we defined above.
        Pipeline p = Pipeline.create(options);

        // Apply the pipeline's transforms.

        // Concept #1: Apply a root transform to the pipeline; in this case, TextIO.Read to read a set
        // of input text files. TextIO.Read returns a PCollection where each element is one line from
        // the input text (a set of Shakespeare's texts).
        p.apply(TextIO.Read.from("gs://dataflow-samples/shakespeare/*"))
                // Concept #2: Apply a ParDo transform to our PCollection of text lines. This ParDo invokes a
                // DoFn (defined in-line) on each element that tokenizes the text line into individual words.
                // The ParDo returns a PCollection<String>, where each element is an individual word in
                // Shakespeare's collected texts.
                .apply(ParDo.named("ExtractWords").of(new DoFn<String, String>() {
                    @Override
                    public void processElement(ProcessContext c) {
                        for (String word : c.element().split("[^a-zA-Z']+")) {
                            if (!word.isEmpty()) {
                                c.output(word);
                            }
                        }
                    }
                }))
                        // Concept #3: Apply the Count transform to our PCollection of individual words. The Count
                        // transform returns a new PCollection of key/value pairs, where each key represents a unique
                        // word in the text. The associated value is the occurrence count for that word.
                .apply(Count.<String>perElement())
                        // Apply a MapElements transform that formats our PCollection of word counts into a printable
                        // string, suitable for writing to an output file.
                .apply("FormatResults", MapElements.via(new SimpleFunction<KV<String, Long>, String>() {
                    @Override
                    public String apply(KV<String, Long> input) {
                        return input.getKey() + ": " + input.getValue();
                    }
                }))
                        // Concept #4: Apply a write transform, TextIO.Write, at the end of the pipeline.
                        // TextIO.Write writes the contents of a PCollection (in this case, our PCollection of
                        // formatted strings) to a series of text files in Google Cloud Storage.
                        // CHANGE 3/3: The Google Cloud Storage path is required for outputting the results to.
                .apply(TextIO.Write.to("gs://dataflow-chrome-oven-144308/scheduled"));

        // Run the pipeline.
        p.run();
    }
}
