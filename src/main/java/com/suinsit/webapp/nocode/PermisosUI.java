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
import org.enartframework.nocode.datamodel.jdbc.Criterias;
import org.enartframework.nocode.datamodel.model.Entity;
import org.enartframework.nocode.datamodel.model.Query;
import org.enartframework.nocode.datamodel.model.QueryBuilder;
import org.enartframework.orm.DataScroll;
import org.enartframework.orm.exception.DaoException;
import org.enartframework.security.Role;
import org.enartframework.suinsit.Context;
import org.enartframework.web.zk.navigation.Navigation;
import org.enartframework.web.zk.page.BaseMasterHandler;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.suinsit.nocode.web.MenuBean;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.ListModelList;

import com.enartsystems.mapping.BuilderModel;
import com.enartsystems.webapp.beans.AplicationBean;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author manuel
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)

public class PermisosUI extends BaseMasterHandler{
	private static final long serialVersionUID = 1L;
	private static final IEnartLogger log = EnartLoggerFactory.getLogger(PermisosUI.class);
	ObjectMapper mapper = new ObjectMapper();
	@WireVariable("ctxBean")
	protected Context ctxBean;
	@WireVariable("entityDao")
	IEntityLocal dao;
	@WireVariable("context")
	GenericApplicationContext context;
	@WireVariable
	private Environment environment;
	 Entity bean;
	 Class<?> clsBean;
	 BuilderModel builder;
	 List<Object> rol ;
	 ListModelList<Object> roles;
	 Criterias criterias;
	 Query query;
	 List<AplicationBean> applications = new ArrayList<AplicationBean>();
	 ListModelList<MenuBean> menus = new ListModelList<>();
	 ListModelList<MenuBean> items = new ListModelList<>();
	 
	public ListModelList<MenuBean> getItems() {
		return items;
	}
	public void setItems(ListModelList<MenuBean> items) {
		this.items = items;
	}
	public ListModelList<MenuBean> getMenus() {
		return menus;
	}
	public void setMenus(ListModelList<MenuBean> menus) {
		this.menus = menus;
	}
	public List<AplicationBean> getApplications() {
		return applications;
	}
	public void setApplications(List<AplicationBean> applications) {
		this.applications = applications;
	}
	public ListModelList<Object> getRoles() {
		return roles;
	}
	public void setRoles(ListModelList<Object> roles) {
		this.roles = roles;
	}
	/**
	 * 
	 */
	public PermisosUI() {
		// TODO Auto-generated constructor stub
	}
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		Selectors.wireVariables(page, this, Selectors.newVariableResolvers(getClass(), null));
		this.view = view;
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
		bean = builder.loadEntity("SSOROL");
		clsBean = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps."+bean.getNamespace()+"."+TextParser.getClassNameFormater(bean.getCoentity()));
		initDao();
		find();
		listProjects();
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
	
	private void find() {
		List<String> lfields = new ArrayList<>();
    	bean.getFields().forEach(sfield->{
			 lfields.add(sfield.getAtribute());
		});
    	//idssoaplicacion
		query = QueryBuilder.buildQueryWithLeftJoin(bean, lfields.toArray(new String[lfields.size()])); 
		criterias = new Criterias();
		try {
			rol = new ArrayList<>(dao.query(clsBean,new DataScroll(), query, criterias).getDataList());
			
		} catch (DaoException e) {
			log.error(e);		
		}
	}
	private void listProjects() throws Exception, JsonMappingException, IOException {
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
						applications.add(ab);	
				}
				}
			}
			  new SortCollections().sort(applications, "projectBase.txname", true);
	}
	String pathMenu;
	AplicationBean aplication;
	@NotifyChange({"*","menus","item","roles"})
	@Command
	public void onSelectApp(@BindingParam("item")AplicationBean item ) {
		aplication =item;
		menu = null;
		items.clear();
		if(roles!=null)roles.clear();
		this.item = null;
		pathMenu = ctxBean.getPathHome() + File.separator + "apps" + File.separator + TextParser.getFormaterUnixPath(item.getProjectBase().getNamespace()).toLowerCase() + File.separator +"webapp"+ File.separator+"menu"; 
        loadMenu(pathMenu);
		
	}
	private void storeMenu() throws JsonGenerationException, JsonMappingException, IOException {
		String pathDirScreen = ctxBean.getPathHome()+File.separator +  "studio" + File.separator +TextParser.getFormaterUnixPath(aplication.getProjectBase().getNamespace()).toLowerCase() 
				+ File.separator +"designer/screens";
		log.debug(pathDirScreen);
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File(pathDirScreen + File.separator + "main"+ File.separator+"root.json"), root);
		deployMenu(root);
	}
    private void deployMenu(MenuBean root) {
    	try {
		String path = ctxBean.getPathHome() + File.separator + "apps" + File.separator + TextParser.getFormaterUnixPath(aplication.getProjectBase().getNamespace()).toLowerCase() + File.separator +"webapp"+ File.separator+"menu";
		log.debug(path);
		new File(path).mkdirs();
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path + File.separator+"root.json"), root);
    	} catch (IOException e) {
			log.warn(e.getMessage());
		}
    }
	MenuBean menu = new MenuBean();
	MenuBean item;
	
	public MenuBean getItem() {
		return item;
	}
	public void setItem(MenuBean item) {
		this.item = item;
	}
	public MenuBean getMenu() {
		return menu;
	}

	public void setMenu(MenuBean menu) {
		this.menu = menu;
	}

	@NotifyChange({"*","item","roles"})
	@Command("editMenuRoot")
	public void editMenuRoot(@BindingParam("item") MenuBean item) {
		menu = item;
		items.clear();
		items.addAll(menu.getItems());
		if(roles!=null)roles.clear();
		this.item = null;
		BindUtils.postNotifyChange(null, null, this, "items");
		BindUtils.postNotifyChange(null, null, this, "menu");
	}
	@NotifyChange({"*","item"})
	@Command("permisos")
	public void permisos(@BindingParam("item") MenuBean item) {
		this.item =item;
		roles = new ListModelList<Object>(rol);
		roles.setMultiple(true);
		if(item.getRoles()!=null) {
			item.getRoles().forEach(rol->{
				if(roles.stream().filter(r->((Long)getNested(r, "idxssorol"))==rol.getId()).findFirst().isPresent()) {
					roles.addToSelection(roles.stream().filter(r->((Long)getNested(r, "idxssorol"))==rol.getId()).findFirst().get());	
				}
			});	
		}
		
	}
	@NotifyChange({"*","item"})
	@Command("save")
	public void save() {
		if(item!=null) {
			List<Role> lroles = new ArrayList<>(0); 
			roles.getSelection().forEach(r->{
				lroles.add(new Role((String) getNested(r, "rol"),(boolean) getNested(r, "system"),false,(Long)getNested(r, "idxssorol")));
			});
			try {
				root.getItems().stream().filter(mb->mb.getId().equals(menu.getId())).findFirst().get()
				.getItems().stream().filter(mb->mb.getId().equals(item.getId())).findFirst().get().setRoles(lroles);
				storeMenu();
			} catch (IOException e) {
				log.error(e);
			}
			Clients.showNotification("Permisos de "+item.getName()+" actualizados", "info", null, "middle_center", 50);	
		}
	}
	
	MenuBean root;
	private void loadMenu(String pathMenu) {
		menus = new ListModelList<MenuBean>();
		items.clear();
		File fil = new File(pathMenu + File.separator + "root.json");
		if(fil.exists()) {
			try {
				root = mapper.readValue(fil, MenuBean.class);
				if(root.getOrient()!=null) {
				}
				menus.addAll(root.getItems());
			} catch (IOException e) {
				log.error(e);
			}
		}
	}
	@Override
	protected void doAfterCompose() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
