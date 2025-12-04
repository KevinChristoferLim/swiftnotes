package com.example.wifty.ui.screens.modules

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.wifty.R

@Composable
fun TopNavBar(
    onSearchClick: (() -> Unit)? = null,
    onOpenFolders: (() -> Unit)? = null,
    onOpenProfile: (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        onSearchClick?.let {
            Icon(Icons.Default.Search, contentDescription = "Search",
                modifier = Modifier.clickable { it() })
            Spacer(Modifier.width(18.dp))
        }

        onOpenFolders?.let {
            Icon(Icons.Default.AccountBox, contentDescription = "Folders",
                modifier = Modifier
                    .size(26.dp)
                    .clickable { it() })
            Spacer(Modifier.width(18.dp))
        }

        onOpenProfile?.let {
            Image(
                painter = painterResource(id = R.drawable.sample_profile),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable { it() }
            )
        }
    }
}

