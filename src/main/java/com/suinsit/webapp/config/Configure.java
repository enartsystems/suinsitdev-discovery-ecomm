/*
 * Copyright (c) 2020-2025 EnartSystems.com
 * @author Manuel González (enartsystems) 
 */
package com.suinsit.webapp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.enartframework.integration.aws.s3.S3Client;
import org.enartframework.integration.aws.s3.S3FileHandler;
import org.enartframework.nocode.dao.EntityDao;
import org.enartframework.nocode.dao.IEntityLocal;
import org.enartframework.suinsit.Context;
import org.enartframework.suinsit.LIMITS;
import org.enartframework.suinsit.applications.config.ConfigApplication;
//import org.enartframework.suinsit.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.zkoss.spring.config.ZkScopesConfigurer;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.suinsit.framework.integration.mail.MailProvider;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ComponentScan({ "com.suinsit.*", "org.enartframework.integration.aws.s3.*", "com.suinsit.framework.integration.*",
		"com.suinsit.applications.erp.*", "com.suinsit.applications.app.*", "org.enartframework.suinsit.*" })
@Import({ ZkScopesConfigurer.class })
@Slf4j
public class Configure implements Serializable {
	private static final String MY_QUEUE_NAME = "actualizar.suinsit";
	public final static String FANOUT_QUEUE_1_NAME = "io.suinist.platform.amqp.fanout.queue1";
	public final static String FANOUT_QUEUE_2_NAME = "io.suinist.platform.amqp.fanout.queue2";
	public final static String FANOUT_EXCHANGE_NAME = "io.suinist.platform.amqp.fanout.exchange";
	public final static String TOPIC_QUEUE_1_NAME = "io.suinist.platform.amqp.topic.queue1";
	public final static String TOPIC_QUEUE_2_NAME = "io.suinist.platform.amqp.topic.queue2";
	public final static String TOPIC_EXCHANGE_NAME = "io.suinist.platform.amqp.topic.exchange";
	public static final String BINDING_PATTERN_IMPORTANT = "*.important.*";
	public static final String BINDING_PATTERN_ERROR = "#.error";
	@Autowired
	private Environment environment;
	@Autowired
	DataSource datasource;
	@Autowired
	MailProvider mail;
	@Autowired
	ConfigApplication configApp;
	
	@Bean(name = "entityDao")
	public IEntityLocal getIEntityLocal(DataSource datasource) {
		IEntityLocal dao = new EntityDao();
		dao.setDataSource(datasource);
		try {
			log.debug(datasource.getConnection().getSchema());
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		
		return dao;
	}
	
	@Bean(name = "ctxBean")
	public Context getContextBean() throws Exception {
		Context ctx = new Context();
		ctx.setConfig(configApp);
		ctx.setMode(System.getenv(ENVIROMENTS.SUINSIT_MODE.name()));
		ctx.setEnviroment(System.getenv(ENVIROMENTS.SUINSIT_ENVIROMENT.name()));
		ctx.setPathHome(System.getenv(ENVIROMENTS.SUINSIT_HOME.name()));
		ctx.setPathClasses(System.getenv(ENVIROMENTS.SUINSIT_HOME.name()) + "/data/classes");
		ctx.setPathDeploy(System.getenv("SUINSIT_DEPLOY"));
		if (System.getenv(ENVIROMENTS.SUINSIT_EXECUTION.name()) != null) {
			ctx.setExecution(System.getenv(ENVIROMENTS.SUINSIT_EXECUTION.name()));
		}
		
		ctx.setProductName(environment.getRequiredProperty("suinsit.application.product"));
		ctx.setApplicationName(environment.getRequiredProperty("suinsit.application.name"));
		ctx.setCustomerName(environment.getRequiredProperty("suinsit.customer.name"));
		ctx.setCustomerUID(environment.getRequiredProperty("suinsit.customer.uid"));
		ctx.setCustomerLabel(environment.getRequiredProperty("suinsit.customer.label"));
		ctx.getSecurity().put("scope", environment.getRequiredProperty("suinsit.security.scope"));
		ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
		Map<String, Object> prop = new HashMap<>();
		prop.put("APPLICATION_DS", datasource);
		env.getPropertySources().addFirst(new MapPropertySource("SUINSIT_PROPS", prop));
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(new File(ctx.getPathHome()+"/version.properties")));
			ctx.setProduct(p.getProperty("producto"));
			ctx.setLicence(p.getProperty("licencia"));
			ctx.setRevision(p.getProperty("revision"));
			ctx.setAltaProduct(p.getProperty("alta"));
			ctx.setReleaseVersion(p.getProperty("version"));
			ctx.getLimits().put(LIMITS.USERS,p.getProperty("usuarios"));
			ctx.getLimits().put(LIMITS.CLIENTES,p.getProperty("empresas"));
		} catch (IOException e) {
			log.error("",e);
			throw new Exception(e);
		}
		
		
		log.debug(ctx.toString());
		return ctx;
	}

	@Bean
	public S3Client getS3Client() {
		S3Client client = new S3Client(environment.getProperty("s3.accesskey"), environment.getProperty("s3.secretkey"),
				environment.getProperty("s3.endpointurl"));
		try {
			if (!client.getClientS3().doesBucketExistV2(environment.getProperty("s3.backet"))) {
				log.debug("creamos el backet en s3 " + environment.getProperty("s3.backet"));
				client.getClientS3().createBucket(environment.getProperty("s3.backet"));
				log.debug("backet creado en s3 " + environment.getProperty("s3.backet"));
			}
		} catch (Exception e) {
			if (e.getMessage().indexOf("InvalidBucketName") != -1) {
				client.getClientS3().createBucket(environment.getProperty("s3.backet"));
			} else {
				log.error("error al crear el backet", e);
			}

		}
		return client;
	}

	@Bean
	public S3FileHandler getS3FileHandler() {
		S3FileHandler s3FileHandler = new S3FileHandler(new S3Client(environment.getProperty("s3.accesskey"),
				environment.getProperty("s3.secretkey"), environment.getProperty("s3.endpointurl")));
		s3FileHandler.setBacketName(environment.getProperty("s3.backet"));
		return s3FileHandler;
	}
}
