package com.telusko.config;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.telusko.binding.Customer;
import com.telusko.repo.CustomerRepository;

@Configuration
public class SpringBatchConfig 
{
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;
	
	//item reader
	
	@Bean
	public FlatFileItemReader<Customer> customerReader()
	{
		FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
		itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv")); //where the csv file
		itemReader.setName("csv-reader");
		itemReader.setLinesToSkip(1);
		itemReader.setLineMapper(lineMapper());
		return itemReader;
	}
	
	//how to read file.. like if there null value then what to do
	private LineMapper<Customer> lineMapper() 
	{
		//how to read
		DefaultLineMapper<Customer> lineMapper=new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("id","firstName","lastName","email","gender","contactNo","country","dob");
		
		//which is the class mapped to
		BeanWrapperFieldSetMapper<Customer> fieldSetMapper =new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(Customer.class);
		
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);
		
		return lineMapper;
	}
	
	//item processor
	
	@Bean
	public CustomerProcessor customerProcessor()
	{
		return new CustomerProcessor();
	}
	
	//item writer
	
	@Bean
	public RepositoryItemWriter<Customer> writer()
	{
		RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
		writer.setRepository(customerRepository);
		writer.setMethodName("save");
		return writer;
	}
	
	//step
	
	@Bean
	public Step step()
	{
		return new StepBuilder("step-1",jobRepository).<Customer, Customer>chunk(10,transactionManager).
				reader(customerReader()).
				processor(customerProcessor()).
				writer(writer()).
				build();
	}
	
	@Bean
	public Job job()
	{
		return new JobBuilder("customers-import",jobRepository)
				.start(step())
				.build();
	}
}