package de.ersatzhero.ticketregionalpipe.batch.configuration;

import de.ersatzhero.ticketregionalpipe.batch.Processor;
import de.ersatzhero.ticketregionalpipe.batch.ProcessorListener;
import de.ersatzhero.ticketregionalpipe.batch.Writer;
import de.ersatzhero.ticketregionalpipe.batch.model.ExtendedTicketRegionalData;
import de.ersatzhero.ticketregionalpipe.batch.model.TicketRegionalData;
import de.ersatzhero.ticketregionalpipe.client.GeolocationClient;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class BatchConfiguration {

    @Value("#{environment.BATCH_FOLDER}") String folder;
    @Autowired JobLauncher jobLauncher;
    @Autowired Job uploadTicketRegionalJob;

    @Bean
    @JobScope
    public SystemCommandTasklet convertXLSTasklet(@Value("#{environment.BATCH_PATH2XLS2CSV}") String xls2csvScript,
                                                  @Value("#{jobParameters['fileName']}") String resource,
                                                  @Value("#{jobParameters['csvFileName']}") String csvFile,
                                                  @Value("#{jobParameters['folder']}") String folder) {
        SystemCommandTasklet systemCommandTasklet = new SystemCommandTasklet();
        systemCommandTasklet.setTimeout(5000);
        String command = xls2csvScript + " " + folder + "/" + resource + " " + folder + "/" + csvFile;
        systemCommandTasklet.setCommand(command);
        return systemCommandTasklet;
    }

    @Bean
    public Step convertXLStoCSV(StepBuilderFactory factory, Tasklet convertXLSTasklet) {
        return factory.get("convertXLStoCSV")
                .tasklet(convertXLSTasklet)
                .build();
    }

    @Bean
    @JobScope
    public FlatFileItemReader<TicketRegionalData> reader(@Value("#{jobParameters['csvFileName']}") String csvFile, @Value("#{jobParameters['folder']}") String folder) {
        //Create reader instance
        FlatFileItemReader<TicketRegionalData> reader = new FlatFileItemReader<>();

        //Set file resource
        reader.setResource(new FileSystemResource(folder + "/" + csvFile));

        //Set number of lines to skips. Use it if file has header rows.
        reader.setLinesToSkip(1);

        reader.setStrict(false);

        //Configure how each line will be parsed and mapped to different values
        reader.setLineMapper(new DefaultLineMapper<>() {
            {
                //3 columns in each row
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setStrict(false);
                        setNames(
                            "ticketNumber",
                            "row",
                            "category",
                            "stand",
                            "orderData",
                            "memberNumber",
                            "discount",
                            "price",
                            "notes",
                            "soldBy",
                            "soldAt",
                            "deliveryMethod",
                            "name",
                            "description",
                            "empty",
                            "barcode",
                            "firstEntry",
                            "lastEntry",
                            "scannerEntry",
                            "scannerExit",
                            "scannerZip",
                            "phoneNumber",
                            "address",
                            "firstName",
                            "lastName"
                        );
                    }
                });
                //Set values in Employee class
                setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                    {
                        setTargetType(TicketRegionalData.class);
                    }
                });
            }
        });
        return reader;
    }

    @Bean
    @JobScope
    public Processor processor(GeolocationClient osmClient, @Value("#{jobParameters['fileName']}") String resource) {
        Pattern compile = Pattern.compile("buchungen_(.*)_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}\\.xls");
        Matcher matcher = compile.matcher(resource);

        if (matcher.find()) {
            return new Processor(osmClient, resource, matcher.group(1));
        }
        throw new RuntimeException("Could not extract event name from filename.");
    }

    @Bean
    public ItemWriter<ExtendedTicketRegionalData> writer(RestHighLevelClient esClient) {
        return new Writer(esClient);
    }

    @Bean
    public ItemProcessListener<TicketRegionalData, ExtendedTicketRegionalData> itemProcessListener() {
        return new ProcessorListener();
    }

    @Bean
    public Step uploadTicketRegionalStep(StepBuilderFactory factory,
                                         FlatFileItemReader<TicketRegionalData> reader,
                                         ItemProcessor<TicketRegionalData, ExtendedTicketRegionalData> processor,
                                         ItemWriter<ExtendedTicketRegionalData> writer,
                                         ItemProcessListener<TicketRegionalData, ExtendedTicketRegionalData> itemProcessListener) {
        return factory.get("uploadTicketRegionalStep")
                .<TicketRegionalData, ExtendedTicketRegionalData>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(itemProcessListener)
                .build();
    }

    @Bean
    @JobScope
    public Tasklet removeFileTasklet(@Value("#{jobParameters['folder']}") String folder, @Value("#{jobParameters['fileName']}") String filename, @Value("#{jobParameters['csvFileName']}") String csvFile) {
        return (sc, cc) -> {
            try {
                Files.delete(Path.of(folder, filename));
                Files.delete(Path.of(folder, csvFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step removeFileStep(StepBuilderFactory factory, Tasklet removeFileTasklet) {
        return factory.get("removeFileStep")
                .tasklet(removeFileTasklet)
                .build();
    }

    @Bean
    public Job uploadTicketRegionalJob(JobBuilderFactory factory, Step convertXLStoCSV, Step uploadTicketRegionalStep, Step removeFileStep) {
        return factory.get("uploadTicketRegionalJob")
                .start(convertXLStoCSV)
                .next(uploadTicketRegionalStep)
                .next(removeFileStep)
                .build();
    }

    @Scheduled(cron = "#{environment.BATCH_CRON}")
    public void scheduledRun() {
        File f = new File(folder);
        String[] list = f.list(new WildcardFileFilter("*.xls"));
        if (list == null) {
            return;
        }
        Arrays.stream(list).map(FileSystemResource::new).forEach(resource -> {
            JobParameters parameters = new JobParametersBuilder()
                    .addString("fileName", resource.getFilename())
                    .addString("csvFileName", resource.getFilename().replace("xls", "csv"))
                    .addString("folder", folder)
                    .toJobParameters();
            try {
                jobLauncher.run(uploadTicketRegionalJob, parameters);
            } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | JobParametersInvalidException | JobRestartException e) {
                e.printStackTrace();
            }
        });
    }
}
