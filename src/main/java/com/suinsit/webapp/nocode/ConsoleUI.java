/**
 * 
 */
package com.suinsit.webapp.nocode;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

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
import org.enartframework.suinsit.Context;
import org.enartframework.web.exception.UiException;
import org.enartframework.web.zk.navigation.Navigation;
import org.enartframework.web.zk.navigation.NavigationItem;
import org.enartframework.web.zk.page.BaseMasterHandler;
import org.enartframework.zk.utils.MessageWin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.suinsit.nocode.web.MenuBean;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.lang.Library;
import org.zkoss.util.Locales;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.ComponentNotFoundException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkmax.zul.Nav;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Style;
import org.zkoss.zul.theme.Themes;

import com.enartsystems.mapping.BuilderModel;
import com.enartsystems.platform.nocode.builder.ScreenModel;
import com.enartsystems.webapp.beans.AplicationBean;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

/**
 * @author manuel
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)

public class ConsoleUI  extends BaseMasterHandler{
	private static final long serialVersionUID = 1L;
	private static final IEnartLogger log = EnartLoggerFactory.getLogger(ConsoleUI.class);
	ObjectMapper mapper = new ObjectMapper();
	@WireVariable("ctxBean")
	protected Context ctxBean;
	@WireVariable("entityDao")
	IEntityLocal dao;
	@WireVariable("context")
	GenericApplicationContext context;
	@WireVariable
	private Environment environment;
	@WireVariable
	@Autowired
	DataSource datasource;
	DataSource ds;
	String pathDirScreen;
	String pathDirModel;
	String pathDirSchema;
	String pathMenu;
	String appName;
	String pageName;
	AplicationBean proyect;
	@Wire
	Style theme;
	Entity bean;
	Query query;
	ScreenModel screenModel;
	List<Entity> beans = new ArrayList<>();
	Criterias criterias = new Criterias();
	Properties lang;
	String memory;
	EventQueue eventLocale;
	Component view;
	List<MenuBean> menus = new ArrayList<>();
	Entity permisos;
	String menu;
	Class<?> clsPermisos;
	 BuilderModel builder;
	public String getMenu() {
		return menu;
	}
	public void setMenu(String menu) {
		this.menu = menu;
	}
	public List<MenuBean> getMenus() {
		return menus;
	}
	public void setMenus(List<MenuBean> menus) {
		this.menus = menus;
	}
	
	public AplicationBean getProyect() {
		return proyect;
	}
	public void setProyect(AplicationBean proyect) {
		this.proyect = proyect;
	}
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		Selectors.wireVariables(page, this, Selectors.newVariableResolvers(getClass(), null));
		this.view = view;
		HttpServletRequest req = (HttpServletRequest) desktop.getExecution().getNativeRequest();
		if(req.getSession().getAttribute("APP")==null) {
			Executions.sendRedirect("apps.zhtml");
		}
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
		proyect =  (AplicationBean) req.getSession().getAttribute("APP");
		appName = proyect.getProjectBase().getTxname();
	
		pathDirModel =  ctxBean.getPathHome() + File.separator + "data" + File.separator +"model";
		pathDirScreen = ctxBean.getPathHome() + File.separator + "apps" + File.separator + TextParser.getFormaterUnixPath(proyect.getProjectBase().getNamespace()).toLowerCase() + File.separator +"webapp"+ File.separator+"pages";
		pathDirSchema = ctxBean.getPathHome() + File.separator + "apps" + File.separator + TextParser.getFormaterUnixPath(proyect.getProjectBase().getNamespace()).toLowerCase() + File.separator +"webapp"+ File.separator+"screens";
		pathMenu = ctxBean.getPathHome() + File.separator + "apps" + File.separator + TextParser.getFormaterUnixPath(proyect.getProjectBase().getNamespace()).toLowerCase() + File.separator +"webapp"+ File.separator+"menu"; 
		ds = environment.getProperty("APPLICATION_DS", DataSource.class);
		initDao();
		builder = new BuilderModel(ctxBean.getPathHome()+ File.separator + "data"+ File.separator + "model");
		permisos = builder.loadFromDataGrid("SSOVPERMROL");
		clsPermisos = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps."+permisos.getNamespace()+"."+TextParser.getClassNameFormater(permisos.getCoentity()));
		updateLocate();
		EventQueues.lookup("locale",EventQueues.SESSION,true).subscribe(new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				updateLocate();
			}
		});
		eventLocale= EventQueues.lookup("locale",EventQueues.SESSION,true);
		Runtime rt = Runtime.getRuntime();
		long usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
		memory = usedMB + "Mb. / " + rt.totalMemory() / 1024 / 1024 + " Mb.";
		load();
		loadPage();
		
	}
	
	private MenuBean getMenuBean(String label, String page) {
		MenuBean menu = new MenuBean();
		menu.setName(label);
		menu.setNameScreen(page);
		menu.setAplication(appName);
		return menu;
	}
	@NotifyChange("*")
	@Command("changeTheme")
	public void onChangeTheme(@BindingParam("theme")String tema) {
		theme.setSrc("themes/"+tema+"/bootstrap.css");
		setCookie("theme",tema);
		theme.invalidate();
		view.invalidate();
	}
	public void setCookie(String name, String value) {
		HttpServletResponse resp = (HttpServletResponse)Executions.getCurrent().getNativeResponse();
		resp.addCookie(new Cookie(name, value));
	}
	@NotifyChange("*")
	@Command("changeThemeZk")
	public void onChangeThemeZk(@BindingParam("theme")String tema) {
		Themes.setTheme(Executions.getCurrent(), tema);
		Library.setProperty("org.zkoss.theme.preferred", tema);
		//view.invalidate();
		Executions.sendRedirect("");
		
	//	
	}
	private void loadPage() {
		HttpServletRequest req = (HttpServletRequest) desktop.getExecution().getNativeRequest();
		 if(req.getParameter("theme")!=null) {
	        	theme.setSrc("themes/"+req.getParameter("theme")+"/bootstrap.css");
         }
		 Cookie[] cookies = req.getCookies();
			if(cookies != null) {
				for(Cookie cookie : cookies) {
					if(cookie.getName().equals("theme")) {
						theme.setSrc("themes/"+cookie.getValue()+"/bootstrap.css");
					}
				}
			}
		 Map<String,Object> args = new HashMap();
		 args.put("PATH_MODEL", pathDirModel);
		 args.put("PATH_SCREEN", pathDirScreen);
		 args.put("PATH_SCHEMA", pathDirSchema);
		 
		 if (req.getParameter("page")!=null) {
	            try {
	            	if(req.getParameter("prefix")!=null) {
	            		appendPage(pathDirScreen + File.separator +"/"+req.getParameter("prefix")+req.getParameter("page")+".zul",iddesktop,args);
	            	}else {
	            		appendPage(pathDirScreen + File.separator +"/index"+req.getParameter("page")+".zul",iddesktop,args);	
	            	}
            		
	            } catch (ComponentNotFoundException e) {
	                log.error(e);
	                MessageWin.getMessageBox();
	                Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
	            }
	        }
	}
	@NotifyChange("*")
	@Command("selectItem")
	public void onSelectItem(@BindingParam("item")MenuBean item,@BindingParam("main")String menu) {
		Map<String,Object> args = new HashMap();
		 args.put("PATH_MODEL", pathDirModel);
		 args.put("PATH_SCREEN", pathDirScreen);
		 args.put("PATH_SCHEMA", pathDirSchema);
		 this.menu=menu;
		 if(item.getPageUrl()!=null) {
			 appendPage(pathDirScreen + File.separator +item.getPageUrl(),iddesktop,args);
		 }else {
			 String prefix = item.getPrefix()!=null?item.getPrefix():"index";
			 appendPage(pathDirScreen + File.separator +prefix+item.getNameScreen()+".zul",iddesktop,args);	 
		 }
		 	
	}
	public Entity loadEntity(String name) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(new File(pathDirScreen + "/" + name + ".json"), Entity.class);
	}

	public ScreenModel loadScreenModel(String name) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(new File(pathDirScreen + "/" + name + ".json"), ScreenModel.class);
	}

	private void initDao() {
		if (dao == null) {
			dao = new EntityDao();
			((EntityDao) dao).setDataSource(ds);
		}
	}
	private void updateLocate() {
		lang = new Properties();
//		try {
//			File fil =new File(pathDirScreen + "/" + idpagina + "_" + org.zkoss.util.Locales.getCurrent().getLanguage() + ".properties"); 
//			if(fil.exists()) {
//				lang.load(new FileInputStream(fil));	
//			}
//			
//		} catch (IOException e) {
//			log.error(e.getCause());
//		}
		BindUtils.postNotifyChange(null, null, this, "lang");
	}
	private void load() {
		Field r = permisos.getFieldByAtribute("ROL").copy();
		Field a = permisos.getFieldByAtribute("IDXAPLICACION").copy();
		a.setValue(proyect.getProjectBase().getIdxproject());
		try {
			r.setValue((List<String>)getUser().getRoles());
		} catch (UiException e1) {
			log.error(e1);
		}
		criterias.addCriteria(new Criteria(Operation.AND,Evaluation.IN,r));
		criterias.addCriteria(new Criteria(Operation.AND,Evaluation.EQUALS,a));
		List<String> lfields = new ArrayList<>();
		permisos.getFields().forEach(f->{
			lfields.add(f.getAtribute());
		});
		QueryBuilder.buildQueryWithoutJoin(permisos, lfields.toArray(new String[lfields.size()]));
		DataScroll ds = new DataScroll();
		ds.setMaxRows(100);
		try {
		 List<Object>	perms =  dao.query(clsPermisos,ds, QueryBuilder.buildQueryWithoutJoin(permisos, lfields.toArray(new String[lfields.size()])),  criterias).getDataList();
		 perms.forEach(p->{
			 
		 });
		} catch (DaoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		File fil = new File(pathMenu + File.separator + "root.json");
		if(fil.exists()) {
			navigation = new Navigation(); 
			MenuBean root;
			try {
				root = mapper.readValue(fil, MenuBean.class);
				List<Navigation> navs = new ArrayList<>();
				for(MenuBean menu:root.getItems()) {
					navs.add(getNavigation(menu));	
					addMenu(menu);
				}
				reloadNavigation(navs);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void addMenu(MenuBean menu) {
		List<MenuBean> items= new ArrayList<>();
		for(MenuBean item:menu.getItems()) {
			if(item.getRoles()!=null) {
				try {
					boolean add=false;
					
					for(String rol:(List<String>) getUser().getRoles()){
						if(item.getRoles().stream().filter(r->r.getName().equalsIgnoreCase(rol.toString())).findFirst().isPresent()) {
							add=true;
							
						}
					};
					if(add) {
						items.add(item);
					}
				} catch (UiException e) {
					log.error(e);
				}
			}
		}
		menu.getItems().clear();
		menu.getItems().addAll(items);
		if(menu.getItems().size()>0) {
			menus.add(menu);	
		}
		
	}
	Map<String,MenuBean> pages = new HashMap<>();
	List<String> names= new ArrayList<>();
	private Navigation getNavigation(MenuBean menu) {
		pages.clear();
		Navigation nav = getNavigation(menu.getName(),menu.getIconClass());
		for(MenuBean item:menu.getItems()) {
			
			if(item.getRoles()!=null) {
				try {
					boolean add=false;
					for(String rol:(List<String>) getUser().getRoles()){
						if(item.getRoles().stream().filter(r->r.getName().equalsIgnoreCase(rol.toString())).findFirst().isPresent()) {
							add=true;
						}
					};
					if(add) {
						if(!names.contains(item.getName())) {
							nav.getItems().add(getNavigationItem(item.getName(), item.getIconClass(), item.getPage()!=null?item.getPage().getIdscreen():item.getPageUrl()));
							names.add(item.getName());
							pages.put(item.getIdscreen(), item);	
						}
					}
				} catch (UiException e) {
					log.error(e);
				}
				
			}else {
//				nav.getItems().add(getNavigationItem(item.getName(), item.getIconClass(), item.getPage()!=null?item.getPage().getIdscreen():item.getPageUrl()));
//				pages.put(item.getIdscreen(), item);	
			}
			
		}
		
		return nav;
	}
	@Wire
	Navbar navbar2;
	@Wire
	Div iddesktop;
	@NotifyChange("*")
	@Command("locale")
	public void setLocale(@BindingParam("locale") String local,@BindingParam("label")String label,@BindingParam("image") String image) {
		 Locale locale = org.zkoss.util.Locales.getLocale(local);
		// session.setAttribute(Attributes.PREFERRED_LOCALE, locale);
		 try {
			Clients.reloadMessages(locale);
		} catch (IOException e) {
			log.error(e);
		}
		 Locales.setThreadLocal(locale);
		 this.imageLocale=image;
		 this.labelLocale=Labels.getLabel(label, label);
		 reloadNavigation();
	//	 eventLocale.publish(new Event("locale", view, locale));
	}
	private String imageLocale="../global_assets/images/lang/gb.png";
	private String labelLocale="English";
	
	
	public String getImageLocale() {
		return imageLocale;
	}

	public void setImageLocale(String imageLocale) {
		this.imageLocale = imageLocale;
	}

	public String getLabelLocale() {
		return labelLocale;
	}

	public void setLabelLocale(String labelLocale) {
		this.labelLocale = labelLocale;
	}
	protected void reloadNavigation() {
		navbar2.getChildren().clear();
		for(Navigation naviga:navigation.getNavs()) {
			navbar2.appendChild(getNav(naviga));
		}
	}
	protected void reloadNavigation(List<Navigation> nav) {
		if(navbar2==null)return;
		navbar2.getChildren().clear();
		navigation.getNavs().clear();
		navigation.getNavs().addAll(nav);
		for(Navigation naviga:navigation.getNavs()) {
			navbar2.appendChild(getNav(naviga));
		}
	}
	Navigation navigation;
	boolean collapse=false;
	
	public boolean isCollapse() {
		return collapse;
	}

	public void setCollapse(boolean collapse) {
		this.collapse = collapse;
	}
	private Nav getNav(final Navigation naviga) {
		Nav nav = new Nav(Labels.getLabel(naviga.getName(), naviga.getName()));
		nav.setBadgeText(String.valueOf(naviga.getItems().size()+naviga.getNavs().size()));
		if(naviga.getIconclazz()!=null)nav.setIconSclass(naviga.getIconclazz());
		if(naviga.getImage()!=null)nav.setImage(naviga.getImage());
		if(naviga.getImageHover()!=null)nav.setHoverImage(naviga.getImageHover());
		for(NavigationItem items:naviga.getItems()) {
			nav.appendChild(getItem(items));
		}
		for(Navigation n:naviga.getNavs()) {
			nav.appendChild(getNav(n));
		}
		return nav;
	}
	private Navitem getItem(final NavigationItem item) {
		Navitem ni = new Navitem();
		ni.setLabel(Labels.getLabel(item.getLabel(), item.getLabel()));
		if(item.getIconclazz()!=null) {
			ni.setIconSclass(item.getIconclazz());	
		}
		ni.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				try {
					((Navitem)event.getTarget()).setSelected(true);
					onNavigate(item.getPage());
					
				}catch (ComponentNotFoundException e) {
					log.error(e);
					MessageWin.getMessageBox().show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
				}
			}
		});
		
		return ni;
	}
	private static Navigation getNavigation(String label,String icono) {
		Navigation nav = new  Navigation();
		nav.setIconclazz(icono);
        nav.setName(Labels.getLabel(label, label));
		return nav;
	}
    private NavigationItem getNavigationItem(String label,String icono,String page) {
    	NavigationItem item = new NavigationItem();
    	item.setLabel(Labels.getLabel(label, label));
    	if(icono!=null)item.setIconclazz(icono);
    	item.setPage(page);
    	return item;
    }
	public Navigation getNavigation() {
		return navigation;
	}

	public void setNavigation(Navigation navigation) {
		this.navigation = navigation;
	}
//	String currentPage="console.zul";
	String currentPage="inicio.zul";
	@NotifyChange("*")
	@Command("navigate")
	public void onNavigate(@BindingParam("page") String page) {
	//	if(!page.equals("#"))
	//	appendPage(pathDirScreen + File.separator +"sources/index"+page+".zul",iddesktop,null);
//			
//		this.currentPage=page;
//		BindUtils.postNotifyChange(null, EventQueues.DESKTOP, this, "currentPage");
	}
	
	
	public String getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(String currentPage) {
		this.currentPage = currentPage;
	}

	@NotifyChange("*")
	@Command("logout")
	public void logoutt() {
		SecurityContextHolder.clearContext();
		Executions.getCurrent().getSession().invalidate();
		Executions.sendRedirect("/");
	}

	// Add some life
	@Listen("onTimer = #timer1")
	public void updateData() {
		Runtime rt = Runtime.getRuntime();
		long usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
		rt.gc();
		memory = usedMB + "Mb. / " + rt.totalMemory() / 1024 / 1024 + " Mb.";
		BindUtils.postNotifyChange(null, null, this, "memory");
	}
	
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}
	public Properties getLang() {
		return lang;
	}

	public void setLang(Properties lang) {
		this.lang = lang;
	}

	public Criterias getCriterias() {
		return criterias;
	}

	public void setCriterias(Criterias criterias) {
		this.criterias = criterias;
	}

	public List<Entity> getBeans() {
		return beans;
	}

	public void setBeans(List<Entity> beans) {
		this.beans = beans;
	}

	public Entity getBean() {
		return bean;
	}

	public void setBean(Entity bean) {
		this.bean = bean;
	}
	@Override
	protected void doAfterCompose() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
