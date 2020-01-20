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

        @Override
        public void setServletContext(final ServletContext servletContext) {
            log.info("setServletContext");
            final File tempdir = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            if (tempdir == null) {
                log.info("null tempdir");
            } else {
                final FileAlterationObserver observer = new FileAlterationObserver(tempdir);
                final FileAlterationMonitor monitor = new FileAlterationMonitor(50l);
                final FileAlterationListener listener = new FileAlterationListenerAdaptor() {
                    @Override
                    public void onFileCreate(final File file) {
                        log.info("onFileCreate: " + file);
                    }

                    @Override
                    public void onFileDelete(final File file) {
                        log.info("onFileDelete: " + file);
                    }

                    @Override
                    public void onFileChange(final File file) {
                        // log.info("onFileChange: " + file);
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
    }
}
