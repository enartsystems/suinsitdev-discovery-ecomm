package com.suinsit.webapp.login;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.commons.lang3.RandomUtils;
import org.enartframework.core.shared.commons.TextParser;
import org.enartframework.core.shared.logger.EnartLoggerFactory;
import org.enartframework.core.shared.logger.IEnartLogger;
import org.enartframework.nocode.dao.EntityDao;
import org.enartframework.nocode.dao.IEntityLocal;
import org.enartframework.nocode.datamodel.jdbc.Criteria;
import org.enartframework.nocode.datamodel.jdbc.Criterias;
import org.enartframework.nocode.datamodel.jdbc.Evaluation;
import org.enartframework.nocode.datamodel.jdbc.Operation;
import org.enartframework.nocode.datamodel.model.Entity;
import org.enartframework.nocode.datamodel.model.Field;
import org.enartframework.nocode.datamodel.model.Foreing;
import org.enartframework.nocode.datamodel.model.Query;
import org.enartframework.nocode.datamodel.model.QueryBuilder;
import org.enartframework.orm.DataScroll;
import org.enartframework.orm.exception.DaoException;
import org.enartframework.security.Usuario;
import org.enartframework.security.crypto.Encrypt;
//import org.enartframework.micros.client.ClientException;
import org.enartframework.security.jwt.JwtTokenUtil;
import org.enartframework.suinsit.Context;
//import org.enartframework.suinsit.Context;
//import org.enartframework.suinsit.model.application.UsuarioDto;
import org.enartframework.web.annotation.View;
import org.enartframework.web.exception.UiException;
import org.enartframework.web.zk.page.MasterPage;
import org.enartframework.zk.utils.Notificacion.TYPENOTI;
import org.postgresql.replication.LogSequenceNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.suinsit.nocode.web.BuildEntityFromClass;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.impl.GlobalDesktopCacheProvider;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Div;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Textbox;

import com.enartsystems.mapping.BuilderDataBase;
import com.enartsystems.mapping.BuilderModel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suinsit.framework.integration.mail.EmailContact;
import com.suinsit.framework.integration.mail.EmailSender;
import com.suinsit.framework.integration.mail.MAIL;
import com.suinsit.framework.integration.mail.MailException;
import com.suinsit.framework.integration.mail.MailProvider;
import com.suinsit.webapp.config.Configure;

import lombok.extern.slf4j.Slf4j;





@View
// Anotacion utilizada para la navegacion
@VariableResolver(DelegatingVariableResolver.class)
@Init(superclass = true)
@org.springframework.stereotype.Component
@Slf4j
@Scope("prototype")
public class LoginUI extends MasterPage {
	private static final long serialVersionUID = 1L;
	@WireVariable
	protected Session session;
	@WireVariable DataSource datasource;
	@WireVariable
	MailProvider mail;
	@Wire
	Div login;
	@Wire
	Div forget;
	private Integer random;
	@WireVariable
	private Environment environment;
	@Wire
	Div codeBox;
	@Wire Div change;
	@Wire Div termsbox;
	public LoginUI() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Wire Intbox numCode;
	@Wire Textbox newp;
	String repeat;
	
	public String getRepeat() {
		return repeat;
	}

	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}

	public Integer getRandom() {
		return random;
	}
	BuilderModel builder;
	BuilderDataBase builderDB;
	private LoginTO loginTO = new LoginTO();
	@WireVariable("ctxBean")
	protected Context ctxBean;
	@WireVariable("context")
	GenericApplicationContext context;
	ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	@WireVariable("entityDao")
	IEntityLocal dao;
	 ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	 @WireVariable("APPLICATION_DS")
		DataSource ds;
	 Entity Ssoruserol;
	 Class<?> clsSsoruserol;
	 Entity Ssorportalrol;
	 Class<?> clsSsorportalrol;
	 Entity bean;
	 Class<?> clsBean;
	 Entity logacces;
	 Class<?>clsLogAccess;
	 
	 Query query;
	 HttpServletRequest req;
	public Context getCtxBean() {
		return ctxBean;
	}

	/** Recibe un bean del tipo LoginTO */
	public void setLoginTO(LoginTO _var) {
		this.loginTO = _var;
	}
	private void initDao() {
		if (dao == null) {
			dao = new EntityDao();
		}
		if (dao != null && dao.getDataSource() == null) {
			if (!SpringUtil.getApplicationContext().containsBean("APPLICATION_DS")) {
				dao.setDataSource((DataSource) environment.getProperty("APPLICATION_DS", DataSource.class));
			}
		}
	}
	JwtTokenUtil tokenProvider  ;
	private String pathDirModel;
	/** return LoginTO */
	public LoginTO getLoginTO() {
		return this.loginTO;
	}
	
	public void postInit() {
	   	URLClassLoader urlClassLoader;
			try {
				urlClassLoader = new URLClassLoader(new URL[]{new File(ctxBean.getPathClasses()).toURL()},
						 classLoader);
				Thread.currentThread().setContextClassLoader(urlClassLoader);
			} catch (MalformedURLException e) {
				log.error(e.getMessage());
			}
		};
	public void preAfterCompose(Component view,String idpagina)  throws Exception {
			
	}		
	/**
	 * se invoca justo despues de renderizar la pantalla en el navegador
	 *
	 * @param view
	 * @throws Exception
	 */
	@AfterCompose()
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		super.doAfterCompose(view);
		GlobalDesktopCacheProvider cache = 	new GlobalDesktopCacheProvider();
		logout();
		cache.sessionDestroyed(session);
		mail =  (MailProvider) org.zkoss.zkplus.spring.SpringUtil.getBean("mailjet");
		initDao();
		if(tokenProvider==null) {
			tokenProvider = new JwtTokenUtil();
		}
		tokenProvider.setSecret(environment.getProperty("jwt.key"));
		pathDirModel =  ctxBean.getPathHome()+ File.separator + "data"+ File.separator + "model";
		builder = new BuilderModel(pathDirModel);
		postInit();
		clsBean = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps.admin.Ssousuario");
		clsSsoruserol = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps.admin.Ssoruserol");
		clsSsorportalrol = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps.admin.Ssorportalrol");
	    clsLogAccess = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps.admin.Ssologaccess");
	    bean = BuildEntityFromClass.build(clsBean);
		Ssorportalrol = BuildEntityFromClass.build(clsSsorportalrol);
		Ssoruserol = BuildEntityFromClass.build(clsSsoruserol);
		logacces = BuildEntityFromClass.build(clsLogAccess); 
  	    req = (HttpServletRequest) desktop.getExecution().getNativeRequest();
		 viewRequest();
	}
	private void autentificar() {
		 
		
		
	}
	private void viewRequest() {
		log.debug("viewRequest...");
		req = (HttpServletRequest) desktop.getExecution().getNativeRequest();
		log.debug("getHeaderNames...");
		 req.getHeaderNames().asIterator().forEachRemaining(s->{
				log.debug("Header->"+s+":"+req.getHeader(s)+"|");
		 });
		 log.debug("getAttributeNames...");
		 req.getAttributeNames().asIterator().forEachRemaining(a->{
			 log.debug("Atributte->"+a+":"+req.getAttribute(a)+"|");
		 });
		
	}
	private Entity getLog() {
		log.debug("creo logs de acceso...");
		Entity logs = logacces.copy();
		logs.getField("USERNAME").setValue(loginTO.getSsousuario().getTxusername());
		logs.getField("LOGIN").setValue(new java.sql.Timestamp(System.currentTimeMillis()));
		logs.getField("APLICACION").setValue(ctxBean.getApplicationName());
		try {
			StringBuffer sb = new StringBuffer();
			req.getHeaderNames().asIterator().forEachRemaining(s->{
				sb.append(s+":"+req.getHeader(s)+"|");
				log.debug(s+":"+req.getHeader(s)+"|");
			});
			logs.getField("HEADERS").setValue(sb.toString());
		}catch (Exception e) {
			log.error("getLog", e);
		}
		
		logs.getField("DIRIP").setValue(req.getRemoteAddr());
		logs.getField("DISPOSITIVO").setValue(desktop.getDevice().getType());
		logs.getField("ACCESO").setValue(new java.sql.Date(System.currentTimeMillis()));
		
		return logs;
	}
	private void failAccess(String message,String tipo) {
		Entity loge = getLog();
		loge.getField("ERROR").setValue(true);
		loge.getField("TIPOERROR").setValue(tipo);
		loge.getField("MENSAJE").setValue(message);
		try {
			dao.create(loge);
		} catch (DaoException e) {
			log.error(e.getMessage());
		}
	}
	private void Access() {
		Entity loge = getLog();
		try {
			log.debug("grabo logs de acceso...");
			dao.create(loge);
		} catch (DaoException e) {
			log.error(e.getMessage());
		}
	}
	private void evalQuery() {
		List<String> lfields = new ArrayList<>();
		Entity copy = bean.copy();
		copy.getFields().forEach(sfield->{
			 lfields.add(sfield.getAtribute());
		});
		query = QueryBuilder.buildQueryWithoutJoin(copy, lfields.toArray(new String[lfields.size()]));
	}
	private void evalCriterias() {
		criterias = new Criterias();
		Field user = bean.copy().getFieldByAtribute("USUARIO");
		user.setValue(loginTO.getSsousuario().getTxusername());
		criterias.addCriteria(new Criteria(Operation.OR, Evaluation.EQUALS, user));	
		
		Field email = bean.copy().getFieldByAtribute("EMAIL");
		email.setValue(loginTO.getSsousuario().getTxusername());
		criterias.addCriteria(new Criteria(Operation.OR, Evaluation.EQUALS, email));
		
		Field pass = bean.copy().getFieldByAtribute("PASSWORD");
		pass.setValue(Encrypt.encryptSHA3(loginTO.getSsousuario().getErpassword()));
		criterias.addCriteria(new Criteria(Operation.AND, Evaluation.EQUALS, pass));
	}
	private List<String> evalPortal() throws DaoException {
		List<String> lfields = new ArrayList<>();
		List<String> lroles = new ArrayList<>();
		List<Foreing> foreins = new ArrayList<>();
		Entity copy = Ssorportalrol.copy();
		copy.getForeings().forEach(fg->{
			foreins.add(fg);
		    fg.getFkentity().getFields().forEach(sfield->{
		    	lfields.add(sfield.getAtribute());
		    });	
		});
		
		Query query = QueryBuilder.buildQueryWithLeftJoin(copy, foreins,lfields.toArray(new String[lfields.size()])); 
		Criterias criterias = new Criterias();
		Field app = copy.getForeingByAlias("idssoportal").getFkentity().getFieldByAtribute("APLICACION");
		app.setValue(ctxBean.getApplicationName());
		criterias.addCriteria(new Criteria(Operation.AND, Evaluation.EQUALS, app));
		DataScroll scroll = dao.query(clsSsorportalrol,new DataScroll(), query, criterias);
		scroll.getDataList().forEach(tmp->{
			lroles.add((String) getNested(tmp,"idssorol.rol"));
			
		});
		return lroles;
	}
	@NotifyChange("*")
	@Command("changePass")
	public void changePass() {
		try {
			setNested(ssouser, "password", loginTO.getSsousuario().getErpassword());
			dao.update(clsBean, bean.copy(), ssouser);
			loginTO.setSsousuario(new SsousuarioDTO());
			numCode.setValue(null);
			random = null;
			change.setVisible(false);
			login.setVisible(true);
		} catch (DaoException e) {
			log.error(e.getLocalizedMessage());
		}
	}
	@NotifyChange("*")
	@Command("validarOTP")
	public void validarOTP() {
		if (numCode.getValue().intValue() == getRandom()) {
			login.setVisible(false);
			forget.setVisible(false);
			codeBox.setVisible(false);
			change.setVisible(true);
		}else {
			failAccess("OTP NO VALID","recovery");
			showNotification(TYPENOTI.SUCCESS, Labels.getLabel("code.ko", "El código introducido no es valido"));
		}
	}
	@NotifyChange("*")
	@Command("reenviarOTP")
	public void reenviarOTP() {
		random = new RandomUtils().nextInt(1111, 9999);
		try {
			sendEmailOTP();
			showNotification(TYPENOTI.INFORMATION, Labels.getLabel("otp.send", "Nuevo código enviado"));
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		}
	}
	
	@NotifyChange("*")
	@Command("sendOTP")
	public void sendOTP() {
		List<String> lfields = new ArrayList<>();
		Entity copy = bean.copy();
		copy.getFields().forEach(sfield->{
			 lfields.add(sfield.getAtribute());
		});
		query = QueryBuilder.buildQueryWithoutJoin(copy, lfields.toArray(new String[lfields.size()]));
		criterias = new Criterias();
		Field user = copy.getFieldByAtribute("USUARIO");
		user.setValue(loginTO.getSsousuario().getTxusername());
		criterias.addCriteria(new Criteria(Operation.OR, Evaluation.EQUALS, user));	
		Field email = copy.getFieldByAtribute("EMAIL");
		email.setValue(loginTO.getSsousuario().getTxusername());
		criterias.addCriteria(new Criteria(Operation.OR, Evaluation.EQUALS, email));
		if(validateLogin()) {
			login.setVisible(false);
			forget.setVisible(false);
			codeBox.setVisible(true);
			random = new RandomUtils().nextInt(1111, 9999);
			try {
				sendEmailOTP();
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			}
		}
	}
	private void sendEmailOTP() throws Exception{
		EmailSender email = new EmailSender();
		email.setSubject("Código de validación");
		StringBuffer sb = new StringBuffer();
		sb.append("Su codigo de verificación para restablecer la contraseña en suinsit es <br/>");
		sb.append("<b>"+random+"</b>");
		email.setHtmlMessage(sb);
		EmailContact em = new EmailContact();
		em.setEmail(getUser().getEmailuser());
		email.setTo(List.of(em));			
		mail.sendMessage(email,MAIL.HTML_MESSAGE);
	
	}
	Object ssouser;
	private boolean validateLogin() {
		try {
			scroll = dao.query(clsBean,new DataScroll(), query, criterias);
			if(scroll.getDataList().size()>0) {
				Usuario usuario = new Usuario<Object>();
				 List<String> rolesPortal = new ArrayList<String>();
				for(Object user:scroll.getDataList()){
					ssouser = user;
					usuario.setEmailuser((String) getNested(user,"email"));
					usuario.setFirstname((String) getNested(user,"nombre"));
					usuario.setId((Long) getNested(user,"idxssousuario"));
					usuario.setLastname((String) getNested(user,"apellidos"));
				    usuario.setUserApplication(user);	
				    usuario.setUsername((String) getNested(user,"usuario"));
				    usuario.setTypeUser((String) getNested(user,"tipousuario"));
				    setUser(usuario);
				    loadRoles(usuario);
				    List<String> roles = evalPortal();
				    boolean ok=false;
				    for(String rol:(List<String>)usuario.getRoles()) {
				    	if(roles.stream().filter(R->R.equalsIgnoreCase(rol)).findFirst().isPresent()) {
				    		ok=true;
				    		rolesPortal.add(rol);
				    	}
				    }
				    if(!ok) {
				    	failAccess("user no authorized","no access application");
			    		showNotification(TYPENOTI.ALERT, Labels.getLabel("login.noaccess", "No tienes acceso a está aplicación. Por favor contacta con el administrador"),"60000");
			    		return false;
			    	}
				    if((Boolean)getNested(user,"locked")) {
				    	failAccess("user locked","locked");
				    	Executions.sendRedirect(environment.getProperty("401_page"));
				    	return false;
				    }
				    Access();
				    return true;
				};
			}else {
				failAccess("user password fail","fail access");
				showNotification(TYPENOTI.ALERT, Labels.getLabel("login2.ko", "username/email incorrecto"));
				return false;
			}
		} catch (DaoException e) {
			log.error(e.getMessage());
		}
		return false;
	}
	@NotifyChange("*")
	@Command("resetLogin")
	public void resetLogin() {
		login.setVisible(false);
		forget.setVisible(true);
	}
	@NotifyChange("*")
	@Command("login")
	public void login() {
		log.debug("entra en login");
		evalQuery();
		evalCriterias();
		try {
			//buscar por usuario y contraseña
			scroll = dao.query(clsBean,new DataScroll(), query, criterias);
			if(scroll.getDataList().size()>0) {
				//recuperar usurio
				//si usuario es sistema , normal si es portal redirigir al portal
				//recuperar perfil
				Usuario usuario = new Usuario<Object>();
				 List<String> rolesPortal = new ArrayList<String>();
				for(Object user:scroll.getDataList()){
					usuario.setEmailuser((String) getNested(user,"email"));
					usuario.setFirstname((String) getNested(user,"nombre"));
					usuario.setId((Long) getNested(user,"idxssousuario"));
					usuario.setLastname((String) getNested(user,"apellidos"));
				    usuario.setUserApplication(user);	
				    usuario.setUsername((String) getNested(user,"usuario"));
				    usuario.setTypeUser((String) getNested(user,"tipousuario"));
				    setUser(usuario);
				    loadRoles(usuario);
				    
				    List<String> roles = evalPortal();
				    boolean ok=false;
				    for(String rol:(List<String>)usuario.getRoles()) {
				    	if(roles.stream().filter(R->R.equalsIgnoreCase(rol)).findFirst().isPresent()) {
				    		ok=true;
				    		rolesPortal.add(rol);
				    	}
				    }
				    if(!ok) {
				    	failAccess("user no authorized","no access application");
			    		showNotification(TYPENOTI.ALERT, Labels.getLabel("login.noaccess", "No tienes acceso a está aplicación. Por favor contacta con el administrador"),"60000");
						return;
			    	}

				    if((Boolean)getNested(user,"locked")) {
				    	failAccess("user locked","locked");
				    	Executions.sendRedirect(environment.getProperty("401_page"));
				    	return;
				    }
				    
				};
				
				
				
				log.debug("autoriza login");
				//Solo cargamos los roles asociados al portal
				usuario.setRoles(rolesPortal);
				
				List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
				authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
				usuario.getRoles().forEach(rol->{
					authorities.add(new SimpleGrantedAuthority((String) rol));
				});
				ctxBean.setUserLogin(usuario);
				ctxBean.setUsuario(usuario);
				desktop.setAttribute("ctxBean", ctxBean, true);
				Authentication authenticationSpring = new UsernamePasswordAuthenticationToken(usuario, null, authorities);
				SecurityContextHolder.getContext().setAuthentication(authenticationSpring);
				log.debug("redireccionad a "+usuario.getTypeUser().toUpperCase());
			
//					if(usuario.getDashboard()!=null) {
//						Access();
//						Executions.sendRedirect(environment.getProperty("suinsit.desktop.ctx")+"/"+usuario.getDashboard());
//					}else {
						Access();
						Executions.sendRedirect(environment.getProperty("suinsit.desktop.ctx")+"/"+environment.getProperty("suinsit.desktop.page"));	
//					}
				return;
			}else {
				//mensaje de error de autentificacion
				failAccess("user password fail","fail access");
				showNotification(TYPENOTI.ALERT, Labels.getLabel("login.ko", "username/password incorrect"));
				return;
			}
			
		} catch (DaoException e) {
			log.error(e.getMessage());
		}
		
		
	}
	private void logout() {
		SecurityContextHolder.clearContext();
		setUser(null);
		
		
	}
    private void loadRoles(Usuario usuario) {
    	List<String> lfields = new ArrayList<>();
    	Entity copy = Ssoruserol.copy();
    	copy.getFields().forEach(sfield->{
			 lfields.add(sfield.getAtribute());
		});
    	//idssoaplicacion
		query = QueryBuilder.buildQueryWithLeftJoin(copy, List.of(copy.getForeingByAlias("idssorol")),lfields.toArray(new String[lfields.size()])); 
		criterias = new Criterias();
		Field iduser = bean.copy().getFieldByAtribute("IDXSSOUSUARIO");
		iduser.setField("IDSSOUSUARIO0");
		iduser.setAtribute("IDSSOUSUARIO0");
		iduser.setValue(usuario.getId());	
		criterias.addCriteria(new Criteria(Operation.AND, Evaluation.EQUALS, iduser));
		try {
			scroll = dao.query(clsSsoruserol,new DataScroll(), query, criterias);
			scroll.getDataList().forEach(rol->{
				usuario.getRoles().add(getNested(rol, "idssorol.rol"));
				if(usuario.getDashboard()==null) {
					usuario.setDashboard((String)getNested(rol, "idssorol.dashboard"));
				}else {
					//tiene roles con mas de un dashboard, se anula y se deja a la pagina principal
					usuario.setDashboard(null);
				}
				
			});
		} catch (DaoException e) {
			log.error(e.getMessage());
		}
    }
    
    
	boolean sendUserName=false;
	@NotifyChange("*")
	@Command("forget")
	public void forget() {
		login.setVisible(false);
		forget.setVisible(true);
		codeBox.setVisible(false);
	//	submit.setVisible(true);
	}
	@NotifyChange("*")
	@Command("forget2")
	public void forget2() {
		login.setVisible(false);
		forget.setVisible(true);
		codeBox.setVisible(false);
	//	submit.setVisible(true);
		sendUserName=true;
	}
	@NotifyChange("*")
	@Command("send")
	public void send() { 
		//hay que verificar que existe la cuenta de correo, sino existe lanzar mensaje de error.
		// Verificar que existe el correo
		
	
	}

	@NotifyChange("*")
	@Command("validate")
	public void validate() {
		Object[] obj = new Object[1];
		obj[0] = "index";
		if (numCode.getValue().intValue() == getRandom()) {
			login.setVisible(false);
			forget.setVisible(false);
			codeBox.setVisible(false);
			change.setVisible(true);
		}else {
			showNotification(TYPENOTI.SUCCESS, Labels.getLabel("code.ko", "The code is not valid"));
		}
		
		
	}
	
	
	@NotifyChange("*")
	@Command("back")
	public void back() {
		Executions.sendRedirect("index.zhtml");
	}

	@Override
	public void setBeans(Object bean) {
		// TODO Auto-generated method stub
		
	}
}
