package com.akelius.svaponi.demofileupload;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Controller
@RequestMapping({
        "/file",
        "/file4",
})
public class DemoFileUploadController {

    @GetMapping
    protected void doGet(
            @RequestParam("path") final String path,
            final HttpServletResponse response
    ) throws IOException {
        try {
            System.out.println("doGet " + path);
            IOUtils.copy(new FileInputStream(new File(path)), response.getOutputStream());
        } catch (final IOException e) {
            response.sendError(500, e.toString());
        }
    }

    @PostMapping
    protected void doPost(
            @RequestParam("file") final MultipartFile file,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        try {

            final File outFile = new File("target/uploads", file.getOriginalFilename());
            System.out.println("doPost " + outFile);

            outFile.getParentFile().mkdirs();

            IOUtils.copy(file.getInputStream(), new FileOutputStream(outFile));

            final String message = outFile + " uploaded successfully!";
            response.setStatus(201);
            response.getOutputStream().write(message.getBytes());

        } catch (final IOException e) {
            response.sendError(500, e.toString());
        }
    }
}
