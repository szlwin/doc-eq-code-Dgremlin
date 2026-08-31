package dec.core.context.config.manager;

import dec.core.context.EngineContext;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.connection.Connection;
import dec.core.context.config.model.datasource.DataSource;

import java.util.Objects;

//import com.orm.common.xml.model.config.ConfigInfo;
//import com.orm.common.xml.model.config.connection.Connection;
//import com.orm.common.xml.model.config.datasource.DataSource;

public class ConfigManager {

	private static final ConfigManager configManager =  new ConfigManager();
	
	private volatile ConfigInfo configInfo = new ConfigInfo();

	private final ThreadLocal<ConfigInfo> loadingConfigInfo = new ThreadLocal<ConfigInfo>();
	
	private ConfigManager()
	{
		
	}

	public static ConfigManager getInstance()
	{
		return configManager;
	}

	public ConfigInfo getConfigInfo() {
		ConfigInfo loading = loadingConfigInfo.get();
		return loading == null ? configInfo : loading;
	}

	/**
	 * 返回当前线程正在组装的候选配置；没有候选时创建一个新实例。
	 * 数据源类型登记和后续文件解析使用同一个对象，但不会提前修改已安装配置。
	 */
	public ConfigInfo getOrCreateLoadingConfigInfo() {
		ConfigInfo loading = loadingConfigInfo.get();
		if (loading == null) {
			loading = new ConfigInfo();
			loadingConfigInfo.set(loading);
		}
		return loading;
	}

	/** 当前加载完成或失败后清除线程内候选，避免下一次加载复用半成品。 */
	public void clearLoadingConfigInfo() {
		loadingConfigInfo.remove();
	}

	/** 返回已经对业务代码生效的配置，不受当前线程候选解析影响。 */
	public ConfigInfo getInstalledConfigInfo() {
		return configInfo;
	}

	public void setConfigInfo(ConfigInfo configInfo) {
		this.configInfo = Objects.requireNonNull(configInfo, "configInfo");
	}

	/** 完整解析旧格式配置后，以单一引用替换当前配置。 */
	public synchronized ConfigInfo install(ConfigInfo candidate) {
		ConfigInfo checked = Objects.requireNonNull(candidate, "candidate");
		this.configInfo = checked;
		return checked;
	}

	/**
	 * 编译成功后一次发布配置和 EngineContext。
	 * EngineContext 先写入候选对象，再通过 volatile 引用整体生效，读取方不会看到一半新一半旧的状态。
	 */
	public synchronized ConfigInfo install(
			ConfigInfo candidate,
			EngineContext engineContext) {
		ConfigInfo checked = Objects.requireNonNull(candidate, "candidate");
		checked.useEngineContext(Objects.requireNonNull(engineContext, "engineContext"));
		this.configInfo = checked;
		return checked;
	}

	/** 兼容旧启动方式：把编译结果绑定到当前已安装配置。 */
	public void useEngineContext(EngineContext engineContext) {
		install(configInfo, engineContext);
	}

	/** 返回当前已安装配置关联的编译上下文。 */
	public EngineContext getEngineContext() {
		return configInfo.getEngineContext();
	}

	/**
	 * 在当前线程内解析候选配置。解析器通过 ConfigContextUtil 读取到候选对象，
	 * 其他业务线程仍然使用已安装配置。
	 */
	public <T> T withConfigInfo(
			ConfigInfo candidate,
			ConfigOperation<T> operation) throws Exception {
		Objects.requireNonNull(candidate, "candidate");
		Objects.requireNonNull(operation, "operation");
		ConfigInfo previous = loadingConfigInfo.get();
		loadingConfigInfo.set(candidate);
		try {
			return operation.execute();
		} finally {
			if (previous == null) {
				loadingConfigInfo.remove();
			} else {
				loadingConfigInfo.set(previous);
			}
		}
	}

	@FunctionalInterface
	public interface ConfigOperation<T> {
		T execute() throws Exception;
	}
	
	public String getDefaultConName(){
		return getConfigInfo().getDefaultConnection();
	}
	
	public DataSource<?> getDataSourceByName(String dataName){
		return getConfigInfo().getDataSource(dataName);
	}
	
	public DataSource<?> getDataSourceByCon(String conName){
		Connection con = getConfigInfo().getConnection(conName);
		if (con == null) {
			throw new IllegalStateException("连接不存在: " + conName);
		}
		if (con.getDataSourceInfo() == null || con.getDataSourceInfo().getDataSource() == null) {
			throw new IllegalStateException("连接未绑定数据源: " + conName);
		}
		return con.getDataSourceInfo().getDataSource();
	}
}
