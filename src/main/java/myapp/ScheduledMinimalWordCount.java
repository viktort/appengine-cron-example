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
    DataflowPipelineOptions options = PipelineOptionsFactory.create()
        .as(DataflowPipelineOptions.class);
    options.setRunner(BlockingDataflowPipelineRunner.class);
    options.setProject("cpb100");
//    options.setStagingLocation("gs://dataflow-chrome-oven-144308/stagingForScheduledPipeline");

    Pipeline p = Pipeline.create(options);

    System.out.println("get here 0");
    p.apply(TextIO.Read.from("gs://dataflow-samples/shakespeare/loverscomplaint.txt"))
        .apply(ParDo.named("ExtractWords").of(new DoFn<String, String>() {
          @Override
          public void processElement(ProcessContext c) {
            System.out.println("get here 1");
            for (String word : c.element().split("[^a-zA-Z']+")) {
              if (!word.isEmpty()) {
                c.output(word);
              }
            }
          }
        }))
        .apply(Count.<String>perElement())
        .apply("FormatResults", MapElements.via(new SimpleFunction<KV<String, Long>, String>() {
          @Override
          public String apply(KV<String, Long> input) {
            System.out.println("get here 3");
            return input.getKey() + ": " + input.getValue();
          }
        }))

        .apply(TextIO.Write.to("gs://dataflow-chrome-oven-144308/scheduled"));

    p.run();
  }
}
