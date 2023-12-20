package org.morriswa.messageboard.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

@Validated
public record UploadImageRequest (
    @NotBlank byte[] baseEncodedImage,

    @NotBlank String imageFormat
) {
}
