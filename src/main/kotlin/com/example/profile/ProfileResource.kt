package com.example.profile

import com.example.api.ProfileApi
import com.example.api.model.GetProfileByUsername200Response
import com.example.shared.security.SecurityContext
import com.example.user.UserQueryService
import com.example.user.UserService
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response

@ApplicationScoped
class ProfileResource : ProfileApi {
    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var userQueryService: UserQueryService

    @Inject
    lateinit var securityContext: SecurityContext

    override fun getProfileByUsername(username: String): Response {
        val viewerId = securityContext.currentUserId
        val profile = userQueryService.getProfileByUsername(username, viewerId)

        return Response
            .ok(GetProfileByUsername200Response().profile(profile))
            .build()
    }

    @RolesAllowed("**")
    override fun followUserByUsername(username: String): Response {
        val currentUserId = securityContext.currentUserId!!
        userService.followUser(currentUserId, username)

        val profile = userQueryService.getProfileByUsername(username, currentUserId)
        return Response
            .ok(GetProfileByUsername200Response().profile(profile))
            .build()
    }

    @RolesAllowed("**")
    override fun unfollowUserByUsername(username: String): Response {
        val currentUserId = securityContext.currentUserId!!
        userService.unfollowUser(currentUserId, username)

        val profile = userQueryService.getProfileByUsername(username, currentUserId)
        return Response
            .ok(GetProfileByUsername200Response().profile(profile))
            .build()
    }
}
