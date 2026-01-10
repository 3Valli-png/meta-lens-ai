package com.metalens.app.wearables

import androidx.compose.runtime.staticCompositionLocalOf
import com.meta.wearable.dat.core.types.Permission
import com.meta.wearable.dat.core.types.PermissionStatus

/**
 * Request wearable permissions (via Meta AI app) from Compose.
 *
 * Backed by MainActivity's ActivityResult launcher.
 */
fun interface WearablesPermissionRequester {
    suspend fun request(permission: Permission): PermissionStatus
}

val LocalWearablesPermissionRequester =
    staticCompositionLocalOf<WearablesPermissionRequester> {
        error("WearablesPermissionRequester not provided")
    }

