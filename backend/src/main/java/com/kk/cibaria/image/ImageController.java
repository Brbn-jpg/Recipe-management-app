package com.kk.cibaria.image;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/image")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/addPhoto")
    public Image createPhoto(@RequestParam MultipartFile file) throws IOException {
        return imageService.createPhoto(file, null);
    }

    @PostMapping("/deletePhoto")
    public void deletePhoto(@RequestParam String publicId){
        imageService.deletePhoto(publicId);
    }
}
