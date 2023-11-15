package org.morriswa.messageboard.model.validatedrequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor @Valid
public class UploadImageRequest {

    @NotBlank
    private byte[] baseEncodedImage;

    @NotBlank
    private String imageFormat;
}
