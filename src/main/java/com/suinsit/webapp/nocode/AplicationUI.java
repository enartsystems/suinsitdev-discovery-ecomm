/**
 * 
 */
package com.suinsit.webapp.nocode;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.beanutils.PropertyUtils;
import org.enartframework.core.shared.commons.SortCollections;
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
import org.enartframework.nocode.datamodel.model.Query;
import org.enartframework.nocode.datamodel.model.QueryBuilder;
import org.enartframework.orm.DataScroll;
import org.enartframework.orm.exception.DaoException;
import org.enartframework.security.Usuario;
import org.enartframework.suinsit.Context;
import org.enartframework.web.zk.page.BaseMasterHandler;
import org.springframework.core.env.Environment;
import org.suinsit.nocode.web.MenuBean;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zkplus.spring.SpringUtil;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.enartsystems.mapping.BuilderModel;
import com.enartsystems.webapp.beans.AplicationBean;

import lombok.Getter;
import lombok.Setter;

/**
 * @author manuel
 *
 */
@VariableResolver(DelegatingVariableResolver.class)
@Getter
@Setter
public class AplicationUI extends BaseMasterHandler{
	private static final long serialVersionUID = 1L;
	private static final IEnartLogger log = EnartLoggerFactory.getLogger(AplicationUI.class);
	List<AplicationBean> applications = new ArrayList<AplicationBean>();
	ObjectMapper mapper = new ObjectMapper();
	Criterias criterias;
	Query query;
	Entity Ssoruserapp;
	 Class<?> clsSsoruserapp;
	 Entity bean;
	 Class<?> clsBean;
	 BuilderModel builder;
	@WireVariable("ctxBean")
	protected Context ctxBean;
	@WireVariable("entityDao")
	IEntityLocal dao;
	@WireVariable
	private Environment environment;
	public Context getCtxBean() {
		return ctxBean;
	}

	public void setCtxBean(Context ctxBean) {
		this.ctxBean = ctxBean;
	}
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		super.doAfterCompose(view, this);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		URLClassLoader urlClassLoader;
		  ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			urlClassLoader = new URLClassLoader(new URL[]{new File(ctxBean.getPathClasses()).toURL()},
					 classLoader);
			Thread.currentThread().setContextClassLoader(urlClassLoader);
		} catch (MalformedURLException e) {
			log.error(e);
		}
		
		builder = new BuilderModel(ctxBean.getPathHome()+ File.separator + "data"+ File.separator + "model");
		Ssoruserapp = builder.loadFromDataGrid("SSORUSERAPP");
		bean = builder.loadFromDataGrid("SSOUSUARIO");
		clsBean = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps."+bean.getNamespace()+"."+TextParser.getClassNameFormater(bean.getCoentity()));
		clsSsoruserapp = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps."+Ssoruserapp.getNamespace()+"."+TextParser.getClassNameFormater(Ssoruserapp.getCoentity()));
		initDao();
		loadApps(getUser());
		
		
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
	
	private void loadApps(Usuario usuario) {
    	List<String> lfields = new ArrayList<>();
     	Ssoruserapp.getFields().forEach(sfield->{
			 lfields.add(sfield.getAtribute());
		});
    	//idssoaplicacion
		query = QueryBuilder.buildQueryWithLeftJoin(Ssoruserapp, List.of(Ssoruserapp.getForeingByAlias("idssoaplicacion")),lfields.toArray(new String[lfields.size()])); 
		criterias = new Criterias();
		Field iduser = bean.getFieldByAtribute("IDXSSOUSUARIO");
		iduser.setField("IDSSOUSUARIO0");
		iduser.setAtribute("IDSSOUSUARIO0");
		iduser.setValue(usuario.getId());	
		criterias.addCriteria(new Criteria(Operation.AND, Evaluation.EQUALS, iduser));
		try {
				listProjects(dao.query(clsSsoruserapp,new DataScroll(), query, criterias).getDataList());
			} catch (Exception e) {
				log.error(e);
			}

    }
	@NotifyChange("*")
	@Command("editApplication")
	public void editApplication(@BindingParam("app") AplicationBean proyect) {
		
		Executions.getCurrent().getSession().getAttributes().put("APP", proyect);
		Executions.sendRedirect("dashboard.zhtml");
		//redirect(proyect.getProjectBase().getNamespace());
		
	}
	private void redirect(String appName) {
		String path = ctxBean.getPathHome()+ File.separator+"apps"+ File.separator+TextParser.getFormaterUnixPath(appName).toLowerCase()+ File.separator +"webapp"+ File.separator + "menu";
    	File fil = new File(path+ File.separator+"root.json");
    	
    	try {
    		MenuBean root = mapper.readValue(fil, MenuBean.class);
    		String url = "dashboard.zhtml?appName="+appName;
    		for(MenuBean menu:root.getItems()) {
    			boolean salir =false;
    			if(menu.getName().equalsIgnoreCase(appName)) {
    				for(MenuBean item:menu.getItems()) {
    					if(item.isDesktop()) {
    	        			url = onSelectItem(item.getPage(),"dashboard.zhtml",TextParser.getFormaterUnixPath(appName).toLowerCase());
    	        			salir = true;
    	        			break;
    	        		}		
    				}
    			}
    			if(salir)break;
        	}
    		Executions.sendRedirect(url);
		} catch (IOException e) {
			log.error(e);
		}
	}
	public String onSelectItem(MenuBean item,String page,String appName) {
		String url = page+"?appName="+appName+"&page="+item.getNameScreen();
		if(item.getPrefix()!=null)url = url + "&prefix="+item.getPrefix();
		return url;
	}
	
	private void listProjects(List<Object> apps) throws Exception, JsonMappingException, IOException {
		String path = ctxBean.getPathHome()+File.separator+"apps";
		 File fil = new File(path);
			fil.mkdirs();
			for(File file :fil.listFiles()) {
				if(file.isDirectory()) {
					File[] fils = file.listFiles(new FileFilter() {
						@Override
						public boolean accept(File f) {
							if (f.isFile()) { 
								if(f.getName().endsWith("json") && f.getName().startsWith("applica")) {
									return true;	
								}
								
							}
							return false;
						}
					});
					for (File file1 : fils) {
						AplicationBean ab = mapper.readValue(file1, AplicationBean.class); 
						boolean add=false;
						for(Object ap:apps){
							String prefix = (String) getNested(ap, "idssoaplicacion.prefijo");
							if(ab.getProjectBase().getNamespace().equalsIgnoreCase(prefix)) {
								add=true;
								ab.getProjectBase().setIdxproject((Long)getNested(ap, "idssoaplicacion.idxaplicacion"));
								break;
							}
						};
						
						//prefijo
						if(add) {
							
							applications.add(ab);	
						}
						
					}
				}
			}
			  new SortCollections().sort(applications, "projectBase.txname", true);
			
	}
	@Override
	protected void doAfterCompose() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
