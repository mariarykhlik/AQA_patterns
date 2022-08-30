[![Build status](https://ci.appveyor.com/api/projects/status/88uwinri2vlc5pey?svg=true)](https://ci.appveyor.com/project/mariarykhlik/aqa-patterns)  

## Report Portal integrate manual ##  
1. Download docker-compose.yml: https://github.com/reportportal/reportportal/blob/master/docker-compose.yml;
2. Choose settings for unix/windows host;
3. Run ```docker-compose -p reportportal up``` (needs VPN usage);
4. Login to http://localhost:8080/ with superadmin/erebus;
5. Copy and save USER PROFILE/CONFIGURATION EXAMPLES/JAVA (REQUIRED) as src/test/resources/reportportal.properties:

```
  rp.endpoint = http://localhost:8080
  rp.uuid = 75c0ad32-995f-4939-a150-a97c9f9e1af8
  rp.launch = sureradmin_TEST_EXAMPLE
  rp.project = superadmin_personal
```

6. Add to build-gradle:

```
repositories {
    mavenLocal()
}

dependencies {
    implementation 'com.epam.reportportal:agent-java-junit5:5.0.0'
    implementation 'com.epam.reportportal:logger-java-logback:5.0.2'
    implementation 'ch.qos.logback:logback-classic:1.2.3'
}

test {
    testLogging.showStandardStreams = true
    systemProperty 'junit.jupiter.extensions.autodetection.enabled', true
}
```

7. Add as src/test/resources/META-INF/services/org.junit.jupiter.api.extension.Extension:

```
com.epam.reportportal.junit5.ReportPortalExtension
```

8. Add as src/test/resources/logback.xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- Send debug messages to System.out -->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <!-- By default, encoders are assigned the type ch.qos.logback.classic.encoder.PatternLayoutEncoder -->
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} %-5level %logger{5} - %thread - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="RP" class="com.epam.reportportal.logback.appender.ReportPortalAppender">
        <encoder>
            <!--Best practice: don't put time and logging level to the final message. Appender do this for you-->
            <pattern>%d{HH:mm:ss.SSS} [%t] %-5level - %msg%n</pattern>
            <pattern>[%t] - %msg%n</pattern>
        </encoder>
    </appender>

    <!--'additivity' flag is important! Without it logback will double-log log messages-->
    <logger name="binary_data_logger" level="TRACE" additivity="false">
        <appender-ref ref="RP"/>
    </logger>

    <logger name="com.epam.reportportal.service" level="WARN"/>
    <logger name="com.epam.reportportal.utils" level="WARN"/>

    <!-- By default, the level of the root level is set to DEBUG -->
    <root level="TRACE">
        <appender-ref ref="RP"/>
        <appender-ref ref="STDOUT"/>
    </root>

</configuration>
```

9. Add as src/test/java/ru/netology/delivery/util/LoggingUtils.java:

```java
package ru.netology.delivery.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LoggingUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger("binary_data_logger");

  private LoggingUtils() {
    //statics only
  }

  public static void logInfo(String message) {
    LOGGER.info(message);
  }

  public static void log(File file, String message) {
    LOGGER.info("RP_MESSAGE#FILE#{}#{}", file.getAbsolutePath(), message);
  }
}
```

10. Add as src/test/java/ru/netology/delivery/util/ScreenShooterReportPortalExtension.java:

```java
package ru.netology.delivery.util;

import com.codeborne.selenide.Screenshots;
import com.codeborne.selenide.ex.UIAssertionError;
import com.codeborne.selenide.impl.ScreenShotLaboratory;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Optional;

import static com.codeborne.selenide.Selenide.screenshot;
import static com.codeborne.selenide.WebDriverRunner.driver;

/**
 * Use this class to automatically take screenshots in case of ANY errors in tests (not only Selenide errors) and send them to ReportPortal.
 *
 * @see com.codeborne.selenide.junit5.ScreenShooterExtension
 */
public class ScreenShooterReportPortalExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
  private static final Logger log = LoggerFactory.getLogger(ScreenShooterReportPortalExtension.class);

  private final boolean captureSuccessfulTests;

  public ScreenShooterReportPortalExtension() {
    this(false);
  }

  public ScreenShooterReportPortalExtension(final boolean captureSuccessfulTests) {
    this.captureSuccessfulTests = captureSuccessfulTests;
  }

  @Override
  public void beforeTestExecution(final ExtensionContext context) {
    final Optional<Class<?>> testClass = context.getTestClass();
    final String className = testClass.map(Class::getName).orElse("EmptyClass");

    final Optional<Method> testMethod = context.getTestMethod();
    final String methodName = testMethod.map(Method::getName).orElse("emptyMethod");

    Screenshots.startContext(className, methodName);
  }

  @Override
  public void afterTestExecution(final ExtensionContext context) {
    if (captureSuccessfulTests) {
      log.info(screenshot(context.getTestMethod().toString()));
    } else {
      context.getExecutionException().ifPresent(error -> {
        if (!(error instanceof UIAssertionError)) {
          File screenshot = ScreenShotLaboratory.getInstance().takeScreenShotAsFile(driver());
          if (screenshot != null) {
            LoggingUtils.log(screenshot, "Attached screenshot");
          }
        }
      });
    }
    Screenshots.finishContext();
  }
}
```

11. Test class:
* use ```import ru.netology.util.ScreenShooterReportPortalExtension```;
* use annotation ```@ExtendWith({ScreenShooterReportPortalExtension.class})```;
* add steps description using```lofInfo(<message>)``` in tests.

*Finally look for report at http://localhost:8080/LAUNCHES*