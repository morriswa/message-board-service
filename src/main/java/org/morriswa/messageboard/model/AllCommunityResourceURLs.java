package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

@Data @AllArgsConstructor @NoArgsConstructor
public class AllCommunityResourceURLs {
    private URL icon;
    private URL banner;
}
