package case2;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 
public class MaxBook {
	
	public static  class MaxBookMapper extends
    	Mapper<Object, Text, Text, Text> {

		private Text word = new Text();

		public void map(Object key, Text value, Context context)
		    throws IOException, InterruptedException {
			String[] csv = value.toString().split(";");
			String year = csv[3];
			year = year.replace("\"","");
			try{
				int yea = Integer.parseInt(year);
		    word.set(Integer.toString(yea));
		    context.write(word, new Text("1"));
			}catch(Exception e){}
	    }
}
        public static class MaxBookCombiner extends
        Reducer<Text, Text, Text, Text> {
                @SuppressWarnings("unused")
				public void reduce(Text text, Iterable<Text> values, Context context)
                        throws IOException, InterruptedException {
                    int sum = 0;
                    for (Text value : values) {			
                        sum+=1;
                    }
                    context.write(new Text(""), new Text(text+";"+sum));
                    sum=0;
                }
        }	
	public static class MaxBookReducer extends
    	Reducer<Text, Text, Text, Text> {

		public void reduce(Text text, Iterable<Text> values, Context context)
		        throws IOException, InterruptedException {
		    int sum = 0;
		    String year = "";
		    for (Text value : values) {
		        String []yearMax = value.toString().split(";");
			int max = Integer.parseInt(yearMax[1]);
			if(max> sum){
				sum = max;
				year = yearMax[0];
			}
		    }
		    text.set("Max book published in year "+year+" : ");
		    context.write(text, new Text(""+sum));
		}
	}
 
    public static void main(String[] args) throws IOException,
            InterruptedException, ClassNotFoundException {

    	if(args.length<2){
    		System.out.println("USage: <Class name> <input file><Output folder>");
    		System.exit(0);
    	}
    	
    	//Set input/ Output path
    	Path inputPath = new Path(args[0]);
        Path outputDir = new Path(args[1]);
 
        // Create configuration
        Configuration conf = new Configuration(true);
 
        // Create job
		Job job = Job.getInstance(conf);
        job.setJobName("MaxBook");
        job.setJarByClass(MaxBook.class);
 
        // Setup MapReduce class
        job.setMapperClass(MaxBookMapper.class);
        job.setCombinerClass(MaxBookCombiner.class);
        job.setReducerClass(MaxBookReducer.class);
        
        // Set Output key / value
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
 
        // Input
        FileInputFormat.addInputPath(job, inputPath);
        job.setInputFormatClass(TextInputFormat.class);
 
        // Output
        FileOutputFormat.setOutputPath(job, outputDir);
        job.setOutputFormatClass(TextOutputFormat.class);
 
        // Execute job
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

