package com.akelius.svaponi.demofileupload;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

@Slf4j
@Controller
@RequestMapping({
        "/",
        "/file",
        "/upload",
})
public class DemoFileUploadController {

    File LOCAL = new File("uploads");

    @GetMapping
    protected void doGet(
            @RequestParam(value = "path", required = false) final String path,
            final HttpServletResponse response
    ) throws IOException {
        try {
            if (path == null) {

                final StringBuilder sb = new StringBuilder();
                sb.append("tempdir=")
                        .append(DemoFileUploadApplication.ServletContextAwareImpl.tempdir)
                        .append("\n");

                {
                    if (DemoFileUploadApplication.ServletContextAwareImpl.tempdir != null) {
                        final File[] files = DemoFileUploadApplication.ServletContextAwareImpl.tempdir.listFiles();
                        if (files != null) {
                            Arrays.sort(files);
                            for (int i = 0; i < files.length; i++) {
                                sb.append("tempfile_" + i + "=")
                                        .append(files[i])
                                        .append(" (")
                                        .append(formatSize(files[i]))
                                        .append(")\n");
                            }
                        }
                    }
                }

                {
                    final File[] files = LOCAL.listFiles();
                    if (files != null) {
                        Arrays.sort(files);
                        for (int i = 0; i < files.length; i++) {
                            sb.append("file_" + i + "=")
                                    .append(files[i])
                                    .append(" (")
                                    .append(formatSize(files[i]))
                                    .append(")\n");
                        }
                    }
                }

                response.setStatus(200);
                response.getOutputStream().write(sb.toString().getBytes());

            } else {

                final File file = new File(path);
                if (file.exists()) {
                    response.setStatus(200);
                    IOUtils.copy(new FileInputStream(file), response.getOutputStream());
                } else {
                    response.sendError(404, file + " not found");
                }

            }
        } catch (final IOException e) {
            response.sendError(500, e.toString());
        }
    }

    private String formatSize(final File file) {
        final DataSize size = DataSize.ofBytes(file.length());
        if (size.toTerabytes() > 1) {
            return new BigDecimal(size.toBytes()).divide(new BigDecimal(1024 * 1024 * 1024 * 1024)).setScale(2, RoundingMode.HALF_UP) + " TB";
        }
        if (size.toGigabytes() > 1) {
            return new BigDecimal(size.toBytes()).divide(new BigDecimal(1024 * 1024 * 1024)).setScale(2, RoundingMode.HALF_UP) + " GB";
        }
        if (size.toMegabytes() > 1) {
            return new BigDecimal(size.toBytes()).divide(new BigDecimal(1024 * 1024)).setScale(2, RoundingMode.HALF_UP) + " MB";
        }
        if (size.toKilobytes() > 1) {
            return new BigDecimal(size.toBytes()).divide(new BigDecimal(1024)).setScale(2, RoundingMode.HALF_UP) + " KB";
        }
        return size.toBytes() + " Bytes";
    }

    @PostMapping
    protected void doPost(
            @RequestParam("file") final MultipartFile file,
            @RequestParam(value = "ignore", defaultValue = "false") final boolean ignore,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        try {

            log.info("doPost " + file.getOriginalFilename());

            if (ignore) {
                final String message = "ignore input-stream";
                response.setStatus(200);
                response.getOutputStream().write(message.getBytes());
            } else {
                File localFile = new File(LOCAL, file.getOriginalFilename());
                int i = 0;
                while (localFile.exists()) {
                    localFile = new File(LOCAL, file.getOriginalFilename() + "." + i++);
                }
                localFile.getParentFile().mkdirs();
                IOUtils.copy(file.getInputStream(), new FileOutputStream(localFile));
                final String message = localFile.getAbsolutePath();
                response.setStatus(201);
                response.getOutputStream().write(message.getBytes());
            }

        } catch (final IOException e) {
            response.sendError(500, e.toString());
        }
    }
}
