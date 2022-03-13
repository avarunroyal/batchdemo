package com.example.batchdemo.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.example.batchdemo.entity.Tokens;
import com.example.batchdemo.processor.TokensProcessor;

@Configuration
@EnableBatchProcessing
@EnableAutoConfiguration
public class BatchConfiguration {

	@Value("${spring.datasource.driver-class-name}")
	private String databaseDriver;
	@Value("${spring.datasource.url}")
	private String databaseUrl;
	@Value("${spring.datasource.username}")
	private String databaseUsername;
	@Value("${spring.datasource.password}")
	private String databasePassword;
	
	@Autowired
	private DataSource dataSource;

	/**
	 * JPAPagingItemReader
	 *
	 * @return
	 */
	@Bean
	public ItemReader<Tokens> reader() throws Exception {

		String jpqlQuery = "select t from Tokens t";
		JpaPagingItemReader<Tokens> reader = new JpaPagingItemReader<Tokens>();
		reader.setQueryString(jpqlQuery);
		reader.setEntityManagerFactory(entityManagerFactory().getObject());
		reader.setPageSize(3);
		reader.afterPropertiesSet();
		reader.setSaveState(true);
		return reader;
	}

	/**
	 * The ItemProcessor is called after a new record is read and it allows the
	 * developer to transform the data read In our example it simply read and write
	 * the data
	 *
	 * @return
	 */
	@Bean
	public ItemProcessor<Tokens, Tokens> processor() {
		return new TokensProcessor();
	}

	/**
	 * Nothing special here a simple JpaItemWriter-but we don't write anything to DB
	 * 
	 * @return
	 */
	@Bean
	public ItemWriter<Tokens> writer() {

		JpaItemWriter<Tokens> writer = new JpaItemWriter<>();
		writer.setEntityManagerFactory(entityManagerFactory().getObject());
		return writer;
	}

	/**
	 * This method declare the steps that the batch has to follow
	 *
	 * @param jobs
	 * @param s1
	 * @return
	 */
	@Bean
	public Job importTokens(JobBuilderFactory jobs, Step s1) {

		return jobs.get("import").incrementer(new RunIdIncrementer()) // because a spring config bug, this incrementer is not really useful
				.flow(s1).end().build();
	}

	/**
	 * Step We declare that every 1000 lines processed the data has to be committed
	 *
	 * @param stepBuilderFactory
	 * @param reader
	 * @param writer
	 * @param processor
	 * @return
	 */
	@Bean
	public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<Tokens> reader, ItemWriter<Tokens> writer,
			ItemProcessor<Tokens, Tokens> processor) {
		return stepBuilderFactory.get("step1").<Tokens, Tokens>chunk(1000).reader(reader).processor(processor)
				.writer(writer).build();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

		LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
		lef.setPackagesToScan("com.example.batchdemo");
		lef.setDataSource(dataSource);
		lef.setJpaVendorAdapter(jpaVendorAdapter());
		lef.setJpaProperties(new Properties());
		return lef;
	}

	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {

		HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
		jpaVendorAdapter.setDatabase(Database.MYSQL);
		jpaVendorAdapter.setGenerateDdl(true);
		jpaVendorAdapter.setShowSql(false);
		jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");
		return jpaVendorAdapter;
	}
}
