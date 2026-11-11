package com.englishdoctor.vocabsolution.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.englishdoctor.vocabsolution.AppContainer
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.exam.ExamHubScreen
import com.englishdoctor.vocabsolution.ui.exam.ExamSessionScreen
import com.englishdoctor.vocabsolution.ui.home.HomeScreen
import com.englishdoctor.vocabsolution.ui.learn.CollocationsScreen
import com.englishdoctor.vocabsolution.ui.learn.ConfusedListScreen
import com.englishdoctor.vocabsolution.ui.learn.ListenScreen
import com.englishdoctor.vocabsolution.ui.learn.ParaphraseTopicScreen
import com.englishdoctor.vocabsolution.ui.learn.ParaphraseTopicsScreen
import com.englishdoctor.vocabsolution.ui.learn.PassageScreen
import com.englishdoctor.vocabsolution.ui.learn.ReadSubjectsScreen
import com.englishdoctor.vocabsolution.ui.learn.ReadTopicsScreen
import com.englishdoctor.vocabsolution.ui.learn.WriteScreen
import com.englishdoctor.vocabsolution.ui.learn.EquivalentsScreen
import com.englishdoctor.vocabsolution.ui.learn.LearnHubScreen
import com.englishdoctor.vocabsolution.ui.learn.MemoryHookPagerScreen
import com.englishdoctor.vocabsolution.ui.learn.MemoryHooksListScreen
import com.englishdoctor.vocabsolution.ui.learn.PairDetailScreen
import com.englishdoctor.vocabsolution.ui.learn.RootDetailScreen
import com.englishdoctor.vocabsolution.ui.learn.RootListScreen
import com.englishdoctor.vocabsolution.ui.learn.WordBuildingScreen
import com.englishdoctor.vocabsolution.ui.onboarding.OnboardingScreen
import com.englishdoctor.vocabsolution.ui.onboarding.PlacementScreen
import com.englishdoctor.vocabsolution.ui.profile.AboutScreen
import com.englishdoctor.vocabsolution.ui.profile.BookmarksScreen
import com.englishdoctor.vocabsolution.ui.profile.ProfileScreen
import com.englishdoctor.vocabsolution.ui.profile.SettingsScreen
import com.englishdoctor.vocabsolution.ui.review.ReviewHubScreen
import com.englishdoctor.vocabsolution.ui.review.ReviewSessionScreen
import com.englishdoctor.vocabsolution.ui.search.SearchScreen

val LocalApp = staticCompositionLocalOf<AppContainer> { error("no container") }

object Routes {
    const val HOME = "home"
    const val LEARN = "learn"
    const val REVIEW = "review"
    const val EXAM = "exam"
    const val ME = "me"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val BOOKMARKS = "bookmarks"
    const val PLACEMENT = "placement"
    fun module(m: ModuleId) = "module/${m.name}"
    fun root(id: String) = "root/$id"
    fun hook(index: Int) = "hook/$index"
    fun pair(id: String) = "pair/$id"
    fun readSubject(subject: Int) = "read/$subject"
    fun passage(id: String) = "passage/$id"
    fun paraTopic(topic: Int) = "para/$topic"
    fun reviewSession(mode: String) = "review_session/$mode"
    fun examSession(set: Int, mode: String) = "exam_session/$set/$mode"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab(Routes.HOME, "হোম", Icons.Filled.Home),
    Tab(Routes.LEARN, "শিখুন", Icons.AutoMirrored.Filled.MenuBook),
    Tab(Routes.REVIEW, "রিভিশন", Icons.Filled.Replay),
    Tab(Routes.EXAM, "পরীক্ষা", Icons.Filled.Quiz),
    Tab(Routes.ME, "আমি", Icons.Filled.Person),
)

/** Opens a module, honouring the module-specific entry screen. */
fun NavHostController.openModule(m: ModuleId) = navigate(if (m == ModuleId.EXAM) Routes.EXAM else Routes.module(m))

@Composable
fun VocabRoot(app: AppContainer, pendingRoute: String? = null, onRouteConsumed: () -> Unit = {}) {
    val progress by app.progress.state.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalApp provides app) {
        if (!progress.onboarded) {
            OnboardingScreen()
            return@CompositionLocalProvider
        }
        val nav = rememberNavController()
        LaunchedEffect(pendingRoute) {
            if (pendingRoute != null) {
                nav.navigate(pendingRoute) { launchSingleTop = true }
                onRouteConsumed()
            }
        }
        val entry by nav.currentBackStackEntryAsState()
        val current = entry?.destination?.route
        val dueCount = progress.dueKeys(app.progress.today()).size
        Scaffold(
            bottomBar = {
                if (TABS.any { it.route == current }) {
                    NavigationBar {
                        TABS.forEach { tab ->
                            NavigationBarItem(
                                selected = current == tab.route,
                                onClick = {
                                    nav.navigate(tab.route) {
                                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    if (tab.route == Routes.REVIEW && dueCount > 0) {
                                        BadgedBox(badge = { Badge { Text(dueCount.bn()) } }) { Icon(tab.icon, null) }
                                    } else Icon(tab.icon, null)
                                },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }
            },
        ) { pad ->
            NavHost(nav, startDestination = Routes.HOME, modifier = Modifier.padding(bottom = pad.calculateBottomPadding()).consumeWindowInsets(pad)) {
                composable(Routes.HOME) { HomeScreen(nav) }
                composable(Routes.LEARN) { LearnHubScreen(nav) }
                composable(Routes.REVIEW) { ReviewHubScreen(nav) }
                composable(Routes.EXAM) { ExamHubScreen(nav) }
                composable(Routes.ME) { ProfileScreen(nav) }
                composable(Routes.SEARCH) { SearchScreen(nav) }
                composable(Routes.SETTINGS) { SettingsScreen(nav) }
                composable(Routes.ABOUT) { AboutScreen(nav) }
                composable(Routes.BOOKMARKS) { BookmarksScreen(nav) }
                composable(Routes.PLACEMENT) { PlacementScreen(nav) }
                composable("module/{id}") { e ->
                    when (ModuleId.valueOf(e.arguments?.getString("id")!!)) {
                        ModuleId.WORD_BUILDING -> WordBuildingScreen(nav)
                        ModuleId.ROOT_POWER -> RootListScreen(nav)
                        ModuleId.MEMORY_HOOKS -> MemoryHooksListScreen(nav)
                        ModuleId.CONFUSED -> ConfusedListScreen(nav)
                        ModuleId.EQUIVALENTS -> EquivalentsScreen(nav)
                        ModuleId.COLLOCATIONS -> CollocationsScreen(nav)
                        ModuleId.LISTEN -> ListenScreen(nav)
                        ModuleId.READ -> ReadSubjectsScreen(nav)
                        ModuleId.PARAPHRASE -> ParaphraseTopicsScreen(nav)
                        ModuleId.WRITE -> WriteScreen(nav)
                        ModuleId.EXAM -> ExamHubScreen(nav)
                    }
                }
                composable("root/{id}") { e -> RootDetailScreen(nav, e.arguments!!.getString("id")!!) }
                composable("hook/{index}") { e -> MemoryHookPagerScreen(nav, e.arguments!!.getString("index")!!.toInt()) }
                composable("pair/{id}") { e -> PairDetailScreen(nav, e.arguments!!.getString("id")!!) }
                composable("read/{subject}") { e -> ReadTopicsScreen(nav, e.arguments!!.getString("subject")!!.toInt()) }
                composable("passage/{id}") { e -> PassageScreen(nav, e.arguments!!.getString("id")!!) }
                composable("para/{topic}") { e -> ParaphraseTopicScreen(nav, e.arguments!!.getString("topic")!!.toInt()) }
                composable("review_session/{mode}") { e -> ReviewSessionScreen(nav, e.arguments!!.getString("mode")!!) }
                composable("exam_session/{set}/{mode}") { e ->
                    ExamSessionScreen(nav, e.arguments!!.getString("set")!!.toInt(), e.arguments!!.getString("mode")!!)
                }
            }
        }
    }
}
