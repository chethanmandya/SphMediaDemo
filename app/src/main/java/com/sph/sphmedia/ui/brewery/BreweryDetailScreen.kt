package com.sph.sphmedia.ui.brewery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController


@Composable
fun BreweryDetailScreen(breweryId: String) {
    // Use your ViewModel to get the brewery details based on breweryId
    val viewModel: BreweryDetailViewModel = hiltViewModel()
    viewModel.getBreweryById(breweryId) // Replace with your actual fetching logic
    val brewery by viewModel.brewery.observeAsState()

    Box(modifier = Modifier.fillMaxSize().testTag("name_of_the_brewery_detail")) {
        // Display the brewery details
        brewery?.let {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Type: ${it.brewery_type}", style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "Address: ${it.address_1}", style = MaterialTheme.typography.labelLarge
                )
                it.address_2?.let { address2 ->
                    Text(text = address2, style = MaterialTheme.typography.labelLarge)
                }
                it.address_3?.let { address3 ->
                    Text(text = address3, style = MaterialTheme.typography.labelLarge)
                }
                Text(text = "City: ${it.city}", style = MaterialTheme.typography.labelLarge)
                // Add more fields as needed
            }
        }
    }

}
