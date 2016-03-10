grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.work.dir = 'target/work'
//grails.project.class.dir = "target/classes"
//grails.project.test.class.dir = "target/test-classes"
//grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.server.port.http = 9001

//uncomment (and adjustment settings) to fork the JVM to isolate classpaths
//grails.project.fork = [
//   test: [maxMemory:1024, minMemory:1024, debug:true, maxPerm:256]
//]

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits("global") {
		// specify dependency exclusions here; for example, uncomment this to disable ehcache:
		excludes "xml-apis", "commons-digester", "ehcache", "cglib"
	}
	log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	checksums true // Whether to verify checksums on resolve
	legacyResolve false
	// whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

	repositories {
		inherits true // Whether to inherit repository definitions from plugins

//		grailsPlugins() // deprecated
//		grailsHome() // deprecated
		grailsCentral() // https://repo.grails.org/grails/plugins

		mavenLocal()
		mavenCentral()

		mavenRepo "http://repo.grails.org/grails/core" // grails/core 의 순서를 mavenCentral 이후에 넣는다.

		mavenRepo 'http://artifactory.iwilab.com:8088/artifactory/libs-snapshot-local/'
		mavenRepo 'http://artifactory.iwilab.com:8088/artifactory/libs-release-local/'
		mavenRepo 'http://maven.daumcorp.com/content/repositories/kakao-KakaoBuy-release/'
		mavenRepo 'http://maven.daumcorp.com/content/repositories/kakao-KakaoBuy-snapshots/'

		// uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
		//mavenRepo "http://snapshots.repository.codehaus.org"
		//mavenRepo "http://repository.codehaus.org"
		//mavenRepo "http://download.java.net/maven/2/"
		//mavenRepo "http://repository.jboss.com/maven2/"
//		mavenRepo "http://repo.grails.org/grails/core"

	}

	dependencies {

		/* http://artifactory.iwilab.com:8088/artifactory/libs-release-local/ */
		compile "com.kakao.iceland:iceland-papi-client:1.33.1" // 1.21.0 -> 1.33.1
		compile("com.kakao.account:kakao-account-client:1.0.5") {
			exclude module: 'slf4j-log4j12'
		} // 0.3.26 -> 1.0.5

		// com.kakao.talk.thrift.service.TNotFoundException
		compile("com.kakao.talk:maldive_app_commons:1.3.47") {
			exclude module: 'slf4j-log4j12'
		} // 1.3.9 -> 1.3.47

		/* http://artifactory.iwilab.com:8088/artifactory/libs-snapshot-local/ */
		compile "com.kakao.buy:voucher-core:0.4-SNAPSHOT"
		compile "com.kakao.kage:kage-client:1.0-SNAPSHOT"

		/* http://repo.grails.org/grails/core */
		// /grails/core 의 artefact 들은 jar 확장자를 사용한다. plugins { .. } 에 의존성을 설정하는 경우 zip 만 지원하며
		// maven dependency 설정에서 jar 확장자를 지정하여도 작동하지 않는다.
		compile "commons-beanutils:commons-beanutils:jar:1.9.2" // 1.9.2 -> 1.9.2

		/* maven central */
		compile "org.apache.httpcomponents:httpclient:4.5.2"
		compile "org.codehaus.groovy.modules.http-builder:http-builder:0.7.1"
		compile "org.mybatis:mybatis:3.3.1" // 3.2.1 -> 3.3.1
		compile "org.mybatis:mybatis-spring:1.2.4" // 1.2.0 -> 1.2.4
		compile "mysql:mysql-connector-java:5.1.38" // 5.1.26 -> 5.1.38
		compile "net.sf.opencsv:opencsv:2.3" // 2.3 -> 2.3
		runtime "xml-apis:xml-apis:1.4.01" // excluded by global config.

		/* deprecated
		runtime 'mysql:mysql-connector-java:5.1.26'
		runtime 'com.kakao.kage:kage-client:1.0-SNAPSHOT'
		runtime 'xml-apis:xml-apis:1.4.01'

		compile 'org.springframework:spring-jms:4.0.5.RELEASE'
		compile "org.apache.poi:poi:3.8"
		compile("org.apache.poi:poi-ooxml:3.8") {
			exclude 'xml-apis'
		}
		compile 'org.mybatis:mybatis:3.2.1' // Core MyBatis library
		compile 'org.mybatis:mybatis-spring:1.2.0' // Support for Spring IoC</code>
		compile "net.sf.opencsv:opencsv:2.3"
		compile "commons-beanutils:commons-beanutils:1.9.2"
		compile 'joda-time:joda-time:2.3'
		compile "org.apache.commons:commons-pool2:2.3"
		compile 'org.apache.httpcomponents:httpcore:4.4'
		compile 'org.apache.httpcomponents:httpclient:4.3.5'
		compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.7.1'

		compile 'com.kakao.buy:voucher-core:0.4-SNAPSHOT'
		compile 'com.kakao.iceland:iceland-papi-client:1.21.0'
		compile('com.kakao.account:kakao-account-client:0.3.26') {
			exclude 'slf4j-log4j12'
		}

		compile 'com.kakao.talk:maldive-app-commons:1.3.9'
		test "org.grails:grails-datastore-test-support:1.0.2-grails-2.4"
		*/
	}

	plugins {
		/* deprecated
		// plugins for the build system only
		build ":improx:0.3"
		build ":release:2.2.1",
				':tomcat:7.0.52.1', {
			export = false
		}
		*/

		/* 아래 설정은 grails plugin repository 에서 가져오는 artefact 의 설정이다.
		   grails plugin repository - https://repo.grails.org/grails/core */

		/* https://repo.grails.org/grails/plugins */

		compile "org.grails.plugins:asset-pipeline:2.7.2" // 1.9.9
		compile "org.grails.plugins:spring-security-core:2.0.0" // 2.0-RC4
		compile "org.grails.plugins:hibernate4:4.3.8.1" // 4.3.6.1 - 5.0.2.RELEASE works on grails 2.5.x
		compile "org.grails.plugins:redis:1.6.6" // 1.5.4
		compile "org.grails.plugins:rest-client-builder:2.1.1" // 1.0.3
//		compile "org.grails:grails-datastore-rest-client:5.0.2.RELEASE"

		compile "org.grails.plugins:browser-detection:2.7.0" // 2.2.0
		compile "org.grails.plugins:excel-export:0.2.2" // 0.2.1
		compile "org.grails.plugins:cache-ehcache:1.0.5" // 1.0.5

		compile "org.grails.plugins:raven:6.0.0.4" // 6.0.0.4 Sentry Client for Grails
		// Executor will run asynchronous/concurrent/background threads
		compile "org.grails.plugins:executor:0.3" // 0.3

		// admin, seller 에서 jquery plugin 을 사용하고 있다.
		runtime "org.grails.plugins:jquery:1.11.1" // 1.11.1

		/* deprecated
		// plugins for the compile step
		compile ':scaffolding:2.1.2'
//		compile ':cache:1.1.8'
		compile ":cache-ehcache:1.0.5"
		compile ':asset-pipeline:1.9.9'
		compile ":cache-headers:1.1.7"
		compile ":spring-security-core:2.0-RC4"
		*/

		/* rest:0.8 plugin 을 사용하면,
		 * java.lang.NoClassDefFoundError: org/w3c/dom/ElementTraversal
		 * 오류가 발생한다.
		 */
		/* deprecated
//		compile ":rest:0.8"
		compile ":rest-client-builder:1.0.3"
		compile ":remoting:1.3"
		compile ":spring-events:1.2"
		compile ":redis:1.5.4"
		compile ":excel-export:0.2.1"
		compile ":browser-detection:2.2.0"
		compile ":raven:6.0.0.4"
		compile "org.grails.plugins:executor:0.3"

		runtime(':hibernate4:4.3.6.1') {
			exclude 'ehcache-core'
		} // or ':hibernate:3.6.10.14'
		//runtime ':hibernate:3.6.10.14'
		//runtime ':database-migration:1.4.0'
		runtime ":jquery:1.11.1"
//		runtime ":resources:1.2.8"
		//runtime ":zipped-resources:1.0"
//		runtime ":cached-resources:1.0"
		//runtime ":yui-minify-resources:0.1.4"

		*/

		// Provides the way of using interactive mode from other process via TCP.
		compile "org.grails.plugins:improx:0.3"

		test "org.grails.plugins:code-coverage:2.0.3-3"
		compile "org.grails.plugins:codenarc:0.25.1"
	}
}

grails.reload.enabled = true

coverage {
	nopost = true
	xml = true
	enabledByDefault = false
}

codenarc.properties = {
	// http://ehc.ac/p/codenarc/mailman/message/28247392/
	GrailsDomainHasEquals.enabled = false
	GrailsDomainHasToString.enabled = false
}

codenarc.reports = {
	MyXmlReport('xml') {
		outputFile = 'target/CodeNarc-Report.xml'
		title = 'buy common report'
	}

	MyHtmlReport('html') {
		outputFile = 'target/CodeNarc-Report.html'
		title = 'buy common report'
	}

	MyTextReport('text') {
		outputFile = 'target/CodeNarc-Report.txt'
		title = 'buy common report'
	}
}
