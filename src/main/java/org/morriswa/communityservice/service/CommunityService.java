package org.morriswa.communityservice.service;

import org.morriswa.common.model.BadRequestException;
import org.morriswa.common.model.UploadImageRequest;
import org.morriswa.communityservice.model.AllCommunityInfoResponse;
import org.morriswa.communityservice.model.CreateNewCommunityRequest;

import java.io.IOException;
import java.util.UUID;

public interface CommunityService {
    void createNewCommunity(CreateNewCommunityRequest request);

    void updateCommunityIcon(UploadImageRequest uploadImageRequest, Long communityId, String jwt) throws BadRequestException, IOException;

    void updateCommunityBanner(UploadImageRequest uploadImageRequest, Long communityId, String jwt) throws BadRequestException, IOException;

    AllCommunityInfoResponse getAllCommunityInfo(String communityDisplayName) throws BadRequestException;

    void joinCommunity(String authzeroid, Long communityId) throws BadRequestException;

    void leaveCommunity(String authzeroid, Long communityId) throws BadRequestException;

    boolean canUserPostInCommunity(UUID userId, Long communityId) throws BadRequestException;
}