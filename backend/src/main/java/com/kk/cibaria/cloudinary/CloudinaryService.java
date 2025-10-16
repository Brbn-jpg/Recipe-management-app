package com.kk.cibaria.cloudinary;

import com.cloudinary.Cloudinary;
import com.kk.cibaria.exception.ImageErrorException;
import com.kk.cibaria.image.Image;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Image addPhoto(MultipartFile file){
        Map<String,String> options = new HashMap<>();
        options.put("folder","cibaria");
        try{
            var result = cloudinary.uploader().upload(file.getBytes(),options);
            Image image = new Image();
            image.setPublicId(result.get("public_id").toString());
            image.setImageUrl(result.get("url").toString());
            return image;
        } catch (Exception e) {
            throw new ImageErrorException(e.getMessage());
        }
    }

    public void removePhoto(String publicId){
        try{
            cloudinary.uploader().destroy(publicId, new HashMap<>());
        } catch (Exception e) {
            throw new ImageErrorException(e.getMessage());
        }
    }
}
