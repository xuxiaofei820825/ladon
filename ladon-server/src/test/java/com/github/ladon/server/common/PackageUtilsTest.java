package com.github.ladon.server.common;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PackageUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCheckPackage() {
		Set<String> actual = PackageUtils
				.checkPackage( "com.github.hyperion,"
						+ "com.github.ladon,"
						+ "com.github.ladon.p1.p2,"
						+ "com.github.ladon.p1.p2" );

		Assert.assertEquals( 2, actual.size() );

		Assert.assertTrue( actual.contains( "com.github.hyperion" ) );
		Assert.assertTrue( actual.contains( "com.github.ladon" ) );
	}

	@Test
	public void testFindPackageClass() {
		Set<String> actual = PackageUtils
				.findPackageClass( "com.github.ladon" );

		Assert.assertTrue( actual.size() > 0 );
	}

}
