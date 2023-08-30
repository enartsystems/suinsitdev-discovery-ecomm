package com.suinsit.webapp.login;

import java.io.Serializable;

public class SsorolDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean selected;
    /**
     * Flag que indica si el objeto ha sido seleccionado en una tabla de consulta tipo grid Se suele
     * utilizar en los pop up de criterios de busqueda
     */
    public boolean isSelected() {
        return selected;
    }
    /**
     * Flag que asigna si el objeto ha sido seleccionado en una tabla de consulta tipo grid
     *
     * @param boolean var
     */
    public void setSelected(final boolean var) {
        selected = var;
    }

    /** without comment */
    private Integer idxrol;
    /** without comment */
    private String txrol;
    /** without comment */
    private String rttipo;
    /** without comment */
    private Boolean btadministrator;
    private SsorolDTO idparent;
    private String uuid;
    private String extrole;

    public SsorolDTO getIdparent() {
		return idparent;
	}
	public void setIdparent(SsorolDTO idparent) {
		this.idparent = idparent;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getExtrole() {
		return extrole;
	}
	public void setExtrole(String extrole) {
		this.extrole = extrole;
	}
	/**
     * retorna el valor del atributo idxrol</br>
     *
     * @return Integer
     */
    public Integer getIdxrol() {
        return idxrol;
    }

    /**
     * Asigna un valor al atributo idxrol</br> --Undocumented--
     *
     * @param _var Integer
     */
    public void setIdxrol(Integer _var) {
        idxrol = _var;
    }
    /**
     * retorna el valor del atributo txrol</br>
     *
     * @return String
     */
    public String getTxrol() {
        return txrol;
    }

    /**
     * Asigna un valor al atributo txrol</br> --Undocumented--
     *
     * @param _var String
     */
    public void setTxrol(String _var) {
        txrol = _var;
    }
    /**
     * retorna el valor del atributo rttipo</br>
     *
     * @return String
     */
    public String getRttipo() {
        return rttipo;
    }

    /**
     * Asigna un valor al atributo rttipo</br> tipo de usuario
     *
     * @param _var String
     */
    public void setRttipo(String _var) {
        rttipo = _var;
    }
    /**
     * retorna el valor del atributo btadministrator</br>
     *
     * @return Boolean
     */
    public Boolean getBtadministrator() {
        return btadministrator;
    }

    /**
     * Asigna un valor al atributo btadministrator</br> --Undocumented--
     *
     * @param _var Boolean
     */
    public void setBtadministrator(Boolean _var) {
        btadministrator = _var;
    }
}
