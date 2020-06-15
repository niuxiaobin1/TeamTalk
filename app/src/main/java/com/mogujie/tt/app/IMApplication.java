package com.mogujie.tt.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;

import com.luck.picture.lib.app.IApp;
import com.luck.picture.lib.app.PictureAppMaster;
import com.luck.picture.lib.engine.PictureSelectorEngine;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.utils.BackgroundTasks;
import com.mogujie.tt.utils.ImageLoaderUtil;
import com.mogujie.tt.utils.Logger;
import com.mogujie.tt.utils.PictureSelectorEngineImp;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;
import okhttp3.OkHttpClient;


public class IMApplication extends Application implements IApp, CameraXConfig.Provider {
	public static Context sApplicationContext;
	public static String INSTITUTION_NUMBER = "";
	public static String API_KEY = "";
	private Logger logger = Logger.getLogger(IMApplication.class);

	@Override
	public void onCreate() {
		super.onCreate();
		logger.i("Application starts");
		sApplicationContext = getApplicationContext();
		startIMService();
		ImageLoaderUtil.initImageLoaderConfig(getApplicationContext());
		PictureAppMaster.getInstance().setApp(this);
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		builder.detectFileUriExposure();
		configUnits();
		initOkgoConfiger();
		BackgroundTasks.initInstance();
	}


	private void initOkgoConfiger() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
		//log打印级别，决定了log显示的详细程度
		loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
		//log颜色级别，决定了log在控制台显示的颜色
		loggingInterceptor.setColorLevel(Level.INFO);
		builder.addInterceptor(loggingInterceptor);
		//全局的读取超时时间
		builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
		//全局的写入超时时间
		builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
		//全局的连接超时时间
		builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
		//使用内存保持cookie，app退出后，cookie消失
		builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));
		//方法一：信任所有证书,不安全有风险
		HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory();
		builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager);

//        //---------根据需要传,不需要就不传-------------//
//        HttpHeaders headers = new HttpHeaders();
//        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文，不允许有特殊字符
//        headers.put("commonHeaderKey2", "commonHeaderValue2");
//        HttpParams params = new HttpParams();
//        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
//        params.put("commonParamsKey2", "这里支持中文参数");


		OkGo.getInstance().init(this)                       //必须调用初始化
				.setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置将使用默认的
				.setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
				.setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
				.setRetryCount(3);
	}



	private void configUnits() {
		//AndroidAutoSize 默认开启对 dp 的支持, 调用 UnitsManager.setSupportDP(false); 可以关闭对 dp 的支持
		//主单位 dp 和 副单位可以同时开启的原因是, 对于旧项目中已经使用了 dp 进行布局的页面的兼容
		//让开发者的旧项目可以渐进式的从 dp 切换到副单位, 即新页面用副单位进行布局, 然后抽时间逐渐的将旧页面的布局单位从 dp 改为副单位
		//最后将 dp 全部改为副单位后, 再使用 UnitsManager.setSupportDP(false); 将 dp 的支持关闭, 彻底隔离修改 density 所造成的不良影响
		//如果项目完全使用副单位, 则可以直接以像素为单位填写 AndroidManifest 中需要填写的设计图尺寸, 不需再把像素转化为 dp
		AutoSizeConfig.getInstance().getUnitsManager()
				.setSupportDP(false)
				.setDesignSize(375, 667)

				//当使用者想将旧项目从主单位过渡到副单位, 或从副单位过渡到主单位时
				//因为在使用主单位时, 建议在 AndroidManifest 中填写设计图的 dp 尺寸, 比如 360 * 640
				//而副单位有一个特性是可以直接在 AndroidManifest 中填写设计图的 px 尺寸, 比如 1080 * 1920
				//但在 AndroidManifest 中却只能填写一套设计图尺寸, 并且已经填写了主单位的设计图尺寸
				//所以当项目中同时存在副单位和主单位, 并且副单位的设计图尺寸与主单位的设计图尺寸不同时, 可以通过 UnitsManager#setDesignSize() 方法配置
				//如果副单位的设计图尺寸与主单位的设计图尺寸相同, 则不需要调用 UnitsManager#setDesignSize(), 框架会自动使用 AndroidManifest 中填写的设计图尺寸
//                .setDesignSize(2160, 3840)

				//AndroidAutoSize 默认开启对 sp 的支持, 调用 UnitsManager.setSupportSP(false); 可以关闭对 sp 的支持
				//如果关闭对 sp 的支持, 在布局时就应该使用副单位填写字体的尺寸
				//如果开启 sp, 对其他三方库控件影响不大, 也可以不关闭对 sp 的支持, 这里我就继续开启 sp, 请自行斟酌自己的项目是否需要关闭对 sp 的支持
				.setSupportSP(false)

				//AndroidAutoSize 默认不支持副单位, 调用 UnitsManager#setSupportSubunits() 可选择一个自己心仪的副单位, 并开启对副单位的支持
				//只能在 pt、in、mm 这三个冷门单位中选择一个作为副单位, 三个单位的适配效果其实都是一样的, 您觉的哪个单位看起顺眼就用哪个
				//您选择什么单位就在 layout 文件中用什么单位进行布局, 我选择用 mm 为单位进行布局, 因为 mm 翻译为中文是妹妹的意思
				//如果大家生活中没有妹妹, 那我们就让项目中最不缺的就是妹妹!
				.setSupportSubunits(Subunits.MM);
	}

	private void startIMService() {
		logger.i("start IMService");
		Intent intent = new Intent();
		intent.setClass(this, IMService.class);
		startService(intent);
	}

    public static boolean gifRunning = true;//gif是否运行

	@Override
	public Context getAppContext() {
		return this;
	}

	@Override
	public PictureSelectorEngine getPictureSelectorEngine() {
		return new PictureSelectorEngineImp();
	}

	@NonNull
	@Override
	public CameraXConfig getCameraXConfig() {
		return Camera2Config.defaultConfig();
	}
}
