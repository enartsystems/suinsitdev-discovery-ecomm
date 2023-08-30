package com.suinsit.webapp.login;

import java.io.Serializable;
import java.util.List;

public class SsousuarioDTO implements Serializable {
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
    private Integer idxusuario;
    /** without comment */
    private String erpassword;
    /** without comment */
    private String txusername;
    /** without comment */
    private String txtokensso;
    /** without comment */
    private byte[] imavatar;
    /** without comment */
    private Boolean btcambiarpwd;
    /** without comment */
    private String txmacaddress;
    /** without comment */
    private String rtestado;
    /** without comment */
    private java.util.Date tmbaja;
    /** without comment */
    private String txemail;
    /** without comment */
    private Boolean btroot;
    /** without comment */
    private String txipaddress;
    /** without comment */
    private java.util.Date tmalta;
    /** without comment */
    private List<SsorolDTO> roles;
    private String uuid;
  

    public List<SsorolDTO> getRoles() {
		return roles;
	}
	public void setRoles(List<SsorolDTO> roles) {
		this.roles = roles;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	/**
     * retorna el valor del atributo idxusuario</br>
     *
     * @return Integer
     */
    public Integer getIdxusuario() {
        return idxusuario;
    }

    /**
     * Asigna un valor al atributo idxusuario</br> idusuario
     *
     * @param _var Integer
     */
    public void setIdxusuario(Integer _var) {
        idxusuario = _var;
    }
 
    /**
     * retorna el valor del atributo erpassword</br>
     *
     * @return String
     */
    public String getErpassword() {
        return erpassword;
    }

    /**
     * Asigna un valor al atributo erpassword</br> password
     *
     * @param _var String
     */
    public void setErpassword(String _var) {
        erpassword = _var;
    }
 
    /**
     * retorna el valor del atributo txusername</br>
     *
     * @return String
     */
    public String getTxusername() {
        return txusername;
    }

    /**
     * Asigna un valor al atributo txusername</br> usuario
     *
     * @param _var String
     */
    public void setTxusername(String _var) {
        txusername = _var;
    }
    /**
     * retorna el valor del atributo txtokensso</br>
     *
     * @return String
     */
    public String getTxtokensso() {
        return txtokensso;
    }

    /**
     * Asigna un valor al atributo txtokensso</br> tokensso
     *
     * @param _var String
     */
    public void setTxtokensso(String _var) {
        txtokensso = _var;
    }
    /**
     * retorna el valor del atributo imavatar</br>
     *
     * @return byte[]
     */
    public byte[] getImavatar() {
        return imavatar;
    }

    /**
     * Asigna un valor al atributo imavatar</br> avatar
     *
     * @param _var byte[]
     */
    public void setImavatar(byte[] _var) {
        imavatar = _var;
    }
    /**
     * retorna el valor del atributo btcambiarpwd</br>
     *
     * @return Boolean
     */
    public Boolean getBtcambiarpwd() {
        return btcambiarpwd;
    }

    /**
     * Asigna un valor al atributo btcambiarpwd</br> --Undocumented--
     *
     * @param _var Boolean
     */
    public void setBtcambiarpwd(Boolean _var) {
        btcambiarpwd = _var;
    }
    /**
     * retorna el valor del atributo txmacaddress</br>
     *
     * @return String
     */
    public String getTxmacaddress() {
        return txmacaddress;
    }

    /**
     * Asigna un valor al atributo txmacaddress</br> mac address
     *
     * @param _var String
     */
    public void setTxmacaddress(String _var) {
        txmacaddress = _var;
    }
    /**
     * retorna el valor del atributo rtestado</br>
     *
     * @return String
     */
    public String getRtestado() {
        return rtestado;
    }

    /**
     * Asigna un valor al atributo rtestado</br> estado
     *
     * @param _var String
     */
    public void setRtestado(String _var) {
        rtestado = _var;
    }
    /**
     * retorna el valor del atributo tmbaja</br>
     *
     * @return java.util.Date
     */
    public java.util.Date getTmbaja() {
        return tmbaja;
    }

    /**
     * Asigna un valor al atributo tmbaja</br> fecha de baja
     *
     * @param _var java.util.Date
     */
    public void setTmbaja(java.util.Date _var) {
        tmbaja = _var;
    }
    /**
     * retorna el valor del atributo txemail</br>
     *
     * @return String
     */
    public String getTxemail() {
        return txemail;
    }

    /**
     * Asigna un valor al atributo txemail</br> email
     *
     * @param _var String
     */
    public void setTxemail(String _var) {
        txemail = _var;
    }
 
    /**
     * retorna el valor del atributo btroot</br>
     *
     * @return Boolean
     */
    public Boolean getBtroot() {
        return btroot;
    }

    /**
     * Asigna un valor al atributo btroot</br> --Undocumented--
     *
     * @param _var Boolean
     */
    public void setBtroot(Boolean _var) {
        btroot = _var;
    }
    /**
     * retorna el valor del atributo txipaddress</br>
     *
     * @return String
     */
    public String getTxipaddress() {
        return txipaddress;
    }

    /**
     * Asigna un valor al atributo txipaddress</br> direccion ip de instalacion
     *
     * @param _var String
     */
    public void setTxipaddress(String _var) {
        txipaddress = _var;
    }
    /**
     * retorna el valor del atributo tmalta</br>
     *
     * @return java.util.Date
     */
    public java.util.Date getTmalta() {
        return tmalta;
    }

    /**
     * Asigna un valor al atributo tmalta</br> fecha de alta
     *
     * @param _var java.util.Date
     */
    public void setTmalta(java.util.Date _var) {
        tmalta = _var;
    }
 

}
