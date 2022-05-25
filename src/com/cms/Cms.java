package com.cms;

import org.apache.log4j.Logger;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.cms.front.common.CommonController;
import com.cms.front.common.GlobalInterceptor;
import com.cms.front.directive.AdvertisingDirective;
import com.cms.front.directive.ChannelListDirective;
import com.cms.front.directive.ContentListDirective;
import com.cms.front.directive.ContentPageDirective;
import com.cms.front.directive.FriendlinkListDirective;
import com.cms.util.SendMail;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.ext.plugin.tablebind.AutoTableBindPlugin;
import com.jfinal.ext.plugin.tablebind.SimpleNameStyles;
import com.jfinal.ext.route.AutoBindRoutes;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.render.FreeMarkerRender;

/**
 * API引导式配置
 */
public class Cms extends JFinalConfig {
	private static Logger logger = Logger.getLogger(Cms.class); 
	
	/**
	 * 配置常量
	 */
	public void configConstant(Constants me) {
		// 加载少量必要配置，随后可用getProperty(...)获取值
		// loadPropertyFile("config.txt");
		// me.setDevMode(getPropertyToBoolean("devMode", false));

		logger.warn("DEBUG="+System.getenv("DEBUG"));
		logger.warn("JDBCURL="+System.getenv("JDBCURL"));
		logger.warn("DB_USER="+System.getenv("DB_USER"));
		logger.warn("DB_PASS="+System.getenv("DB_PASS"));
		logger.warn("SMTP_SERVER="+System.getenv("SMTP_SERVER"));
		logger.warn("SMTP_USER="+System.getenv("SMTP_USER"));
		logger.warn("SMTP_PASS="+System.getenv("SMTP_PASS"));
		logger.warn("MAIL_TO="+System.getenv("MAIL_TO"));
		me.setDevMode(Boolean.parseBoolean(System.getenv().getOrDefault("DEBUG","false")));
		me.setMaxPostSize(10000000);
	}
	
	/**
	 * 配置路由
	 */
	public void configRoute(Routes me) {
		me.add("/", CommonController.class);
		AutoBindRoutes abRoutes = new AutoBindRoutes();
		abRoutes.addExcludeClass(CommonController.class);
		me.add(abRoutes);
	}
	
	/**
	 * 配置插件
	 */
	public void configPlugin(Plugins me) {
		/*// 配置C3p0数据库连接池插件
		C3p0Plugin c3p0Plugin = new C3p0Plugin(getProperty("jdbcUrl"), getProperty("user"), getProperty("password").trim());
		me.add(c3p0Plugin);
		
		// 配置ActiveRecord插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin(c3p0Plugin);
		//arp.setShowSql(true);
		me.add(arp);
		arp.addMapping("user", User.class);	
		arp.addMapping("role", Role.class);	
		arp.addMapping("category", Category.class);	
		arp.addMapping("company", Company.class);	
		arp.addMapping("dept", Dept.class);	*/
		
		// 配置C3p0数据库连接池插件
		// DruidPlugin dp = new DruidPlugin(getProperty("jdbcUrl"), getProperty("user"), getProperty("password").trim());
		//System.out.print("pass:"+System.getenv("DB_PASS"));
		DruidPlugin dp = new DruidPlugin(System.getenv().getOrDefault("JDBCURL","jdbc:mysql://127.0.0.1/yun_web?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull"), 
				System.getenv().getOrDefault("DB_USER","robin"), System.getenv().getOrDefault("DB_PASS","robin123").trim());
		dp.addFilter(new StatFilter());
		WallFilter wall = new WallFilter();
		wall.setDbType("mysql");
		dp.addFilter(wall);
		me.add(dp);
		
		// 配置ActiveRecord插件
		//ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
		AutoTableBindPlugin atbp = new AutoTableBindPlugin(dp,SimpleNameStyles.LOWER_UNDERLINE);
		atbp.setShowSql(true);//这句话就是ShowSql 
		me.add(atbp);
		
		//QuartzPlugin quartzPlugin = new QuartzPlugin("quartzjob.properties");
		//me.add(quartzPlugin);
	}
	
	/**
	 * 配置全局拦截器
	 */
	public void configInterceptor(Interceptors me) {
		me.add(new GlobalInterceptor());
		me.add(new SessionInViewInterceptor());
	}
	
	/**
	 * 配置处理器
	 */
	public void configHandler(Handlers me) {
		//me.add(new UrlSkipHandler(".+\\.\\w{1,4}", false));
	}
	public void afterJFinalStart() {
	    FreeMarkerRender.getConfiguration().setSharedVariable("text_cut", new com.cms.front.directive.TextCutDirective());
	    FreeMarkerRender.getConfiguration().setSharedVariable("cms_content_list", new ContentListDirective());
	    FreeMarkerRender.getConfiguration().setSharedVariable("cms_channel_list", new ChannelListDirective());
	    FreeMarkerRender.getConfiguration().setSharedVariable("cms_content_page", new ContentPageDirective());
	    FreeMarkerRender.getConfiguration().setSharedVariable("cms_friendlink", new FriendlinkListDirective());
	    FreeMarkerRender.getConfiguration().setSharedVariable("cms_advertising", new AdvertisingDirective());
	}
	/**
	 * 建议使用 JFinal 手册推荐的方式启动项目
	 * 运行此 main 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
	 */
	public static void main(String[] args) {
		JFinal.start("WebRoot", 80, "/", 3);
	}
}
