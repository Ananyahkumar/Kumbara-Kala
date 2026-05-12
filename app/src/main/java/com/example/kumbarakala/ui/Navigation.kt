package com.example.kumbarakala.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kumbarakala.data.ProfileRepository
import com.example.kumbarakala.ui.screens.ArtisanBioScreen
import com.example.kumbarakala.ui.screens.CatalogScreen
import com.example.kumbarakala.ui.screens.SavedStoryGalleryScreen
import com.example.kumbarakala.ui.screens.StoryGeneratorScreen
import com.example.kumbarakala.ui.auth.AuthScreen

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Catalog : Screen("catalog")
    object StoryGenerator : Screen("story_generator/{productId}") {
        fun createRoute(productId: String) = "story_generator/$productId"
    }
    object ArtisanBio : Screen("artisan_bio")
    object SavedStoryGallery : Screen("saved_story_gallery")

    object StoryGeneratorEdit : Screen("story_generator/edit/{savedCardId}") {
        fun createRoute(savedCardId: String) = "story_generator/edit/$savedCardId"
    }
}

@Composable
fun KumbaraKalaApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val profileRepository = remember { ProfileRepository(context) }
    
    val startDest = if (profileRepository.hasProfile()) Screen.Catalog.route else Screen.Auth.route

    NavHost(navController = navController, startDestination = startDest) {
        
        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Catalog.route) {
            CatalogScreen(
                onProductSelected = { product ->
                    navController.navigate(Screen.StoryGenerator.createRoute(product.id))
                },
                onNavigateToBio = {
                    navController.navigate(Screen.ArtisanBio.route)
                },
                onCreateCustomStory = {
                    navController.navigate(Screen.StoryGenerator.createRoute("custom"))
                },
                onOpenSavedGallery = {
                    navController.navigate(Screen.SavedStoryGallery.route)
                }
            )
        }
        
        composable(Screen.StoryGenerator.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            StoryGeneratorScreen(
                productId = productId,
                savedCardIdToEdit = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.StoryGeneratorEdit.route,
            arguments = listOf(
                navArgument("savedCardId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val savedCardId = backStackEntry.arguments?.getString("savedCardId")
            StoryGeneratorScreen(
                productId = null,
                savedCardIdToEdit = savedCardId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ArtisanBio.route) {
            ArtisanBioScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SavedStoryGallery.route) {
            SavedStoryGalleryScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditSavedCard = { savedCardId ->
                    navController.navigate(Screen.StoryGeneratorEdit.createRoute(savedCardId))
                }
            )
        }
    }
}
