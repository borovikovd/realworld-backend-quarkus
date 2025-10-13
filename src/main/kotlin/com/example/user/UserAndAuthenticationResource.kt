package com.example.user

import com.example.api.UserAndAuthenticationApi
import com.example.api.model.CreateUserRequest
import com.example.api.model.Login200Response
import com.example.api.model.LoginRequest
import com.example.api.model.UpdateCurrentUserRequest
import com.example.api.model.User
import com.example.shared.security.SecurityContext
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response

@ApplicationScoped
class UserAndAuthenticationResource : UserAndAuthenticationApi {
    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var securityContext: SecurityContext

    override fun createUser(body: CreateUserRequest): Response {
        val newUser = body.user
        val (user, token) =
            userService.register(
                email = newUser.email,
                username = newUser.username,
                password = newUser.password,
            )

        return Response
            .status(Response.Status.CREATED)
            .entity(
                Login200Response().user(
                    User()
                        .email(user.email)
                        .token(token)
                        .username(user.username)
                        .bio(user.bio)
                        .image(user.image),
                ),
            ).build()
    }

    override fun login(body: LoginRequest): Response {
        val loginUser = body.user
        val (user, token) =
            userService.login(
                email = loginUser.email,
                password = loginUser.password,
            )

        return Response
            .ok(
                Login200Response().user(
                    User()
                        .email(user.email)
                        .token(token)
                        .username(user.username)
                        .bio(user.bio)
                        .image(user.image),
                ),
            ).build()
    }

    @RolesAllowed("**")
    override fun getCurrentUser(): Response {
        val userId = securityContext.currentUserId!!
        val user = userService.getCurrentUser(userId)
        val token = ""

        return Response
            .ok(
                Login200Response().user(
                    User()
                        .email(user.email)
                        .token(token)
                        .username(user.username)
                        .bio(user.bio)
                        .image(user.image),
                ),
            ).build()
    }

    @RolesAllowed("**")
    override fun updateCurrentUser(body: UpdateCurrentUserRequest): Response {
        val userId = securityContext.currentUserId!!
        val updateUser = body.user

        val (user, token) =
            userService.updateUser(
                userId = userId,
                email = updateUser.email,
                username = updateUser.username,
                password = updateUser.password,
                bio = updateUser.bio,
                image = updateUser.image,
            )

        return Response
            .ok(
                Login200Response().user(
                    User()
                        .email(user.email)
                        .token(token)
                        .username(user.username)
                        .bio(user.bio)
                        .image(user.image),
                ),
            ).build()
    }
}
