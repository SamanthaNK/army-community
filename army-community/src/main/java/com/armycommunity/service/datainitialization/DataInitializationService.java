package com.armycommunity.service.datainitialization;

public interface DataInitializationService {
    void initializeMembers();

    void initializeEras();

    void initializeAlbums();

    void initializeSongs();

    void initializeMusicVideos();

    boolean isDataInitialized();
}
