package com.github.ladon.server.common;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.SystemPropertyUtils;

public abstract class PackageUtils {

	/** logger */
	private final static Logger logger = Utils.getLogger();

	// 扫描 scanPackages 下的文件的匹配符
	protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	/**
	 * 根据扫描包的,查询下面的所有类
	 *
	 * @param scanPackages
	 *          扫描的package路径
	 * @return 指定包下的所有类名
	 */
	public static Set<String> findPackageClass( String scanPackages ) {
		if ( StringUtils.isBlank( scanPackages ) ) {
			return Collections.emptySet();
		}

		// 验证及排重包路径,避免父子路径多次扫描
		Set<String> packages = checkPackage( scanPackages );

		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory( resourcePatternResolver );
		Set<String> clazzSet = new HashSet<String>();

		packages.stream()
				.filter( p -> StringUtils.isNotBlank( p ) )
				.forEach( p -> {
					try {

						String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
								org.springframework.util.ClassUtils.convertClassNameToResourcePath(
										SystemPropertyUtils.resolvePlaceholders( p ) )
								+ "/" + DEFAULT_RESOURCE_PATTERN;

						Resource[] resources = resourcePatternResolver.getResources( packageSearchPath );
						for ( Resource resource : resources ) {
							// 检查resource，这里的resource都是class
							String clazz = loadClassName( metadataReaderFactory, resource );
							clazzSet.add( clazz );
						}
					}
					catch ( Exception e ) {
						// warn log
						logger.warn( "failed to get class of package: {}", p );
					}
				} );

		return clazzSet;
	}

	/**
	 * 排重、检测package父子关系，避免多次扫描
	 *
	 * @param scanPackages
	 *          指定的包路径。多个时，使用[,]间隔
	 * @return 返回检查后有效的路径集合
	 */
	public static Set<String> checkPackage( String scanPackages ) {

		// 如果未指定包，则返回空集合
		if ( StringUtils.isBlank( scanPackages ) )
			return Collections.emptySet();

		Set<String> packages = new HashSet<String>();

		// 处理指定包集合
		final Set<String> tmps = Stream.of( scanPackages.split( "," ) )
				.filter( p -> StringUtils.isNotBlank( p ) ) // 去空白
				.filter( p -> !p.equals( "." ) && !p.startsWith( "." ) ) // 去无效路径
				.distinct() // 去重
				.collect( Collectors.toSet() );

		// map一个用来比较
		final List<String> packagesToCompared = tmps.stream()
				.map( p -> p )
				.collect( Collectors.toList() );

		// 去除子包
		packages = tmps.stream()
				.filter( p -> {
					for ( String pToCompare : packagesToCompared ) {
						if ( p.startsWith( pToCompare + "." ) ) {
							// 如果是子包，则删除
							return false;
						}
					}
					return true;
				} )
				.collect( Collectors.toSet() );

		return packages;
	}

	/**
	 * 加载资源，根据resource获取className
	 *
	 * @param metadataReaderFactory
	 *          spring中用来读取resource为class的工具
	 * @param resource
	 *          这里的资源就是一个Class
	 * @throws IOException
	 */
	private static String loadClassName( final MetadataReaderFactory metadataReaderFactory, final Resource resource )
			throws IOException {
		if ( resource.isReadable() ) {
			MetadataReader metadataReader = metadataReaderFactory.getMetadataReader( resource );
			if ( metadataReader != null ) {
				return metadataReader.getClassMetadata().getClassName();
			}
		}

		return null;
	}

}
