package com.akelius.svaponi.demofileupload;

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
            System.out.println("setServletContext");
            final File tempdir = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            if (tempdir == null) {
                System.out.println("null tempdir");
            } else {
                final FileAlterationObserver observer = new FileAlterationObserver(tempdir);
                final FileAlterationMonitor monitor = new FileAlterationMonitor(50l);
                final FileAlterationListener listener = new FileAlterationListenerAdaptor() {
                    @Override
                    public void onFileCreate(final File file) {
                        System.out.println("onFileCreate: " + file);
                    }

                    @Override
                    public void onFileDelete(final File file) {
                        System.out.println("onFileDelete: " + file);
                    }

                    @Override
                    public void onFileChange(final File file) {
                        // System.out.println("onFileChange: " + file);
                    }
                };
                observer.addListener(listener);
                monitor.addObserver(observer);
                try {
                    System.out.println("FileAlterationMonitor start()");
                    monitor.start();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
