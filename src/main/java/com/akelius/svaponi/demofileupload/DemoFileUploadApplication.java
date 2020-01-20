package com.akelius.svaponi.demofileupload;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import java.io.File;

@Slf4j
@SpringBootApplication
public class DemoFileUploadApplication {

    public static void main(final String[] args) {
        SpringApplication.run(DemoFileUploadApplication.class, args);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        final MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setFileSizeThreshold(DataSize.ofBytes(10 * 1024 * 1024));
        factory.setMaxFileSize(DataSize.ofBytes(-1L));
        factory.setMaxRequestSize(DataSize.ofBytes(-1L));
        return factory.createMultipartConfig();
    }

    @Component
    public static class ServletContextAwareImpl implements ServletContextAware {

        static File tempdir;

        @Override
        public void setServletContext(final ServletContext servletContext) {
            log.info("setServletContext");
            tempdir = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            if (tempdir == null) {
                log.info("null tempdir");
            } else {
                final FileAlterationObserver observer = new FileAlterationObserver(tempdir);
                final FileAlterationMonitor monitor = new FileAlterationMonitor(interval());
                final FileAlterationListener listener = new FileAlterationListenerAdaptor() {
                    @Override
                    public void onFileCreate(final File file) {
                        log.info("onFileCreate: " + file + " " + DataSize.ofBytes(file.length()));
                    }

                    @Override
                    public void onFileDelete(final File file) {
                        log.info("onFileDelete: " + file + " " + DataSize.ofBytes(file.length()));
                    }

                    @Override
                    public void onFileChange(final File file) {
                        log.info("onFileChange: " + file + " " + DataSize.ofBytes(file.length()));
                    }
                };
                observer.addListener(listener);
                monitor.addObserver(observer);
                try {
                    log.info("FileAlterationMonitor start()");
                    monitor.start();
                } catch (final Exception e) {
                    log.error("", e);
                }
            }
        }

        private long interval() {
            try {
                return Long.parseLong(System.getenv("MONITOR_INTERVAL"));
            } catch (final NumberFormatException e) {
                return 1000l;
            }
        }
    }
}
