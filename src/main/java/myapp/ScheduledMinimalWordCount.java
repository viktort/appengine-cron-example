package myapp;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.io.TextIO;
import com.google.cloud.dataflow.sdk.options.DataflowPipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.cloud.dataflow.sdk.runners.BlockingDataflowPipelineRunner;
import com.google.cloud.dataflow.sdk.runners.DataflowPipelineRunner;
import com.google.cloud.dataflow.sdk.transforms.Count;
import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.MapElements;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.transforms.SimpleFunction;
import com.google.cloud.dataflow.sdk.values.KV;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * Created by viktor.trako on 13/11/16.
 */
public class ScheduledMinimalWordCount {

  private static final long FILE_BYTES_THRESHOLD = 10 * 1024 * 1024; // 10 MB

  protected static List<String> detectClassPathResourcesToStage(ClassLoader classLoader) {
    if (!(classLoader instanceof URLClassLoader)) {
      String message = String.format("Unable to use ClassLoader to detect classpath elements. "
          + "Current ClassLoader is %s, only URLClassLoaders are supported.", classLoader);
      throw new IllegalArgumentException(message);
    }

    List<String> files = new ArrayList<>();
    for (URL url : ((URLClassLoader) classLoader).getURLs()) {
      try {
        File file = new File(url.toURI());
        if (file.length() < FILE_BYTES_THRESHOLD) {
          files.add(file.getAbsolutePath());
        }
      } catch (IllegalArgumentException e ) {
        String message = String.format("Unable to convert url (%s) to file.", url);
        throw new IllegalArgumentException(message, e);
      } catch (URISyntaxException e) {
        String message = String.format("Unable to convert url (%s) to file.", url);
        throw new IllegalArgumentException(message, e);
      }

    }
    return files;
  }

  public static void run() {
    DataflowPipelineOptions options = PipelineOptionsFactory.create()
        .as(DataflowPipelineOptions.class);
    options.setRunner(BlockingDataflowPipelineRunner.class);
    options.setProject("cpb100");
    options.setFilesToStage(detectClassPathResourcesToStage(DataflowPipelineRunner.class.getClassLoader());
    options.setStagingLocation("gs://dataflow-chrome-oven-144308/stagingForScheduledPipeline");

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
