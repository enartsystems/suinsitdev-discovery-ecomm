/**
 * 
 */
package com.suinsit.webapp.nocode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.enartframework.core.shared.commons.TextParser;
import org.enartframework.core.shared.logger.EnartLoggerFactory;
import org.enartframework.core.shared.logger.IEnartLogger;
import org.enartframework.web.zk.page.BaseMasterHandler;
import org.springframework.context.support.GenericApplicationContext;
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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author manuel
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class MenuNavigation extends BaseMasterHandler {
	private static final long serialVersionUID = 1L;
	private static final IEnartLogger log = EnartLoggerFactory.getLogger(MenuNavigation.class);
	@WireVariable("context")
	GenericApplicationContext context;
	@WireVariable
	private Environment environment;
	String pathDirScreen;
	String pathDirModel;
	String pathDirSchema;
	String appName;
	String pageName;
	Component view;
	List<MenuBean> menus = new ArrayList<>();
	ObjectMapper mapper = new ObjectMapper();
	  boolean home=false;
	public List<MenuBean> getMenus() {
		return menus;
	}

	public void setMenus(List<MenuBean> menus) {
		this.menus = menus;
	}

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		Selectors.wireVariables(page, this, Selectors.newVariableResolvers(getClass(), null));
		this.view = view;
		HttpServletRequest req = (HttpServletRequest) desktop.getExecution().getNativeRequest();
		appName = req.getParameter("appName");
		if(req.getParameter("page")==null) {
			home=true;
		}
		pathDirModel = getClass().getResource("/applications/"+appName+"/model").getFile();
		pathDirScreen = getClass().getResource("/applications/"+appName+"/pages").getFile();
		pathDirSchema = getClass().getResource("/applications/"+appName+"/screens").getFile();
	
		EventQueues.lookup("navigation",EventQueues.SESSION,true).subscribe(new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				load();
			}
		});
		load();
	}
    private void load() {
    	String path = System.getenv("SUINSIT_DEPLOY")+ File.separator+TextParser.getFormaterUnixPath(appName).toLowerCase()+ File.separator + "menu";
    	File fil = new File(path+ File.separator+"root.json");
    	try {
    		MenuBean root = mapper.readValue(fil, MenuBean.class);
    		menus = new ArrayList<>();
    		root.getItems().forEach(mb->{
    			menus.add(clearDuplicates(mb));
    		});
			//menus.addAll(root.getItems());
			if(home) {
				goToHome(menus);
			}
		} catch (IOException e) {
			log.error(e);
		}
    }
    private MenuBean clearDuplicates(MenuBean bean) {
    	List<MenuBean> items = new ArrayList();
    	List<String> names = new ArrayList<>();
              bean.getItems().forEach(mb->{
            	  if(!names.contains(mb.getName())) {
            		  names.add(mb.getName());
            		  List<MenuBean> items1 = new ArrayList();
            		  mb.getItems().forEach(m->{
            			  items1.add(clearDuplicates(m));
            		  });
            		  mb.setItems(items1);
            	  }
            	  items.add(mb);
              });    	
    	
        bean.setItems(items);    	  
    	return bean;
    }
    private void goToHome(List<MenuBean> menus) {
    	for(MenuBean menu:menus) {
    		if(menu.isDesktop()) {
    			onSelectItem(menu.getPage(),"apps.zhtml");
    		}else {
    			goToHome(menu.getItems());
    		}
    		
    	}
    }
	
	private MenuBean getMenuBean(String label, String page) {
		MenuBean menu = new MenuBean();
		menu.setName(label);
		menu.setNameScreen(page);
		menu.setAplication(appName);
		return menu;
	}
	@NotifyChange("*")
	@Command("goToHome")
	public void goToHome(@BindingParam("page")String page) {
		Executions.sendRedirect(page);
	}
	@NotifyChange("*")
	@Command("selectItem")
	public void onSelectItem(@BindingParam("item")MenuBean item,@BindingParam("page")String page) {
		String url = page+"?appName="+appName+"&page="+item.getNameScreen();
		if(item.getPrefix()!=null)url = url + "&prefix="+item.getPrefix();
		Executions.sendRedirect(url);
	}
	@Override
	protected void doAfterCompose() throws Exception {
		// TODO Auto-generated method stub

	}

}
