package com.akelius.svaponi.demofileupload;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Controller
@RequestMapping({
        "/",
        "/file",
        "/upload",
})
public class DemoFileUploadController {

    File LOCAL = new File("target/uploads");

    @GetMapping
    protected void doGet(
            final HttpServletResponse response
    ) throws IOException {
        try {
            final StringBuilder sb = new StringBuilder();

            sb.append("tempdir=")
                    .append(DemoFileUploadApplication.ServletContextAwareImpl.tempdir)
                    .append("\n");

            {
                final File[] files = DemoFileUploadApplication.ServletContextAwareImpl.tempdir.listFiles();
                Arrays.sort(files);
                for (int i = 0; i < files.length; i++) {
                    sb.append("tempfile_" + i + "=")
                            .append(files[i])
                            .append("\n");
                }
            }

            {
                final File[] files = LOCAL.listFiles();
                Arrays.sort(files);
                for (int i = 0; i < files.length; i++) {
                    sb.append("file_" + i + "=")
                            .append(files[i])
                            .append("\n");
                }
            }

            response.setStatus(200);
            response.getOutputStream().write(sb.toString().getBytes());
        } catch (final IOException e) {
            response.sendError(500, e.toString());
        }
    }

    @PostMapping
    protected void doPost(
            @RequestParam("file") final MultipartFile file,
            @RequestParam(value = "name", required = false) final String name,
            @RequestParam(value = "save", defaultValue = "false") final boolean save,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        try {

            log.info("doPost " + file.getOriginalFilename());

            if (save) {
                final File localFile = new File("target/uploads", name == null ? file.getOriginalFilename() : name);
                localFile.getParentFile().mkdirs();
                IOUtils.copy(file.getInputStream(), new FileOutputStream(localFile));
                final String message = file.getOriginalFilename() + " uploaded successfully as " + localFile;
                response.setStatus(201);
                response.getOutputStream().write(message.getBytes());
            } else {
                final String message = file.getOriginalFilename() + " ignored";
                response.setStatus(200);
                response.getOutputStream().write(message.getBytes());
            }

        } catch (final IOException e) {
            response.sendError(500, e.toString());
        }
    }
}
