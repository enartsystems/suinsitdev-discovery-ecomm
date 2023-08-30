package com.suinsit.webapp.nocode;

import java.io.Serializable;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;

import com.suinsit.framework.integration.mail.MailProvider;

@VariableResolver(DelegatingVariableResolver.class)
public class TestWin  extends SelectorComposer implements Serializable {
	private static final String KEY_ORDER_VIEW_CTRL = "KEY_ORDER_VIEW_CTRL";
	public static final String IDDESKTOP = "iddesktop";
	@Autowired DataSource datasource;
	@Autowired
	MailProvider mail;
	public TestWin() {
		// TODO Auto-generated constructor stub
	}
	@AfterCompose()
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		super.doAfterCompose(view);
	}
}
