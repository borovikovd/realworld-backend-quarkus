package com.example.profile

import com.example.api.ProfileApi
import com.example.api.model.GetProfileByUsername200Response
import com.example.api.model.Profile
import com.example.shared.security.SecurityContext
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response

@ApplicationScoped
class ProfileResource : ProfileApi {
    @Inject
    lateinit var profileService: ProfileService

    @Inject
    lateinit var securityContext: SecurityContext

    override fun getProfileByUsername(username: String): Response {
        val currentUserId = securityContext.currentUserId
        val profile = profileService.getProfile(username, currentUserId)

        return Response
            .ok(
                GetProfileByUsername200Response().profile(
                    Profile()
                        .username(profile.username)
                        .bio(profile.bio)
                        .image(profile.image)
                        .following(profile.following),
                ),
            ).build()
    }

    @RolesAllowed("**")
    override fun followUserByUsername(username: String): Response {
        val currentUserId = securityContext.currentUserId!!
        profileService.followUser(currentUserId, username)

        val profile = profileService.getProfile(username, currentUserId)
        return Response
            .ok(
                GetProfileByUsername200Response().profile(
                    Profile()
                        .username(profile.username)
                        .bio(profile.bio)
                        .image(profile.image)
                        .following(profile.following),
                ),
            ).build()
    }

    @RolesAllowed("**")
    override fun unfollowUserByUsername(username: String): Response {
        val currentUserId = securityContext.currentUserId!!
        profileService.unfollowUser(currentUserId, username)

        val profile = profileService.getProfile(username, currentUserId)
        return Response
            .ok(
                GetProfileByUsername200Response().profile(
                    Profile()
                        .username(profile.username)
                        .bio(profile.bio)
                        .image(profile.image)
                        .following(profile.following),
                ),
            ).build()
    }
}
