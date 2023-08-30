package com.suinsit.webapp.nocode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.sql.DataSource;

import org.enartframework.nocode.dao.IEntityLocal;
import org.enartframework.suinsit.Context;
import org.enartframework.suinsit.LIMITS;
import org.enartframework.suinsit.Licence;
import org.enartframework.suinsit.model.database.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.suinsit.framework.integration.mail.MailProvider;
import com.suinsit.webapp.config.Alertas;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
@Getter
@Setter
@VariableResolver(DelegatingVariableResolver.class)
@Slf4j
public class SubscripcionUI  extends SelectorComposer implements Serializable {
	private static final String KEY_ORDER_VIEW_CTRL = "KEY_ORDER_VIEW_CTRL";
	public static final String IDDESKTOP = "iddesktop";
	@Autowired DataSource datasource;
	@Autowired
	MailProvider mail;
	@WireVariable("entityDao")
	IEntityLocal dao;
	@WireVariable("context")
	GenericApplicationContext context;
	@WireVariable
	private Environment environment;
	@WireVariable("ctxBean")
	protected Context ctxBean;
	String pathDirScreen;
	String pathDirModel;
	String pathDirSchema;
	String pathMenu;
	Alertas alertas = new Alertas();
	ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	public SubscripcionUI() {
		// TODO Auto-generated constructor stub
	}
	@AfterCompose()
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		super.doAfterCompose(view);
		try {
			if(!new File(ctxBean.getPathHome()+"/config/alertas.json").exists()) {
				new File(ctxBean.getPathHome()+"/config").mkdirs();
				new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).writerWithDefaultPrettyPrinter().writeValue(new File(ctxBean.getPathHome()+"/config/alertas.json"), alertas);
			}else {
				alertas = mapper.readValue(new File(ctxBean.getPathHome()+"/config/alertas.json"), Alertas.class);
			}
			if(!new File(ctxBean.getPathHome()+"/config/licence.json").exists()) {
				new File(ctxBean.getPathHome()+"/config").mkdirs();
				ctxBean.setLicencia(new Licence());
				ctxBean.getLicencia().setAlta(new Date());
				Calendar calendar = Calendar.getInstance();
				calendar.add(calendar.DAY_OF_YEAR, 367);
				ctxBean.getLicencia().setRenovacion(calendar.getTime());
				new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).writerWithDefaultPrettyPrinter().writeValue(new File(ctxBean.getPathHome()+"/config/licence.json"), ctxBean.getLicencia());
			}else {
				ctxBean.setLicencia(mapper.readValue(new File(ctxBean.getPathHome()+"/config/licence.json"), Licence.class));
			}
			
			
		} catch (IOException e) {
			log.error("",e);
			throw new Exception(e);
		}
	}
	@NotifyChange("*")
	@Command
	public void guardar() throws StreamWriteException, DatabindException, IOException {
		new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).writerWithDefaultPrettyPrinter().writeValue(new File(ctxBean.getPathHome()+"/config/alertas.json"), alertas);
		new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).writerWithDefaultPrettyPrinter().writeValue(new File(ctxBean.getPathHome()+"/config/licence.json"), ctxBean.getLicencia());

	}
}
