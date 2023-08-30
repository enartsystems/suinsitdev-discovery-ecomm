package com.suinsit.webapp.login;

import java.io.Serializable;

import org.enartframework.core.shared.logger.EnartLoggerFactory;
import org.enartframework.core.shared.logger.IEnartLogger;

public class LoginTO implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final IEnartLogger log = EnartLoggerFactory.getLogger(LoginTO.class);

	SsousuarioDTO ssousuario;

	/** retorna un objeto del tipo SsousuarioDTO */
	public SsousuarioDTO getSsousuario() {
		return ssousuario;
	}

	/** recibe un objeto del tipo SsousuarioDTO */
	public void setSsousuario(SsousuarioDTO var) {
		ssousuario = var;
	}

	public LoginTO() {
		ssousuario = new SsousuarioDTO();
	}

	String repeat;

	public String getRepeat() {
		return repeat;
	}

	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}

	String navigation22;

	public void setNavigation22(String _var) {
		navigation22 = _var;
	}

	public String getNavigation22() {
		return navigation22;
	}
}
