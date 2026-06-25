package com.mattiamularoni.saveeat.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.mattiamularoni.saveeat.core.data.local.ProfilePhotoController
import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import org.koin.compose.koinInject
import java.io.File

/**
 * Avatar utente condiviso usato nelle top bar e nel profilo.
 *
 * Priorità della foto mostrata:
 *  1. Foto scelta in locale dall'utente (ProfilePhotoController)
 *  2. Foto di Google (SessionProvider.getAvatarUrl())
 *  3. Icona "Person" di default
 */
@Composable
fun UserAvatar(
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val photoController: ProfilePhotoController = koinInject()
    val sessionProvider: SessionProvider = koinInject()

    val localPath by photoController.localPhotoPath.collectAsState()
    val googleUrl = remember { sessionProvider.getAvatarUrl() }

    val model: Any? =
        when {
            !localPath.isNullOrBlank() -> File(localPath!!)
            !googleUrl.isNullOrBlank() -> googleUrl
            else -> null
        }

    val baseModifier =
        modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    val finalModifier = if (onClick != null) baseModifier.clickable { onClick() } else baseModifier

    Box(modifier = finalModifier, contentAlignment = Alignment.Center) {
        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = "Foto profilo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Profilo",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size * 0.6f),
            )
        }
    }
}
