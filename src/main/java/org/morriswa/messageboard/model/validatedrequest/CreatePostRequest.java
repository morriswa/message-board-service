package org.morriswa.messageboard.model.validatedrequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.morriswa.messageboard.model.PostContentType;

import java.util.GregorianCalendar;
import java.util.UUID;

@Valid @AllArgsConstructor @Getter
public class CreatePostRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private Long communityId;

    @NotBlank
    @Length(min=5, max = 100)
    private String caption;

    @Length(max = 10000)
    private String description;

    @NotNull
    private PostContentType postContentType;

    @NotNull
    private UUID resourceId;
}
