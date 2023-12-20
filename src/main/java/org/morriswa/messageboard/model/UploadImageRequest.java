package org.morriswa.messageboard.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Valid
public record UploadImageRequest (
    @NotBlank byte[] baseEncodedImage,

    @NotBlank String imageFormat
) {
}
