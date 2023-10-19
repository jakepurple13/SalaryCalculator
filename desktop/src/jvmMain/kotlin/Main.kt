import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.programmersbox.common.PerAmount
import com.programmersbox.common.SalaryData
import com.programmersbox.common.UIShow

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
            UIShow()
        }
    }
}

fun main1() {
    val f = SalaryData()
    PerAmount.entries
        .map {
            it to when (it) {
                PerAmount.Hourly -> 50.0
                PerAmount.Daily -> 400.0
                PerAmount.Weekly -> 2000.0
                PerAmount.BiWeekly -> 4000.0
                PerAmount.SemiMonthly -> (4333.0 + 1 / 3)
                PerAmount.Monthly -> (4333.0 + 1 / 3) * 2
                PerAmount.Quarterly -> 26000.0
                PerAmount.Yearly -> 104000.0
            }
        }
        .forEach {
            f.perAmount = it.first
            f.amount = it.second
            println(it.first)
            println(f.amounts)
        }
}