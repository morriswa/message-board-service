package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadImageRequest {
    private String baseEncodedImage;
    private String imageFormat;
}
